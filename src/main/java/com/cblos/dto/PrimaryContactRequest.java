package com.cblos.dto;

public record PrimaryContactRequest(
    String firstName,
    String lastName,
    String email,
    String phoneNumber,
    String designation
) {
    
}
