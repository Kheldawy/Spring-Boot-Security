package com.example.librarysystem.controller;

import com.example.librarysystem.dto.UserDTO;
import com.example.librarysystem.dto.UserRegistrationDTO;
import com.example.librarysystem.entity.User;
import com.example.librarysystem.service.LoanService;
import com.example.librarysystem.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/users")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;
    private final LoanService loanService;

    @Autowired
    public UserController(UserService userService, LoanService loanService) {
        this.userService = userService;
        this.loanService = loanService;
    }

    // Hämta alla användare (endast ADMIN)
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        logger.info("Fetching all users by admin");
        List<UserDTO> users = userService.getAllUsers().stream()
                .map(this::convertToUserDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(users);
    }

    // Hämta användare baserat på ID
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or principal.username == authentication.principal.username")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        logger.info("Fetching user with ID: {} by user: {}", id, authentication.getName());
        return userService.getUserById(id)
                .map(user -> {
                    // Kontrollera åtkomst
                    if (!user.getEmail().equals(authentication.getName()) && !authentication.getAuthorities().stream()
                            .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"))) {
                        logger.warn("Access denied for user: {} attempting to access user ID: {}",
                                authentication.getName(), id);
                        throw new SecurityException("You can only view your own user details.");
                    }
                    return ResponseEntity.ok(convertToUserDTO(user));
                })
                .orElseGet(() -> {
                    logger.error("User with ID: {} not found", id);
                    return ResponseEntity.notFound().build();
                });
    }

    // Hämta användare baserat på e-post
    @GetMapping("/email/{email}")
    @PreAuthorize("hasRole('ADMIN') or #email == authentication.principal.username")
    public ResponseEntity<UserDTO> getUserByEmail(@PathVariable String email) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        logger.info("Fetching user with email: {} by user: {}", email, authentication.getName());
        return userService.getUserByEmail(email)
                .map(user -> {
                    if (!user.getEmail().equals(authentication.getName()) && !authentication.getAuthorities().stream()
                            .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"))) {
                        logger.warn("Access denied for user: {} attempting to access email: {}",
                                authentication.getName(), email);
                        throw new SecurityException("You can only view your own user details.");
                    }
                    return ResponseEntity.ok(convertToUserDTO(user));
                })
                .orElseGet(() -> {
                    logger.error("User with email: {} not found", email);
                    return ResponseEntity.notFound().build();
                });
    }

    // Skapa en ny användare
    @PostMapping
    public ResponseEntity<?> createUser(@Valid @RequestBody UserRegistrationDTO userDTO) {
        logger.info("Attempting to create user with email: {}", userDTO.getEmail());
        try {
            User savedUser = userService.createUser(userDTO);
            logger.info("User created successfully with email: {}", userDTO.getEmail());
            return ResponseEntity.status(HttpStatus.CREATED).body(convertToUserDTO(savedUser));
        } catch (IllegalArgumentException e) {
            logger.error("Failed to create user with email: {}. Error: {}", userDTO.getEmail(), e.getMessage());
            return ResponseEntity.badRequest().body("Unable to create user: " + e.getMessage());
        }
    }

    // Ta bort en användare (endast ADMIN)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        logger.info("Attempting to delete user with ID: {}", id);
        try {
            return userService.getUserById(id)
                    .map(user -> {
                        // Kontrollera om användaren har aktiva lån
                        if (!loanService.getLoansByUserId(id).stream()
                                .allMatch(loan -> loan.getReturnedDate() != null)) {
                            logger.warn("Cannot delete user with ID: {} due to active loans", id);
                            return ResponseEntity.badRequest().body("Cannot delete user with active loans.");
                        }
                        userService.deleteUser(id);
                        logger.info("User with ID: {} deleted successfully", id);
                        return ResponseEntity.ok().body("User deleted successfully.");
                    })
                    .orElseGet(() -> {
                        logger.error("User with ID: {} not found", id);
                        return ResponseEntity.notFound().build();
                    });
        } catch (Exception e) {
            logger.error("Failed to delete user with ID: {}. Error: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unable to delete user: " + e.getMessage());
        }
    }

    // Konvertera User-entity till UserDTO
    private UserDTO convertToUserDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setEmail(user.getEmail());
        dto.setRegistrationDate(user.getRegistrationDate());
        return dto;
    }
}