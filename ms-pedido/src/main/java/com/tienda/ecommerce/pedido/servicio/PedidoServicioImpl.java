package com.tienda.ecommerce.pedido.servicio;

import com.tienda.ecommerce.pedido.dominio.ItemPedido;
import com.tienda.ecommerce.pedido.dominio.Pedido;
import com.tienda.ecommerce.pedido.dto.ItemPedidoDTO;
import com.tienda.ecommerce.pedido.dto.PedidoCreadoEventoDTO;
import com.tienda.ecommerce.pedido.dto.PedidoRequestDTO;
import com.tienda.ecommerce.pedido.dto.externo.ProductoDTO;
import com.tienda.ecommerce.pedido.mensajeria.PedidoPublicador;
import com.tienda.ecommerce.pedido.repositorio.PedidoRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class PedidoServicioImpl implements PedidoServicio {

    private final PedidoRepositorio pedidoRepositorio;
    private final RestTemplate restTemplate;
    private final PedidoPublicador pedidoPublicador;

    public PedidoServicioImpl(PedidoRepositorio pedidoRepositorio,
                               RestTemplate restTemplate,
                               PedidoPublicador pedidoPublicador) {
        this.pedidoRepositorio = pedidoRepositorio;
        this.restTemplate = restTemplate;
        this.pedidoPublicador = pedidoPublicador;
    }

    @Override
    public String crearPedido(PedidoRequestDTO request) {
        Pedido pedido = new Pedido();
        pedido.setNombreCliente(request.getNombre());
        pedido.setTelefono(request.getTelefono());
        pedido.setDireccion(request.getDireccion());
        pedido.setColonia(request.getColonia());
        pedido.setReferencias(request.getReferencias());
        pedido.setMetodoPago(request.getMetodoPago());
        pedido.setFechaCreacion(LocalDateTime.now());

        // Generar código de seguimiento
        String codigo = "ORD-2026-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        pedido.setCodigoSeguimiento(codigo);

        double subtotal = 0.0;
        List<PedidoCreadoEventoDTO.ItemEventoDTO> itemsEvento = new ArrayList<>();

        for (ItemPedidoDTO itemDto : request.getItems()) {
            // Consulta síncrona al ms-producto para obtener precio real
            ProductoDTO producto = restTemplate.getForObject(
                    "http://localhost:8081/api/productos/" + itemDto.getProductoId(), ProductoDTO.class);
            if (producto == null) throw new RuntimeException("Producto no encontrado: " + itemDto.getProductoId());

            ItemPedido item = new ItemPedido();
            item.setProductoId(producto.getId());
            item.setCantidad(itemDto.getCantidad());
            item.setPrecioUnitario(producto.getPrecio());
            pedido.agregarItem(item);

            subtotal += producto.getPrecio() * itemDto.getCantidad();

            // Armar los ítems para el evento (ms-pago necesita productoId + cantidad para luego notificar a ms-producto)
            itemsEvento.add(new PedidoCreadoEventoDTO.ItemEventoDTO(
                    producto.getId(), itemDto.getCantidad(), producto.getPrecio()));
        }

        double envio = 45.0;

        // El pedido se guarda con estado PENDIENTE.
        // ms-pago procesará el pago de forma asíncrona vía RabbitMQ.
        pedido.setSubtotal(subtotal);
        pedido.setDescuento(0.0);   // ms-pago calculará el descuento real
        pedido.setEnvio(envio);
        pedido.setTotal(subtotal + envio);  // total provisional sin descuento
        pedido.setEstadoPago("PENDIENTE");

        pedidoRepositorio.save(pedido);

        // Publicar evento a RabbitMQ → ms-pago lo consumirá
        PedidoCreadoEventoDTO evento = new PedidoCreadoEventoDTO(
                codigo,
                request.getMetodoPago(),
                request.getCodigoDescuento(),
                subtotal,
                envio,
                itemsEvento);

        pedidoPublicador.publicarPedidoCreado(evento);

        return codigo;
    }
}

