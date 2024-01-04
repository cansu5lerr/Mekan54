package com.example.mekan54.model;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name="venues")
public class Venue {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String venueName;

    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }
    public List<Favorite> getFavorites() {
        return favorites;
    }
    public void setFavorites(List<Favorite> favorites) {
        this.favorites= favorites;
    }

    @OneToMany(mappedBy = "venue", cascade = CascadeType.ALL)
    private List<Comment> comments;

    @OneToMany(mappedBy = "venue", cascade = CascadeType.ALL)
    private List <Favorite> favorites;

    public String getWorkingHour() {
        return workingHour;
    }

    public void setWorkingHour(String workingHour) {
        this.workingHour = workingHour;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    private String workingHour;
    private String website;
    private String phoneNumber;

    public List<Image> getImages() {
        return images;
    }
    public void setImages(List<Image> images) {
        this.images = images;
    }
    @OneToMany(mappedBy = "venue", cascade = CascadeType.ALL)
    private List<Image> images ;
    public String getAdress() {
        return adress;
    }
    public void setAdress(String adress) {
        this.adress = adress;
    }
    private String adress;

    @ManyToOne
    @JoinColumn (name ="category_id")
    private Category category;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
    @OneToOne
    @JoinColumn(name = "user_id", unique = true)
    private User user;
    public Venue () {}
    public Category getCategory() {
        return category;
    }
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    public String getVenueName() {
        return venueName;
    }
    public void setVenueName(String venueName) {
        this.venueName = venueName;
    }
    public void setCategory(Category category) {
        this.category = category;
    }


}
