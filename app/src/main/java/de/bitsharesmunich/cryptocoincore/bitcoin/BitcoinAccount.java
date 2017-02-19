package de.bitsharesmunich.cryptocoincore.bitcoin;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.crypto.ChildNumber;
import org.bitcoinj.crypto.HDKeyDerivation;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.bitsharesmunich.cryptocoincore.base.AccountSeed;
import de.bitsharesmunich.cryptocoincore.base.Balance;
import de.bitsharesmunich.cryptocoincore.base.GeneralCoinAccount;
import de.bitsharesmunich.cryptocoincore.base.GeneralCoinAddress;

import static de.bitsharesmunich.cryptocoincore.base.Coin.BITCOIN;

/**
 * Created by henry on 05/02/2017.
 */

public class BitcoinAccount extends GeneralCoinAccount {

    private NetworkParameters param = NetworkParameters.fromID(NetworkParameters.ID_TESTNET);

    BitcoinAccount(String id, String name, AccountSeed seed, int accountNumber, int lastExternalIndex, int lastChangeIndex) {
        super(id, name, BITCOIN, seed, accountNumber, lastExternalIndex, lastChangeIndex);

    }

    public BitcoinAccount(final AccountSeed seed, String name) {
        this(seed, name, false);
    }

    BitcoinAccount(final AccountSeed seed, String name, boolean importing) {
        super("", name, BITCOIN, seed, 0, 0, 0);
        if (importing) {
            //TODO calculate the number of account
        }
    }

    @Override
    public List<Balance> getBalance() {
        long uncofirmedAmount = 0;
        long confirmedAmount = 0;
        int lessConfirmed = -1;
        Date lastDate = null;
        for (GeneralCoinAddress key : externalKeys.values()) {
            uncofirmedAmount += key.getUnconfirmedBalance();
            confirmedAmount += key.getConfirmedBalance();
            int keyLessConf = key.getLessConfirmed();
            if(keyLessConf != -1 && (lessConfirmed== -1 || keyLessConf< lessConfirmed)){
                lessConfirmed = keyLessConf;
            }
            if(lastDate == null || (key.getLastDate()!= null && lastDate.before(key.getLastDate()))){
                lastDate = key.getLastDate();
            }
        }

        for (GeneralCoinAddress key : changeKeys.values()) {
            uncofirmedAmount += key.getUnconfirmedBalance();
            confirmedAmount += key.getConfirmedBalance();
            int keyLessConf = key.getLessConfirmed();
            if(keyLessConf != -1 && (lessConfirmed== -1 || keyLessConf< lessConfirmed)){
                lessConfirmed = keyLessConf;
            }
            if(lastDate == null || (key.getLastDate()!= null && lastDate.before(key.getLastDate()))){
                lastDate = key.getLastDate();
            }
        }

        Balance balance = new Balance();
        balance.setType(BITCOIN);
        balance.setDate(lastDate);
        balance.setConfirmedAccomunt(confirmedAmount);
        balance.setUnconfirmedAmount(uncofirmedAmount);
        List<Balance> balances = new ArrayList();
        balances.add(balance);
        return balances;
    }

    public String getNextRecieveAddress() {
        if (!externalKeys.containsKey(lastExternalIndex)) {
            externalKeys.put(lastExternalIndex, new GeneralCoinAddress(this, false, lastExternalIndex, HDKeyDerivation.deriveChildKey(externalKey, new ChildNumber(lastExternalIndex, false))));
        }

        //Finding the next unused address
        while(externalKeys.get(lastExternalIndex).getInputTransaction().size()>0){
            ++lastExternalIndex;
            if (!externalKeys.containsKey(lastExternalIndex)) {
                externalKeys.put(lastExternalIndex, new GeneralCoinAddress(this, false, lastExternalIndex, HDKeyDerivation.deriveChildKey(externalKey, new ChildNumber(lastExternalIndex, false))));
            }
        }
        return externalKeys.get(lastExternalIndex).getAddressString(param);
    }

    public void sendCoin(Address to, Coin amount) {

        //Get from address
        //Get Change address to use
        //Put all input in transaction
        //Put all output in transaction
        //sign transaction
    }

    public Address getAddress() {
        return externalKeys.get(lastExternalIndex).getAddress(param);
    }

    @Override
    public String toString() {
        return "BitcoinAccount{"
                + "name=" + name
                + ", idSeed=" + seed.getId()
                + ", AccountNumber=" + accountNumber
                + ", nextAddress=" + getNextRecieveAddress()
                + ", param=" + param + '}';
    }

    @Override
    public String getAddressString(int index, boolean change) {
        if (change) {
            if (!changeKeys.containsKey(index)) {
                changeKeys.put(index, new GeneralCoinAddress(this, true, index, HDKeyDerivation.deriveChildKey(changeKey, new ChildNumber(index, false))));
            }
            return changeKeys.get(index).getAddressString(param);
        } else {
            if (!externalKeys.containsKey(index)) {
                externalKeys.put(index, new GeneralCoinAddress(this, false, index, HDKeyDerivation.deriveChildKey(externalKey, new ChildNumber(index, false))));
            }
            return externalKeys.get(index).getAddressString(param);
        }
    }

    @Override
    public NetworkParameters getNetworkParam() {
        return param;
    }

    @Override
    public GeneralCoinAddress getAddress(int index, boolean change) {
        if (change) {
            if (!changeKeys.containsKey(index)) {
                changeKeys.put(index, new GeneralCoinAddress(this, true, index, HDKeyDerivation.deriveChildKey(changeKey, new ChildNumber(index, false))));
            }
            return changeKeys.get(index);
        } else {
            if (!externalKeys.containsKey(index)) {
                externalKeys.put(index, new GeneralCoinAddress(this, false, index, HDKeyDerivation.deriveChildKey(externalKey, new ChildNumber(index, false))));
            }
            return externalKeys.get(index);
        }
    }
}
