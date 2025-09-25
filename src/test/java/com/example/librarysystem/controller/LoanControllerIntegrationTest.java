package com.example.librarysystem.controller;

import com.example.librarysystem.LibrarySystemApplication;
import com.example.librarysystem.entity.Author;
import com.example.librarysystem.entity.Book;
import com.example.librarysystem.entity.User;
import com.example.librarysystem.repository.AuthorRepository;
import com.example.librarysystem.repository.BookRepository;
import com.example.librarysystem.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = LibrarySystemApplication.class,
        properties = "spring.config.location=classpath:/application-test.properties")
@AutoConfigureMockMvc
@ActiveProfiles("test")
class LoanControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private AuthorRepository authorRepository; // Tillagd för att spara författaren

    private User user;
    private Book book;
    private Author author;

    @BeforeEach
    void setUp() {
        // Rensa databasen före varje test
        userRepository.deleteAll();
        bookRepository.deleteAll();
        authorRepository.deleteAll();

        // Skapa och spara en författare
        author = new Author();
        author.setFirstName("Camilla");
        author.setLastName("Läckberg");
        author.setBirthYear(1974);
        author.setNationality("Swedish");
        authorRepository.save(author);

        // Skapa och spara en användare
        user = new User();
        user.setFirstName("Khaled");
        user.setLastName("Ibrahim");
        user.setEmail("khaled.ibrahim@example.com");
        user.setPassword("pass");
        user.setRegistrationDate(LocalDateTime.now().toString());
        userRepository.save(user);

        // Skapa och spara en bok med kopplad författare
        book = new Book();
        book.setTitle("Test Book");
        book.setPublicationYear(2020);
        book.setTotalCopies(5);
        book.setAvailableCopies(5);
        book.setAuthor(author);
        bookRepository.save(book);
    }

    @Test
    void createLoan_success() throws Exception {
        // Arrange
        String json = String.format("{\"userId\": %d, \"bookId\": %d}", user.getId(), book.getId());

        // Act & Assert
        mockMvc.perform(post("/loans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.borrowedDate", notNullValue()))
                .andExpect(jsonPath("$.dueDate", notNullValue()))
                .andExpect(jsonPath("$.returnedDate", nullValue()));

        // Verifiera att bokexemplar minskat
        Book updatedBook = bookRepository.findById(book.getId()).get();
        assertEquals(4, updatedBook.getAvailableCopies());
    }

    @Test
    void createLoan_userNotFound_returnsBadRequest() throws Exception {
        // Arrange
        String json = String.format("{\"userId\": %d, \"bookId\": %d}", 999L, book.getId());

        // Act & Assert
        mockMvc.perform(post("/loans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").value("User not found"));
    }

    @Test
    void createLoan_bookNotFound_returnsBadRequest() throws Exception {
        // Arrange
        String json = String.format("{\"userId\": %d, \"bookId\": %d}", user.getId(), 999L);

        // Act & Assert
        mockMvc.perform(post("/loans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").value("The book is not available"));
    }

}
