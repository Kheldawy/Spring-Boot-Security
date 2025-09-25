package com.example.librarysystem.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.owasp.encoder.Encode;

@Data
public class BookDTO {
    private Long id;

    // Säkerställer att titeln inte är tom eller null
    @NotBlank(message = "Title is required")
    @Size(min = 1, max = 200, message = "Title must be between 1 and 200 characters")
    @Pattern(regexp = "^[a-zA-Z0-9\\s-]{1,200}$", message = "Title must contain only letters, numbers, spaces, or hyphens")
    private String title;

    // Säkerställer att publiceringsåret inte är tidigare än 1800
    @Min(value = 1800, message = "Publication year must be 1800 or later")
    private Integer publicationYear;

    // Får inte vara null
    @NotNull(message = "Available copies is required")
    @Min(value = 0, message = "Available copies cannot be negative")
    private Integer availableCopies;

    // Får inte vara null
    @NotNull(message = "Total copies is required")
    @Min(value = 0, message = "Total copies cannot be negative")
    private Integer totalCopies;

    // Förhindrar null-värde för författarens ID
    @NotNull(message = "Author ID is required")
    private Long authorId;

    // Kodar titeln för att förhindra XSS-attacker
    public void setTitle(String title) {
        this.title = Encode.forHtml(title);
    }
}