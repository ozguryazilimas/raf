package com.ozguryazilim.raf.rest;

import java.io.Serializable;

/**
 * @author oyas
 */
public class UserPayload implements Serializable {

    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String loginName;
    private String userType;
    private String passwordEncodedHash;
    private String mobile;
    private Boolean autoCreated;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getLoginName() {
        return loginName;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public String getPasswordEncodedHash() {
        return passwordEncodedHash;
    }

    public void setPasswordEncodedHash(String passwordEncodedHash) {
        this.passwordEncodedHash = passwordEncodedHash;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public Boolean isAutoCreated() {
        return autoCreated;
    }

    public void setAutoCreated(Boolean autoCreated) {
        this.autoCreated = autoCreated;
    }

    @Override
    public String toString() {
        return "UserPayload{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", loginName='" + loginName + '\'' +
                ", userType='" + userType + '\'' +
                ", passwordEncodedHash='" + passwordEncodedHash + '\'' +
                ", mobile='" + mobile + '\'' +
                ", autoCreated='" + autoCreated + '\'' +
                '}';
    }
}
