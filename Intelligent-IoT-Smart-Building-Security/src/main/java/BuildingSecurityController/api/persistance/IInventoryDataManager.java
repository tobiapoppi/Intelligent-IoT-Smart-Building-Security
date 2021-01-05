package BuildingSecurityController.api.persistance;

import BuildingSecurityController.api.exception.IInventoryDataManagerConflict;
import BuildingSecurityController.api.exception.IInventoryDataManagerException;
import BuildingSecurityController.api.model.*;
import smartBuilding.server.resource.coap.CoapPirResource;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface IInventoryDataManager {

    //policies management

    public List<PolicyDescriptor> getPolicyList() throws IInventoryDataManagerException;
    public List<PolicyDescriptor> getPolicyListByFloor(String floorId) throws IInventoryDataManagerException;
    public Optional<PolicyDescriptor> getPolicy(String policy_id) throws IInventoryDataManagerException;
    public PolicyDescriptor createNewPolicy(PolicyDescriptor policyDescriptor) throws IInventoryDataManagerException, IInventoryDataManagerConflict;
    public PolicyDescriptor updatePolicy(PolicyDescriptor policyDescriptor) throws IInventoryDataManagerException;
    public PolicyDescriptor deletePolicy(String policy_id) throws IInventoryDataManagerException;

    //users management

    public List<String> getUsernameList() throws IInventoryDataManagerException;
    public Optional<UserDescriptor> getUser(String username) throws IInventoryDataManagerException;
    public UserDescriptor createNewUser(UserDescriptor userDescriptor) throws IInventoryDataManagerException, IInventoryDataManagerConflict, IOException;
    public UserDescriptor updateUser(UserDescriptor userDescriptor) throws IInventoryDataManagerException;
    public UserDescriptor deleteUser(String username) throws IInventoryDataManagerException;

    //BUILDING MANAGEMENT

    public List<FloorDescriptor> getFloorList() throws IInventoryDataManagerException;
    public FloorDescriptor createNewFloor(FloorDescriptor floorDescriptor) throws IInventoryDataManagerException, IInventoryDataManagerConflict;
    public Optional<FloorDescriptor> getFloor(String floorId) throws IInventoryDataManagerException;
    public FloorDescriptor updateFloor(FloorDescriptor floorDescriptor) throws IInventoryDataManagerException;
    public FloorDescriptor deleteFloor(String floorId) throws IInventoryDataManagerException;

    //AREA MANAGEMENT

    public List<AreaDescriptor> getAreaList() throws IInventoryDataManagerException;
    public AreaDescriptor createNewArea(AreaDescriptor areaDescriptor) throws IInventoryDataManagerException, IInventoryDataManagerConflict;
    public Optional<AreaDescriptor> getArea(String areaId) throws IInventoryDataManagerException;
    public AreaDescriptor updateArea(AreaDescriptor areaDescriptor) throws IInventoryDataManagerException;
    public AreaDescriptor deleteArea(String areaId) throws IInventoryDataManagerException;

    //DEVICES management

    public List<GenericDeviceDescriptor> getDeviceList() throws IInventoryDataManagerException;
    public GenericDeviceDescriptor createNewDevice(GenericDeviceDescriptor genericDeviceDescriptor) throws IInventoryDataManagerException, IInventoryDataManagerConflict;
    public Optional<GenericDeviceDescriptor> getDevice(String device_id) throws IInventoryDataManagerException;
    public GenericDeviceDescriptor updateDevice(GenericDeviceDescriptor genericDeviceDescriptor) throws  IInventoryDataManagerException;
    public GenericDeviceDescriptor deleteDevice(String device_id) throws IInventoryDataManagerException;

    //RESOURCE DEVICES management

    public List<ResourceDescriptor> getResourceList() throws IInventoryDataManagerException;
    public Optional<ResourceDescriptor> getResource(String device_id) throws IInventoryDataManagerException;
    public ResourceDescriptor updateResource(ResourceDescriptor resourceDescriptor) throws  IInventoryDataManagerException;


}
