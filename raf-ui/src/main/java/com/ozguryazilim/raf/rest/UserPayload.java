package com.ozguryazilim.raf.rest;

import java.io.Serializable;

/**
 *
 * @author oyas
 */
public class UserPayload implements Serializable{


    private Long id;
    private String loginName;
    private String firstName;
    private String lastName;
    private String email;
    private String mobile;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLoginName() {
        return loginName;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    @Override
    public String toString() {
        return "UserPayload{" + "id=" + id + ", loginName=" + loginName + ", firstName=" + firstName + ", lastName=" + lastName + ", email=" + email + ", mobile=" + mobile + '}';
    }
    
    
    
}
