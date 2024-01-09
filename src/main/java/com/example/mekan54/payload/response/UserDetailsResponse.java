package com.example.mekan54.payload.response;

import com.example.mekan54.model.Venue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.Map;

public class UserDetailsResponse {


    @NotBlank
    @Size(max = 50)
    @Email
    private String email;


    @NotBlank
    @Size(min = 3, max = 20)
    private String name;

    @NotBlank
    @Size(min = 3, max = 20)
    private String surname;


    public List<Map<String, Object>> getVenuesResponseList() {
        return venuesResponseList;
    }

    public void setVenuesResponseList(List<Map<String, Object>> venuesResponseList) {
        this.venuesResponseList = venuesResponseList;
    }

    private  List<Map<String, Object>> venuesResponseList;


    public UserDetailsResponse() {}


    public UserDetailsResponse (String name, String surname, String email) {
        this.name = name;
        this.surname= surname;
        this.email=email;
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }


}
