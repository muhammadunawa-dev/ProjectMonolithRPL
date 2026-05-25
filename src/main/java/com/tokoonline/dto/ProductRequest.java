package com.tokoonline.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class ProductRequest {
    @NotBlank(message = "Nama produk wajib diisi")
    private String name;
    private String description;
    @NotNull(message = "Harga wajib diisi")
    @DecimalMin(value = "0.0", inclusive = false, message = "Harga harus lebih dari 0")
    private BigDecimal price;
    @NotNull(message = "Stok wajib diisi")
    @Min(value = 0, message = "Stok tidak boleh negatif")
    private Integer stock;
    private String category;
}
