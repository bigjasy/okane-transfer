package ma.ensam.okanetransfer.exception;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends BusinessException {
    public ResourceNotFoundException(String resourceName, Object identifier) {
        super("RESOURCE_NOT_FOUND", resourceName + " not found: " + identifier, HttpStatus.NOT_FOUND);
    }
}
