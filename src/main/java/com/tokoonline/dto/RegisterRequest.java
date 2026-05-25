package com.tokoonline.dto;
import jakarta.validation.constraints.*;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class RegisterRequest {
    @NotBlank private String username;
    @NotBlank @Size(min = 6) private String password;
    @Email @NotBlank private String email;
}
