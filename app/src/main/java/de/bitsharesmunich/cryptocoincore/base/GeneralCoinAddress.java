package de.bitsharesmunich.cryptocoincore.base;

import android.util.Log;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.crypto.DeterministicKey;

import java.util.ArrayList;
import java.util.List;

import de.bitsharesmunich.graphenej.Util;

/**
 * Created by henry on 06/02/2017.
 */

public class GeneralCoinAddress {

    private String id;
    private final GeneralCoinAccount account;
    private final boolean isChange;
    private final int index;
    private ECKey key;

    private List<GIOTx> inputTransaction = new ArrayList();
    private List<GIOTx> outputTransaction = new ArrayList();


    public GeneralCoinAddress(String id, GeneralCoinAccount account, boolean isChange, int index, String publicHexKey) {
        this.id = id;
        this.account = account;
        this.isChange = isChange;
        this.index = index;

        this.key = ECKey.fromPublicOnly(Util.hexToBytes(publicHexKey));
    }

    public GeneralCoinAddress(GeneralCoinAccount account, boolean isChange, int index, DeterministicKey key) {
        this.account = account;
        this.isChange = isChange;
        this.index = index;
        this.key = key;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public GeneralCoinAccount getAccount() {
        return account;
    }

    public boolean isIsChange() {
        return isChange;
    }

    public int getIndex() {
        return index;
    }

    public ECKey getKey() {
        return key;
    }

    public void setKey(DeterministicKey key) {
        this.key = key;
    }

    public String getAddressString(NetworkParameters param) {
        return key.toAddress(param).toString();
    }

    public Address getAddress(NetworkParameters param) {
        return key.toAddress(param);
    }

    public List<GIOTx> getInputTransaction() {
        return inputTransaction;
    }

    public void setInputTransaction(List<GIOTx> inputTransaction) {
        this.inputTransaction = inputTransaction;
    }

    public List<GIOTx> getOutputTransaction() {
        return outputTransaction;
    }

    public void setOutputTransaction(List<GIOTx> outputTransaction) {
        this.outputTransaction = outputTransaction;
    }

    public long getBalance(){
        long answer = 0 ;
        for(GIOTx input : inputTransaction){
            answer += input.getAmount();
        }

        for(GIOTx output : outputTransaction){
            answer -= output.getAmount();
        }

        return answer;
    }

    public void BalanceChange(){
        this.getAccount().balanceChange();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GeneralCoinAddress that = (GeneralCoinAddress) o;

        if (isChange != that.isChange) return false;
        if (index != that.index) return false;
        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (account != null ? !account.equals(that.account) : that.account != null) return false;
        if (key != null ? !key.equals(that.key) : that.key != null) return false;
        if (inputTransaction != null ? !inputTransaction.equals(that.inputTransaction) : that.inputTransaction != null)
            return false;
        return outputTransaction != null ? outputTransaction.equals(that.outputTransaction) : that.outputTransaction == null;

    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (account != null ? account.hashCode() : 0);
        result = 31 * result + (isChange ? 1 : 0);
        result = 31 * result + index;
        result = 31 * result + (key != null ? key.hashCode() : 0);
        result = 31 * result + (inputTransaction != null ? inputTransaction.hashCode() : 0);
        result = 31 * result + (outputTransaction != null ? outputTransaction.hashCode() : 0);
        return result;
    }
}
