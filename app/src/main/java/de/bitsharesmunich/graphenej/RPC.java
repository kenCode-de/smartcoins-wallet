package de.bitsharesmunich.graphenej;

/**
 * Created by nelson on 11/16/16.
 */
public class RPC {
    public static final String VERSION = "2.0";
    public static final String CALL_LOGIN = "login";
    public static final String CALL_NETWORK_BROADCAST = "network_broadcast";
    public static final String CALL_HISTORY = "history";
    public static final String CALL_DATABASE = "database";
    //Returns information about the given asset.
    //Ref: http://docs.bitshares.eu/api/wallet-api.html#asset-calls
    public static final String CALL_GET_ASSET_INFO = "get_asset";
    public static final String CALL_GET_ACCOUNT_BY_NAME = "get_account_by_name";
    public static final String CALL_GET_ACCOUNTS = "get_accounts";
    public static final String CALL_GET_DYNAMIC_GLOBAL_PROPERTIES = "get_dynamic_global_properties";
    public static final String CALL_BROADCAST_TRANSACTION = "broadcast_transaction";
    //For each operation calculate the required fee in the specified asset type.
    // If the asset type does not have a valid core_exchange_rate
    //Ref: http://docs.bitshares.eu/api/database.html?highlight=get_required_fees
    public static final String CALL_GET_REQUIRED_FEES = "get_required_fees";
    public static final String CALL_GET_KEY_REFERENCES = "get_key_references";
    public static final String CALL_GET_RELATIVE_ACCOUNT_HISTORY = "get_relative_account_history";
    public static final String CALL_LOOKUP_ACCOUNTS = "lookup_accounts";
    public static final String CALL_GET_ASSET = "lookup_asset_symbols"; // Remove this later
    public static final String CALL_LOOKUP_ASSET_SYMBOLS = "lookup_asset_symbols";
    public static final String CALL_GET_BLOCK_HEADER = "get_block_header";
    public static final String CALL_GET_LIMIT_ORDERS = "get_limit_orders";
    public static final String CALL_GET_TRADE_HISTORY = "get_trade_history";
    public static final String CALL_GET_MARKET_HISTORY = "get_market_history";
}
