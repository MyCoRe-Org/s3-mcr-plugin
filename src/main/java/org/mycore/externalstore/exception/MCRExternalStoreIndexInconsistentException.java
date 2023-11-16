package org.mycore.externalstore.exception;

public class MCRExternalStoreIndexInconsistentException extends MCRExternalStoreException {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public MCRExternalStoreIndexInconsistentException(String message) {
        super(message);
    }

    public MCRExternalStoreIndexInconsistentException() {
        this("Index is inconsistent");
    }

}
