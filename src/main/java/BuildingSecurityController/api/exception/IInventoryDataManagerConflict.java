package BuildingSecurityController.api.exception;

public class IInventoryDataManagerConflict extends Exception{
    public IInventoryDataManagerConflict(String errorMessage){
        super(errorMessage);
    }
}
