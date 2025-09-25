package com.example.librarysystem.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

@Entity
@Table(name = "books")
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "book_id",columnDefinition = "INTEGER")
    private Long id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "publication_year")
    private Integer publicationYear;

    @Column(name = "available_copies")
    private Integer availableCopies;

    @Column(name = "total_copies")
    private Integer totalCopies;


// Undvik att serialisera "books" från författaren tillbaka för att förhindra oändliga JSON-loopar
    @JsonIgnoreProperties("books")
    @ManyToOne
    @JoinColumn(name = "author_id")
    private Author author;

    // Tom konstruktor
    public Book() {
    }

    // Konstruktor med fält
    public Book(Long id, String title, Integer publicationYear, Integer availableCopies, Integer totalCopies, Author author) {
        this.id = id;
        this.title = title;
        this.publicationYear = publicationYear;
        this.availableCopies = availableCopies;
        this.totalCopies = totalCopies;
        this.author = author;
    }

    // Getter och setter
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getPublicationYear() {
        return publicationYear;
    }

    public void setPublicationYear(Integer publicationYear) {
        this.publicationYear = publicationYear;
    }

    public Integer getAvailableCopies() {
        return availableCopies;
    }

    public void setAvailableCopies(Integer availableCopies) {
        this.availableCopies = availableCopies;
    }

    public Integer getTotalCopies() {
        return totalCopies;
    }

    public void setTotalCopies(Integer totalCopies) {
        this.totalCopies = totalCopies;
    }

    public Author getAuthor() {
        return author;
    }

    public void setAuthor(Author author) {
        this.author = author;
    }


}

