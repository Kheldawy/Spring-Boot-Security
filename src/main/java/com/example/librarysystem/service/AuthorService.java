package com.example.librarysystem.service;

import com.example.librarysystem.entity.Author;
import com.example.librarysystem.repository.AuthorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AuthorService {
    private final AuthorRepository authorRepository;

    @Autowired
    public AuthorService(AuthorRepository authorRepository) {
        this.authorRepository = authorRepository;
    }

    // Hämtar alla författare
    public List<Author> getAllAuthors() {
        return authorRepository.findAll();
    }

    // Hämtar en författare på id
    public Optional<Author> getAuthorById(Long id) {
        return authorRepository.findById(id);
    }

    // Hämtar författare på efternamn
    public List<Author> getAuthorsByLastName(String lastName) {
        return authorRepository.findByLastNameIgnoreCase(lastName);
    }

    // Skapar en ny författare
    public Author createAuthor(Author author) { return authorRepository.save(author);
    }
}

