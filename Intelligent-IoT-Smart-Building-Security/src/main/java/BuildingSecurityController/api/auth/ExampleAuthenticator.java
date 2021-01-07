package BuildingSecurityController.api.auth;

import BuildingSecurityController.api.model.UserDescriptor;
import BuildingSecurityController.api.resources.PolicyResource;
import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authenticator;
import io.dropwizard.auth.basic.BasicCredentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.zip.GZIPInputStream;

public class ExampleAuthenticator implements Authenticator<BasicCredentials, User>{


    final protected Logger logger = LoggerFactory.getLogger(ExampleAuthenticator.class);
    private HashMap<String, UserDescriptor> userMap = new HashMap<>();

    public void deserialize() throws IOException, ClassNotFoundException {
        try {
            FileInputStream fileInputStream = new FileInputStream("users-file");
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);

            userMap = (HashMap<String, UserDescriptor>) objectInputStream.readObject();

            fileInputStream.close();
            objectInputStream.close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

    }


    @Override
    public Optional<User> authenticate(BasicCredentials credentials) throws AuthenticationException {
        try {
            deserialize();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        if (userMap.get(credentials.getUsername()).getPassword().equals(credentials.getPassword())) {
            return Optional.of(new User(credentials.getUsername(), userMap.get(credentials.getUsername()).getRole()));
        }

        return Optional.empty();
    }
}