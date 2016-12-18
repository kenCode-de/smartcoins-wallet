package com.luminiasoft.bitshares.models;

import android.content.Context;

import com.luminiasoft.bitshares.TransferOperation;

import java.text.SimpleDateFormat;
import java.util.Date;

import de.bitshares_munich.utils.Helper;

/**
 * This class offers support to deserialization of transfer operations received by the API
 * method get_relative_account_history.
 *
 * More operations types might be listed in the response of that method, but by using this class
 * those will be filtered out of the parsed result.
 */
public class HistoricalTransfer {
    private String id;
    private TransferOperation op;
    public Object[] result;
    private long block_num;
    private long trx_in_block;
    private long op_in_trx;
    private long virtual_op;

    // Extra field
    private long timestamp;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public TransferOperation getOperation() {
        return op;
    }

    public void setOperation(TransferOperation op) {
        this.op = op;
    }

    public long getBlockNum() {
        return block_num;
    }

    public void setBlockNum(long block_num) {
        this.block_num = block_num;
    }

    public long getTransactionsInBlock() {
        return trx_in_block;
    }

    public void setTransactionsInBlock(long trx_in_block) {
        this.trx_in_block = trx_in_block;
    }

    public long getOperationsInTrx() {
        return op_in_trx;
    }

    public void setOperationsInTrx(long op_in_trx) {
        this.op_in_trx = op_in_trx;
    }

    public long getVirtualOp() {
        return virtual_op;
    }

    public void setVirtualOp(long virtual_op) {
        this.virtual_op = virtual_op;
    }

    public void setTimestamp(long timestamp){
        this.timestamp = timestamp;
    }

    public long getTimestamp(){
        return this.timestamp;
    }

    public String getDateString(Context context)
    {
        return Helper.convertDateToGMT(new Date(timestamp),context);

    }

    public String getDateStringWithYear(Context context)
    {
        return Helper.convertDateToGMTWithYear(new Date(timestamp),context);

    }

    public String getTimeString(Context context)
    {
        return Helper.convertTimeToGMT(new Date(timestamp),context);
    }

    public String getTimeZone(Context context)
    {
        return Helper.convertTimeZoneToRegion(new Date(timestamp),context);
    }

    public boolean getSent(){
        return true;
    }

}