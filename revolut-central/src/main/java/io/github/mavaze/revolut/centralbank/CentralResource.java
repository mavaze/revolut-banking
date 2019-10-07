package io.github.mavaze.revolut.centralbank;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

@Path("central")
public class CentralResource {

    @Inject
    private CentralQueueTemplate template;

    @GET
    public String getCentralResource() {
        return "Not Implemented";
    }
}
