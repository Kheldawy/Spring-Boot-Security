package com.example.librarysystem.controller;

import com.example.librarysystem.dto.LoanRequestDTO;
import com.example.librarysystem.dto.LoanWithUserIdDTO;
import com.example.librarysystem.entity.Loan;
import com.example.librarysystem.entity.User;
import com.example.librarysystem.service.LoanService;
import com.example.librarysystem.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/loans")
public class LoanController {
    private final LoanService loanService;
    private final UserService userService;

    @Autowired
    public LoanController(LoanService loanService, UserService userService) {
        this.loanService = loanService;
        this.userService = userService;
    }

    // Hämta alla lån (endast ADMIN har rättigheter)
    @GetMapping
    public ResponseEntity<?> getAllLoans() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication
                .getPrincipal().equals("anonymousUser")) {
            return ResponseEntity.status(401).body("Please log in to view loans.");
        }

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin) {
            return ResponseEntity.status(403).body("Only admins can view all loans.");
        }

        List<Loan> loans = loanService.getAllLoans();
        return ResponseEntity.ok(loans);
    }

    // Hämta ett specifikt lån via ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getLoanById(@PathVariable Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal()
                .equals("anonymousUser")) {
            return ResponseEntity.status(401).body("Please log in to view loan details.");
        }

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

        return loanService.getLoanById(id)
                .map(loan -> {
                    if (!isAdmin && !loan.getUser().getEmail().equals(authentication.getName())) {
                        return ResponseEntity.status(403).body("You can only view your own loans.");
                    }
                    return ResponseEntity.ok(loan);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Skapa ett nytt lån
    @PostMapping
    public ResponseEntity<?> createLoan(@Valid @RequestBody LoanRequestDTO loanRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication
                .getPrincipal().equals("anonymousUser")) {
            return ResponseEntity.status(401).body("Please log in to create a loan.");
        }

        try {
            Optional<User> user = userService.getUserByEmail(authentication.getName());
            if (user.isEmpty() || !user.get().getId().equals(loanRequest.getUserId())) {
                return ResponseEntity.status(403).body("You can only create loans for yourself.");
            }

            Loan savedLoan = loanService.createLoan(loanRequest.getUserId(), loanRequest.getBookId());
            return ResponseEntity.ok(savedLoan);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Returnera en bok
    @PutMapping("/{id}/return")
    public ResponseEntity<?> returnBook(@PathVariable Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
            return ResponseEntity.status(401).body("Please log in to return a book.");
        }

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

        try {
            Optional<Loan> loanOptional = loanService.getLoanById(id);
            if (loanOptional.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            Loan loan = loanOptional.get();
            if (!isAdmin && !loan.getUser().getEmail().equals(authentication.getName())) {
                return ResponseEntity.status(403).body("You can only return your own loans.");
            }

            Loan updatedLoan = loanService.returnBook(id);
            return ResponseEntity.ok(updatedLoan);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Förläng ett lån
    @PutMapping("/{id}/extend")
    public ResponseEntity<?> extendLoan(@PathVariable Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
            return ResponseEntity.status(401).body("Please log in to extend a loan.");
        }

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

        try {
            Optional<Loan> loanOptional = loanService.getLoanById(id);
            if (loanOptional.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            Loan loan = loanOptional.get();
            if (!isAdmin && !loan.getUser().getEmail().equals(authentication.getName())) {
                return ResponseEntity.status(403).body("You can only extend your own loans.");
            }

            Loan updatedLoan = loanService.extendLoan(id);
            return ResponseEntity.ok(updatedLoan);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Hämta alla lån för en specifik användare
    @GetMapping("/users/{userId}/loans")
    public ResponseEntity<?> getUserLoans(@PathVariable Long userId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
            return ResponseEntity.status(401).body("Please log in to view your loans.");
        }

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

        Optional<User> userOptional = userService.getUserById(userId);
        if (!userOptional.isPresent()) {
            return ResponseEntity.badRequest().body("User not found");
        }

        if (!isAdmin && !userOptional.get().getEmail().equals(authentication.getName())) {
            return ResponseEntity.status(403).body("You can only view your own loans.");
        }

        List<Loan> loans = loanService.getLoansByUserId(userId);
        List<LoanWithUserIdDTO> loanDTOs = loans.stream()
                .map(loan -> convertToLoanWithUserIdDTO(loan, userId))
                .collect(Collectors.toList());
        return ResponseEntity.ok(loanDTOs);
    }

    // Konverterar Loan-entity till DTO med användar-ID
    private LoanWithUserIdDTO convertToLoanWithUserIdDTO(Loan loan, Long userId) {
        LoanWithUserIdDTO dto = new LoanWithUserIdDTO();
        dto.setId(loan.getId());
        dto.setUserId(userId);
        dto.setBookId(loan.getBook().getId());
        dto.setBorrowedDate(loan.getBorrowedDate());
        dto.setDueDate(loan.getDueDate());
        dto.setReturnedDate(loan.getReturnedDate());
        return dto;
    }
}