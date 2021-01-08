package buildingSecurityController.api.services;

import buildingSecurityController.api.persistance.DefaultInventoryDataManager;
import buildingSecurityController.api.persistance.IInventoryDataManager;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import io.federecio.dropwizard.swagger.SwaggerBundleConfiguration;

public class OperatorAppConfig extends Configuration {

    @JsonProperty("swagger")
    public SwaggerBundleConfiguration swaggerBundleConfiguration;

    private IInventoryDataManager inventoryDataManager = null;

    public IInventoryDataManager getInventoryDataManager(){
        if(this.inventoryDataManager == null)
            this.inventoryDataManager = new DefaultInventoryDataManager();
        return this.inventoryDataManager;
    }

}
