package io.quarkus.resteasy.reactive.jaxb.deployment.test;

import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.MediaType;

import io.quarkus.runtime.BlockingOperationControl;
import io.smallrye.common.annotation.NonBlocking;

@Path("/simple")
@NonBlocking
public class SimpleXmlResource {

    @GET
    @Path("/person")
    public Person getPerson() {
        Person person = new Person();
        person.setFirst("Bob");
        person.setLast("Builder");
        return person;
    }

    @POST
    @Path("/person")
    @Produces(MediaType.APPLICATION_XML)
    @Consumes(MediaType.APPLICATION_XML)
    public Person getPerson(Person person) {
        if (BlockingOperationControl.isBlockingAllowed()) {
            throw new RuntimeException("should not have dispatched");
        }
        return person;
    }

    @POST
    @Path("/person-large")
    @Produces(MediaType.APPLICATION_XML)
    @Consumes(MediaType.APPLICATION_XML)
    public Person personTest(Person person) {
        //large requests should get bumped from the IO thread
        if (!BlockingOperationControl.isBlockingAllowed()) {
            throw new RuntimeException("should have dispatched");
        }
        return person;
    }

    @POST
    @Path("/person-validated")
    @Produces(MediaType.APPLICATION_XML)
    @Consumes(MediaType.APPLICATION_XML)
    public Person getValidatedPerson(@Valid Person person) {
        return person;
    }

    @POST
    @Path("/person-invalid-result")
    @Produces(MediaType.APPLICATION_XML)
    @Consumes(MediaType.APPLICATION_XML)
    @Valid
    public Person getInvalidPersonResult(@Valid Person person) {
        person.setLast(null);
        return person;
    }

    @GET
    @Path("/async-person")
    @Produces(MediaType.APPLICATION_XML)
    public void getPerson(@Suspended AsyncResponse response) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Person person = new Person();
                person.setFirst("Bob");
                person.setLast("Builder");
                response.resume(person);
            }
        }).start();
    }
}
