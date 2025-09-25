package com.example.librarysystem.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "authors")
public class Author {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "author_id",columnDefinition = "INTEGER")
    private Long id;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "birth_year")
    private Integer birthYear;

    @Column(name = "nationality")
    private String nationality;

//    @JsonManagedReference

    @JsonIgnore
    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL)
    private List<Book> books = new ArrayList<>();

    // Tom konstruktor
    public Author() {

    }

    // Konstruktor med fält
    public Author(Long id, String firstName, String lastName, Integer birthYear, String nationality, List<Book> books) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.birthYear = birthYear;
        this.nationality = nationality;
        this.books = books;
    }

    // Getter och setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public Integer getBirthYear() { return birthYear; }
    public void setBirthYear(Integer birthYear) { this.birthYear = birthYear; }

    public String getNationality() { return nationality; }
    public void setNationality(String nationality) {
        this.nationality = nationality;
    }

    public List<Book> getBooks() { return books; }
    public void setBooks(List<Book> books) {
        this.books = books;
    }
}

