package com.example.librarysystem.controller;

import com.example.librarysystem.dto.AuthorInfoDTO;
import com.example.librarysystem.dto.BookDTO;
import com.example.librarysystem.dto.BookWithDetailsDTO;
import com.example.librarysystem.entity.Author;
import com.example.librarysystem.entity.Book;
import com.example.librarysystem.service.BookService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
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
@RequestMapping("/books")
public class BookController {
    private final BookService bookService;

    @Autowired
    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    // Hämta alla böcker med olika åtkomst beroende på inloggade användare
    @GetMapping
    public ResponseEntity<?> getAllBooks() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        List<BookWithDetailsDTO> books = bookService.getAllBooks().stream()
                .map(this::convertToBookWithDetailsDTO)
                .collect(Collectors.toList());

        // Gäståtkomst – ser endast begränsad information
        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal()
                .equals("anonymousUser"))
        {
            return ResponseEntity.ok("Guest access: Only public book information available." +
                    " Please log in to access full details.");
        }

        // Kontrollera om användaren är admin
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(auth -> auth
                        .getAuthority().equals("ROLE_ADMIN"));

        if (isAdmin) {
            return ResponseEntity.ok(books); // Admin ser alla detaljer
        } else {
            return ResponseEntity.ok("User access: " + books.size() + " books available for borrowing.");
        }
    }

    // Hämta en specifik bok baserat på ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getBookById(@PathVariable Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return bookService.getBookById(id)
                .map(book -> {
                    // Gäståtkomst – visar endast titel
                    if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
                        return ResponseEntity.ok("Guest access: Book title - " + book.getTitle());
                    }
                    // Inloggade användare ser detaljerad information
                    return ResponseEntity.ok(convertToBookWithDetailsDTO(book));
                })
                .orElseGet(() -> ResponseEntity.notFound().build()); // 404 om boken inte hittas
    }

    // Skapa en ny bok (endast ADMIN)
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BookWithDetailsDTO> createBook(@Valid @RequestBody BookDTO bookDTO) {
        Book book = new Book();
        book.setTitle(bookDTO.getTitle());
        book.setPublicationYear(bookDTO.getPublicationYear());
        book.setAvailableCopies(bookDTO.getAvailableCopies());
        book.setTotalCopies(bookDTO.getTotalCopies());
        book.setAuthor(new Author());
        book.getAuthor().setId(bookDTO.getAuthorId());
        Book savedBook = bookService.createBook(book);
        return ResponseEntity.status(HttpStatus.CREATED).body(convertToBookWithDetailsDTO(savedBook));
    }

    // Sök böcker baserat på titel
    @GetMapping("/search")
    public ResponseEntity<?> searchBooks(
            @RequestParam(value = "title", required = false) @Pattern(regexp = "^[a-zA-Z0-9\\s-]{1,200}$", message = "Title must be 1-200 characters and contain only letters, numbers, spaces, or hyphens") String title) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        List<Book> books = bookService.searchBooks(title);

        // Gäståtkomst – visar endast titlar
        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
            List<String> titles = books.stream().map(Book::getTitle).collect(Collectors.toList());
            return ResponseEntity.ok("Guest access: Found " + titles.size() + " book titles: " + titles);
        }

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

        List<BookWithDetailsDTO> bookDTOs = books.stream()
                .map(this::convertToBookWithDetailsDTO)
                .collect(Collectors.toList());

        if (isAdmin) {
            return ResponseEntity.ok(bookDTOs);
        } else {
            return ResponseEntity.ok("User access: Found " + bookDTOs.size() + " books available for borrowing.");
        }
    }

    // Konverterar en Book-entity till en DTO med detaljerad information
    private BookWithDetailsDTO convertToBookWithDetailsDTO(Book book) {
        BookWithDetailsDTO dto = new BookWithDetailsDTO();
        dto.setId(book.getId());
        dto.setTitle(book.getTitle());
        dto.setPublicationYear(book.getPublicationYear());
        dto.setAvailableCopies(book.getAvailableCopies());
        dto.setTotalCopies(book.getTotalCopies());

        if (book.getAuthor() != null) {
            AuthorInfoDTO authorDTO = new AuthorInfoDTO();
            authorDTO.setId(book.getAuthor().getId());
            authorDTO.setFirstName(book.getAuthor().getFirstName());
            authorDTO.setLastName(book.getAuthor().getLastName());
            authorDTO.setBirthYear(book.getAuthor().getBirthYear());
            authorDTO.setNationality(book.getAuthor().getNationality());
            dto.setAuthor(authorDTO);
        } else {
            dto.setAuthor(null); // Om boken inte har en författare
        }

        return dto;
    }
}