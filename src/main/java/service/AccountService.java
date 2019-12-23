package service;

import java.util.Collection;

import model.Account;

public interface AccountService {

    public void addAccount(Account account);

    public Collection<Account> getAccounts();

    public Account getAccount(String accountNumber);

    public Account editAccount(Account account);

    public void deleteAccount(String accountNumber);
}
