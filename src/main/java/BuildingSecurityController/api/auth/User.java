package BuildingSecurityController.api.auth;


import java.security.Principal;


public class User implements Principal {
    private final String name;

    private final String role;

    public User(String name) {
        this.name = name;
        this.role = null;
    }

    public User(String name, String role) {
        this.name = name;
        this.role = role;
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return (int) (Math.random() * 100);
    }

    public String getRole() {
        return role;
    }

    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                ", role='" + role + '\'' +
                '}';
    }
}