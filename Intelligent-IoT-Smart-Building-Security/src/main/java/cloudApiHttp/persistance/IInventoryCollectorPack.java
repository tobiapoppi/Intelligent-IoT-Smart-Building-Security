package cloudApiHttp.persistance;

import buildingSecurityController.api.exception.IInventoryDataManagerConflict;
import buildingSecurityController.api.exception.IInventoryDataManagerException;
import utils.SenMLPack;


public interface IInventoryCollectorPack {

    public SenMLPack createNewPack(SenMLPack pack) throws IInventoryDataManagerException, IInventoryDataManagerConflict;

}
