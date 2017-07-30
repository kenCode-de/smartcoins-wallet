package de.bitsharesmunich.cryptocoincore.bitcoin;

import de.bitsharesmunich.cryptocoincore.base.AccountSeed;
import de.bitsharesmunich.cryptocoincore.base.GeneralCoinManager;

/**
 *
 * Created by henry on 05/02/2017.
 */

/**
 * Singleton to manage the creation, import and retrieval of bitcoin accounts.
 *
 */
public class BitcoinManager extends GeneralCoinManager<BitcoinAccount> {

    static private BitcoinManager sInstance = null; /**< the only instance of this singleton*/

    private BitcoinManager() {}

    /**
     * if there's no instance of this class, creates one, otherwise
     * returns the already created
     *
     * @return
     */
    public static BitcoinManager getInstance() {
        if (BitcoinManager.sInstance == null) {
            BitcoinManager.sInstance = new BitcoinManager();
        }
        return BitcoinManager.sInstance;
    }

    /**
     * Creates a new bitcoin account with a seed and a name
     *
     * @param seed the master seed of the account
     * @param name the new name of the account in the scwall
     * @return a new bitcoin account created with the given seed and name
     */
    @Override
    public BitcoinAccount newAccount(AccountSeed seed, String name) {
        return new BitcoinAccount(seed, name);
    }

    /**
     * Creates a new bitcoin account with a seed and a name. This must be used
     * when importing another account
     *
     * @param seed the master seed of the account
     * @param name the new name of the account in the scwall
     * @return a new bitcoin account created with the given seed and name
     */
    @Override
    public BitcoinAccount importAccount(AccountSeed seed, String name) {
        return new BitcoinAccount(seed, name, true);
    }

    /**
     * Creates a new bitcoin account with specific attributes
     *
     * @param id the id of the account in the database
     * @param name the name of the account in scwall
     * @param seed the master seed of the account
     * @param accountIndex the index of the account as detailed in bip44
     * @param externalIndex the external index of the account
     * @param changeIndex the change index of the account
     * @return a bitcoin account with the given attributes
     */
    @Override
    public BitcoinAccount getAccount(long id, String name, AccountSeed seed, int accountIndex, int externalIndex, int changeIndex) {
        return new BitcoinAccount(id, name, seed, accountIndex, externalIndex, changeIndex);
    }
}
