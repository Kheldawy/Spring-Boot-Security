package com.example.librarysystem.dto;

import lombok.Data;

@Data
public class UserDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String registrationDate;

    // Tom konstruktor
    public UserDTO() {
    }

    // Konstruktor med alla fält
    public UserDTO(Long id, String firstName, String lastName, String email, String registrationDate) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.registrationDate = registrationDate;
    }

}

