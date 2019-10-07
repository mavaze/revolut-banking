package io.github.mavaze.revolut.corebank.exceptions;

import lombok.NoArgsConstructor;
import lombok.NonNull;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.CONFLICT;

// More fine tuned exception hierarchy, with meaningful messages and corresponding status codes,
// must be established. The following BusinessException is just one representation.
@Provider
@NoArgsConstructor
public class BusinessException extends RuntimeException implements ExceptionMapper<BusinessException> {

    private static final long serialVersionUID = 4945212197153129877L;

    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public Response toResponse(@NonNull final BusinessException ex) {
        final ErrorMessage errorMessage = new ErrorMessage("4XX", ex.getMessage());
        setHttpStatus(ex, errorMessage);

        return Response.status(errorMessage.getStatus())
                .entity(errorMessage)
                .type(APPLICATION_JSON)
                .build();
    }

    private void setHttpStatus(Throwable ex, ErrorMessage errorMessage) {
        if(ex instanceof WebApplicationException) {
            errorMessage.setStatus(((WebApplicationException)ex).getResponse().getStatus());
        } else {
            errorMessage.setStatus(CONFLICT.getStatusCode());
        }
    }
}
