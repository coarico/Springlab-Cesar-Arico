package edu.espe.springlab.dto;

import jakarta.validation.constraints.*;

import java.time.LocalDate;

public class StudentRequestData {
    @NotBlank @Size(min = 3, max = 120)
    private String fullName;
    @NotBlank @Email @Size(max = 120)
    private String email;

    private LocalDate birthDate;

    public @NotBlank @Size(min = 3, max = 120) String getFullName() {
        return fullName;
    }

    public void setFullName(@NotBlank @Size(min = 3, max = 120) String fullName) {
        this.fullName = fullName;
    }

    public @NotBlank @Email @Size(max = 120) String getEmail() {
        return email;
    }

    public void setEmail(@NotBlank @Email @Size(max = 120) String email) {
        this.email = email;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }
}
