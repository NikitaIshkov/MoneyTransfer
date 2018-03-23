package org.MoneyTransfer;

import org.MoneyTransfer.dao.AccountDAO;
import org.MoneyTransfer.dao.AccountDAOImpl;
import org.glassfish.jersey.internal.inject.AbstractBinder;

public class ApplicationBinder extends AbstractBinder
{
    @Override
    protected void configure()
    {
        bind(AccountDAOImpl.class).to(AccountDAO.class);
    }
}
