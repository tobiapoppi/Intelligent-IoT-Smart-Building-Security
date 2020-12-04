package BuildingSecurityController.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class UserDescriptor {

    @JsonProperty("id")
    private String internalId;

    @JsonProperty("username")
    private String username;

    @JsonProperty("password")
    private String password;

    @JsonProperty("role")
    private String role;

    @JsonProperty("email")
    private String email;

    @JsonProperty("allowed_locations")
    private List<String> allowedLocationList;

    public UserDescriptor(){

    }

    public UserDescriptor(String internalId, String username, String password, String role, String email, List<String> allowedLocationList) {
        this.internalId = internalId;
        this.username = username;
        this.password = password;
        this.role = role;
        this.email = email;
        this.allowedLocationList = allowedLocationList;
    }

    public String getInternalId() {
        return internalId;
    }

    public void setInternalId(String internalId) {
        this.internalId = internalId;
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

    public List<String> getAllowedLocationList() {
        return allowedLocationList;
    }

    public void setAllowedLocationList(List<String> allowedLocationList) {
        this.allowedLocationList = allowedLocationList;
    }

    @Override
    public String toString() {
        return "UserDescriptor{" +
                "internalId='" + internalId + '\'' +
                ", username='" + username + '\'' +
                ", role='" + role + '\'' +
                ", email='" + email + '\'' +
                ", allowedLocationList=" + allowedLocationList +
                '}';
    }
}
