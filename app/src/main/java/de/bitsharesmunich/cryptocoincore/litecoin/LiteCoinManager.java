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

    public static LiteCoinManager getInstance() {
        if (LiteCoinManager.instance == null) {
            LiteCoinManager.instance = new LiteCoinManager();
        }

        return LiteCoinManager.instance;
    }

    @Override
    public LiteCoinAccount newAccount(AccountSeed seed, String name) {
        return new LiteCoinAccount(seed, name);
    }

    @Override
    public LiteCoinAccount importAccount(AccountSeed seed, String name) {
        return new LiteCoinAccount(seed, name, true);
    }

    @Override
    public LiteCoinAccount getAccount(long id, String name, AccountSeed seed, int accountIndex, int externalIndex, int changeIndex) {
        return new LiteCoinAccount(id, name, seed, accountIndex, externalIndex, changeIndex);
    }

}
