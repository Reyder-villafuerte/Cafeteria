package com.upeu.producto.mapper;

import com.upeu.producto.dto.ProductoRequest;
import com.upeu.producto.dto.ProductoResponse;
import com.upeu.producto.entity.Producto;
import org.springframework.stereotype.Component;

@Component
public class ProductoMapper {

    // ENTITY <- REQUEST
    public Producto toEntity(ProductoRequest request) {
        if (request == null)
            return null;

        return Producto.builder()
                .nombre(request.getNombre())
                .descripcion(request.getDescripcion())
                .idCategoria(request.getIdCategoria())
                .precio(request.getPrecio())
                .stock(request.getStock())
                .build();
    }

    // ❌ NO incluir Feign aquí (IMPORTANTE)
    // RESPONSE <- ENTITY (solo datos de BD)
    public ProductoResponse toResponse(Producto entity) {
        if (entity == null)
            return null;

        return ProductoResponse.builder()
                .id(entity.getId())
                .nombre(entity.getNombre())
                .descripcion(entity.getDescripcion())
                .idCategoria(entity.getIdCategoria())
                .precio(entity.getPrecio())
                .stock(entity.getStock())
                .build(); // 👈 SIN categoria (Feign va en service)
    }

    // UPDATE ENTITY <- REQUEST
    public void updateEntityFromRequest(Producto entity, ProductoRequest request) {
        entity.setNombre(request.getNombre());
        entity.setDescripcion(request.getDescripcion());
        entity.setIdCategoria(request.getIdCategoria());
        entity.setPrecio(request.getPrecio());
        entity.setStock(request.getStock());
    }
}