package com.luminiasoft.bitshares.interfaces;

import com.luminiasoft.bitshares.models.BaseResponse;

/**
 * Interface to be implemented by any party interested in the result of a transaction broadcast.
 */
public interface TransactionBroadcastListener {

    void onSuccess();

    void onError(BaseResponse.Error error);
}
