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

    /**
     * if there's no instance of this class, creates one, otherwise
     * returns the already created
     *
     */
    public static DashManager getInstance() {
        if (DashManager.instance == null) {
            DashManager.instance = new DashManager();
        }

        return DashManager.instance;
    }

    /**
     * Creates a new dash account with a seed and a name
     *
     * @param seed the master seed of the account
     * @param name the new name of the account in the scwall
     * @return a new dash account created with the given seed and name
     */
    @Override
    public DashAccount newAccount(AccountSeed seed, String name) {
        return new DashAccount(seed, name);
    }

    /**
     * Creates a new dash account with a seed and a name. This must be used
     * when importing another account
     *
     * @param seed the master seed of the account
     * @param name the new name of the account in the scwall
     * @return a new dash account created with the given seed and name
     */
    @Override
    public DashAccount importAccount(AccountSeed seed, String name) {
        return new DashAccount(seed, name, true);
    }

    /**
     * Creates a new dash account with specific attributes
     *
     * @param id the id of the account in the database
     * @param name the name of the account in scwall
     * @param seed the master seed of the account
     * @param accountIndex the index of the account as detailed in bip44
     * @param externalIndex the external index of the account
     * @param changeIndex the change index of the account
     * @return a dash account with the given attributes
     */
    @Override
    public DashAccount getAccount(long id, String name, AccountSeed seed, int accountIndex, int externalIndex, int changeIndex) {
        return new DashAccount(id, name, seed, accountIndex, externalIndex, changeIndex);
    }
}
