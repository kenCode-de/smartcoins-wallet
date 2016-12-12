package com.luminiasoft.bitshares;

import com.luminiasoft.bitshares.interfaces.ByteSerializable;

import org.bitcoinj.core.ECKey;
import org.spongycastle.math.ec.ECPoint;

/**
 * Created by nelson on 11/30/16.
 */
public class PublicKey implements ByteSerializable {
    private final String TAG = this.getClass().getName();
    //TODO: Make this class throw an error if a private key is passed
    private ECKey publicKey;

    public PublicKey(ECKey key) {
        this.publicKey = key;
    }

    public ECKey getKey(){
        return publicKey;
    }

    @Override
    public byte[] toBytes() {
        if(publicKey.isCompressed()) {
            return publicKey.getPubKey();
        }else{
            publicKey = ECKey.fromPublicOnly(ECKey.compressPoint(publicKey.getPubKeyPoint()));
            return publicKey.getPubKey();
        }
    }

    public String getAddress(){
        ECKey pk = ECKey.fromPublicOnly(publicKey.getPubKey());
        if(!pk.isCompressed()){
            ECPoint point = ECKey.compressPoint(pk.getPubKeyPoint());
            pk = ECKey.fromPublicOnly(point);
        }
        return new Address(pk).toString();
    }
}
