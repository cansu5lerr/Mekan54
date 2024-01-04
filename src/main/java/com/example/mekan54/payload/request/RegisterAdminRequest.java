package com.example.mekan54.payload.request;

public class RegisterAdminRequest {

    private String venueName;
    private String adress;
    private String email;
    private String password;
    private String repeatPassword;
    private String categoryName;
    public String getVenueName() {return venueName;}

    public void setVenueName(String venueName) {this.venueName = venueName;}

    public String getAdress() {return adress;}
    public void  setAdress (String adress) {this.adress= adress;}
    public String getEmail () {return email;}
    public void setEmail(String email) {this.email = email;}
    public String getPassword() {return password;}
    public void setPassword(String password) {this.password= password;}
    public String getRepeatPassword() {return repeatPassword;}
    public void setRepeatPassword(String repeatPassword) {this.repeatPassword = repeatPassword;}
    public String getCategoryName () {return categoryName; }
    public void setCategoryName(String categoryName) {this.categoryName= categoryName;}


}
