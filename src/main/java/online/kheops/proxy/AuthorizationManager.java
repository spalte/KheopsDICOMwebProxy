package online.kheops.proxy;

import org.dcm4che3.data.Attributes;

import java.net.URI;

// Class that is used to get authorization to study/series for a user
// this class caches the study/series that the user already has access to, or that were
// previously denied
public final class AuthorizationManager {

    public AuthorizationManager(URI authorizationUri) {

    }

    // Returns true if acquiring the authorization was successful
    // This method might block while a connection is made the authorization server
    // Throws an exception the describes the reason the authorization could not be acquired.
    public void getAuthorization(Part part) throws AuthorizationManagerException {
        // TODO implement

    }

    public Attributes patchAttributes(Attributes attributes) {

    }

}
