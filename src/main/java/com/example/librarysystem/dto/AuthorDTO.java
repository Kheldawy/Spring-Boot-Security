package com.example.librarysystem.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import org.owasp.encoder.Encode;

@Data
public class AuthorDTO {
    private Long id;

    // Förnamn med validering
    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z\\s-]{2,50}$", message = "First name must contain only letters, spaces, or hyphens")
    private String firstName;

    // Efternamn med validering
    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z\\s-]{2,50}$", message = "Last name must contain only letters, spaces, or hyphens")
    private String lastName;

    // Nationalitet med validering
    @Size(max = 100, message = "Nationality must not exceed 100 characters")
    @Pattern(regexp = "^[a-zA-Z\\s-]{0,100}$", message = "Nationality must contain only letters, spaces, or hyphens")
    private String nationality;

    // Födelseår med validering
    @Min(value = 1000, message = "Birth year must be 1000 or later")
    @Max(value = 2025, message = "Birth year cannot be in the future")
    private Integer birthYear;

    // Sätter och HTML-kodar förnamn för att förhindra XSS-attacker
    public void setFirstName(String firstName) {
        this.firstName = Encode.forHtml(firstName);
    }

    // Sätter och HTML-kodar efternamn för att förhindra XSS-attacker
    public void setLastName(String lastName) {
        this.lastName = Encode.forHtml(lastName);
    }

    // Sätter och HTML-kodar nationalitet om värdet inte är null
    public void setNationality(String nationality) {
        this.nationality = nationality != null ? Encode.forHtml(nationality) : null;
    }

}