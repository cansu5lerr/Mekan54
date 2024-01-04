package com.example.mekan54.security.jwt;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class EmailPasswordAuthenticationToken extends UsernamePasswordAuthenticationToken {

    private final String email;

    public EmailPasswordAuthenticationToken(String email, String password) {
        super(email, password);
        this.email = email;
    }

    public EmailPasswordAuthenticationToken(Object principal, Object credentials, Collection<? extends GrantedAuthority> authorities) {
        super(principal, credentials, authorities);
        this.email = null;
    }


    public String getEmail() {
        return email;
    }
}