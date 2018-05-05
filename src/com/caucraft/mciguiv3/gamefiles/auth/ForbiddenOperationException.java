package com.caucraft.mciguiv3.gamefiles.auth;

/**
 *
 * @author caucow
 */
public class ForbiddenOperationException extends RuntimeException {
    
    public ForbiddenOperationException() {
        super();
    }
    
    public ForbiddenOperationException(String msg) {
        super(msg);
    }
    
    public ForbiddenOperationException(Throwable cause) {
        super(cause);
    }
    
    public ForbiddenOperationException(String msg, Throwable cause) {
        super(msg, cause);
    }
    
}
