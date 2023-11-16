package org.mycore.externalstore.exception;

public class MCRExternalStoreFileNameCollisionException extends MCRExternalStoreException {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public MCRExternalStoreFileNameCollisionException(String message) {
        super(message);
    }

    public MCRExternalStoreFileNameCollisionException() {
        this("file name collision");
    }

}
