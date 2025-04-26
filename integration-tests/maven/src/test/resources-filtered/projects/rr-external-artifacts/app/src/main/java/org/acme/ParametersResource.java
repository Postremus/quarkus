package org.acme;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.acme.lib.StringType;

@Path("/parameters")
public class ParametersResource {

    @Path("{stringValue}")
    @POST
    @Produces(MediaType.TEXT_PLAIN)
    public String stringTypePath(@PathParam("stringValue") final StringType stringValue) {
        return stringValue.getValue();
    }

}
