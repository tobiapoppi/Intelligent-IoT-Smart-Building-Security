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
import java.util.Optional;
import java.util.zip.GZIPInputStream;

public class ExampleAuthenticator implements Authenticator<BasicCredentials, User>{


    final protected Logger logger = LoggerFactory.getLogger(PolicyResource.class);
    private HashMap<String, UserDescriptor> userMap = null;

    public void deserialize() throws IOException, ClassNotFoundException {
        File file = new File("test.dat");
        ObjectInputStream input = new ObjectInputStream(new GZIPInputStream(new FileInputStream(file)));
        //Reads the first object in
        Object readObject = input.readObject();
        input.close();

        if(!(readObject instanceof HashMap)) throw new IOException("Data is not a hashmap");
        userMap = (HashMap<String, UserDescriptor>) readObject;
        //Prints out everything in the map.
        for(String key : userMap.keySet()) {
            System.out.println(key + ": " + userMap.get(key));
        }
        logger.info("ciaoooooooooooooooooooooooooooooooooooooo");
    }


    @Override
    public Optional<User> authenticate(BasicCredentials credentials) throws AuthenticationException {
        try {
            deserialize();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        if (userMap.get(credentials.getUsername()).getPassword().equals(credentials.getPassword())) {
            return Optional.of(new User(credentials.getUsername()));
        }
        return Optional.empty();
    }
}