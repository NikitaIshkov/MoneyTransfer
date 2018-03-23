package org.MoneyTransfer.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Account
{
    private static int count = 0;

    private int id;
    private final String name;
    private BigDecimal balance;
    private ReadWriteLock lock;

    public Account(String name, BigDecimal balance)
    {
        id = ++count;
        this.name = name;
        this.balance = balance.setScale(2, RoundingMode.HALF_UP);
        lock = new ReentrantReadWriteLock();
    }

    public int getId()
    {
        return id;
    }

    public String getName()
    {
        return name;
    }

    public BigDecimal getBalance()
    {
        lock.readLock().lock();
        try {
            return balance;
        } finally {
            lock.readLock().unlock();
        }
    }

    public BigDecimal updateBalance(BigDecimal amount)
    {
        lock.writeLock().lock();
        try {
            balance = balance.add(amount);
        } finally {
            lock.writeLock().unlock();
        }

        return balance;
    }

    public BigDecimal sendMoney(Account toAcc, BigDecimal amount)
    {
        BigDecimal ret = BigDecimal.valueOf(-1);

        lock.writeLock().lock();
        try {
            if (getBalance().compareTo(amount) >= 0) {
                toAcc.updateBalance(amount);
                ret = updateBalance(amount.negate());
            }
        } finally {
            lock.writeLock().unlock();
        }

        return ret;
    }
}
