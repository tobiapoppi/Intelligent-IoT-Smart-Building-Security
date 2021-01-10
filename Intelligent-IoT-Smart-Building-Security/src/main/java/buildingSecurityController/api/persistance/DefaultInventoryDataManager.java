package buildingSecurityController.api.persistance;

import buildingSecurityController.api.exception.IInventoryDataManagerConflict;
import buildingSecurityController.api.exception.IInventoryDataManagerException;
import buildingSecurityController.api.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class DefaultInventoryDataManager implements IInventoryDataManager {

    final protected Logger logger = LoggerFactory.getLogger(DefaultInventoryDataManager.class);

    private HashMap<String, PolicyDescriptor> policyMap;
    private HashMap<String, UserDescriptor> userMap;
    private HashMap<String, FloorDescriptor> floorMap;
    private HashMap<String, AreaDescriptor> areaMap;
    private HashMap<String, GenericDeviceDescriptor> deviceMap;
    private HashMap<String, ResourceDescriptor> resourceDeviceMap;



    public DefaultInventoryDataManager() {
        this.policyMap = new HashMap<>();
        this.floorMap = new HashMap<>();
        this.areaMap = new HashMap<>();
        this.deviceMap = new HashMap<>();
        this.resourceDeviceMap = new HashMap<>();
        this.userMap = new HashMap<>();
    }

    public void SerializeOnFile(HashMap<String, UserDescriptor> hmap) throws IOException {

        try {
            FileOutputStream fileOutputStream = new FileOutputStream("users-file");
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);

            objectOutputStream.writeObject(hmap);

            fileOutputStream.close();
            objectOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        logger.info(hmap.toString());

    }

    ///POLICY RESOURCE MANAGEMENT

    @Override
    public List<PolicyDescriptor> getPolicyList() throws IInventoryDataManagerException {
        return new ArrayList<>(this.policyMap.values());
    }

    @Override
    public List<PolicyDescriptor> getPolicyListByFloor(String floorId) throws IInventoryDataManagerException {
        return this.policyMap.values().stream()
                .filter(policyDescriptor -> policyDescriptor != null && policyDescriptor.getArea_id().equals(floorId))
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

    ///USER RESOURCE MANAGEMENTS

    @Override
    public List<String> getUsernameList() throws IInventoryDataManagerException {
        return new ArrayList<>(this.userMap.keySet());
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
        this.SerializeOnFile(this.userMap);
        return userDescriptor;
    }

    @Override
    public UserDescriptor updateUser(UserDescriptor userDescriptor) throws IInventoryDataManagerException {
        this.userMap.put(userDescriptor.getUsername(), userDescriptor);
        return userDescriptor;
    }

    @Override
    public UserDescriptor deleteUser(String username) throws IInventoryDataManagerException {
        return this.userMap.remove(username);
    }

    ///BUILDING RESOURCE MANAGEMENT

    @Override
    public List<FloorDescriptor> getFloorList() throws IInventoryDataManagerException{
        return new ArrayList<>(this.floorMap.values());
    }

    @Override
    public FloorDescriptor createNewFloor(FloorDescriptor floorDescriptor) throws IInventoryDataManagerException, IInventoryDataManagerConflict {
        if(this.getFloor(floorDescriptor.getFloor_id()).isPresent())
            throw new IInventoryDataManagerConflict("Floor already exists!");

        this.floorMap.put(floorDescriptor.getFloor_id(), floorDescriptor);
        return floorDescriptor;
    }

    @Override
    public Optional<FloorDescriptor> getFloor(String floorId) throws IInventoryDataManagerException {
        return Optional.ofNullable(this.floorMap.get(floorId));
    }

    @Override
    public FloorDescriptor updateFloor(FloorDescriptor floorDescriptor) throws IInventoryDataManagerException {
        this.floorMap.put(floorDescriptor.getFloor_id(), floorDescriptor);
        return floorDescriptor;
    }

    @Override
    public FloorDescriptor deleteFloor(String floorId) throws IInventoryDataManagerException {
        return this.floorMap.remove(floorId);
    }

    @Override
    public List<AreaDescriptor> getAreaList() throws IInventoryDataManagerException {
        return new ArrayList<>(this.areaMap.values());
    }

    @Override
    public AreaDescriptor createNewArea(AreaDescriptor areaDescriptor) throws IInventoryDataManagerException, IInventoryDataManagerConflict {
        if(this.getArea(areaDescriptor.getAreaId()).isPresent())
            throw new IInventoryDataManagerConflict("Area already exists!");

        areaDescriptor.setAreaId(String.format("%s:%s", areaDescriptor.getFloorId(), areaDescriptor.getAreaName()));

        this.areaMap.put(areaDescriptor.getAreaId(), areaDescriptor);
        return areaDescriptor;
    }

    @Override
    public Optional<AreaDescriptor> getArea(String areaId) throws IInventoryDataManagerException {
        return Optional.ofNullable(this.areaMap.get(areaId));
    }

    @Override
    public AreaDescriptor updateArea(AreaDescriptor areaDescriptor) throws IInventoryDataManagerException {
        this.areaMap.put(areaDescriptor.getAreaId(), areaDescriptor);
        return areaDescriptor;
    }

    @Override
    public AreaDescriptor deleteArea(String areaId) throws IInventoryDataManagerException {
        return this.areaMap.remove(areaId);
    }

    @Override
    public List<GenericDeviceDescriptor> getDeviceList() throws IInventoryDataManagerException {
        return new ArrayList<>(this.deviceMap.values());
    }

    @Override
    public GenericDeviceDescriptor createNewDevice(GenericDeviceDescriptor genericDeviceDescriptor) throws IInventoryDataManagerException, IInventoryDataManagerConflict {

        if(this.getDevice(genericDeviceDescriptor.getDeviceId()).isPresent())
            throw new IInventoryDataManagerConflict("Device already exists!");

        this.deviceMap.put(genericDeviceDescriptor.getDeviceId(), genericDeviceDescriptor);
        return genericDeviceDescriptor;
    }

    @Override
    public Optional<GenericDeviceDescriptor> getDevice(String device_id) throws IInventoryDataManagerException {
        return Optional.ofNullable(this.deviceMap.get(device_id));
    }

    @Override
    public GenericDeviceDescriptor updateDevice(GenericDeviceDescriptor genericDeviceDescriptor) throws IInventoryDataManagerException {
        this.deviceMap.put(genericDeviceDescriptor.getDeviceId(), genericDeviceDescriptor);
        this.areaMap.get(genericDeviceDescriptor.getAreaId()).addDeviceToList(genericDeviceDescriptor.getDeviceId());
        return genericDeviceDescriptor;
    }

    @Override
    public GenericDeviceDescriptor deleteDevice(String device_id) throws IInventoryDataManagerException {
        return this.deviceMap.remove(device_id);
    }

    @Override
    public ResourceDescriptor createNewResource(ResourceDescriptor resourceDescriptor) throws IInventoryDataManagerException, IInventoryDataManagerConflict {

        this.resourceDeviceMap.put(resourceDescriptor.getResourceId(), resourceDescriptor);
        return resourceDescriptor;
    }

    @Override
    public List<ResourceDescriptor> getResourceList() throws IInventoryDataManagerException {
        return new ArrayList<>(this.resourceDeviceMap.values());
    }

    @Override
    public Optional<ResourceDescriptor> getResource(String resource_id) throws IInventoryDataManagerException {
        return Optional.ofNullable(this.resourceDeviceMap.get(resource_id));
    }

    @Override
    public ResourceDescriptor updateResource(ResourceDescriptor resourceDescriptor) throws IInventoryDataManagerException {
        this.resourceDeviceMap.put(resourceDescriptor.getResourceId(), resourceDescriptor);
        return resourceDescriptor;
    }


}
