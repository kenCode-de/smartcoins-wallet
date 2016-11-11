package com.luminiasoft.bitshares;

/**
 * Created by nelson on 11/5/16.
 */
public abstract class BaseOperation implements ByteSerializable {

    protected OperationType type;

    public BaseOperation(OperationType type){
        this.type = type;
    }

    public abstract byte getId();

    public abstract byte[] toBytes();
}
