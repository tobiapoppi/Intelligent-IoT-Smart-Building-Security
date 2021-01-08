package buildingSecurityController.api.auth;

import io.dropwizard.auth.Authorizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExampleAuthorizer implements Authorizer<User> {

    final protected Logger logger = LoggerFactory.getLogger(ExampleAuthorizer.class);

    @Override
    public boolean authorize(User user, String role) {
        //in this method, only the users with role defined in the "@AllowedRoles" in the resource file, are allowed.
        return (user.getRole().equals(role));
    }
}