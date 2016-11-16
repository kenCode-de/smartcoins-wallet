package com.luminiasoft.bitshares.interfaces;

/**
 * Interface that must be implemented by any party interested in the result of any database query RPC call.
 * Some examples of supported methods are: 'get_objects', 'get_account_by_name', 'lookup_asset_symbols',
 * 'get_required_fees', etc.
 */
public interface OnDatabaseQueryListener {

    void onResult(String result);

    void onError(String message);
}
