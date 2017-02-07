package de.bitsharesmunich.cryptocoincore.base;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.crypto.DeterministicKey;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by henry on 06/02/2017.
 */

public class GeneralCoinAddress {

    private String id;
    private final GeneralCoinAccount account;
    private final boolean isChange;
    private final int index;
    private DeterministicKey key;

    private List<GIOTx> inputTransaction = new ArrayList();
    private List<GIOTx> outputTransaction = new ArrayList();


    public GeneralCoinAddress(String id, GeneralCoinAccount account, boolean isChange, int index, DeterministicKey key) {
        this.id = id;
        this.account = account;
        this.isChange = isChange;
        this.index = index;
        this.key = key;
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

    public DeterministicKey getKey() {
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
}
