package com.cblos.dto;

public record PrimaryContactRequest(
    String firstname,
    String lastname,
    String email,
    String phoneNumber,
    String designation,
    boolean primary
) {
    
}
