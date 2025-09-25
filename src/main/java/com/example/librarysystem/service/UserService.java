package com.example.librarysystem.service;

import com.example.librarysystem.dto.UserRegistrationDTO;
import com.example.librarysystem.entity.User;
import com.example.librarysystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Hämta alla användare
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // Hämta en användare baserat på ID
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    // Hämta en användare baserat på email
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmailIgnoreCase(email);
    }

    // Skapa en ny användare
    public User createUser(UserRegistrationDTO userDTO) {
        // Kontrollera om e-post redan finns registrerad
        if (userRepository.findByEmailIgnoreCase(userDTO.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already exists");
        }

        // Verifiera att den aktuella användaren är administratör om rollen ADMIN har angetts
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if ("ADMIN".equalsIgnoreCase(userDTO.getRole())) {
            if (authentication == null || !authentication.isAuthenticated() ||
                    authentication.getAuthorities().stream().noneMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"))) {
                throw new SecurityException("Only an admin can create another admin.");
            }
        }

        // Validate password policy (done via annotations in UserRegistrationDTO)
        User user = new User();
        user.setFirstName(userDTO.getFirstName());
        user.setLastName(userDTO.getLastName());
        user.setEmail(userDTO.getEmail());
        user.setPassword(passwordEncoder.encode(userDTO.getPassword())); // Encrypt password
//        user.setRole("USER"); // Set default role to USER
        user.setRole(userDTO.getRole() != null ? userDTO.getRole() : "USER"); // Tilldela roll, annars USER som standard
        user.setRegistrationDate(LocalDate.now().toString());


        return userRepository.save(user); // Spara användaren i databasen
    }

    // Ta bort en användare baserat på ID
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

//    // Hämta PasswordEncoder-instansen
//    public PasswordEncoder getPasswordEncoder() {
//        return passwordEncoder;
//    }

}
