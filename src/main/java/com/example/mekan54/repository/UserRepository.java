package com.example.mekan54.repository;

import java.util.Optional;


import com.example.mekan54.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;



@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional <User> findByEmail(String email);
    Boolean existsByEmail(String email);
    Optional<User> findByGenerateCode(String generateCode);



}