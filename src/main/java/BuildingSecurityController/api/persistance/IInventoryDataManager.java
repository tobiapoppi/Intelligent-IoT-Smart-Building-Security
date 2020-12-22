package BuildingSecurityController.api.persistance;

import BuildingSecurityController.api.exception.IInventoryDataManagerConflict;
import BuildingSecurityController.api.exception.IInventoryDataManagerException;
import BuildingSecurityController.api.model.FloorDescriptor;
import BuildingSecurityController.api.model.PolicyDescriptor;
import BuildingSecurityController.api.model.UserDescriptor;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface IInventoryDataManager {

    //policies management

    public List<PolicyDescriptor> getPolicyList() throws IInventoryDataManagerException;
    public List<PolicyDescriptor> getPolicyListByLocation(String location_id) throws IInventoryDataManagerException;
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

    //building management

    public List<FloorDescriptor> getFloorList() throws IInventoryDataManagerException;
    public FloorDescriptor createNewFloor(FloorDescriptor floorDescriptor) throws IInventoryDataManagerException, IInventoryDataManagerConflict;
    public Optional<FloorDescriptor> getFloor(int floor_number) throws IInventoryDataManagerException;
    public FloorDescriptor updateFloor(FloorDescriptor floorDescriptor) throws IInventoryDataManagerException;
    public FloorDescriptor deleteFloor(int floor_inumber) throws IInventoryDataManagerException;

}
