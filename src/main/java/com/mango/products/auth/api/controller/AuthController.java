package com.mango.products.auth.api.controller;

import com.mango.products.config.security.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "JWT authentication endpoint")
public class AuthController {

    private final JwtService jwtService;

    public AuthController(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    @Operation(summary = "Login and get JWT token", description = "Authenticate and receive a Bearer token for API access")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Token generated successfully",
                    content = @Content(schema = @Schema(implementation = LoginResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid credentials",
                    content = @Content(schema = @Schema(implementation = com.mango.products.config.GlobalExceptionHandler.ErrorResponse.class))
            )
    })
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        // Simple hardcoded credentials for demo
        // In production, validate against a user repository
        if ("admin".equals(request.username()) && "password".equals(request.password())) {
            String token = jwtService.generateToken(request.username());
            return ResponseEntity.ok(new LoginResponse(token));
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .build();
    }

    public record LoginRequest(String username, String password) {
    }

    public record LoginResponse(String token) {
    }
}

