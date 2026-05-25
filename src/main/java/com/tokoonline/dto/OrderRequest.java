package com.tokoonline.dto;
import jakarta.validation.constraints.*;
import lombok.*;
import java.util.List;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class OrderRequest {
    @NotBlank(message = "Alamat pengiriman wajib diisi")
    private String shippingAddress;
    @NotEmpty(message = "Item order wajib diisi")
    private List<OrderItemRequest> items;
}
