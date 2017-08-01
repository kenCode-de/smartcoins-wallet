package de.bitshares_munich.database;

import android.provider.BaseColumns;

/**
 * Database contract class. Here we define table and column names as constants
 * grouped in their own public static classes.
 *
 * Created by nelson on 12/13/16.
 */
public class SCWallDatabaseContract {

    private SCWallDatabaseContract(){}

    public static class BaseTable implements BaseColumns {
        public static final String COLUMN_CREATION_DATE = "creation_date";
    }

    public static class Assets implements BaseColumns {
        public static final String TABLE_NAME = "assets";
        public static final String COLUMN_ID = "id";
        public static final String COLUMN_SYMBOL = "symbol";
        public static final String COLUMN_PRECISION = "precision";
        public static final String COLUMN_ISSUER = "issuer";
        public static final String COLUMN_DESCRIPTION = "description";
        public static final String COLUMN_MAX_SUPPLY = "max_supply";
    }

    public static class Transfers extends BaseTable {
        public static final String TABLE_NAME = "transfers";
        public static final String COLUMN_ID = "id";
        public static final String COLUMN_TIMESTAMP = "timestamp";
        public static final String COLUMN_FEE_AMOUNT = "fee_amount";
        public static final String COLUMN_FEE_ASSET_ID = "fee_asset_id";
        public static final String COLUMN_FROM = "source";
        public static final String COLUMN_TO = "destination";
        public static final String COLUMN_TRANSFER_AMOUNT = "transfer_amount";
        public static final String COLUMN_TRANSFER_ASSET_ID = "transfer_asset_id";
        public static final String COLUMN_MEMO_MESSAGE = "memo";
        public static final String COLUMN_MEMO_FROM = "memo_from_key";
        public static final String COLUMN_MEMO_TO = "memo_to_key";
        public static final String COLUMN_BLOCK_NUM = "block_num";
        public static final String COLUMN_EQUIVALENT_VALUE_ASSET_ID = "equivalent_value_asset_id";
        public static final String COLUMN_EQUIVALENT_VALUE = "equivalent_value";
    }

    public static class UserAccounts extends BaseTable {
        public static final String TABLE_NAME = "user_accounts";
        public static final String COLUMN_ID = "id";
        public static final String COLUMN_NAME = "name";
    }

    public static class AccountKeys extends BaseTable {
        public static final String TABLE_NAME = "account_keys";
        public static final String COLUMN_BRAINKEY = "brainkey";
        public static final String COLUMN_SEQUENCE_NUMBER = "sequence_number";
        public static final String COLUMN_WIF = "wif";
    }

    //CryptoCoinCore Section

    public static class Seeds implements BaseColumns {
        public static final String TABLE_NAME = "seeds";
        public static final String COLUMN_ID = "id";
        public static final String COLUMN_SEED_TYPE = "derived_seed_type";
        public static final String COLUMN_MNEMONIC = "mnemonic";
        public static final String COLUMN_ADDITIONAL = "additional";
        public static final String CONSTRAINT_SEED = "seedContraint";
    }

    public static class GeneralAccounts implements BaseColumns {
        public static final String TABLE_NAME = "general_accounts";
        public static final String COLUMN_ID = "id";
        public static final String COLUMN_NAME = "account_name"; //this is only to tag this account
        public static final String COLUMN_COIN_TYPE = "coin_type";//bitcoin,litecoin
        public static final String COLUMN_ID_SEED = "id_seed";
        public static final String COLUMN_ACCOUNT_INDEX = "account_index"; // the account index used
        public static final String COLUMN_EXTERNAL_INDEX = "external_index"; // the last external address index used
        public static final String COLUMN_CHANGE_INDEX = "change_index"; // the last change address index used
        public static final String CONSTRAINT_ACCOUNT = "accountContraint";
    }

    /**
     * TODO SLIP-48 This must be implemented in future releases
     */
    public static class GrapheneAccounts implements BaseColumns {
        public static final String TABLE_NAME = "graphene_accounts";
        public static final String COLUMN_ID = "id";
        public static final String COLUMN_NAME = "account_name"; //The name of the account
        public static final String COLUMN_NETWORK_TYPE = "network_type";//Steem,BitShares,PeerPlays,Muse
        public static final String COLUMN_ID_SEED = "id_seed";
        public static final String COLUMN_ACCOUNT_INDEX = "account_index"; // the account used
        //TODO Each graphene network has its own role types, we need to design a way of indexing each address of each role
    }

    public static class GeneralOrphanKeys implements BaseColumns {
        public static final String TABLE_NAME = "general_orphan_key";
        public static final String COLUMN_ID = "id";
        public static final String COLUMN_NAME = "name"; //this is only to tag this key
        public static final String COLUMN_COIN_TYPE = "coin_type";//bitcoin,litecoin,Dash,Dogecoin
        public static final String COLUMN_WIF = "wif";
        public static final String CONSTRAINT_KEY = "generalOprhanContraint";
    }

    //TODO GrapheneOrphanKeys


    public static class GeneralCoinAddress implements BaseColumns {
        public static final String TABLE_NAME = "general_coin_address";
        public static final String COLUMN_ID = "id";
        public static final String COLUMN_ID_ACCOUNT = "id_account";
        public static final String COLUMN_IS_CHANGE = "is_change";
        public static final String COLUMN_INDEX = "address_index";
        public static final String COLUMN_PUBLIC_KEY = "pub_key";
        public static final String CONSTRAINT_ADDRESS = "genAddressContraint";
    }

    public static class Inputs implements BaseColumns {
        public static final String TABLE_NAME = "input_tx";
        public static final String COLUMN_ID = "id";
        public static final String COLUMN_COIN_TYPE = "coin_type"; // Bitcoin,LiteCoin,Dash,DogeCoin
        public static final String COLUMN_ID_ADDRESS = "id_address"; //point to a GeneralCoinAddress
        public static final String COLUMN_ADDRESS_STRING = "address_string"; //point to a GeneralCoinAddress
        public static final String COLUMN_ID_TRANSACTION = "id_transaction"; //point to a BitcoinTransaction
        public static final String COLUMN_AMOUNT = "amount";
        public static final String COLUMN_SCRIPT_HEX = "script_hex";
        public static final String COLUMN_INDEX = "tx_index";
        public static final String COLUMN_ORIGIN_TXID = "origin_txid";
        public static final String CONSTRAINT_ADDRESS = "genInputsContraint";
    }

    public static class Outputs implements BaseColumns {
        public static final String TABLE_NAME = "output_tx";
        public static final String COLUMN_ID = "id";
        public static final String COLUMN_COIN_TYPE = "coin_type"; // Bitcoin,LiteCoin,Dash,DogeCoin
        public static final String COLUMN_ID_ADDRESS = "id_address"; //point to a GeneralCoinAddress
        public static final String COLUMN_ADDRESS_STRING = "address_string"; //point to a GeneralCoinAddress
        public static final String COLUMN_ID_TRANSACTION = "id_transaction"; //point to a BitcoinTransaction
        public static final String COLUMN_AMOUNT = "amount";
        public static final String COLUMN_SCRIPT_HEX = "script_hex";
        public static final String COLUMN_INDEX = "tx_index";
        public static final String CONSTRAINT_ADDRESS = "genOutputsContraint";
    }

    public static class GeneralTransaction implements BaseColumns {
        public static final String TABLE_NAME = "general_transaction";
        public static final String COLUMN_ID = "id";
        public static final String COLUMN_TXID = "txid";
        public static final String COLUMN_COIN_TYPE = "coin_type"; // Bitcoin,LiteCoin,Dash,DogeCoin
        public static final String COLUMN_BLOCK = "block"; //the number of the block where is include
        public static final String COLUMN_BLOCK_HEIGHT = "block_height";
        public static final String COLUMN_DATE = "date"; //receive date
        public static final String COLUMN_FEE = "fee"; //the amount of the comission
        public static final String COLUMN_CONFIRMS = "confirm"; //the last confirm reader, this only matters when is lower than 6
        public static final String COLUMN_MEMO = "memo";
        public static final String COLUMN_ACCOUNT_ID = "account_id"; //The account id associated to this transaction. Even when a transaction can have many accounts involve, this account was the purpouse of having the transaction in the db.
        public static final String COLUMN_BALANCE_CACHE = "balance_cache"; //Will hold the balance change for this transaction. Is a cache because this information can be calculated with the inputs and outputs associated.
        public static final String COLUMN_SPENT = "spent"; //A boolean, true when the transaction was already spent and false on the contrary. 0 for false, 1 for true
        public static final String CONSTRAINT_TRANSACTION = "generalTransactionContraint";
    }

    public static class GeneralCoinSetting implements BaseColumns {
        public static final String TABLE_NAME = "general_coin_setting";
        public static final String COLUMN_ID = "id";
        public static final String COLUMN_COIN_TYPE = "coin_type"; // Bitcoin,LiteCoin,Dash,DogeCoin
        public static final String COLUMN_SETTING = "setting";
        public static final String COLUMN_VALUE = "value";
        public static final String CONSTRAINT_COIN_SETTING = "generalCoinSettingConstraint";
    }

    /*public static class GeneralTransactionBalanceCache implements BaseColumns {
        public static final String TABLE_NAME = "general_account_balance_cache";
        public static final String COLUMN_ACCOUNT_ID = "account_id";
        public static final String COLUMN_BALANCE = "balance";
        public static final String COLUMN_LAST_TRANSACTION_ID = "transaction_id";
    }*/

    // Contacts Section

    public static class Contacs implements BaseColumns{
        public static final String TABLE_NAME = "contact";
        public static final String COLUMN_ID = "id";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_ACCOUNT = "account";
        public static final String COLUMN_NOTE = "note";
        public static final String COLUMN_EMAIL = "email";
        public static final String CONSTRAINT_NAME ="contactsNameConstrain";
    }

    public static class ContacAddress implements BaseColumns{
        public static final String TABLE_NAME = "contact_address";
        public static final String COLUMN_COIN_TYPE = "coin_type";
        public static final String COLUMN_CONTACT_ID = "contact_id";
        public static final String COLUMN_ADDRESS = "address";
    }

}
