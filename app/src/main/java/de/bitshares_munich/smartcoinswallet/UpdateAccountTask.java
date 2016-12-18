package de.bitshares_munich.smartcoinswallet;

import com.luminiasoft.bitshares.BrainKey;
import com.luminiasoft.bitshares.UserAccount;

/**
 * Created by nelson on 12/17/16.
 */
public class UpdateAccountTask {
    private BrainKey brainKey;
    private UserAccount account;
    private boolean updateOwner;
    private boolean updateMemo;

    UpdateAccountTask(UserAccount account, BrainKey brainKey){
        this.account = account;
        this.brainKey = brainKey;
    }

    public UserAccount getAccount() {
        return account;
    }

    public void setAccount(UserAccount account) {
        this.account = account;
    }

    public boolean isUpdateOwner() {
        return updateOwner;
    }

    public void setUpdateOwner(boolean updateOwner) {
        this.updateOwner = updateOwner;
    }

    public boolean isUpdateMemo(){ return updateMemo; }

    public void setUpdateMemo(boolean updateMemo){
        this.updateMemo = updateMemo;
    }

    public BrainKey getBrainKey(){
        return brainKey;
    }
}
