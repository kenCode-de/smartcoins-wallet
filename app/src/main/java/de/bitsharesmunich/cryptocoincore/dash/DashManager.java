package de.bitsharesmunich.cryptocoincore.dash;

import de.bitsharesmunich.cryptocoincore.base.AccountSeed;
import de.bitsharesmunich.cryptocoincore.base.GeneralCoinManager;

/**
 * Created by henry on 14/03/2017.
 */

public class DashManager extends GeneralCoinManager<DashAccount> {
    static private DashManager instance = null;

    private DashManager() {

    }

    public static DashManager getInstance() {
        if (DashManager.instance == null) {
            DashManager.instance = new DashManager();
        }

        return DashManager.instance;
    }

    @Override
    public DashAccount newAccount(AccountSeed seed, String name) {
        return new DashAccount(seed, name);
    }

    @Override
    public DashAccount importAccount(AccountSeed seed, String name) {
        return new DashAccount(seed, name, true);
    }

    @Override
    public DashAccount getAccount(long id, String name, AccountSeed seed, int accountIndex, int externalIndex, int changeIndex) {
        return new DashAccount(id, name, seed, accountIndex, externalIndex, changeIndex);
    }
}
