package com.example.mekan54.payload.response;

public class VenueUpdateResponse {
    private String venueName;
    private String categoryName;


    private String phoneNumber;

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

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

    private String workingHour;
    private String website;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    private Long id;
    public VenueUpdateResponse(Long id,String venueName, String categoryName, String phoneNumber, String workingHour, String website, String adress) {
       this.id = id;
        this.venueName = venueName;
        this.categoryName = categoryName;
        this.phoneNumber = phoneNumber;
        this.workingHour = workingHour;
        this.website = website;
        this.adress = adress;
    }

    public String getVenueName() {
        return venueName;
    }
    public String getCategoryName() {return categoryName;}
    public void setCategoryName(String categoryName) {this.categoryName= categoryName;}
    public void setVenueName(String venueName) {
        this.venueName = venueName;
    }

    public String getAdress() {
        return adress;
    }

    public void setAdress(String adress) {
        this.adress = adress;
    }
    private String adress;
}
