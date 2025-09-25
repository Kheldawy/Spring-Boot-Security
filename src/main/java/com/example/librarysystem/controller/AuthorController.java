package com.example.librarysystem.controller;

import com.example.librarysystem.dto.AuthorDTO;
import com.example.librarysystem.entity.Author;
import com.example.librarysystem.service.AuthorService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/authors")
public class AuthorController {
    private final AuthorService authorService;

    @Autowired
    public AuthorController(AuthorService authorService) {
        this.authorService = authorService;
    }

    // Hämta alla författare (endast ADMIN har tillgång)
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Author>> getAllAuthors() {
        List<Author> authors = authorService.getAllAuthors();
        return ResponseEntity.ok(authors);
    }

    // Hämta en specifik författare baserat på ID (endast ADMIN)
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Author> getAuthorById(@PathVariable Long id) {
        return authorService.getAuthorById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Hämta författare baserat på efternamn med valideringsregel (endast ADMIN)
    @GetMapping("/name/{lastName}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Author>> getAuthorsByLastName(
            @PathVariable @Pattern(regexp = "^[a-zA-Z\\s-]{2,50}$", message = "Last name must be 2-50 characters and contain only letters, spaces, or hyphens") String lastName) {
        List<Author> authors = authorService.getAuthorsByLastName(lastName);
        return ResponseEntity.ok(authors);
    }

    // Skapa en ny författare (endast ADMIN)
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Author> createAuthor(@Valid @RequestBody AuthorDTO authorDTO) {
        Author author = new Author();
        author.setFirstName(authorDTO.getFirstName());
        author.setLastName(authorDTO.getLastName());
        author.setNationality(authorDTO.getNationality());
        author.setBirthYear(authorDTO.getBirthYear());
        Author savedAuthor = authorService.createAuthor(author);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedAuthor);
    }
}