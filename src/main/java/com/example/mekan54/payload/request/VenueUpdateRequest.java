package com.example.mekan54.payload.request;

public class  VenueUpdateRequest {

    private String venueName;
    private String categoryName;

    private String phoneNumber;

    private String website;

    public Boolean getReservation() {
        return isReservation;
    }

    public void setReservation(Boolean reservation) {
      this.isReservation = reservation;
    }

    private Boolean isReservation;

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getWorkingHour() {
        return workingHour;
    }

    public void setWorkingHour(String workingHour) {
        this.workingHour = workingHour;
    }

    private String workingHour;


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
