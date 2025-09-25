package com.example.librarysystem.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.owasp.encoder.Encode;

@Data
public class UserRegistrationDTO {
    private Long id;

    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z\\s-]{2,50}$", message = "First name must contain only letters, spaces, or hyphens")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z\\s-]{2,50}$", message = "Last name must contain only letters, spaces, or hyphens")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 50, message = "Password must be between 8 and 50 characters")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
            message = "Password must contain at least one letter, one number, and one special character")
    private String password;

    //--------//
    @NotBlank(message = "Role is required")
    @Pattern(regexp = "^(USER|ADMIN)$", message = "Role must be either USER or ADMIN")
    private String role;
    //--------//

    private String registrationDate; // Datum då användaren registrerades

    public void setFirstName(String firstName) {
        // Kodar förnamnet för att förhindra XSS-attacker
        this.firstName = Encode.forHtml(firstName);
    }

    public void setLastName(String lastName) {
        // Kodar efternamnet för att förhindra XSS-attacker
        this.lastName = Encode.forHtml(lastName);
    }

    public void setEmail(String email) {
        // Kodar e-postadressen för att förhindra XSS-attacker
        this.email = Encode.forHtml(email);
    }
}