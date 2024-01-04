package com.example.mekan54.payload.request;

public class ResetPassword {

    private String password;
    private String repeatPassword;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRepeatPassword() {return repeatPassword;}
    public void setRepeatPassword(String repeatPassword) {this.repeatPassword= repeatPassword;}


}
