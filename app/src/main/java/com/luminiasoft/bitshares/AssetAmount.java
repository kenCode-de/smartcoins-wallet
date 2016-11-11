package com.luminiasoft.bitshares;

import com.google.common.primitives.UnsignedLong;

/**
 * Created by nelson on 11/7/16.
 */
public class AssetAmount implements ByteSerializable {
    private UnsignedLong amount;
    private Asset asset;

    public AssetAmount(UnsignedLong amount, Asset asset){
        this.amount = amount;
        this.asset = asset;
    }

    @Override
    public byte[] toBytes() {
        byte[] serialized = new byte[8 + 1];
        byte[] amountBytes = this.amount.bigIntegerValue().toByteArray();
        serialized[serialized.length - 1] = (byte) asset.instance;

        for(int i = 0; i < amountBytes.length; i++)
            serialized[i] = amountBytes[amountBytes.length - 1 - i];

        return serialized;
    }
}
