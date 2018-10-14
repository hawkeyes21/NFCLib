package com.dell.nfclib;

public class userDetails
{
    String fullName, email, designation;

    public userDetails()
    {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public userDetails(String fullName, String email)
    {
        this.fullName = fullName;
        this.email = email;
    }

    public String getDesignation()
    {
        return designation;
    }

    public void setDesignation(String designation)
    {
        this.designation = designation;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

}
