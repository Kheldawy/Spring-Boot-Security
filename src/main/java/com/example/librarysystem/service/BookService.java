package com.example.librarysystem.service;

import com.example.librarysystem.entity.Author;
import com.example.librarysystem.entity.Book;
import com.example.librarysystem.repository.AuthorRepository;
import com.example.librarysystem.repository.BookRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BookService {
    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;

    @Autowired
    public BookService(BookRepository bookRepository, AuthorRepository authorRepository) {
        this.bookRepository = bookRepository;
        this.authorRepository = authorRepository;
    }

    // Hämtar alla böcker från databasen
    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    // Hämtar en bok baserat på dess ID
    public Optional<Book> getBookById(Long id) {
        return bookRepository.findById(id);
    }

    // Skapar en ny bok och sparar den i databasen
    public Book createBook(Book book) {
        if (book.getAuthor() == null || book.getAuthor().getId() == null) {
            throw new IllegalArgumentException("Author ID must be provided when creating a book.");
        }
        if (book.getAvailableCopies() > book.getTotalCopies()) {
            throw new IllegalArgumentException("Available copies cannot exceed total copies.");
        }
        Long authorId = book.getAuthor().getId();
        Author author = authorRepository.findById(authorId)
                .orElseThrow(() -> new EntityNotFoundException("Author not found with ID: " + authorId));
        book.setAuthor(author);
        return bookRepository.save(book);
    }

    // Sök efter böcker baserat på titel
    public List<Book> searchBooks(String title) {
        if (title != null) {
            return bookRepository.findByTitleContainingIgnoreCase(title);
        } else {
            return bookRepository.findAll();
        }
    }
}