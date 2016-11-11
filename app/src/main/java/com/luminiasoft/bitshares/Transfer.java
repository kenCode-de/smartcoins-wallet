package com.luminiasoft.bitshares;

import com.google.common.primitives.Bytes;

/**
 * Created by nelson on 11/9/16.
 */
public class Transfer extends BaseOperation {
    private AssetAmount fee;
    private AssetAmount transferAmount;
    private UserAccount from;
    private UserAccount to;
    private Memo memo;

    public Transfer(UserAccount from, UserAccount to, AssetAmount transferAmount, AssetAmount fee){
        super(OperationType.transfer_operation);
        this.from = from;
        this.to = to;
        this.transferAmount = transferAmount;
        this.fee = fee;
        this.memo = new Memo();
    }

    @Override
    public byte getId() {
        return (byte) this.type.ordinal();
    }

    @Override
    public byte[] toBytes() {
        byte[] feeBytes = fee.toBytes();
        byte[] fromBytes = from.toBytes();
        byte[] toBytes = to.toBytes();
        byte[] amountBytes = transferAmount.toBytes();
        byte[] memoBytes = memo.toBytes();
        return Bytes.concat(feeBytes, fromBytes, toBytes, amountBytes, memoBytes);
    }
}
