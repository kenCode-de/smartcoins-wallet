package de.bitsharesmunich.cryptocoincore.dogecoin;

import de.bitsharesmunich.cryptocoincore.base.AccountSeed;
import de.bitsharesmunich.cryptocoincore.base.GeneralCoinManager;

/**
 * Singleton to manage the creation, import and retrieval of DogeCoin accounts.
 *
 */
public class DogeCoinManager extends GeneralCoinManager<DogeCoinAccount> {

    /**
     * The only instance of this singleton
     */
    private static DogeCoinManager sInstance = null;

    private DogeCoinManager() {

    }

    /**
     * if there's no instance of this class, creates one, otherwise
     * returns the already created
     *
     */
    public static DogeCoinManager getInstance() {
        if (DogeCoinManager.sInstance == null) {
            DogeCoinManager.sInstance = new DogeCoinManager();
        }

        return DogeCoinManager.sInstance;
    }

    /**
     * Creates a new dogecoin account with a seed and a name
     *
     * @param seed the master seed of the account
     * @param name the new name of the account in the scwall
     * @return a new dogecoin account created with the given seed and name
     */
    @Override
    public DogeCoinAccount newAccount(AccountSeed seed, String name) {
        return new DogeCoinAccount(seed, name);
    }

    /**
     * Creates a new dogecoin account with a seed and a name. This must be used
     * when importing another account
     *
     * @param seed the master seed of the account
     * @param name the new name of the account in the scwall
     * @return a new dogecoin account created with the given seed and name
     */
    @Override
    public DogeCoinAccount importAccount(AccountSeed seed, String name) {
        return new DogeCoinAccount(seed, name, true);
    }

    /**
     * Creates a new dogecoin account with specific attributes
     *
     * @param id the id of the account in the database
     * @param name the name of the account in scwall
     * @param seed the master seed of the account
     * @param accountIndex the index of the account as detailed in bip44
     * @param externalIndex the external index of the account
     * @param changeIndex the change index of the account
     * @return a dogecoin account with the given attributes
     */
    @Override
    public DogeCoinAccount getAccount(long id, String name, AccountSeed seed, int accountIndex, int externalIndex, int changeIndex) {
        return new DogeCoinAccount(id, name, seed, accountIndex, externalIndex, changeIndex);
    }

}
