package org.MoneyTransfer;

import org.MoneyTransfer.dao.AccountDAO;
import org.MoneyTransfer.model.Account;
import org.glassfish.jersey.server.ResourceConfig;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.stream.Collectors;

@Singleton
@Path("/")
public class Hello extends ResourceConfig
{
    @Inject
    private AccountDAO accDAO;

    @POST
    @Path("/create")
    public Response create(@FormParam("name") String name,
                           @FormParam("balance") String balance)
    {
        int id = accDAO.insert(new Account(name, new BigDecimal(balance)));

        return Response.status(Response.Status.OK).entity("id " + id + " inserted").build();
    }

    @GET
    @Path("/getAll")
    public Response getAccounts()
    {
        Collection<Account> accList = accDAO.findAll();

        return Response.status(Response.Status.OK)
                .entity(accList.stream().map(Account::getName)
                        .collect(Collectors.joining( "," ))).build();
    }

    @GET
    @Path("/{id}")
    public Response getAccountById(@PathParam("id") int id)
    {
        Account acc =  accDAO.findById(id);

        if (acc == null)
            throw new WebApplicationException("No such account", Response.Status.NOT_FOUND);

        return Response.status(Response.Status.OK).entity(acc.getName()).build();
    }

    @GET
    @Path("/balance/{id}")
    public Response getBalance(@PathParam("id") int id)
    {
        Account acc = accDAO.findById(id);

        if (acc == null)
            throw new WebApplicationException("No such account", Response.Status.NOT_FOUND);

        return Response.status(Response.Status.OK).entity(acc.getBalance().toString()).build();
    }

    @PUT
    @Path("/add")
    public Response putMoney(@FormParam("id") int id,
                             @FormParam("amount") String amount)
    {
        BigDecimal bdAmount = new BigDecimal(amount);

        if (bdAmount.compareTo(BigDecimal.ZERO) < 1)
            throw new WebApplicationException("Incorrect amount", Response.Status.BAD_REQUEST);

        Account acc = accDAO.findById(id);

        if (acc == null)
            throw new WebApplicationException("No such account", Response.Status.NOT_FOUND);

        BigDecimal newBalance = acc.updateBalance(bdAmount);

        return Response.status(Response.Status.OK).entity(newBalance.toString()).build();
    }

    @PUT
    @Path("/send")
    public Response sendMoney(@FormParam("fromId") int fromId,
                              @FormParam("toId") int toId,
                              @FormParam("amount") String amount)
    {
        BigDecimal bdAmount = new BigDecimal(amount);

        if (bdAmount.compareTo(BigDecimal.ZERO) < 1)
            throw new WebApplicationException("Incorrect amount", Response.Status.BAD_REQUEST);

        Account fromAcc = accDAO.findById(fromId);
        if (fromAcc == null)
            throw new WebApplicationException("No such (source) account", Response.Status.NOT_FOUND);

        Account toAcc = accDAO.findById(toId);
        if (toAcc == null)
            throw new WebApplicationException("No such (destination) account", Response.Status.NOT_FOUND);

        if (fromAcc.getBalance().compareTo(bdAmount) < 0)
            throw new WebApplicationException("Not sufficient fund on source account",
                    Response.Status.BAD_REQUEST);

        BigDecimal newBalance = fromAcc.sendMoney(toAcc, bdAmount);

        if (newBalance.compareTo(BigDecimal.valueOf(-1)) == 0)
            throw new WebApplicationException("Not sufficient fund on source account",
                    Response.Status.BAD_REQUEST);

        return Response.status(Response.Status.OK).entity(newBalance.toString()).build();
    }

    @DELETE
    @Path("/delete/{id}")
    public Response deleteAccount(@PathParam("id") int id)
    {
        if (accDAO.delete(id) == null)
            throw new WebApplicationException("No such account", Response.Status.NOT_FOUND);

        return Response.status(Response.Status.OK).entity(id).build();
    }
}
