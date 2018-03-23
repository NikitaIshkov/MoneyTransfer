package org.MoneyTransfer.dao;

import org.MoneyTransfer.model.Account;
import org.glassfish.jersey.spi.Contract;

import java.util.Collection;

@Contract
public interface AccountDAO
{
    Collection<Account> findAll();
    Account findById(int id);
    int insert(Account acc);
    Account delete(int id);
}
