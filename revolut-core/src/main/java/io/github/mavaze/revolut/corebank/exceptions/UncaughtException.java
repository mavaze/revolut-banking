package io.github.mavaze.revolut.corebank.exceptions;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;

@Slf4j
@Provider
public class UncaughtException extends Throwable implements ExceptionMapper<Throwable> {

    private static final long serialVersionUID = -8591523770291363472L;

    public Response toResponse(@NonNull final Throwable ex) {

        final ErrorMessage errorMessage = new ErrorMessage("5XX", ex.getMessage());
        setHttpStatus(ex, errorMessage);

        return Response.status(errorMessage.getStatus())
                .entity(errorMessage)
                .type(APPLICATION_JSON)
                .build();
    }

    private void setHttpStatus(Throwable ex, ErrorMessage errorMessage) {
        log.error("Response interceptor received an error: ", ex);
        if(ex instanceof WebApplicationException) {
            errorMessage.setStatus(((WebApplicationException)ex).getResponse().getStatus());
        } else {
            errorMessage.setStatus(INTERNAL_SERVER_ERROR.getStatusCode());
        }
    }
}
