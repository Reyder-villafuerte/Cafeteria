package com.upeu.producto.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductoRequest {

    @NotBlank
    private String nombre;

    private String descripcion;

    @NotNull
    private Integer idCategoria;

    @NotNull
    private BigDecimal precio;

    @NotNull
    private Integer stock;
}