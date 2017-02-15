package de.bitshares_munich.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


/**
 * Created by nelson on 12/13/16.
 */
public class SCWallSQLiteOpenHelper extends SQLiteOpenHelper {
    private final String TAG = this.getClass().getName();
    public static final int DATABASE_VERSION = 6;
    public static final String DATABASE_NAME = "scwall.db";

    private static final String TYPE_TEXT = " TEXT";
    private static final String TYPE_INTEGER = " INTEGER";
    private static final String TYPE_REAL = " REAL";

    private static final String SQL_CREATE_ASSETS_TABLE = "CREATE TABLE " + SCWallDatabaseContract.Assets.TABLE_NAME + " (" +
            SCWallDatabaseContract.Assets.COLUMN_ID + " TEXT PRIMARY KEY, " +
            SCWallDatabaseContract.Assets.COLUMN_SYMBOL + TYPE_TEXT + ", " +
            SCWallDatabaseContract.Assets.COLUMN_PRECISION + TYPE_INTEGER + ", " +
            SCWallDatabaseContract.Assets.COLUMN_ISSUER + TYPE_TEXT + ", " +
            SCWallDatabaseContract.Assets.COLUMN_DESCRIPTION + TYPE_TEXT + ", " +
            SCWallDatabaseContract.Assets.COLUMN_MAX_SUPPLY + TYPE_INTEGER + ")";

    private static final String SQL_CREATE_TRANSFERS_TABLE = "CREATE TABLE " + SCWallDatabaseContract.Transfers.TABLE_NAME + " (" +
            SCWallDatabaseContract.Transfers.COLUMN_ID + " TEXT PRIMARY KEY, " +
            SCWallDatabaseContract.Transfers.COLUMN_TIMESTAMP + TYPE_INTEGER + ", " +
            SCWallDatabaseContract.Transfers.COLUMN_FEE_AMOUNT + TYPE_INTEGER + ", " +
            SCWallDatabaseContract.Transfers.COLUMN_FEE_ASSET_ID + TYPE_TEXT + ", " +
            SCWallDatabaseContract.Transfers.COLUMN_FROM + TYPE_TEXT + ", " +
            SCWallDatabaseContract.Transfers.COLUMN_TO + TYPE_TEXT + ", " +
            SCWallDatabaseContract.Transfers.COLUMN_TRANSFER_AMOUNT + TYPE_INTEGER + ", " +
            SCWallDatabaseContract.Transfers.COLUMN_TRANSFER_ASSET_ID + TYPE_TEXT + " DEFAULT '', " +
            SCWallDatabaseContract.Transfers.COLUMN_MEMO_MESSAGE + TYPE_TEXT + ", " +
            SCWallDatabaseContract.Transfers.COLUMN_MEMO_FROM + TYPE_TEXT + ", " +
            SCWallDatabaseContract.Transfers.COLUMN_MEMO_TO + TYPE_TEXT + ", " +
            SCWallDatabaseContract.Transfers.COLUMN_BLOCK_NUM + TYPE_INTEGER + ", " +
            SCWallDatabaseContract.Transfers.COLUMN_EQUIVALENT_VALUE_ASSET_ID + TYPE_TEXT + ", " +
            SCWallDatabaseContract.Transfers.COLUMN_EQUIVALENT_VALUE + TYPE_INTEGER + ", " +
            "FOREIGN KEY (" + SCWallDatabaseContract.Transfers.COLUMN_FEE_ASSET_ID + ") REFERENCES " +
            SCWallDatabaseContract.Assets.TABLE_NAME + "(" + SCWallDatabaseContract.Assets.COLUMN_ID +"), " +
            "FOREIGN KEY (" + SCWallDatabaseContract.Transfers.COLUMN_TRANSFER_ASSET_ID + ") REFERENCES " +
            SCWallDatabaseContract.Assets.TABLE_NAME + "(" + SCWallDatabaseContract.Assets.COLUMN_ID + "), " +
            "FOREIGN KEY (" + SCWallDatabaseContract.Transfers.COLUMN_EQUIVALENT_VALUE_ASSET_ID + ") REFERENCES "+
            SCWallDatabaseContract.Assets.TABLE_NAME + "(" + SCWallDatabaseContract.Assets.COLUMN_ID + "))";

    private static final String SQL_CREATE_USER_ACCOUNTS_TABLE = "CREATE TABLE " + SCWallDatabaseContract.UserAccounts.TABLE_NAME + "(" +
            SCWallDatabaseContract.UserAccounts.COLUMN_ID + " TEXT PRIMARY KEY, " +
            SCWallDatabaseContract.UserAccounts.COLUMN_NAME + TYPE_TEXT + ")";

    private static final String SQL_CREATE_SEED_TABLE = "CREATE TABLE " + SCWallDatabaseContract.Seeds.TABLE_NAME + " (" +
            SCWallDatabaseContract.Seeds.COLUMN_ID + " TEXT PRIMARY KEY, " +
            SCWallDatabaseContract.Seeds.COLUMN_SEED_TYPE + TYPE_TEXT + ", " +
            SCWallDatabaseContract.Seeds.COLUMN_MNEMONIC + TYPE_TEXT + ", "+
            SCWallDatabaseContract.Seeds.COLUMN_ADDITIONAL + TYPE_TEXT +", " +
            "CONSTRAINT seedContraint UNIQUE ("+SCWallDatabaseContract.Seeds.COLUMN_MNEMONIC+","+SCWallDatabaseContract.Seeds.COLUMN_ADDITIONAL+") "+
            ") ";

    private static final String SQL_CREATE_ACCOUNT_TABLE = "CREATE TABLE " + SCWallDatabaseContract.GeneralAccounts.TABLE_NAME + " (" +
            SCWallDatabaseContract.GeneralAccounts.COLUMN_ID + " TEXT PRIMARY KEY, " +
            SCWallDatabaseContract.GeneralAccounts.COLUMN_NAME + TYPE_TEXT + ", " +
            SCWallDatabaseContract.GeneralAccounts.COLUMN_COIN_TYPE + TYPE_TEXT + ", " +
            SCWallDatabaseContract.GeneralAccounts.COLUMN_ID_SEED + TYPE_TEXT + ", "+
            SCWallDatabaseContract.GeneralAccounts.COLUMN_ACCOUNT_INDEX + TYPE_INTEGER + ", "+
            SCWallDatabaseContract.GeneralAccounts.COLUMN_EXTERNAL_INDEX + TYPE_INTEGER + ", "+
            SCWallDatabaseContract.GeneralAccounts.COLUMN_CHANGE_INDEX + TYPE_INTEGER + ", "+
            " FOREIGN KEY("+SCWallDatabaseContract.GeneralAccounts.COLUMN_ID_SEED+") REFERENCES "+SCWallDatabaseContract.Seeds.TABLE_NAME+"("+SCWallDatabaseContract.Seeds.COLUMN_ID+"),"+
            "CONSTRAINT accountContraint UNIQUE ("+SCWallDatabaseContract.GeneralAccounts.COLUMN_ID_SEED+","+SCWallDatabaseContract.GeneralAccounts.COLUMN_COIN_TYPE +","+SCWallDatabaseContract.GeneralAccounts.COLUMN_ACCOUNT_INDEX+") "+
            ")";

    private static final String SQL_CREATE_GENERAL_ORPHAN_KEY_TABLE = "CREATE TABLE " + SCWallDatabaseContract.GeneralOrphanKeys.TABLE_NAME + " (" +
            SCWallDatabaseContract.GeneralOrphanKeys.COLUMN_ID + " TEXT PRIMARY KEY, " +
            SCWallDatabaseContract.GeneralOrphanKeys.COLUMN_NAME + TYPE_TEXT + ", " +
            SCWallDatabaseContract.GeneralOrphanKeys.COLUMN_COIN_TYPE + TYPE_TEXT + ", " +
            SCWallDatabaseContract.GeneralOrphanKeys.COLUMN_WIF + TYPE_TEXT + ", "+
            "CONSTRAINT generalOprhanContraint UNIQUE ("+SCWallDatabaseContract.GeneralOrphanKeys.COLUMN_WIF+"))";

    private static final String SQL_CREATE_GENERAL_ADDRESS_TABLE = "CREATE TABLE " + SCWallDatabaseContract.GeneralCoinAddress.TABLE_NAME + " (" +
            SCWallDatabaseContract.GeneralCoinAddress.COLUMN_ID + " TEXT PRIMARY KEY, " +
            SCWallDatabaseContract.GeneralCoinAddress.COLUMN_ID_ACCOUNT + TYPE_TEXT + ", " +
            SCWallDatabaseContract.GeneralCoinAddress.COLUMN_INDEX + TYPE_INTEGER + ", " +
            SCWallDatabaseContract.GeneralCoinAddress.COLUMN_IS_CHANGE + TYPE_INTEGER + ", " +
            SCWallDatabaseContract.GeneralCoinAddress.COLUMN_PUBLIC_KEY + TYPE_TEXT + ", " +
            " FOREIGN KEY("+SCWallDatabaseContract.GeneralCoinAddress.COLUMN_ID_ACCOUNT+") REFERENCES "+SCWallDatabaseContract.GeneralAccounts.TABLE_NAME+"("+SCWallDatabaseContract.GeneralAccounts.COLUMN_ID+"),"+
            "CONSTRAINT genAddressContraint UNIQUE ("+SCWallDatabaseContract.GeneralCoinAddress.COLUMN_ID_ACCOUNT+","+SCWallDatabaseContract.GeneralCoinAddress.COLUMN_INDEX+","+SCWallDatabaseContract.GeneralCoinAddress.COLUMN_IS_CHANGE+") "+
            ")";

    private static final String SQL_CREATE_GENERAL_TRANSACTION_TABLE = "CREATE TABLE " + SCWallDatabaseContract.GeneralTransaction.TABLE_NAME + " (" +
            SCWallDatabaseContract.GeneralTransaction.COLUMN_ID + " TEXT PRIMARY KEY, " +
            SCWallDatabaseContract.GeneralTransaction.COLUMN_COIN_TYPE + TYPE_TEXT + ", " +
            SCWallDatabaseContract.GeneralTransaction.COLUMN_TXID + TYPE_TEXT + ", " +
            SCWallDatabaseContract.GeneralTransaction.COLUMN_DATE + TYPE_TEXT + ", " +
            SCWallDatabaseContract.GeneralTransaction.COLUMN_BLOCK + TYPE_INTEGER + ", " +
            SCWallDatabaseContract.GeneralTransaction.COLUMN_CONFIRMS + TYPE_INTEGER + ", " +
            SCWallDatabaseContract.GeneralTransaction.COLUMN_FEE + TYPE_INTEGER + ")";

    private static final String SQL_CREATE_INPUT_TX_TABLE = "CREATE TABLE " + SCWallDatabaseContract.Inputs.TABLE_NAME + " (" +
            SCWallDatabaseContract.Inputs.COLUMN_ID + " TEXT PRIMARY KEY, " +
            SCWallDatabaseContract.Inputs.COLUMN_COIN_TYPE + TYPE_TEXT + ", " +
            SCWallDatabaseContract.Inputs.COLUMN_ID_ADDRESS + TYPE_TEXT + ", " +
            SCWallDatabaseContract.Inputs.COLUMN_ADDRESS_STRING + TYPE_TEXT + ", " +
            SCWallDatabaseContract.Inputs.COLUMN_ID_TRANSACTION + TYPE_TEXT + ", " +
            SCWallDatabaseContract.Inputs.COLUMN_AMOUNT + TYPE_INTEGER + ", " +
            " FOREIGN KEY("+SCWallDatabaseContract.Inputs.COLUMN_ID_ADDRESS+") REFERENCES "+SCWallDatabaseContract.GeneralCoinAddress.TABLE_NAME+"("+SCWallDatabaseContract.GeneralCoinAddress.COLUMN_ID+"),"+
            " FOREIGN KEY("+SCWallDatabaseContract.Inputs.COLUMN_ID_TRANSACTION+") REFERENCES "+SCWallDatabaseContract.GeneralTransaction.TABLE_NAME+"("+SCWallDatabaseContract.GeneralTransaction.COLUMN_ID+"))";

    private static final String SQL_CREATE_OUTPUT_TX_TABLE = "CREATE TABLE " + SCWallDatabaseContract.Outputs.TABLE_NAME + " (" +
            SCWallDatabaseContract.Outputs.COLUMN_ID + " TEXT PRIMARY KEY, " +
            SCWallDatabaseContract.Outputs.COLUMN_COIN_TYPE + TYPE_TEXT + ", " +
            SCWallDatabaseContract.Outputs.COLUMN_ID_ADDRESS + TYPE_TEXT + ", " +
            SCWallDatabaseContract.Outputs.COLUMN_ADDRESS_STRING + TYPE_TEXT + ", " +
            SCWallDatabaseContract.Outputs.COLUMN_ID_TRANSACTION + TYPE_TEXT + ", " +
            SCWallDatabaseContract.Outputs.COLUMN_AMOUNT + TYPE_INTEGER + ", " +
            " FOREIGN KEY("+SCWallDatabaseContract.Outputs.COLUMN_ID_ADDRESS+") REFERENCES "+SCWallDatabaseContract.GeneralCoinAddress.TABLE_NAME+"("+SCWallDatabaseContract.GeneralCoinAddress.COLUMN_ID+"),"+
            " FOREIGN KEY("+SCWallDatabaseContract.Outputs.COLUMN_ID_TRANSACTION+") REFERENCES "+SCWallDatabaseContract.GeneralTransaction.TABLE_NAME+"("+SCWallDatabaseContract.GeneralTransaction.COLUMN_ID+"))";


    public SCWallSQLiteOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "onCreate");
        Log.d(TAG, SQL_CREATE_ASSETS_TABLE);
        Log.d(TAG, SQL_CREATE_TRANSFERS_TABLE);
        Log.d(TAG, SQL_CREATE_USER_ACCOUNTS_TABLE);

        db.execSQL(SQL_CREATE_ASSETS_TABLE);
        db.execSQL(SQL_CREATE_TRANSFERS_TABLE);
        db.execSQL(SQL_CREATE_USER_ACCOUNTS_TABLE);

        Log.d(TAG, SQL_CREATE_SEED_TABLE);
        Log.d(TAG, SQL_CREATE_ACCOUNT_TABLE);
        Log.d(TAG, SQL_CREATE_GENERAL_ORPHAN_KEY_TABLE);
        Log.d(TAG, SQL_CREATE_GENERAL_ADDRESS_TABLE);
        Log.d(TAG, SQL_CREATE_GENERAL_TRANSACTION_TABLE);
        Log.d(TAG, SQL_CREATE_INPUT_TX_TABLE);
        Log.d(TAG, SQL_CREATE_OUTPUT_TX_TABLE);

        db.execSQL(SQL_CREATE_SEED_TABLE);
        db.execSQL(SQL_CREATE_ACCOUNT_TABLE);
        db.execSQL(SQL_CREATE_GENERAL_ORPHAN_KEY_TABLE);
        db.execSQL(SQL_CREATE_GENERAL_ADDRESS_TABLE);
        db.execSQL(SQL_CREATE_GENERAL_TRANSACTION_TABLE);
        db.execSQL(SQL_CREATE_INPUT_TX_TABLE);
        db.execSQL(SQL_CREATE_OUTPUT_TX_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "onUpgrade");
        db.execSQL("DROP TABLE IF EXISTS " + SCWallDatabaseContract.Transfers.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + SCWallDatabaseContract.UserAccounts.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + SCWallDatabaseContract.Assets.TABLE_NAME);

        db.execSQL("DROP TABLE IF EXISTS " + SCWallDatabaseContract.Inputs.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + SCWallDatabaseContract.Outputs.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + SCWallDatabaseContract.GeneralTransaction.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + SCWallDatabaseContract.GeneralCoinAddress.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + SCWallDatabaseContract.GeneralOrphanKeys.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + SCWallDatabaseContract.GeneralAccounts.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + SCWallDatabaseContract.Seeds.TABLE_NAME);

        onCreate(db);
    }
}
