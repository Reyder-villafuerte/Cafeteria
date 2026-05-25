package com.upeu.producto.service.impl;

import com.upeu.producto.dto.ProductoRequest;
import com.upeu.producto.dto.ProductoResponse;
import com.upeu.producto.entity.Producto;
import com.upeu.producto.exception.ResourceNotFoundException;
import com.upeu.producto.mapper.ProductoMapper;
import com.upeu.producto.repository.ProductoRepository;
import com.upeu.producto.service.ProductoService;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.upeu.producto.client.CategoriaClient;
import com.upeu.producto.dto.CategoriaDto;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductoServiceImpl implements ProductoService {

    private final ProductoRepository productoRepository;
    private final ProductoMapper productoMapper;
    private final CategoriaClient categoriaClient;

    @Override
    @Transactional
    public ProductoResponse create(ProductoRequest request) {

        Producto producto = productoMapper.toEntity(request);
        Producto saved = productoRepository.save(producto);

        CategoriaDto categoria = categoriaClient
                .findCategoriaById(saved.getIdCategoria().longValue());

        return ProductoResponse.builder()
                .id(saved.getId())
                .nombre(saved.getNombre())
                .descripcion(saved.getDescripcion())
                .idCategoria(saved.getIdCategoria())
                .precio(saved.getPrecio())
                .stock(saved.getStock())
                .categoria(categoria)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductoResponse> findAll() {

        log.info("Recuperando lista de productos");

        return productoRepository.findAll()
                .stream()
                .map(producto -> {

                    CategoriaDto categoria = null;

                    try {
                        categoria = categoriaClient.findCategoriaById(
                                producto.getIdCategoria().longValue());
                    } catch (Exception e) {
                        log.warn("No se pudo obtener categoria id {}", producto.getIdCategoria());
                    }

                    return ProductoResponse.builder()
                            .id(producto.getId())
                            .nombre(producto.getNombre())
                            .descripcion(producto.getDescripcion())
                            .idCategoria(producto.getIdCategoria())
                            .precio(producto.getPrecio())
                            .stock(producto.getStock())
                            .categoria(categoria)
                            .build();
                })
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ProductoResponse findById(Integer id) {
        log.info("Buscando producto con ID: {}", id);
        Producto producto = getProductoById(id);
        log.info("Producto encontrado: {} (ID: {})", producto.getNombre(), id);
        return productoMapper.toResponse(producto);
    }

    @Override
    @Transactional
    public ProductoResponse update(Integer id, ProductoRequest request) {
        log.info("Iniciando actualizacion de producto ID: {}", id);
        Producto producto = getProductoById(id);
        productoMapper.updateEntityFromRequest(producto, request);
        Producto updatedProducto = productoRepository.save(producto);
        log.info("Producto ID: {} actualizado exitosamente", id);
        return productoMapper.toResponse(updatedProducto);
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        log.info("Iniciando eliminacion de producto ID: {}", id);
        getProductoById(id);
        productoRepository.deleteById(id);
        log.info("Producto ID: {} eliminado exitosamente", id);
    }

    private Producto getProductoById(Integer id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Producto no encontrado: ID {}", id);
                    return new ResourceNotFoundException("Producto con id " + id + " no encontrado");
                });
    }

    @Override
    @Transactional(readOnly = true)
    @CircuitBreaker(name = "categoria", fallbackMethod = "fallbackCategoria")
    public ProductoResponse findDetalleById(Integer id) {
        log.info("[PRODUCTO] Buscando detalle de producto con ID: {}", id);

        Producto producto = getProductoById(id);
        log.info("[PRODUCTO] Consultando categoriaId={} en categoria", producto.getIdCategoria());

        CategoriaDto categoria = categoriaClient.findCategoriaById(
                producto.getIdCategoria().longValue());

        return ProductoResponse.builder()
                .id(producto.getId())
                .nombre(producto.getNombre())
                .descripcion(producto.getDescripcion())
                .idCategoria(producto.getIdCategoria())
                .categoria(categoria)
                .build();
    }

    public ProductoResponse fallbackCategoria(Integer id, Throwable ex) {
        log.warn("[PRODUCTO] Fallback activado para producto ID {}. Motivo: {}", id, ex.getMessage());

        Producto producto = getProductoById(id);

        return ProductoResponse.builder()
                .id(producto.getId())
                .nombre(producto.getNombre())
                .descripcion(producto.getDescripcion())
                .idCategoria(producto.getIdCategoria())
                .categoria(null)
                .build();
    }

}
