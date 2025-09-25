package com.example.librarysystem.dto;

import lombok.Data;

@Data
public class BookWithDetailsDTO {
    private Long id;
    private String title;
    private Integer publicationYear;
    private Integer availableCopies;
    private Integer totalCopies;
    private AuthorInfoDTO author;

}
