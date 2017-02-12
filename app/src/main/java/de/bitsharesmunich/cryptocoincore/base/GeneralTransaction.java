package de.bitsharesmunich.cryptocoincore.base;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by henry on 06/02/2017.
 */

public class GeneralTransaction {

    private String id;
    private String txid;
    private Coin type;
    private long block;
    private double fee;
    private int confirm;
    private Date date;

    private List<GIOTx> txInputs = new ArrayList();
    private List<GIOTx> txOutputs = new ArrayList();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTxid() {        return txid;    }

    public void setTxid(String txid) {        this.txid = txid;    }

    public Coin getType() {
        return type;
    }

    public void setType(Coin type) {
        this.type = type;
    }

    public long getBlock() {
        return block;
    }

    public void setBlock(long block) {
        this.block = block;
    }

    public double getFee() {
        return fee;
    }

    public void setFee(double fee) {
        this.fee = fee;
    }

    public int getConfirm() {
        return confirm;
    }

    public void setConfirm(int confirm) {
        this.confirm = confirm;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public List<GIOTx> getTxInputs() {
        return txInputs;
    }

    public void setTxInputs(List<GIOTx> txInputs) {
        this.txInputs = txInputs;
    }

    public List<GIOTx> getTxOutputs() {
        return txOutputs;
    }

    public void setTxOutputs(List<GIOTx> txOutputs) {
        this.txOutputs = txOutputs;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GeneralTransaction that = (GeneralTransaction) o;

        if (txid != null ? !txid.equals(that.txid) : that.txid != null) return false;
        return type == that.type;

    }

    @Override
    public int hashCode() {
        int result = txid != null ? txid.hashCode() : 0;
        result = 31 * result + type.hashCode();
        return result;
    }
}
