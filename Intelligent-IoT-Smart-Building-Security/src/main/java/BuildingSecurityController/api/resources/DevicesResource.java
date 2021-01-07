package BuildingSecurityController.api.resources;

import BuildingSecurityController.api.client.CoapResourceClient;
import BuildingSecurityController.api.data_transfer_object.DeviceUpdateRequest;
import BuildingSecurityController.api.model.AreaDescriptor;
import BuildingSecurityController.api.model.GenericDeviceDescriptor;
import BuildingSecurityController.api.model.ResourceDescriptor;
import BuildingSecurityController.api.services.OperatorAppConfig;
import com.codahale.metrics.annotation.Timed;
import io.dropwizard.jersey.errors.ErrorMessage;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.awt.geom.Area;
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

    CoapResourceClient coapResourceClient = new CoapResourceClient();

    public DevicesResource(OperatorAppConfig conf) throws InterruptedException {
        this.conf = conf;
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
    public Response updateDevice(@Context ContainerRequestContext requestContext, @Context UriInfo uriInfo, DeviceUpdateRequest deviceUpdateRequest, @PathParam("device_id") String deviceId) {

        try {

            //Check the request
            if (deviceUpdateRequest == null || !deviceUpdateRequest.getDeviceId().equals(deviceId))
                return Response.status(Response.Status.BAD_REQUEST).type(MediaType.APPLICATION_JSON_TYPE).entity(new ErrorMessage(Response.Status.BAD_REQUEST.getStatusCode(), "Invalid Floorid or AreaId or DeviceId Provided !")).build();

            if (!this.conf.getInventoryDataManager().getDevice(deviceId).isPresent())
                return Response.status(Response.Status.NOT_FOUND).type(MediaType.APPLICATION_JSON_TYPE).entity(new ErrorMessage(Response.Status.NOT_FOUND.getStatusCode(),"Device not found !")).build();

            GenericDeviceDescriptor genericDeviceDescriptor = this.conf.getInventoryDataManager().getDevice(deviceId).get();

            genericDeviceDescriptor.setFloorId(deviceUpdateRequest.getFloorId());
            genericDeviceDescriptor.setDeviceId(deviceUpdateRequest.getDeviceId());
            genericDeviceDescriptor.setAreaId(deviceUpdateRequest.getAreaId());

            Optional<AreaDescriptor> areaDescriptor = this.conf.getInventoryDataManager().getArea(deviceUpdateRequest.getAreaId());

            if (areaDescriptor.isPresent()){
                if(areaDescriptor.get().getFloorId().equals(deviceUpdateRequest.getFloorId())){
                    this.conf.getInventoryDataManager().updateDevice(genericDeviceDescriptor);
                    return Response.noContent().build();
                }
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
    @Path("/{floor_id}/area/{area_id}/device/{device_id}/resource/{resource_id}")
    @Timed
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Get a Resource's infos")
    public Response getDevice(@Context ContainerRequestContext requestContext,
                              @PathParam("floor_id") String floorId, @PathParam("area_id") String areaId, @PathParam("device_id") String deviceId,
                              @PathParam("resource_id") String resourceId) {
        try {

            //Check the request
            if (floorId == null || areaId == null || deviceId == null || resourceId == null)
                return Response.status(Response.Status.BAD_REQUEST).type(MediaType.APPLICATION_JSON_TYPE).entity(new ErrorMessage(Response.Status.BAD_REQUEST.getStatusCode(), "Invalid FloorId or AreaId or DeviceId Provided !")).build();

            logger.info("Loading infos for resource: {}, in device {}, in Area {}, in floor {}", resourceId, deviceId, areaId, floorId);

            Optional<ResourceDescriptor> resourceDescriptor = this.conf.getInventoryDataManager().getResource(resourceId);

            //check if the device is present and if it is inside the correct area and the area is inside the correct floor
            if (!resourceDescriptor.isPresent() || !resourceDescriptor.get().getDeviceId().equals(deviceId) || !this.conf.getInventoryDataManager().getDevice(deviceId).get().getAreaId().equals(areaId) || !this.conf.getInventoryDataManager().getArea(areaId).get().getFloorId().equals(floorId))
                return Response.status(Response.Status.NOT_FOUND).type(MediaType.APPLICATION_JSON_TYPE).entity(new ErrorMessage(Response.Status.NOT_FOUND.getStatusCode(), "Resource Not Found !")).build();

            return Response.ok(resourceDescriptor.get()).build();


        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).type(MediaType.APPLICATION_JSON_TYPE).entity(new ErrorMessage(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), "Internal Server Error !")).build();
        }
    }
}