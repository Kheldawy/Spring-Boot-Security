package com.example.librarysystem.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "loans")
public class Loan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "loan_id", columnDefinition = "INTEGER")
    private Long id;

    // JsonIgnore används för att undvika att användardata skickas i JSON-svaret
    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "book_id")
    private Book book;

    // Datum då boken lånades, visas i JSON
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "borrowed_date")
    private LocalDateTime borrowedDate;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "due_date")
    private LocalDateTime dueDate;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "returned_date")
    private LocalDateTime returnedDate;

    // Tom konstruktor
    public Loan() {
    }

    // Konstruktor med fält
    public Loan(Long id, User user, Book book, LocalDateTime borrowedDate, LocalDateTime dueDate, LocalDateTime returnedDate) {
        this.id = id;
        this.user = user;
        this.book = book;
        this.borrowedDate = borrowedDate;
        this.dueDate = dueDate;
        this.returnedDate = returnedDate;
    }

    // Getter och setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Book getBook() { return book; }
    public void setBook(Book book) { this.book = book; }

    public LocalDateTime getBorrowedDate() { return borrowedDate; }
    public void setBorrowedDate(LocalDateTime borrowedDate) { this.borrowedDate = borrowedDate; }

    public LocalDateTime getDueDate() { return dueDate; }
    public void setDueDate(LocalDateTime dueDate) { this.dueDate = dueDate; }

    public LocalDateTime getReturnedDate() { return returnedDate; }
    public void setReturnedDate(LocalDateTime returnedDate) { this.returnedDate = returnedDate; }
}

