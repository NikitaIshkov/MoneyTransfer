package org.MoneyTransfer;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class HelloTest extends JerseyTest
{
    @Override
    public Application configure()
    {
        return new ResourceConfig(Hello.class).register(new ApplicationBinder());
    }

    @Test
    public void test()
    {
        Form form;
        Response res;
        String name;
        String balance;
        final String randomNumber = "2"; // random enough

        // create 3 accounts
        for (int i = 0; i < 3; i++) {
            if (i == 0) {
                name = "First";
                balance = "100.00";
            } else if (i == 1) {
                name = "Second";
                balance = "200.00";
            } else {
                name = "Third";
                balance = "300.00";
            }

            form = new Form();
            form.param("name", name);
            form.param("balance", balance);

            res = target("/create").request()
                    .post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED));
            assertEquals("create", Response.Status.OK.getStatusCode(), res.getStatus());
            assertNotEquals("Should not be empty", "", res.readEntity(String.class));
        }

        // ensure they were created
        res = target("/getAll").request().get();
        assertEquals("getAll ret code", Response.Status.OK.getStatusCode(), res.getStatus());
        assertEquals("/create result: ", "First,Second,Third", res.readEntity(String.class));

        // get account by id
        res = target("/" + randomNumber).request().get();
        assertEquals("getById ret code", Response.Status.OK.getStatusCode(), res.getStatus());
        assertEquals("getById result: ", "Second", res.readEntity(String.class));

        // check balance
        res = target("/balance/" + randomNumber).request().get();
        assertEquals("getBalance ret code", Response.Status.OK.getStatusCode(), res.getStatus());
        assertEquals("/balance result: ","200.00", res.readEntity(String.class));

        // add balance
        form = new Form();
        form.param("id", randomNumber);
        form.param("amount", "500.00");

        res = target("/add").request()
                .put(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED));
        assertEquals("add ret code", Response.Status.OK.getStatusCode(), res.getStatus());
        assertEquals("/add result: ","700.00", res.readEntity(String.class));

        // transfer money
        ExecutorService executor = Executors.newFixedThreadPool(5);

        for (int i = 0; i < 5; i++) {
            executor.submit(() -> {
                int rFrom = ThreadLocalRandom.current().nextInt(1, 3 + 1);
                int rTo = ThreadLocalRandom.current().nextInt(1, 3 + 1);

                Form form1 = new Form();
                form1.param("fromId", String.valueOf(rFrom));
                form1.param("toId", String.valueOf(rTo));
                form1.param("amount", "100.00");

                Response res1 = target("/send").request()
                        .put(Entity.entity(form1, MediaType.APPLICATION_FORM_URLENCODED));

                if (res1.getStatus() != Response.Status.OK.getStatusCode())
                    System.out.println("no fund on " + rFrom);
                else
                    System.out.println("balance of " + rFrom + ": " + res1.readEntity(String.class));
            });
        }
        try {
            executor.awaitTermination(1000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        executor.shutdown();

        // delete account
        res = target("/delete/" + randomNumber).request().delete();
        assertEquals("delete ret code", Response.Status.OK.getStatusCode(), res.getStatus());
        assertEquals("/delete result: ", randomNumber, res.readEntity(String.class));
    }
}
