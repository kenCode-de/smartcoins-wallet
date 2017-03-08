package de.bitsharesmunich.cryptocoincore.base;

/**
 * Created by henry on 05/02/2017.
 */

public abstract class GeneralCoinManager<T extends GeneralCoinAccount> {

    public abstract T newAccount(AccountSeed seed, String name);

    public abstract T importAccount(AccountSeed seed, String name);

    public abstract T getAccount(long id, String name, AccountSeed seed, int accountIndex, int externalIndex, int changeIndex);
}
