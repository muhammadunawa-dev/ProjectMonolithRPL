package com.tokoonline.dto;
import jakarta.validation.constraints.*;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class AuthRequest {
    @NotBlank private String username;
    @NotBlank private String password;
}
