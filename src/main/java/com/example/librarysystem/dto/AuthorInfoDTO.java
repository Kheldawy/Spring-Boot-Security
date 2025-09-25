package com.example.librarysystem.dto;

import lombok.Data;

@Data
public class AuthorInfoDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private Integer birthYear;
    private String nationality;
}

