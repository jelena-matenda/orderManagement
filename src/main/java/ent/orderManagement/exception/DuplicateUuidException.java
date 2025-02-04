package ent.orderManagement.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)  
public class DuplicateUuidException extends RuntimeException {
    public DuplicateUuidException(String message) {
        super(message);
    }
}

