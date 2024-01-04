package com.example.mekan54.payload.request;

import java.util.Set;

import jakarta.validation.constraints.*;

public class RegisterRequest {

    private String email;
    private String name;

    private String surname;
    //private Set<String> role;
    private String password;
    private String repeatPassword;
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    public String getSurname() {
        return surname;
    }
    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRepeatPassword() {return repeatPassword;}
    public void setRepeatPassword(String repeatPassword) {this.repeatPassword= repeatPassword;}
  /*  public Set<String> getRole() {
        return this.role;
    }
    public void setRole(Set<String> role) {
        this.role = role;
    } */
}
