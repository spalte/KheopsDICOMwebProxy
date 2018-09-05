package online.kheops.proxy;

import online.kheops.proxy.part.Part;
import org.dcm4che3.data.Attributes;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

// Class that is used to get authorization to study/series for a user
// this class caches the study/series that the user already has access to, or that were
// previously denied
public final class AuthorizationManager {
    private final Set<SeriesID> authorizedSeriesIDs = new HashSet<>();
    private final Set<ContentLocation> authorizedContentLocations = new HashSet<>();
    private final URI authorizationUri;

    public AuthorizationManager(URI authorizationUri) {
        this.authorizationUri = authorizationUri;
    }

    // Returns true if acquiring the authorization was successful
    // This method blocks while a connection is made to the authorization server
    // Throws an exception that describes the reason the authorization could not be acquired.
    // stores authorizations that have failed so that attributes can be patched
    public void getAuthorization(Part part) throws AuthorizationManagerException {
        if (part.getSeriesID().isPresent()) {
            getAuthorization(part.getSeriesID().get());
        }
        if (part.getContentLocation().isPresent()) {
            getAuthorization(part.getContentLocation().get());
        }

        authorizeContentLocations(part.getBulkDataLocations());
    }

    public Attributes patchAttributes(Attributes attributes) {

    }

    private void getAuthorization(SeriesID seriesID) throws AuthorizationManagerException{

    }

    private void getAuthorization(ContentLocation contentLocation) throws AuthorizationManagerException{
        if (!authorizedContentLocations.contains(contentLocation)) {
            throw new AuthorizationManagerException("No Reason Found", AuthorizationManagerException.Reason.UnknownContentLocation);
        }
    }

    private void authorizeContentLocations(Set<ContentLocation> contentLocations) {
        authorizedContentLocations.addAll(contentLocations);
    }

}
