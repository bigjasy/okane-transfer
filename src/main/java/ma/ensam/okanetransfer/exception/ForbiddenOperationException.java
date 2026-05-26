package ma.ensam.okanetransfer.exception;

import org.springframework.http.HttpStatus;

public class ForbiddenOperationException extends BusinessException {
    public ForbiddenOperationException(String message) {
        super("FORBIDDEN_OPERATION", message, HttpStatus.FORBIDDEN);
    }
}
