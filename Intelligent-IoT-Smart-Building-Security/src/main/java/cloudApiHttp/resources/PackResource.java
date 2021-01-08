package cloudApiHttp.resources;


import buildingSecurityController.api.data_transfer_object.PolicyCreationRequest;
import buildingSecurityController.api.exception.IInventoryDataManagerConflict;
import buildingSecurityController.api.model.PolicyDescriptor;
import buildingSecurityController.api.resources.PolicyResource;
import buildingSecurityController.api.services.OperatorAppConfig;
import cloudApiHttp.services.CloudAppConfig;
import com.codahale.metrics.annotation.Timed;
import io.dropwizard.jersey.errors.ErrorMessage;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.SenMLPack;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;

@Path("/cloudCollector/")
@Api("Packs resources Collector")
public class PackResource {

    final protected Logger logger = LoggerFactory.getLogger(PackResource.class);

    @SuppressWarnings("serial")
    public static class MissingKeyException extends Exception{}
    final CloudAppConfig conf;

    public PackResource(CloudAppConfig conf){
        this.conf = conf;
    }

    @POST
    @Path("/pack")
    @Timed
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value="Create a new Pack")
    public Response createPackAlarm(@Context ContainerRequestContext req,
                                   @Context UriInfo uriInfo,
                                   SenMLPack senMLPack) {
        try {

            logger.info("Incoming Policy Creation Request: {}", senMLPack);

            //Check the request
            if(senMLPack == null)
                return Response.status(Response.Status.BAD_REQUEST).type(MediaType.APPLICATION_JSON_TYPE).entity(new ErrorMessage(Response.Status.BAD_REQUEST.getStatusCode(),"Invalid request payload")).build();

            SenMLPack newPack = (SenMLPack) senMLPack;

            this.conf.getInventoryCollectorPack().createNewPack(newPack);

            return Response.ok().build();

        } catch (IInventoryDataManagerConflict e){
            return Response.status(Response.Status.CONFLICT).type(MediaType.APPLICATION_JSON_TYPE).entity(new ErrorMessage(Response.Status.CONFLICT.getStatusCode(),"Policy already available !")).build();
        } catch (Exception e){
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).type(MediaType.APPLICATION_JSON_TYPE).entity(new ErrorMessage(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),"Internal Server Error !")).build();
        }
    }
}
