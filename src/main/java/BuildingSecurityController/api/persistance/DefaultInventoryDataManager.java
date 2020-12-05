package BuildingSecurityController.api.persistance;


import BuildingSecurityController.api.exception.IInventoryDataManagerConflict;
import BuildingSecurityController.api.exception.IInventoryDataManagerException;
import BuildingSecurityController.api.model.PolicyDescriptor;
import BuildingSecurityController.api.model.UserDescriptor;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.GZIPOutputStream;

public class DefaultInventoryDataManager implements IInventoryDataManager {


    private HashMap<String, PolicyDescriptor> policyMap;
    private HashMap<String, UserDescriptor> userMap;


    public DefaultInventoryDataManager() {
        this.policyMap = new HashMap<>();
        this.userMap = new HashMap<>();
    }

    public void SerializeOnFile(HashMap hmap) throws IOException {

        File file = new File("test.dat");
        ObjectOutputStream output = new ObjectOutputStream(new GZIPOutputStream(new FileOutputStream(file)));


        //Write the map to the output stream, then close
        output.writeObject(hmap);
        output.flush();
        output.close();

    }


    @Override
    public List<PolicyDescriptor> getPolicyList() throws IInventoryDataManagerException {
        return new ArrayList<>(this.policyMap.values());
    }

    @Override
    public List<PolicyDescriptor> getPolicyListByLocation(String location_id) throws IInventoryDataManagerException {
        return this.policyMap.values().stream()
                .filter(policyDescriptor -> policyDescriptor != null && policyDescriptor.getLocation_id().equals(location_id))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<PolicyDescriptor> getPolicy(String policy_id) throws IInventoryDataManagerException {
        return Optional.ofNullable(this.policyMap.get(policy_id));
    }

    @Override
    public PolicyDescriptor createNewPolicy(PolicyDescriptor policyDescriptor) throws IInventoryDataManagerException, IInventoryDataManagerConflict{
        if(policyDescriptor.getPolicy_id() != null && this.getPolicy(policyDescriptor.getPolicy_id()).isPresent())
            throw new IInventoryDataManagerConflict("Policy for this zone already exists!");

        if(policyDescriptor.getPolicy_id() == null)
            policyDescriptor.setPolicy_id(UUID.randomUUID().toString());

        this.policyMap.put(policyDescriptor.getPolicy_id(), policyDescriptor);
        return policyDescriptor;
    }

    @Override
    public PolicyDescriptor updatePolicy(PolicyDescriptor policyDescriptor) throws IInventoryDataManagerException {
        this.policyMap.put(policyDescriptor.getPolicy_id(), policyDescriptor);
        return policyDescriptor;
    }

    @Override
    public PolicyDescriptor deletePolicy(String policy_id) throws IInventoryDataManagerException {
        return this.policyMap.remove(policy_id);
    }

    ///USER MANAGEMENTS

    @Override
    public List<UserDescriptor> getUserList() throws IInventoryDataManagerException {
        return null;
    }

    @Override
    public List<UserDescriptor> getUserListByUsername(String username) throws IInventoryDataManagerException {
        return null;
    }

    @Override
    public Optional<UserDescriptor> getUser(String username) throws IInventoryDataManagerException {
        return Optional.ofNullable(this.userMap.get(username));
    }

    @Override
    public UserDescriptor createNewUser(UserDescriptor userDescriptor) throws IInventoryDataManagerException, IInventoryDataManagerConflict, IOException {
        if(userDescriptor.getUsername() != null && this.getUser(userDescriptor.getUsername()).isPresent())
            throw new IInventoryDataManagerConflict("User already exists!");

        if(userDescriptor.getUsername() == null)
            userDescriptor.setUsername(UUID.randomUUID().toString());

        this.userMap.put(userDescriptor.getUsername(), userDescriptor);
        this.SerializeOnFile(userMap);
        return userDescriptor;
    }

    @Override
    public UserDescriptor updateUser(UserDescriptor userDescriptor) throws IInventoryDataManagerException {
        return null;
    }

    @Override
    public UserDescriptor deleteUser(String username) throws IInventoryDataManagerException {
        return null;
    }
}
