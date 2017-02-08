package de.bitsharesmunich.cryptocoincore.base;

import org.bitcoinj.crypto.ChildNumber;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.HDKeyDerivation;

import java.util.HashMap;

import de.bitshares_munich.database.SCWallDatabase;

/**
 * Created by henry on 05/02/2017.
 */

public abstract class GeneralCoinAccount extends CryptoCoinAccount {
    protected int accountNumber;
    protected int lastExternalIndex;
    protected int lastChangeIndex;
    protected DeterministicKey accountKey;
    protected DeterministicKey externalKey;
    protected DeterministicKey changeKey;
    protected HashMap<Integer, GeneralCoinAddress> externalKeys = new HashMap();
    protected HashMap<Integer, GeneralCoinAddress> changeKeys = new HashMap();

    public GeneralCoinAccount(String id, String name, Coin coin, final AccountSeed seed, int accountNumber, int lastExternalIndex, int lastChangeIndex) {
        super(id, name, coin, seed);
        this.accountNumber = accountNumber;
        this.lastExternalIndex = lastExternalIndex;
        this.lastChangeIndex = lastChangeIndex;
        calculateAddresses();
    }

    private void calculateAddresses() {
        //BIP44
        DeterministicKey masterKey = HDKeyDerivation.createMasterPrivateKey(seed.getSeed());
        DeterministicKey purposeKey = HDKeyDerivation.deriveChildKey(masterKey, new ChildNumber(44, true));
        DeterministicKey coinKey = HDKeyDerivation.deriveChildKey(purposeKey, new ChildNumber(0, true));
        accountKey = HDKeyDerivation.deriveChildKey(coinKey, new ChildNumber(accountNumber, true));
        externalKey = HDKeyDerivation.deriveChildKey(accountKey, new ChildNumber(0, false));
        changeKey = HDKeyDerivation.deriveChildKey(accountKey, new ChildNumber(1, false));
    }

    public void calculateGapExternal(){
        if(externalKey == null){
            calculateAddresses();
        }
        for(int i = 0; i < lastExternalIndex;i++){
            if(!externalKeys.containsKey(i)){
                externalKeys.put(i,new GeneralCoinAddress(this,false,i,HDKeyDerivation.deriveChildKey(externalKey, new ChildNumber(0, false))));
            }
        }
    }

    public void calculateGapChange(){
        if(changeKey == null){
            calculateAddresses();
        }
        for(int i = 0; i < lastChangeIndex;i++){
            if(!changeKeys.containsKey(i)){
                changeKeys.put(i,new GeneralCoinAddress(this,false,i,HDKeyDerivation.deriveChildKey(changeKey, new ChildNumber(0, false))));
            }
        }
    }

    public void saveAddresses(SCWallDatabase db){
        for(GeneralCoinAddress externalAddress : externalKeys.values()){
            if(externalAddress.getId() == null || externalAddress.getId().isEmpty() || externalAddress.getId().equalsIgnoreCase("null")){
                db.putGeneralCoinAddress(externalAddress);
            }else{
                db.updateGeneralCoinAddress(externalAddress);
            }
        }

        for(GeneralCoinAddress changeAddress : changeKeys.values()){
            if(changeAddress.getId() == null || changeAddress.getId().isEmpty() || changeAddress.getId().equalsIgnoreCase("null")){
                db.putGeneralCoinAddress(changeAddress);
            }else{
                db.updateGeneralCoinAddress(changeAddress);
            }
        }
    }

    public int getAccountNumber() {
        return accountNumber;
    }

    public int getLastExternalIndex() {
        return lastExternalIndex;
    }

    public int getLastChangeIndex() {
        return lastChangeIndex;
    }
}
