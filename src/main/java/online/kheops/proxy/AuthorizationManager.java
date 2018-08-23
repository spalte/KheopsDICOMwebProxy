package online.kheops.proxy;

// Class that is used to get authorization to study/series for a user
// this class caches the study/series that the user already has access to, or that were
// previously denied
public final class AuthorizationManager {

    // Returns true if acquiring the authorization was successful
    // This method might block while a connection is made the authorization server
    // Throws an exception the describes the reason the authorization could not be acquired.
    public void getAuthorization(SeriesID seriesID) throws AuthorizationManagerException {
        // TODO implement

    }



}
