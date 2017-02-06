package de.bitsharesmunich.cryptocoincore.bitcoin;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.crypto.DeterministicKey;

/**
 * Created by henry on 05/02/2017.
 */

public class BitcoinAddress {


    private final DeterministicKey privateKey;
    private final int index;
    private boolean isChange;
    private final NetworkParameters params;

    public BitcoinAddress(DeterministicKey privateKey, int index, boolean isChange, NetworkParameters params) {
        this.privateKey = privateKey;
        this.index = index;
        this.isChange = isChange;
        this.params = params;
    }

    public String getAddressString() {
        return this.privateKey.toAddress(params).toString();
    }

    public Address getAddress() {
        return this.privateKey.toAddress(params);
    }

    public ECKey getPrivateKey() {
        return privateKey;
    }

    public boolean isIsChange() {
        return isChange;
    }

    public void setIsChange(boolean isChange) {
        this.isChange = isChange;
    }

    public int getIndex() {
        return index;
    }

}
