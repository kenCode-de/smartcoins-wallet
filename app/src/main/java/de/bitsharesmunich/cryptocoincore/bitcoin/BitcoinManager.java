package de.bitsharesmunich.cryptocoincore.bitcoin;

import de.bitsharesmunich.cryptocoincore.base.AccountSeed;
import de.bitsharesmunich.cryptocoincore.base.GeneralCoinManager;

/**
 * Created by henry on 05/02/2017.
 */

public class BitcoinManager extends GeneralCoinManager<BitcoinAccount> {

static private BitcoinManager instance = null;

private BitcoinManager() {

        }

public static BitcoinManager getInstance() {
        if (BitcoinManager.instance == null) {
        BitcoinManager.instance = new BitcoinManager();
        }

        return BitcoinManager.instance;
        }

@Override
public BitcoinAccount newAccount(AccountSeed seed, String name) {
        return new BitcoinAccount(seed,name);
        }

@Override
public BitcoinAccount importAccount(AccountSeed seed,String name) {
        return new BitcoinAccount(seed,name,true);
        }

@Override
public BitcoinAccount getAccount(String id, String name, AccountSeed seed, int accountIndex, int externalIndex, int changeIndex) {
        return new BitcoinAccount(id, name, seed, accountIndex, externalIndex, changeIndex);
        }

        }
