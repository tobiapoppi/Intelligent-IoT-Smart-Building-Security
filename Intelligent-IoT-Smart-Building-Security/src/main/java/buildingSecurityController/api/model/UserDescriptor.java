package buildingSecurityController.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

public class UserDescriptor implements Serializable {


    @JsonProperty("username")
    private String username;

    @JsonProperty("password")
    private String password;

    @JsonProperty("role")
    private String role;

    @JsonProperty("email")
    private String email;

    public UserDescriptor(){

    }

    public UserDescriptor(String username, String password, String role, String email) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.email = email;
    }





    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }




    @Override
    public String toString() {
        return "[ username : " + username + ", password : " + password + ", role : " + role + ", email : " + email + "]";
    }
}
