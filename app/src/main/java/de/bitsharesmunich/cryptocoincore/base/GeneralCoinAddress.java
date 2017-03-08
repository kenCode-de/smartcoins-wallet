package de.bitsharesmunich.cryptocoincore.base;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.crypto.DeterministicKey;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.bitsharesmunich.graphenej.Util;

/**
 * Created by henry on 06/02/2017.
 */

public class GeneralCoinAddress {

    private long id = -1;
    private final GeneralCoinAccount account;
    private final boolean isChange;
    private final int index;
    private ECKey key;

    private List<GTxIO> transactionInput = new ArrayList();
    private List<GTxIO> transactionOutput = new ArrayList();


    public GeneralCoinAddress(long id, GeneralCoinAccount account, boolean isChange, int index, String publicHexKey) {
        this.id = id;
        this.account = account;
        this.isChange = isChange;
        this.index = index;
        this.key = ECKey.fromPublicOnly(Util.hexToBytes(publicHexKey));
    }

    public GeneralCoinAddress(GeneralCoinAccount account, boolean isChange, int index, DeterministicKey key) {
        this.id = -1;
        this.account = account;
        this.isChange = isChange;
        this.index = index;
        this.key = key;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
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

    public List<GTxIO> getTransactionInput() {
        return transactionInput;
    }

    public void setTransactionInput(List<GTxIO> transactionInput) {
        this.transactionInput = transactionInput;
    }

    public boolean hasTransactionInput(GTxIO inputToFind, NetworkParameters param) {
        for (GTxIO input : transactionInput) {
            if ((input.getTransaction().getTxid().equals(inputToFind.getTransaction().getTxid()))
                    && (input.getAddress().getAddressString(param).equals(inputToFind.getAddress().getAddressString(param)))
                    ) {
                return true;
            }
        }
        return false;
    }

    public List<GTxIO> getTransactionOutput() {
        return transactionOutput;
    }

    public boolean hasTransactionOutput(GTxIO outputToFind, NetworkParameters param) {
        for (GTxIO output : transactionOutput) {
            if ((output.getTransaction().getTxid().equals(outputToFind.getTransaction().getTxid()))
                    && (output.getAddress().getAddressString(param).equals(outputToFind.getAddress().getAddressString(param)))
                    ) {
                return true;
            }
        }
        return false;
    }

    public void setTransactionOutput(List<GTxIO> outputTransaction) {
        this.transactionOutput = outputTransaction;
    }

    public long getUnconfirmedBalance() {
        long answer = 0;
        for (GTxIO input : transactionInput) {
            if (input.getTransaction().getConfirm() < account.getCoin().getConfirmationsNeeded()) {
                answer += input.getAmount();
            }
        }

        for (GTxIO output : transactionOutput) {
            if (output.getTransaction().getConfirm() < account.getCoin().getConfirmationsNeeded()) {
                answer -= output.getAmount();
            }
        }

        return answer;
    }

    public long getConfirmedBalance() {
        long answer = 0;
        for (GTxIO input : transactionInput) {
            if (input.getTransaction().getConfirm() >= account.getCoin().getConfirmationsNeeded()) {
                answer += input.getAmount();
            }
        }

        for (GTxIO output : transactionOutput) {
            if (output.getTransaction().getConfirm() >= account.getCoin().getConfirmationsNeeded()) {
                answer -= output.getAmount();
            }
        }

        return answer;
    }

    public Date getLastDate() {
        Date lastDate = null;
        for (GTxIO input : transactionInput) {
            if (lastDate == null || lastDate.before(input.getTransaction().getDate())) {
                lastDate = input.getTransaction().getDate();
            }
        }
        for (GTxIO output : transactionOutput) {
            if (lastDate == null || lastDate.before(output.getTransaction().getDate())) {
                lastDate = output.getTransaction().getDate();
            }
        }
        return lastDate;
    }

    public int getLessConfirmed(){
        int lessConfirm = -1;
        for (GTxIO input : transactionInput) {
            if (lessConfirm == -1 || input.getTransaction().getConfirm() < lessConfirm) {
                lessConfirm = input.getTransaction().getConfirm();
            }
        }

        for (GTxIO output : transactionOutput) {
            if (lessConfirm == -1 || output.getTransaction().getConfirm() < lessConfirm) {
                lessConfirm = output.getTransaction().getConfirm();
            }
        }
        return lessConfirm;
    }

    public List<GTxIO> getUTXos(){
        List<GTxIO> utxo = new ArrayList();
        for(GTxIO gitx : transactionInput){
            boolean find = false;
            for(GTxIO gotx : transactionOutput){
                if(gitx.getTransaction().getTxid().equals(gotx.getOriginalTxid())){
                    find = true;
                    break;
                }
            }
            if(!find){
                utxo.add(gitx);
            }
        }
        return utxo;
    }

    public void BalanceChange() {
        this.getAccount().balanceChange();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GeneralCoinAddress that = (GeneralCoinAddress) o;

        if (isChange != that.isChange) return false;
        if (index != that.index) return false;
        if (id != -1) return false;
        if (account != null ? !account.equals(that.account) : that.account != null) return false;
        if (key != null ? !key.equals(that.key) : that.key != null) return false;
        if (transactionInput != null ? !transactionInput.equals(that.transactionInput) : that.transactionInput != null)
            return false;
        return transactionOutput != null ? transactionOutput.equals(that.transactionOutput) : that.transactionOutput == null;

    }

    @Override
    public int hashCode() {
        int result = (int) id;
        result = 31 * result + (account != null ? account.hashCode() : 0);
        result = 31 * result + (isChange ? 1 : 0);
        result = 31 * result + index;
        result = 31 * result + (key != null ? key.hashCode() : 0);
        result = 31 * result + (transactionInput != null ? transactionInput.hashCode() : 0);
        result = 31 * result + (transactionOutput != null ? transactionOutput.hashCode() : 0);
        return result;
    }

    public boolean updateTransaction(GeneralTransaction transaction){
        for(GTxIO gitx : transactionInput){
            if(gitx.getTransaction().equals(transaction)){
                gitx.getTransaction().setConfirm(transaction.getConfirm());
                gitx.getTransaction().setBlock(transaction.getBlock());
                gitx.getTransaction().setBlockHeight(transaction.getBlockHeight());
                gitx.getTransaction().setDate(transaction.getDate());
                gitx.getTransaction().setMemo(transaction.getMemo());
                return true;
            }
        }

        for(GTxIO gotx : transactionOutput){
            if(gotx.getTransaction().equals(transaction)){
                gotx.getTransaction().setConfirm(transaction.getConfirm());
                gotx.getTransaction().setBlock(transaction.getBlock());
                gotx.getTransaction().setBlockHeight(transaction.getBlockHeight());
                gotx.getTransaction().setDate(transaction.getDate());
                gotx.getTransaction().setMemo(transaction.getMemo());
                return true;
            }
        }
        return false;
    }


}

