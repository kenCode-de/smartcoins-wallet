package de.bitshares_munich.database;

import android.provider.BaseColumns;

/**
 * Created by nelson on 12/13/16.
 */

public class SCWallDatabaseContract {

    private SCWallDatabaseContract(){}

    public static class BaseTable implements BaseColumns {
        public static final String COLUMN_CREATION_DATE = "creation_date";
    }

    public static class AssetsTable implements BaseColumns {
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
        public static final String COLUMN_BLOCK_NUM = "block_num";
        public static final String COLUMN_EQUIVALENT_VALUE_ASSET_SYMBOL = "equivalent_value_asset_id";
        public static final String COLUMN_EQUIVALENT_VALUE = "equivalent_value";
    }
}
