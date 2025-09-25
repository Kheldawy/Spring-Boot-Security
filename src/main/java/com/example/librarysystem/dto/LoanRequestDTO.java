package com.example.librarysystem.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LoanRequestDTO {
    @NotNull(message = "User ID is required")
    // Säkerställer att användarens ID inte är null
    private Long userId;

    @NotNull(message = "Book ID is required")
    // Säkerställer att bokens ID inte är null
    private Long bookId;
}