package de.bitsharesmunich.cryptocoincore.litecoin;

import de.bitsharesmunich.cryptocoincore.base.AccountSeed;
import de.bitsharesmunich.cryptocoincore.base.GeneralCoinManager;

/**
 *
 * @author Henry
 */
public class LiteCoinManager extends GeneralCoinManager<LiteCoinAccount> {

    static private LiteCoinManager instance = null;

    private LiteCoinManager() {

    }

    /**
     * if there's no instance of this class, creates one, otherwise
     * returns the already created
     *
     */
    public static LiteCoinManager getInstance() {
        if (LiteCoinManager.instance == null) {
            LiteCoinManager.instance = new LiteCoinManager();
        }

        return LiteCoinManager.instance;
    }

    /**
     * Creates a new litecoin account with a seed and a name
     *
     * @param seed the master seed of the account
     * @param name the new name of the account in the scwall
     * @return a new litecoin account created with the given seed and name
     */
    @Override
    public LiteCoinAccount newAccount(AccountSeed seed, String name) {
        return new LiteCoinAccount(seed, name);
    }

    /**
     * Creates a new litecoin account with a seed and a name. This must be used
     * when importing another account
     *
     * @param seed the master seed of the account
     * @param name the new name of the account in the scwall
     * @return a new litecoin account created with the given seed and name
     */
    @Override
    public LiteCoinAccount importAccount(AccountSeed seed, String name) {
        return new LiteCoinAccount(seed, name, true);
    }

    /**
     * Creates a new litecoin account with specific attributes
     *
     * @param id the id of the account in the database
     * @param name the name of the account in scwall
     * @param seed the master seed of the account
     * @param accountIndex the index of the account as detailed in bip44
     * @param externalIndex the external index of the account
     * @param changeIndex the change index of the account
     * @return a litecoin account with the given attributes
     */
    @Override
    public LiteCoinAccount getAccount(long id, String name, AccountSeed seed, int accountIndex, int externalIndex, int changeIndex) {
        return new LiteCoinAccount(id, name, seed, accountIndex, externalIndex, changeIndex);
    }

}
