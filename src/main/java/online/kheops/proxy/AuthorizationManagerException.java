package online.kheops.proxy;

public class AuthorizationManagerException extends Exception {
    public static enum Reason {
        UnknownContentLocation
    }

    private final Reason reason;

    public AuthorizationManagerException(String message, Reason reason) {
        super(message);
        this.reason = reason;
    }

    public Reason getReason() {
        return reason;
    }
}
