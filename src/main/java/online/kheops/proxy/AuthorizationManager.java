package online.kheops.proxy;

import online.kheops.proxy.part.MissingAttributeException;
import online.kheops.proxy.part.Part;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Sequence;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.dcm4che3.net.Status;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public final class AuthorizationManager {
    private static final Client CLIENT = ClientBuilder.newClient();

    private final Set<SeriesID> authorizedSeriesIDs = new HashSet<>();
    private final Set<SeriesID> forbiddenSeriesIDs = new HashSet<>();
    private final Set<InstanceID> forbiddenInstanceIDs = new HashSet<>();
    private final Set<ContentLocation> authorizedContentLocations = new HashSet<>();
    private final UriBuilder authorizationUriBuilder;
    private final String bearerToken;
    private final String albumId;

    public AuthorizationManager(URI authorizationServerRoot, String bearerToken, String albumId, String studyInstanceUID) {
        this.bearerToken = Objects.requireNonNull(bearerToken);
        this.albumId = albumId;
        UriBuilder uriBuilder = UriBuilder.fromUri(Objects.requireNonNull(authorizationServerRoot)).path("studies");
        if (studyInstanceUID != null) {
            uriBuilder = uriBuilder.path(studyInstanceUID);
        }
        if (albumId != null) {
            uriBuilder = uriBuilder.queryParam("album", albumId);
        }
        authorizationUriBuilder = uriBuilder.path("{StudyInstanceUID}/series/{SeriesInstanceUID}");
    }

    // This method blocks while a connection is made to the authorization server
    // Throws an exception that describes the reason the authorization could not be acquired.
    // stores authorizations that have failed so that attributes can be patched
    public void getAuthorization(Part part) throws AuthorizationManagerException, STOWGatewayException {
        try {
            if (part.getInstanceID().isPresent()) {
                getAuthorization(part.getInstanceID().get());
            }
        } catch (MissingAttributeException e) {
            throw new AuthorizationManagerException("Unable to get instance", AuthorizationManagerException.Reason.MISSING_ATTRIBUTE, e);
        }
        if (part.getContentLocation().isPresent()) {
            getAuthorization(part.getContentLocation().get());
        }

        authorizeContentLocations(part.getBulkDataLocations());
    }

    public Response getResponse(Attributes attributes) {
        if (attributes == null) {
            return Response.status(Response.Status.CONFLICT).build();
        }

        boolean hasFailedSOPs = false;

        // look at the attributes, and see if there were any failures
        Sequence failedSOPs = attributes.getSequence(Tag.FailedSOPSequence);
        if (failedSOPs != null) {
            hasFailedSOPs = true;
        } else if (!forbiddenInstanceIDs.isEmpty()) {
            failedSOPs = attributes.newSequence(Tag.FailedSOPSequence, forbiddenInstanceIDs.size());
            hasFailedSOPs = true;
        }

        for (InstanceID forbiddenInstance: forbiddenInstanceIDs) {
            Attributes failedAttributes = new Attributes(3);
            failedAttributes.setString(Tag.ReferencedSOPInstanceUID, VR.UI, forbiddenInstance.getSOPInstanceUID());
            failedAttributes.setString(Tag.ReferencedSOPClassUID, VR.UI, forbiddenInstance.getSOPClassUID());
            failedAttributes.setInt(Tag.FailureReason, VR.US, Status.NotAuthorized);

            failedSOPs.add(failedAttributes);
        }

        return Response.status(hasFailedSOPs ? Response.Status.ACCEPTED : Response.Status.OK).entity(attributes).build();
    }

    private void getAuthorization(InstanceID instanceID) throws AuthorizationManagerException, STOWGatewayException {
        final SeriesID seriesID = instanceID.getSeriesID();
        if (authorizedSeriesIDs.contains(seriesID)) {
            return;
        }
        if (forbiddenSeriesIDs.contains(seriesID)) {
            forbiddenInstanceIDs.add(instanceID);
            throw new AuthorizationManagerException("Series access forbidden", AuthorizationManagerException.Reason.SERIES_ACCESS_FORBIDDEN);
        }

        URI uri = authorizationUriBuilder.build(seriesID.getStudyUID(), seriesID.getSeriesUID());

        final Response response;
        try {
            response = CLIENT.target(uri)
                    .request()
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerToken)
                    .put(Entity.text(""));
        } catch (ProcessingException e) {
            forbiddenSeriesIDs.add(seriesID);
            forbiddenInstanceIDs.add(instanceID);
            throw new STOWGatewayException("Error while getting the access token", e);
        }  catch (WebApplicationException e) {
            forbiddenSeriesIDs.add(seriesID);
            forbiddenInstanceIDs.add(instanceID);
            throw new AuthorizationManagerException("Series access forbidden", AuthorizationManagerException.Reason.SERIES_ACCESS_FORBIDDEN, e);
        }

        if (response.getStatusInfo().getFamily() == Response.Status.Family.SUCCESSFUL) {
            authorizedSeriesIDs.add(seriesID);
        } else {
            forbiddenSeriesIDs.add(seriesID);
            forbiddenInstanceIDs.add(instanceID);
            throw new AuthorizationManagerException("Series access forbidden", AuthorizationManagerException.Reason.SERIES_ACCESS_FORBIDDEN);
        }
    }

    private void getAuthorization(ContentLocation contentLocation) throws AuthorizationManagerException{
        if (!authorizedContentLocations.contains(contentLocation)) {
            throw new AuthorizationManagerException("Unknown content location", AuthorizationManagerException.Reason.UNKNOWN_CONTENT_LOCATION);
        }
    }

    private void authorizeContentLocations(Set<ContentLocation> contentLocations) {
        authorizedContentLocations.addAll(contentLocations);
    }

}
