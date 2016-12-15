package de.bitshares_munich.adapters;

import android.util.Log;

import com.luminiasoft.bitshares.UserAccount;
import com.luminiasoft.bitshares.models.HistoricalTransfer;

import java.util.Comparator;

/**
 * Created by nelson on 12/14/16.
 */
public class TransferSendReceiveComparator implements Comparator<HistoricalTransfer> {
    private String TAG = this.getClass().getName();

    private UserAccount me;

    public TransferSendReceiveComparator(UserAccount userAccount){
        this.me = userAccount;
    }

    @Override
    public int compare(HistoricalTransfer lhs, HistoricalTransfer rhs) {
        Log.d(TAG, "My user account id: "+me.getObjectId()+", lhs operation's from user account id: "+lhs.getOperation().getFrom().getObjectId());
        boolean isOutgoing = lhs.getOperation().getFrom().getObjectId().equals(me.getObjectId());
        if(isOutgoing)
            return -1;
        else
            return 1;
    }
}
