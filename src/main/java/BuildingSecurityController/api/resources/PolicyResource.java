package BuildingSecurityController.api.resources;


import BuildingSecurityController.api.model.PolicyDescriptor;
import BuildingSecurityController.api.services.OperatorAppConfig;
import com.codahale.metrics.annotation.Timed;
import io.dropwizard.jersey.errors.ErrorMessage;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/api/iot/inventory/policy")
@Api("IoT Policy Inventory Endpoint")
public class PolicyResource {

    final protected Logger logger = LoggerFactory.getLogger(PolicyResource.class);

    @SuppressWarnings("serial")
    public static class MissingKeyException extends Exception{}
    final OperatorAppConfig conf;

    public PolicyResource(OperatorAppConfig conf){
        this.conf = conf;
    }

    @GET
    @Path("/")
    @Timed
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Get all the Policies")
    public Response GetPolicies(@Context ContainerRequestContext requestContext,
                                @QueryParam("location_id") String location_id){

        try{
            logger.info("Loading all stored IoT Inventory Policies.");

            List<PolicyDescriptor> serviceList = null;

            //without filter
            //if(location_id == null)
            serviceList = this.conf.getInventoryDataManager().getPolicyList();
            //else if(location_id != null)
            //    serviceList = this.conf.getInventoryDataManager().getPolicyListByLocation(location_id);
            //else
            //    return Response.status(Response.Status.BAD_REQUEST).type(MediaType.APPLICATION_JSON_TYPE).entity(new ErrorMessage(Response.Status.BAD_REQUEST.getStatusCode(), "only locationId is required for filtering!")).build();

            if(serviceList == null)
                return Response.status(Response.Status.NOT_FOUND).type(MediaType.APPLICATION_JSON_TYPE).entity(new ErrorMessage(Response.Status.NOT_FOUND.getStatusCode(), "Policies not found")).build();

            return Response.ok(serviceList).build();

        } catch(Exception e){
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).type(MediaType.APPLICATION_JSON_TYPE).entity(new ErrorMessage(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), "Internal Server Error!")).build();
        }
    }


}
