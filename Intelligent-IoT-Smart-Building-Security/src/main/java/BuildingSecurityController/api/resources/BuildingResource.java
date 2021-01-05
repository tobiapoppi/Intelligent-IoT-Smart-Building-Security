package BuildingSecurityController.api.resources;

import BuildingSecurityController.api.client.CoapResourceClient;
import BuildingSecurityController.api.client.LookupAndObserveProcess;
import BuildingSecurityController.api.data_transfer_object.*;
import BuildingSecurityController.api.exception.IInventoryDataManagerConflict;
import BuildingSecurityController.api.model.AreaDescriptor;
import BuildingSecurityController.api.model.FloorDescriptor;
import BuildingSecurityController.api.model.PolicyDescriptor;
import BuildingSecurityController.api.services.OperatorAppConfig;
import com.codahale.metrics.annotation.Timed;
import io.dropwizard.jersey.errors.ErrorMessage;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.awt.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Path("/buildingPoppiZaniboniInc/floor")
@Api("IoT Building Resource Endpoint")
public class BuildingResource {

    final protected Logger logger = LoggerFactory.getLogger(BuildingResource.class);

    @SuppressWarnings("serial")
    public static class MissingKeyException extends Exception{}
    final OperatorAppConfig conf;

    CoapResourceClient coapResourceClient = new CoapResourceClient();

    public BuildingResource(OperatorAppConfig conf) throws InterruptedException {
        this.conf = conf;
    }

    //FLOOR MANAGEMENT

    @RolesAllowed("USER")
    @GET
    @Path("/")
    @Timed
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Get all the Floors of the building")
    public Response GetFloors(@Context ContainerRequestContext requestContext){
        try{
            List<FloorDescriptor> floorList = null;

            logger.info("Loading all the building's Floors");

            floorList = this.conf.getInventoryDataManager().getFloorList();

            if (floorList == null)
                return Response.status(Response.Status.NOT_FOUND).type(MediaType.APPLICATION_JSON_TYPE).entity(new ErrorMessage(Response.Status.NOT_FOUND.getStatusCode(), "Floors not found")).build();

            return Response.ok(floorList).build();

        }catch(Exception e){
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).type(MediaType.APPLICATION_JSON_TYPE).entity(new ErrorMessage(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), "Internal Server Error!")).build();
        }
    }

    @RolesAllowed("USER")
    @POST
    @Path("/")
    @Timed
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.TEXT_PLAIN)
    @ApiOperation(value="Create a new Floor")
    public Response createFloor(@Context ContainerRequestContext req,
                                   @Context UriInfo uriInfo,
                                FloorCreationRequest floorCreationRequest) {

        try {
            logger.info("Incoming Floor Creation Request: {}", floorCreationRequest);

            //Check the request
            if(floorCreationRequest == null)
                return Response.status(Response.Status.BAD_REQUEST).type(MediaType.APPLICATION_JSON_TYPE).entity(new ErrorMessage(Response.Status.BAD_REQUEST.getStatusCode(),"Invalid request payload")).build();

            FloorDescriptor newFloorDescriptor = (FloorDescriptor) floorCreationRequest;

            newFloorDescriptor.setFloor_id(null);

            newFloorDescriptor = this.conf.getInventoryDataManager().createNewFloor(newFloorDescriptor);

            return Response.created(new URI(String.format("%s/%s",uriInfo.getAbsolutePath(),newFloorDescriptor.getFloor_id()))).build();

        } catch (IInventoryDataManagerConflict e){
            return Response.status(Response.Status.CONFLICT).type(MediaType.APPLICATION_JSON_TYPE).entity(new ErrorMessage(Response.Status.CONFLICT.getStatusCode(),"Floor already available !")).build();
        } catch (Exception e){
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).type(MediaType.APPLICATION_JSON_TYPE).entity(new ErrorMessage(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),"Internal Server Error !")).build();
        }
    }

    @RolesAllowed("USER")
    @GET
    @Path("/{floor_id}")
    @Timed
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value="Get a Floor's infos")
    public Response getFloor(@Context ContainerRequestContext requestContext,
                                @PathParam("floor_id") String floorId) {

        try {

            //Check the request
            if(floorId == null)
                return Response.status(Response.Status.BAD_REQUEST).type(MediaType.APPLICATION_JSON_TYPE).entity(new ErrorMessage(Response.Status.BAD_REQUEST.getStatusCode(),"Invalid Floor id Provided !")).build();

            logger.info("Loading infos for floor: {}", floorId);

            Optional<FloorDescriptor> floorDescriptor = this.conf.getInventoryDataManager().getFloor(floorId);

            if(!floorDescriptor.isPresent())
                return Response.status(Response.Status.NOT_FOUND).type(MediaType.APPLICATION_JSON_TYPE).entity(new ErrorMessage(Response.Status.NOT_FOUND.getStatusCode(),"Floor Not Found !")).build();

            return Response.ok(floorDescriptor.get()).build();

        } catch (Exception e){
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).type(MediaType.APPLICATION_JSON_TYPE).entity(new ErrorMessage(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),"Internal Server Error !")).build();
        }
    }

    @RolesAllowed("USER")
    @PUT
    @Path("/{floor_id}")
    @Timed
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value="Update an existing Floor")
    public Response updateLocation(@Context ContainerRequestContext req,
                                   @Context UriInfo uriInfo,
                                   @PathParam("floor_id") String floorId,
                                   FloorUpdateRequest floorUpdateRequest) {

        try {

            logger.info("Incoming Floor ({}) Update Request: {}", floorId, floorUpdateRequest);

            //Check if the request is valid, the id must be the same in the path and in the json request payload
            if(floorUpdateRequest == null || !floorUpdateRequest.getFloor_id().equals(floorId))
                return Response.status(Response.Status.BAD_REQUEST).type(MediaType.APPLICATION_JSON_TYPE).entity(new ErrorMessage(Response.Status.BAD_REQUEST.getStatusCode(),"Invalid request ! Check Floor Number")).build();

            //Check if the device is available and correctly registered otherwise a 404 response will be sent to the client
            if(!this.conf.getInventoryDataManager().getFloor(floorId).isPresent())
                return Response.status(Response.Status.NOT_FOUND).type(MediaType.APPLICATION_JSON_TYPE).entity(new ErrorMessage(Response.Status.NOT_FOUND.getStatusCode(),"Floor not found !")).build();
            FloorDescriptor newFloorDescriptor = (FloorDescriptor) floorUpdateRequest;
            this.conf.getInventoryDataManager().updateFloor(newFloorDescriptor);

            return Response.noContent().build();

        } catch (Exception e){
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).type(MediaType.APPLICATION_JSON_TYPE).entity(new ErrorMessage(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),"Internal Server Error !")).build();
        }
    }

    @RolesAllowed("USER")
    @DELETE
    @Path("/{floor_id}")
    @Timed
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value="Delete a Floor")
    public Response deleteFloor(@Context ContainerRequestContext req,
                                @PathParam("floor_id") String floorId) {

        try {

            logger.info("Deleting Floor: {}", floorId);

            //Check the request
            if(floorId == null)
                return Response.status(Response.Status.BAD_REQUEST).type(MediaType.APPLICATION_JSON_TYPE).entity(new ErrorMessage(Response.Status.BAD_REQUEST.getStatusCode(),"Invalid Floor Id Provided !")).build();

            //Check if the device is available or not
            if(!this.conf.getInventoryDataManager().getFloor(floorId).isPresent())
                return Response.status(Response.Status.NOT_FOUND).type(MediaType.APPLICATION_JSON_TYPE).entity(new ErrorMessage(Response.Status.NOT_FOUND.getStatusCode(),"Floor Not Found !")).build();

            //Delete the location
            this.conf.getInventoryDataManager().deleteFloor(floorId);

            return Response.noContent().build();

        } catch (Exception e){
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).type(MediaType.APPLICATION_JSON_TYPE).entity(new ErrorMessage(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),"Internal Server Error !")).build();
        }
    }

    //AREA MANAGEMENT

    @RolesAllowed("USER")
    @GET
    @Path("/{floor_id}/area")
    @Timed
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Get all the Areas of the floor")
    public Response GetAreas(@Context ContainerRequestContext requestContext,
                                @PathParam("floor_id") String floorId){
        try{


            if(floorId == null)
                return Response.status(Response.Status.BAD_REQUEST).type(MediaType.APPLICATION_JSON_TYPE).entity(new ErrorMessage(Response.Status.BAD_REQUEST.getStatusCode(),"Invalid Floor id Provided !")).build();

            logger.info("Loading all the Floor {} 's Areas", floorId);

            Optional<FloorDescriptor> floorDescriptor = this.conf.getInventoryDataManager().getFloor(floorId);

            if(!floorDescriptor.isPresent())
                return Response.status(Response.Status.NOT_FOUND).type(MediaType.APPLICATION_JSON_TYPE).entity(new ErrorMessage(Response.Status.NOT_FOUND.getStatusCode(),"Floor Not Found !")).build();


            if (floorDescriptor.get().getAreaList().isEmpty())
                return Response.status(Response.Status.NOT_FOUND).type(MediaType.APPLICATION_JSON_TYPE).entity(new ErrorMessage(Response.Status.NOT_FOUND.getStatusCode(), "No areas found")).build();

            return Response.ok(floorDescriptor.get().getAreaList()).build();

        }catch(Exception e){
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).type(MediaType.APPLICATION_JSON_TYPE).entity(new ErrorMessage(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), "Internal Server Error!")).build();
        }
    }

    @RolesAllowed("USER")
    @POST
    @Path("/{floor_id}/area")
    @Timed
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.TEXT_PLAIN)
    @ApiOperation(value="Create a new Area")
    public Response createArea(@Context ContainerRequestContext req,
                               @Context UriInfo uriInfo,
                               @PathParam("floor_id") String floorId, AreaCreationRequest areaCreationRequest){

        try {
            logger.info("Incoming Area Creation Request: {} on Floor {}", areaCreationRequest, floorId);

            //Check the request
            if(areaCreationRequest == null)
                return Response.status(Response.Status.BAD_REQUEST).type(MediaType.APPLICATION_JSON_TYPE).entity(new ErrorMessage(Response.Status.BAD_REQUEST.getStatusCode(),"Invalid request payload")).build();



            AreaDescriptor newAreaDescriptor = (AreaDescriptor) areaCreationRequest;

            if(!floorId.equals(newAreaDescriptor.getFloorId())){
                return Response.status(Response.Status.BAD_REQUEST).type(MediaType.APPLICATION_JSON_TYPE).entity(new ErrorMessage(Response.Status.BAD_REQUEST.getStatusCode(),"Invalid floorId in payload")).build();
            }

            newAreaDescriptor = this.conf.getInventoryDataManager().createNewArea(newAreaDescriptor);

            return Response.created(new URI(String.format("%s/%s",uriInfo.getAbsolutePath(),newAreaDescriptor.getArea_id()))).build();


        } catch (Exception e){
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).type(MediaType.APPLICATION_JSON_TYPE).entity(new ErrorMessage(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),"Internal Server Error !")).build();
        }
    }

    @RolesAllowed("USER")
    @GET
    @Path("/{floor_id}/area/{area_id}")
    @Timed
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value="Get an Area's infos")
    public Response getFloor(@Context ContainerRequestContext requestContext,
                             @PathParam("floor_id") String floorId, @PathParam("area_id") String areaId) {

        try {

            //Check the request
            if(floorId == null && areaId == null)
                return Response.status(Response.Status.BAD_REQUEST).type(MediaType.APPLICATION_JSON_TYPE).entity(new ErrorMessage(Response.Status.BAD_REQUEST.getStatusCode(),"Invalid Floor_id and Area_id Provided !")).build();

            logger.info("Loading infos for area: {} in floor: {}", areaId, floorId);

            Optional<AreaDescriptor> areaDescriptor = this.conf.getInventoryDataManager().getArea(areaId);

            if(!areaDescriptor.isPresent()){
                return Response.status(Response.Status.NOT_FOUND).type(MediaType.APPLICATION_JSON_TYPE).entity(new ErrorMessage(Response.Status.NOT_FOUND.getStatusCode(),"Floor Not Found !")).build();

            }

            return Response.ok(areaDescriptor.get()).build();


        } catch (Exception e){
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).type(MediaType.APPLICATION_JSON_TYPE).entity(new ErrorMessage(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),"Internal Server Error !")).build();
        }
    }


    @RolesAllowed("USER")
    @PUT
    @Path("/{floor_id}/area/{area_id}")
    @Timed
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value="Update an existing Area")
    public Response updateLocation(@Context ContainerRequestContext req,
                                   @Context UriInfo uriInfo,
                                   @PathParam("floor_id") String floorId, @PathParam("area_id") String areaId,
                                   AreaUpdateRequest areaUpdateRequest) {

        try {

            logger.info("Incoming Area {} in floor {} Update Request: {}", areaId, floorId, areaUpdateRequest);

            //Check if the request is valid, the id must be the same in the path and in the json request payload
            if(areaUpdateRequest == null || !areaUpdateRequest.getArea_id().equals(areaId) || !areaUpdateRequest.getFloorId().equals(floorId))
                return Response.status(Response.Status.BAD_REQUEST).type(MediaType.APPLICATION_JSON_TYPE).entity(new ErrorMessage(Response.Status.BAD_REQUEST.getStatusCode(),"Invalid request ! Check AreaId and FloorId")).build();

            //Check if the device is available and correctly registered otherwise a 404 response will be sent to the client
            if(!this.conf.getInventoryDataManager().getArea(areaId).isPresent())
                return Response.status(Response.Status.NOT_FOUND).type(MediaType.APPLICATION_JSON_TYPE).entity(new ErrorMessage(Response.Status.NOT_FOUND.getStatusCode(),"Area not found !")).build();
            AreaDescriptor newAreaDescriptor = (AreaUpdateRequest) areaUpdateRequest;
            this.conf.getInventoryDataManager().updateArea(newAreaDescriptor);

            return Response.noContent().build();

        } catch (Exception e){
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).type(MediaType.APPLICATION_JSON_TYPE).entity(new ErrorMessage(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),"Internal Server Error !")).build();
        }
    }

    @RolesAllowed("USER")
    @DELETE
    @Path("/{floor_id}/area/{area_id}")
    @Timed
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value="Delete a Area")
    public Response deleteFloor(@Context ContainerRequestContext req,
                                @PathParam("floor_id") String floorId, @PathParam("area_id") String areaId) {

        try {

            logger.info("Deleting Area: {} in floor: {}", areaId, floorId);

            //Check the request
            if(floorId == null || areaId == null)
                return Response.status(Response.Status.BAD_REQUEST).type(MediaType.APPLICATION_JSON_TYPE).entity(new ErrorMessage(Response.Status.BAD_REQUEST.getStatusCode(),"Invalid FloorId or AreaId Provided !")).build();

            //Check if the device is available or not
            if(!this.conf.getInventoryDataManager().getArea(areaId).isPresent())
                return Response.status(Response.Status.NOT_FOUND).type(MediaType.APPLICATION_JSON_TYPE).entity(new ErrorMessage(Response.Status.NOT_FOUND.getStatusCode(),"Area Not Found !")).build();

            //Delete the location
            this.conf.getInventoryDataManager().deleteArea(areaId);

            return Response.noContent().build();

        } catch (Exception e){
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).type(MediaType.APPLICATION_JSON_TYPE).entity(new ErrorMessage(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),"Internal Server Error !")).build();
        }
    }

//    @RolesAllowed("USER")
//    @GET
//    @Path("/{floor_id}/area/{area_id}/device")
//    @Timed
//    @Produces(MediaType.APPLICATION_JSON)
//    @ApiOperation(value = "Get all the Areas of the floor")
//    public Response GetAreas(@Context ContainerRequestContext requestContext,
//                             @PathParam("floor_id") String floorId){
//        try{
//
//
//            if(floorId == null)
//                return Response.status(Response.Status.BAD_REQUEST).type(MediaType.APPLICATION_JSON_TYPE).entity(new ErrorMessage(Response.Status.BAD_REQUEST.getStatusCode(),"Invalid Floor id Provided !")).build();
//
//            logger.info("Loading all the Floor {} 's Areas", floorId);
//
//            Optional<FloorDescriptor> floorDescriptor = this.conf.getInventoryDataManager().getFloor(floorId);
//
//            if(!floorDescriptor.isPresent())
//                return Response.status(Response.Status.NOT_FOUND).type(MediaType.APPLICATION_JSON_TYPE).entity(new ErrorMessage(Response.Status.NOT_FOUND.getStatusCode(),"Floor Not Found !")).build();
//
//
//            if (floorDescriptor.get().getAreaList().isEmpty())
//                return Response.status(Response.Status.NOT_FOUND).type(MediaType.APPLICATION_JSON_TYPE).entity(new ErrorMessage(Response.Status.NOT_FOUND.getStatusCode(), "No areas found")).build();
//
//            return Response.ok(floorDescriptor.get().getAreaList()).build();
//
//        }catch(Exception e){
//            e.printStackTrace();
//            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).type(MediaType.APPLICATION_JSON_TYPE).entity(new ErrorMessage(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), "Internal Server Error!")).build();
//        }
//    }


    @RolesAllowed("USER")
    @GET
    @Path("/{floor_id}/area/{area_id}/device/{device_id}")
    @Timed
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value="Get a Device's infos")
    public Response getDevice(@Context ContainerRequestContext requestContext,
                             @PathParam("floor_id") String floorId, @PathParam("area_id") String areaId, @PathParam("device_id") String deviceId) {

        try {

            //Check the request
            if(floorId == null)
                return Response.status(Response.Status.BAD_REQUEST).type(MediaType.APPLICATION_JSON_TYPE).entity(new ErrorMessage(Response.Status.BAD_REQUEST.getStatusCode(),"Invalid Floor id Provided !")).build();

            logger.info("Loading infos for device: {}, in Area {}, in floor {}", deviceId, areaId, floorId);

            CoapResponse response = coapResourceClient.getRequest(String.format("%s/%s/%s", floorId, areaId, deviceId));
            logger.info("Response Pretty Print:\n{}", Utils.prettyPrint(response));
            String text = response.getResponseText();
            logger.info("Payload: {}", text);

            if(text == null){
                return Response.status(Response.Status.NOT_FOUND).type(MediaType.APPLICATION_JSON_TYPE).entity(new ErrorMessage(Response.Status.NOT_FOUND.getStatusCode(), "Device not found")).build();
            }

            return Response.ok(text).build();

        } catch (Exception e){
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).type(MediaType.APPLICATION_JSON_TYPE).entity(new ErrorMessage(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),"Internal Server Error !")).build();
        }
    }

//
//    @RolesAllowed("USER")
//    @DELETE
//    @Path("/{floor_id}/area/{area_id}")
//    @Timed
//    @Produces(MediaType.APPLICATION_JSON)
//    @ApiOperation(value="Delete an Area and all her devices")
//    public Response deleteArea(@Context ContainerRequestContext req,
//                               @PathParam("floor_id") String floorId, @PathParam("area_id") String areaId) {
//
//        try {
//
//            logger.info("Deleting Area: {}", areaId);
//
//            //Check the request
//            if(areaId == null)
//                return Response.status(Response.Status.BAD_REQUEST).type(MediaType.APPLICATION_JSON_TYPE).entity(new ErrorMessage(Response.Status.BAD_REQUEST.getStatusCode(),"Invalid AreaId Provided !")).build();
//
//            //Check if the area exists is available or not
//            if(LookupAndObserveProcess.getAreas(floorId).isEmpty()){
//                return Response.status(Response.Status.NOT_FOUND).type(MediaType.APPLICATION_JSON_TYPE).entity(new ErrorMessage(Response.Status.NOT_FOUND.getStatusCode(), "No areas found")).build();
//
//            }
//
//            CoapResponse response = coapResourceClient.deleteRequest(String.format("%s/%s", floorId, areaId));
//
//            lookupAndObserveProcess.deleteRelation(String.format("%s/%s", floorId, areaId));
//
//            //Delete the location
//
//            return Response.ok(response).build();
//
//        } catch (Exception e){
//            e.printStackTrace();
//            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).type(MediaType.APPLICATION_JSON_TYPE).entity(new ErrorMessage(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),"Internal Server Error !")).build();
//        }
//    }

}
