package com.example.mekan54.payload.response;

import java.util.List;
import java.util.Map;

public class VenueResponse {

    private String venueName;
    private String adress;
    private Integer favoriteSize;

    public List<String> getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(List<String> imgUrl) {
        this.imgUrl = imgUrl;
    }

    private List<String> imgUrl;

    public List<Map<String, String>> getComments() {
        return comments;
    }

    public void setComments(List<Map<String, String>> comments) {
        this.comments = comments;
    }

    private List<Map<String, String>> comments;

    public Map<String, String> getAbout() {
        return about;
    }

    public void setAbout(Map<String, String> about) {
        this.about = about;
    }

    private Map<String,String> about;

    private String categoryName;
    public String getVenueName() {return venueName;}
    public void setVenueName(String venueName) {this.venueName = venueName;}
    public Integer getFavoriteSize() {return favoriteSize;}
    public void setFavoriteSize(Integer favoriteSize) {this.favoriteSize= favoriteSize;}
    public String getAdress() {return adress;}
    public void setAdress(String adress) {this.adress= adress;}
    public String getCategoryName() {return categoryName;}
    public void setCategoryName(String categoryName) {this.categoryName= categoryName;}

}