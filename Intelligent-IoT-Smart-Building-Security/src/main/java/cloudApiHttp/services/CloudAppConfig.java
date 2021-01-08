package cloudApiHttp.services;

import buildingSecurityController.api.persistance.DefaultInventoryDataManager;
import buildingSecurityController.api.persistance.IInventoryDataManager;
import cloudApiHttp.persistance.DefaultInventoryDataCollector;
import cloudApiHttp.persistance.IInventoryCollectorPack;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import io.federecio.dropwizard.swagger.SwaggerBundleConfiguration;

public class CloudAppConfig extends Configuration {

    @JsonProperty("swagger")
    public SwaggerBundleConfiguration swaggerBundleConfiguration;

    private IInventoryCollectorPack inventoryCollectorPack = null;

    public IInventoryCollectorPack getInventoryCollectorPack(){
        if(this.inventoryCollectorPack == null)
            this.inventoryCollectorPack = new DefaultInventoryDataCollector();
        return this.inventoryCollectorPack;
    }
}
