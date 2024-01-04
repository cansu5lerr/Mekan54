package com.example.mekan54.repository;

import com.example.mekan54.model.Category;
import com.example.mekan54.model.User;
import com.example.mekan54.model.Venue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    Boolean existsByCategoryName(String categoryName);
    Optional <Category> findById(Long id);
    Optional <Category> findByCategoryName(String name);


}
