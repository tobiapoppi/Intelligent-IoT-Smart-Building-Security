package buildingSecurityController.api.resources;

import buildingSecurityController.api.client.CoapResourceClient;
import buildingSecurityController.api.client.LookupAndObserveProcess;
import buildingSecurityController.api.data_transfer_object.DeviceUpdateRequest;
import buildingSecurityController.api.model.AreaDescriptor;
import buildingSecurityController.api.model.GenericDeviceDescriptor;
import buildingSecurityController.api.model.ResourceDescriptor;
import buildingSecurityController.api.services.OperatorAppConfig;
import com.codahale.metrics.annotation.Timed;
import io.dropwizard.jersey.errors.ErrorMessage;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.eclipse.californium.core.CoapResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.List;
import java.util.Optional;

@Path("/TheBuildingSecurity/allDevices")
@Api("IoT Devices Resource")
public class DevicesResource {

    final protected Logger logger = LoggerFactory.getLogger(DevicesResource.class);

    @SuppressWarnings("serial")
    public static class MissingKeyException extends Exception {
    }

    final OperatorAppConfig conf;
    LookupAndObserveProcess lookupAndObserveProcess;

    CoapResourceClient coapResourceClient = new CoapResourceClient();

    public DevicesResource(OperatorAppConfig conf, LookupAndObserveProcess lookupAndObserveProcess) throws InterruptedException {
        this.conf = conf;
        this.lookupAndObserveProcess = lookupAndObserveProcess;
    }

    //DEVICE MANAGEMENT

    @RolesAllowed("USER")
    @GET
    @Path("/")
    @Timed
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Get all the Devices of the building")
    public Response GetDevices(@Context ContainerRequestContext requestContext) {
        try {

            logger.info("Loading all the Unallocated Devices.");

            List<GenericDeviceDescriptor> genericDeviceDescriptors = this.conf.getInventoryDataManager().getDeviceList();

            if (genericDeviceDescriptors.isEmpty())
                return Response.status(Response.Status.NOT_FOUND).type(MediaType.APPLICATION_JSON_TYPE).entity(new ErrorMessage(Response.Status.NOT_FOUND.getStatusCode(), "No Devices found")).build();

            return Response.ok(genericDeviceDescriptors).build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).type(MediaType.APPLICATION_JSON_TYPE).entity(new ErrorMessage(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), "Internal Server Error!")).build();
        }
    }


    @RolesAllowed("USER")
    @PUT
    @Path("/{device_id}")
    @Timed
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Modify a Device or allocate it to an Area")
    public Response updateDevice(@Context ContainerRequestContext requestContext, @Context UriInfo uriInfo,
                                 @PathParam("device_id") String deviceId,
                                 DeviceUpdateRequest deviceUpdateRequest) {

        try {

            //Check the request
            if (deviceUpdateRequest == null || !deviceUpdateRequest.getDeviceId().equals(deviceId))
                return Response.status(Response.Status.BAD_REQUEST).type(MediaType.APPLICATION_JSON_TYPE).entity(new ErrorMessage(Response.Status.BAD_REQUEST.getStatusCode(), "Invalid Floorid or AreaId or DeviceId Provided !")).build();

            if (!this.conf.getInventoryDataManager().getDevice(deviceId).isPresent())
                return Response.status(Response.Status.NOT_FOUND).type(MediaType.APPLICATION_JSON_TYPE).entity(new ErrorMessage(Response.Status.NOT_FOUND.getStatusCode(),"Device not found !")).build();

            GenericDeviceDescriptor genericDeviceDescriptor = this.conf.getInventoryDataManager().getDevice(deviceId).get();

            genericDeviceDescriptor.setDeviceId(deviceUpdateRequest.getDeviceId());
            genericDeviceDescriptor.setAreaId(deviceUpdateRequest.getAreaId());

            Optional<AreaDescriptor> areaDescriptor = this.conf.getInventoryDataManager().getArea(deviceUpdateRequest.getAreaId());

            if (areaDescriptor.isPresent()){

                this.conf.getInventoryDataManager().updateDevice(genericDeviceDescriptor);
                return Response.noContent().build();

            }
            return Response.status(Response.Status.BAD_REQUEST).type(MediaType.APPLICATION_JSON_TYPE).entity(new ErrorMessage(Response.Status.BAD_REQUEST.getStatusCode(), "Area or floor does not exists !")).build();


        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).type(MediaType.APPLICATION_JSON_TYPE).entity(new ErrorMessage(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), "Internal Server Error !")).build();
        }
    }

    //RESOURCE MANAGEMENT

    @RolesAllowed("USER")
    @GET
    @Path("/{device_id}/resource")
    @Timed
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Get all the Devices' Resources")
    public Response GetResource(@Context ContainerRequestContext requestContext,
                                @PathParam("device_id") String deviceId) {
        try {

            if (deviceId == null)
                return Response.status(Response.Status.BAD_REQUEST).type(MediaType.APPLICATION_JSON_TYPE).entity(new ErrorMessage(Response.Status.BAD_REQUEST.getStatusCode(), "Invalid DeviceId Provided !")).build();

            logger.info("Loading all the Device {} 's Resources", deviceId);

            Optional<GenericDeviceDescriptor> genericDeviceDescriptor = this.conf.getInventoryDataManager().getDevice(deviceId);

            if (!genericDeviceDescriptor.isPresent())
                return Response.status(Response.Status.NOT_FOUND).type(MediaType.APPLICATION_JSON_TYPE).entity(new ErrorMessage(Response.Status.NOT_FOUND.getStatusCode(), "Resource Not Found !")).build();


            if (genericDeviceDescriptor.get().getResourceList().isEmpty())
                return Response.status(Response.Status.NOT_FOUND).type(MediaType.APPLICATION_JSON_TYPE).entity(new ErrorMessage(Response.Status.NOT_FOUND.getStatusCode(), "No Resources found")).build();



            List<ResourceDescriptor> resourceDescriptors = this.conf.getInventoryDataManager().getResourceList();
            logger.info("{}", resourceDescriptors);

            resourceDescriptors.removeIf(resourceDescriptor -> !resourceDescriptor.getDeviceId().equals(deviceId));

            return Response.ok(resourceDescriptors).build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).type(MediaType.APPLICATION_JSON_TYPE).entity(new ErrorMessage(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), "Internal Server Error!")).build();
        }
    }


    @RolesAllowed("USER")
    @GET
    @Path("/resource/{resource_id}")
    @Timed
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Get a Resource's infos")
    public Response getDevice(@Context ContainerRequestContext requestContext,
                              @PathParam("resource_id") String resourceId) {
        try {

            //Check the request
            if (resourceId == null)
                return Response.status(Response.Status.BAD_REQUEST).type(MediaType.APPLICATION_JSON_TYPE).entity(new ErrorMessage(Response.Status.BAD_REQUEST.getStatusCode(), "Invalid FloorId or AreaId or DeviceId Provided !")).build();

            logger.info("Loading infos for resource: {}", resourceId);

            Optional<ResourceDescriptor> resourceDescriptor = this.conf.getInventoryDataManager().getResource(resourceId);

            //check if the device is present and if it is inside the correct area and the area is inside the correct floor
            if (!resourceDescriptor.isPresent())
                return Response.status(Response.Status.NOT_FOUND).type(MediaType.APPLICATION_JSON_TYPE).entity(new ErrorMessage(Response.Status.NOT_FOUND.getStatusCode(), "Resource Not Found !")).build();

            return Response.ok(resourceDescriptor.get()).build();


        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).type(MediaType.APPLICATION_JSON_TYPE).entity(new ErrorMessage(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), "Internal Server Error !")).build();
        }
    }

    @RolesAllowed("USER")
    @GET
    @Path("/resource/{resource_id}/proxy")
    @Timed
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Make a Coap Get request to the Resource")
    public Response getProxy(@Context ContainerRequestContext requestContext,
                              @PathParam("resource_id") String resourceId) {
        try {

            //Check the request
            if (resourceId == null)
                return Response.status(Response.Status.BAD_REQUEST).type(MediaType.APPLICATION_JSON_TYPE).entity(new ErrorMessage(Response.Status.BAD_REQUEST.getStatusCode(), "ResourceId Provided !")).build();

            logger.info("Loading infos for resource: {}", resourceId);

            Optional<ResourceDescriptor> resourceDescriptor = this.conf.getInventoryDataManager().getResource(resourceId);

            //check if the device is present and if it is inside the correct area and the area is inside the correct floor
            if (!resourceDescriptor.isPresent())
                return Response.status(Response.Status.NOT_FOUND).type(MediaType.APPLICATION_JSON_TYPE).entity(new ErrorMessage(Response.Status.NOT_FOUND.getStatusCode(), "Resource Not Found !")).build();

            CoapResourceClient coapResourceClient = new CoapResourceClient();

            String url = "";

            if (resourceId.contains("pir") || resourceId.contains("camera")){
                url = String.format("%s/%s:%s", this.conf.getInventoryDataManager().getResource(resourceId).get().getDeviceId(),resourceId.split(":")[0] ,resourceId.split(":")[2]);
                logger.info("url: {}", url);

            }else if(resourceId.contains("light") || resourceId.contains("alarm")){
                url = this.conf.getInventoryDataManager().getResource(resourceId).get().getDeviceId();
                logger.info("url: {}", url);
            }

            CoapResponse response = coapResourceClient.getRequest(String.format("coap://192.168.1.107:5683/%s", url));
            return Response.ok(response.getResponseText()).build();


        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).type(MediaType.APPLICATION_JSON_TYPE).entity(new ErrorMessage(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), "Internal Server Error !")).build();
        }
    }
    @RolesAllowed("USER")
    @PUT
    @Path("/resource/{resource_id}/proxy")
    @Timed
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Make a Coap Put request to the Resource")
    public Response putProxy(@Context ContainerRequestContext requestContext,
                             @PathParam("resource_id") String resourceId, String payload) {
        try {

            //Check the request
            if (resourceId == null || payload == null)
                return Response.status(Response.Status.BAD_REQUEST).type(MediaType.APPLICATION_JSON_TYPE).entity(new ErrorMessage(Response.Status.BAD_REQUEST.getStatusCode(), "ResourceId Provided !")).build();

            logger.info("Loading infos for resource: {}", resourceId);

            Optional<ResourceDescriptor> resourceDescriptor = this.conf.getInventoryDataManager().getResource(resourceId);

            //check if the device is present and if it is inside the correct area and the area is inside the correct floor
            if (!resourceDescriptor.isPresent())
                return Response.status(Response.Status.NOT_FOUND).type(MediaType.APPLICATION_JSON_TYPE).entity(new ErrorMessage(Response.Status.NOT_FOUND.getStatusCode(), "Resource Not Found !")).build();

            CoapResourceClient coapResourceClient = new CoapResourceClient();

            String url = "";

            if (resourceId.contains("pir") || resourceId.contains("camera")){
                url = String.format("%s/%s:%s", this.conf.getInventoryDataManager().getResource(resourceId).get().getDeviceId(),resourceId.split(":")[0] ,resourceId.split(":")[2]);
                logger.info("url: {}", url);

            }else if(resourceId.contains("light") || resourceId.contains("alarm")){
                url = this.conf.getInventoryDataManager().getResource(resourceId).get().getDeviceId();
                logger.info("url: {}", url);
            }

            CoapResponse response = coapResourceClient.putRequest(String.format("coap://192.168.1.107:5683/%s", url), payload);
            return Response.ok(response).build();


        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).type(MediaType.APPLICATION_JSON_TYPE).entity(new ErrorMessage(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), "Internal Server Error !")).build();
        }
    }
    @RolesAllowed("USER")
    @POST
    @Path("/resource/{resource_id}/proxy")
    @Timed
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Make a Coap Post request to the Resource")
    public Response putProxy(@Context ContainerRequestContext requestContext,
                             @PathParam("resource_id") String resourceId) {
        try {

            //Check the request
            if (resourceId == null)
                return Response.status(Response.Status.BAD_REQUEST).type(MediaType.APPLICATION_JSON_TYPE).entity(new ErrorMessage(Response.Status.BAD_REQUEST.getStatusCode(), "ResourceId Provided !")).build();

            logger.info("Loading infos for resource: {}", resourceId);

            Optional<ResourceDescriptor> resourceDescriptor = this.conf.getInventoryDataManager().getResource(resourceId);

            //check if the device is present and if it is inside the correct area and the area is inside the correct floor
            if (!resourceDescriptor.isPresent())
                return Response.status(Response.Status.NOT_FOUND).type(MediaType.APPLICATION_JSON_TYPE).entity(new ErrorMessage(Response.Status.NOT_FOUND.getStatusCode(), "Resource Not Found !")).build();

            CoapResourceClient coapResourceClient = new CoapResourceClient();

            String url = "";

            if (resourceId.contains("pir") || resourceId.contains("camera")){
                url = String.format("%s/%s:%s", this.conf.getInventoryDataManager().getResource(resourceId).get().getDeviceId(),resourceId.split(":")[0] ,resourceId.split(":")[2]);
                logger.info("url: {}", url);

            }else if(resourceId.contains("light") || resourceId.contains("alarm")){
                url = this.conf.getInventoryDataManager().getResource(resourceId).get().getDeviceId();
                logger.info("url: {}", url);
            }

            CoapResponse response = coapResourceClient.postRequest(String.format("coap://192.168.1.107:5683/%s", url));
            return Response.ok(response).build();


        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).type(MediaType.APPLICATION_JSON_TYPE).entity(new ErrorMessage(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), "Internal Server Error !")).build();
        }
    }
}