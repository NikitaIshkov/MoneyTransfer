package org.MoneyTransfer.dao;

import org.MoneyTransfer.model.Account;
import org.jvnet.hk2.annotations.Service;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class AccountDAOImpl implements AccountDAO
{
    private final ConcurrentMap<Integer, Account> accounts;

    public AccountDAOImpl()
    {
        accounts = new ConcurrentHashMap<>();
    }

    @Override
    public Collection<Account> findAll()
    {
        return accounts.values();
    }

    @Override
    public Account findById(int id)
    {
        return accounts.get(id);
    }

    @Override
    public int insert(Account acc)
    {
        accounts.putIfAbsent(acc.getId(), acc);

        return acc.getId();
    }

    @Override
    public Account delete(int id)
    {
        return accounts.remove(id);
    }
}
