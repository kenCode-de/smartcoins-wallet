package de.bitshares_munich.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.google.common.primitives.UnsignedLong;
import com.luminiasoft.bitshares.Asset;
import com.luminiasoft.bitshares.AssetAmount;
import com.luminiasoft.bitshares.TransferOperation;
import com.luminiasoft.bitshares.UserAccount;
import com.luminiasoft.bitshares.models.HistoricalTransfer;

import java.util.ArrayList;
import java.util.List;

import de.bitshares_munich.models.TransactionDetails;

/**
 * Database wrapper class, providing access to the underlying database.
 *
 * Created by nelson on 12/13/16.
 */
public class SCWallDatabase {
    private String TAG = this.getClass().getName();
    private SCWallSQLiteOpenHelper dbHelper;
    private SQLiteDatabase db;

    public SCWallDatabase(Context context){
        dbHelper = new SCWallSQLiteOpenHelper(context);
        db = dbHelper.getWritableDatabase();
    }

    public void close(){
        db.close();
    }

    /**
     * Stores a list of historical transfer transactions as obtained from
     * the full node into the database
     * @param transactions: List of historical transfer transactions.
     */
    public void putTransactions(List<HistoricalTransfer> transactions){
        long before = System.currentTimeMillis();
        ContentValues contentValues;
        for(int i = 0; i < transactions.size(); i++){
            contentValues = new ContentValues();
            HistoricalTransfer transfer = transactions.get(i);
            TransferOperation operation = transfer.getOperation();
            if(operation == null) continue;

            contentValues.put(SCWallDatabaseContract.Transfers.COLUMN_ID, transfer.getId());
            contentValues.put(SCWallDatabaseContract.Transfers.COLUMN_TIMESTAMP, 0); //TODO: Obtain the transaction timestamp from the block number later
            contentValues.put(SCWallDatabaseContract.Transfers.COLUMN_FEE_AMOUNT, operation.getFee().getAmount().longValue());
            contentValues.put(SCWallDatabaseContract.Transfers.COLUMN_FEE_ASSET_ID, operation.getFee().getAsset().getObjectId());
            contentValues.put(SCWallDatabaseContract.Transfers.COLUMN_FROM, operation.getFrom().getObjectId());
            contentValues.put(SCWallDatabaseContract.Transfers.COLUMN_TO, operation.getTo().getObjectId());
            contentValues.put(SCWallDatabaseContract.Transfers.COLUMN_TRANSFER_AMOUNT, operation.getTransferAmount().getAmount().longValue());
            contentValues.put(SCWallDatabaseContract.Transfers.COLUMN_TRANSFER_ASSET_ID, operation.getTransferAmount().getAsset().getObjectId());
            contentValues.put(SCWallDatabaseContract.Transfers.COLUMN_BLOCK_NUM, transfer.getBlockNum());

            //TODO: Add equivalent value data
//            contentValues.put(SCWallDatabaseContract.Transfers.COLUMN_EQUIVALENT_VALUE_ASSET_SYMBOL, "");
//            contentValues.put(SCWallDatabaseContract.Transfers.COLUMN_EQUIVALENT_VALUE, 0);

            try{
                long id = db.insertOrThrow(SCWallDatabaseContract.Transfers.TABLE_NAME, null, contentValues);
                Log.d(TAG, "Inserted transfer in database with id: "+id);
            }catch (SQLException e){
                //Ignoring exception, usually throwed becase the UNIQUE constraint failed.
            }
        }
        long after = System.currentTimeMillis();
        Log.d(TAG, String.format("putTransactions took %d ms with %d transactions", (after - before), transactions.size()));
    }

    /**
     * Retrieves the list of historical transfers.
     * @return: The list of historical transfer transactions.
     */
    public List<HistoricalTransfer> getTransactions(){
        long before = System.currentTimeMillis();
        Cursor cursor = db.query(SCWallDatabaseContract.Transfers.TABLE_NAME, null, null, null, null, null, SCWallDatabaseContract.Transfers.COLUMN_BLOCK_NUM, null);
        ArrayList<HistoricalTransfer> transfers = new ArrayList<>();
        cursor.moveToFirst();
        do{
            HistoricalTransfer historicalTransfer = new HistoricalTransfer();

            // Building a TransferOperation
            UserAccount from = new UserAccount(cursor.getString(cursor.getColumnIndex(SCWallDatabaseContract.Transfers.COLUMN_FROM)));
            UserAccount to = new UserAccount(cursor.getString(cursor.getColumnIndex(SCWallDatabaseContract.Transfers.COLUMN_TO)));
            Asset transferAsset = new Asset(cursor.getString(cursor.getColumnIndex(SCWallDatabaseContract.Transfers.COLUMN_TRANSFER_ASSET_ID)));
            Asset feeAsset = new Asset(cursor.getString(cursor.getColumnIndex(SCWallDatabaseContract.Transfers.COLUMN_FEE_ASSET_ID)));
            AssetAmount tranferAmount = new AssetAmount(UnsignedLong.valueOf(cursor.getLong(cursor.getColumnIndex(SCWallDatabaseContract.Transfers.COLUMN_TRANSFER_AMOUNT))), transferAsset);
            AssetAmount feeAmount = new AssetAmount(UnsignedLong.valueOf(cursor.getLong(cursor.getColumnIndex(SCWallDatabaseContract.Transfers.COLUMN_FEE_AMOUNT))), feeAsset);
            TransferOperation transferOperation = new TransferOperation(from, to, tranferAmount, feeAmount);

            // Adding other historical transfer data
            historicalTransfer.setId(cursor.getString(cursor.getColumnIndex(SCWallDatabaseContract.Transfers.COLUMN_ID)));
            historicalTransfer.setOperation(transferOperation);
            historicalTransfer.setBlockNum(cursor.getInt(cursor.getColumnIndex(SCWallDatabaseContract.Transfers.COLUMN_BLOCK_NUM)));

            // Adding historical transfer to array
            transfers.add(historicalTransfer);
        }while(cursor.moveToNext());
        cursor.close();
        long after = System.currentTimeMillis();
        Log.d(TAG, String.format("getTransactions took %d ms with %d transactions", (after - before), transfers.size()));
        return transfers;
    }

    /**
     * Stores a list of assets
     * @param assets: Assets to store
     */
    public void putAssets(List<Asset> assets){
        ContentValues contentValues = new ContentValues();
        for(Asset asset : assets){
            contentValues.put(SCWallDatabaseContract.AssetsTable.COLUMN_ID, asset.getObjectId());
            contentValues.put(SCWallDatabaseContract.AssetsTable.COLUMN_PRECISION, asset.getPrecision());
            contentValues.put(SCWallDatabaseContract.AssetsTable.COLUMN_SYMBOL, asset.getSymbol());
            contentValues.put(SCWallDatabaseContract.AssetsTable.COLUMN_ISSUER, asset.getIssuer());

            db.insertWithOnConflict(SCWallDatabaseContract.AssetsTable.TABLE_NAME, null, contentValues, SQLiteDatabase.CONFLICT_IGNORE);
        }
    }

    /**
     * Retrieves the full list of assets.
     * @return
     */
    public List<Asset> getAssets(){

        return null;
    }

    /**
     * Legacy method introduced in order to support the current infraestructure.
     * @return
     */
    public List<TransactionDetails> getTransactionDetails(){
        return null;
    }
}
