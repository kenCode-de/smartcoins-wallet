package de.bitsharesmunich.cryptocoincore.dogecoin;

import de.bitsharesmunich.cryptocoincore.base.AccountSeed;
import de.bitsharesmunich.cryptocoincore.base.GeneralCoinManager;

/**
 *
 * @author Henry
 */
public class DogeCoinManager extends GeneralCoinManager<DogeCoinAccount> {

    static private DogeCoinManager instance = null;

    private DogeCoinManager() {

    }

    public static DogeCoinManager getInstance() {
        if (DogeCoinManager.instance == null) {
            DogeCoinManager.instance = new DogeCoinManager();
        }

        return DogeCoinManager.instance;
    }

    @Override
    public DogeCoinAccount newAccount(AccountSeed seed, String name) {
        return new DogeCoinAccount(seed, name);
    }

    @Override
    public DogeCoinAccount importAccount(AccountSeed seed, String name) {
        return new DogeCoinAccount(seed, name, true);
    }

    @Override
    public DogeCoinAccount getAccount(long id, String name, AccountSeed seed, int accountIndex, int externalIndex, int changeIndex) {
        return new DogeCoinAccount(id, name, seed, accountIndex, externalIndex, changeIndex);
    }

}
