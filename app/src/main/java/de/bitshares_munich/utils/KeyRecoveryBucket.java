package de.bitshares_munich.utils;

import org.bitcoinj.core.ECKey;

import de.bitsharesmunich.graphenej.BrainKey;
import de.bitsharesmunich.graphenej.PublicKey;

/**
 * This recovery bucket class is quite complex because of the fact that different versions
 * of the app were storing old keys in different ways.
 *
 * Initially, all the app did when performing a key update operation was to store the old private
 * key in its WIF representation format. The access to the original brainkey was thus lost in
 * these old versions.
 *
 * More recent versions also stored every brain key ever generated. So for these cases we have
 * both the brain key and the derived private key.
 *
 * The strategy used in order to recover the keys is to ask the network for the current public
 * keys and then try to find a match between either the stored old private keys in the WIF
 * format or brainkey suggestions.
 */

public class KeyRecoveryBucket {
    private final String TAG = this.getClass().getName();

    private ECKey privateKey;
    private BrainKey brainKey;

    public KeyRecoveryBucket(ECKey privateKey, BrainKey brainKey){
        this.privateKey = privateKey;
        this.brainKey = brainKey;
        if(brainKey != null){
            this.privateKey = brainKey.getPrivateKey();
        }
    }

    public ECKey getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(ECKey privateKey) {
        this.privateKey = privateKey;
    }

    public BrainKey getBrainKey() {
        return brainKey;
    }

    public void setBrainKey(BrainKey brainKey) {
        this.brainKey = brainKey;
    }

    public PublicKey getPublicKey(){
        return new PublicKey(ECKey.fromPublicOnly(privateKey.getPubKey()));
    }

    @Override
    public int hashCode() {
        return this.privateKey.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return this.privateKey.equals(((KeyRecoveryBucket)obj).getPrivateKey());
    }
}
