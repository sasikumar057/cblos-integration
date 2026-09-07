package com.cblos.dto;

public record PrimaryContactResponse(
        Integer id,
        String firstName,
        String lastName,
        String email,
        String phoneNumber,
        String designation) {
}
