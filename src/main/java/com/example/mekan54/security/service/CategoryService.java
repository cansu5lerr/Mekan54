package com.example.mekan54.security.service;

import com.example.mekan54.model.Category;
import com.example.mekan54.payload.request.RegisterAdminRequest;
import com.example.mekan54.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CategoryService {

    @Autowired
    CategoryRepository categoryRepository;
    public Long getCategoryId (String categoryName) {
      //  String categoryName = registerAdminRequest.getCategoryName();
        switch (categoryName) {
            case "Asya Restoranı" :
                return getCategoryByName("Asya Restoranı").getId();
            case "Aşevi" :
                return getCategoryByName("Aşevi").getId();
            case "Bakkal" :
                return getCategoryByName("Bakkal").getId();
            case "Bar" :
                return getCategoryByName("Bar").getId();
            case "Büfe" :
                return getCategoryByName("Büfe").getId();
            case "Çay Bahçesi" :
                return getCategoryByName("Çay Bahçesi").getId();
            case "Deniz Ürünleri" :
                return getCategoryByName("Deniz Ürünleri").getId();
            case "Fast Food" :
                return getCategoryByName("Fast Food").getId();
            case "Fırın" :
                return getCategoryByName("Fırın").getId();
            case "Gece Kulübü" :
                return getCategoryByName("Gece Kulübü").getId();
            case "Hint Restoranı" :
                return getCategoryByName("Hint Restoranı").getId();
            case "İtalyan Restoranı" :
                return getCategoryByName("İtalyan Restoranı").getId();
            case "Kahve Dükkanı" :
                return getCategoryByName("Kahve Dükkanı").getId();
            case "Kasap" :
                return getCategoryByName("Kasap").getId();
            case "Lokanta" :
                return getCategoryByName("Lokanta").getId();
            case "Türk Restoranı" :
                return getCategoryByName("Türk Restoranı").getId();
            case "Vejetaryen/Vegan" :
                return getCategoryByName("Vejetaryen/Vegan").getId();
            default:
                throw new IllegalArgumentException("Geçersiz alan adı: ");

        }
    }

    public Category getCategoryByName(String categoryName) {

        if (categoryRepository.existsByCategoryName(categoryName)){
            return categoryRepository.findByCategoryName(categoryName).orElseThrow(() -> new IllegalArgumentException("Kategori Zaten : " + categoryName));
        } else {
            Category category = new Category();
            category.setCategoryName(categoryName);
            categoryRepository.save(category);
            return category;
        }

    }
}
