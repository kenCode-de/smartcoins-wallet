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
import com.luminiasoft.bitshares.models.AccountProperties;
import com.luminiasoft.bitshares.models.BlockHeader;
import com.luminiasoft.bitshares.models.HistoricalTransfer;
import com.luminiasoft.bitshares.objects.Memo;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;

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
    public int putTransactions(List<HistoricalTransferEntry> transactions){
        long before = System.currentTimeMillis();
        int count = 0;
        ContentValues contentValues;
        for(int i = 0; i < transactions.size(); i++){
            contentValues = new ContentValues();
            HistoricalTransferEntry transferEntry = transactions.get(i);
            HistoricalTransfer historicalTransfer = transferEntry.getHistoricalTransfer();
            TransferOperation operation = historicalTransfer.getOperation();
            if(operation == null) continue;

            contentValues.put(SCWallDatabaseContract.Transfers.COLUMN_ID, historicalTransfer.getId());
            contentValues.put(SCWallDatabaseContract.Transfers.COLUMN_TIMESTAMP, transferEntry.getTimestamp());
            contentValues.put(SCWallDatabaseContract.Transfers.COLUMN_FEE_AMOUNT, operation.getFee().getAmount().longValue());
            contentValues.put(SCWallDatabaseContract.Transfers.COLUMN_FEE_ASSET_ID, operation.getFee().getAsset().getObjectId());
            contentValues.put(SCWallDatabaseContract.Transfers.COLUMN_FROM, operation.getFrom().getObjectId());
            contentValues.put(SCWallDatabaseContract.Transfers.COLUMN_TO, operation.getTo().getObjectId());
            contentValues.put(SCWallDatabaseContract.Transfers.COLUMN_TRANSFER_AMOUNT, operation.getTransferAmount().getAmount().longValue());
            contentValues.put(SCWallDatabaseContract.Transfers.COLUMN_TRANSFER_ASSET_ID, operation.getTransferAmount().getAsset().getObjectId());
            contentValues.put(SCWallDatabaseContract.Transfers.COLUMN_BLOCK_NUM, historicalTransfer.getBlockNum());

            Memo memo = operation.getMemo();
            if(!memo.getPlaintextMessage().equals("")){
                Log.d(TAG,"Memo has plaintext message");
                contentValues.put(SCWallDatabaseContract.Transfers.COLUMN_MEMO_FROM, memo.getSource().toString());
                contentValues.put(SCWallDatabaseContract.Transfers.COLUMN_MEMO_TO, memo.getSource().toString());
                contentValues.put(SCWallDatabaseContract.Transfers.COLUMN_MEMO_MESSAGE, memo.getPlaintextMessage());
            }else{
                Log.i(TAG,"Memo has no message");
            }
            try{
                long id = db.insertOrThrow(SCWallDatabaseContract.Transfers.TABLE_NAME, null, contentValues);
                Log.d(TAG, "Inserted transfer in database with id: "+id);
                count++;
            }catch (SQLException e){
                //Ignoring exception, usually throwed becase the UNIQUE constraint failed.
            }
        }
        long after = System.currentTimeMillis();
        Log.d(TAG, String.format("putTransactions took %d ms with %d transactions", (after - before), transactions.size()));
        return count;
    }

    /**
     * Retrieves the list of historical transfers.
     * @return: The list of historical transfer transactions.
     */
    public List<HistoricalTransferEntry> getTransactions(UserAccount userAccount){
        long before = System.currentTimeMillis();
        HashMap<String, String> userMap = this.getUserMap();
        HashMap<String, Asset> assetMap = this.getAssetMap();

        String tableName = SCWallDatabaseContract.Transfers.TABLE_NAME;
        String orderBy = SCWallDatabaseContract.Transfers.COLUMN_BLOCK_NUM + " DESC";
        String selection = SCWallDatabaseContract.Transfers.COLUMN_FROM + " = ? OR " + SCWallDatabaseContract.Transfers.COLUMN_TO + " = ?";
        String[] selectionArgs = { userAccount.getObjectId(), userAccount.getObjectId() };
        Cursor cursor = db.query(tableName, null, selection, selectionArgs, null, null, orderBy, null);
        ArrayList<HistoricalTransferEntry> transfers = new ArrayList<>();
        if(cursor.moveToFirst()){
            do{
                HistoricalTransferEntry transferEntry = new HistoricalTransferEntry();
                HistoricalTransfer historicalTransfer = new HistoricalTransfer();

                // Getting origin and destination user account ids
                String fromId = cursor.getString(cursor.getColumnIndex(SCWallDatabaseContract.Transfers.COLUMN_FROM));
                String toId = cursor.getString(cursor.getColumnIndex(SCWallDatabaseContract.Transfers.COLUMN_TO));

                // Building UserAccount instances
                UserAccount from = new UserAccount(fromId, userMap.get(fromId));
                UserAccount to = new UserAccount(toId, userMap.get(toId));

                String transferAssetId = cursor.getString(cursor.getColumnIndex(SCWallDatabaseContract.Transfers.COLUMN_TRANSFER_ASSET_ID));
                String feeAssetId = cursor.getString(cursor.getColumnIndex(SCWallDatabaseContract.Transfers.COLUMN_FEE_ASSET_ID));

                // Transfer and fee assets
                Asset transferAsset = assetMap.get(transferAssetId);
                Asset feeAsset = assetMap.get(feeAssetId);

                // Transfer and fee amounts
                AssetAmount tranferAmount = new AssetAmount(UnsignedLong.valueOf(cursor.getLong(cursor.getColumnIndex(SCWallDatabaseContract.Transfers.COLUMN_TRANSFER_AMOUNT))), transferAsset);
                AssetAmount feeAmount = new AssetAmount(UnsignedLong.valueOf(cursor.getLong(cursor.getColumnIndex(SCWallDatabaseContract.Transfers.COLUMN_FEE_AMOUNT))), feeAsset);

                // Building a TransferOperation
                TransferOperation transferOperation = new TransferOperation(from, to, tranferAmount, feeAmount);

                // Adding other historical transfer data
                historicalTransfer.setId(cursor.getString(cursor.getColumnIndex(SCWallDatabaseContract.Transfers.COLUMN_ID)));
                historicalTransfer.setOperation(transferOperation);
                historicalTransfer.setBlockNum(cursor.getInt(cursor.getColumnIndex(SCWallDatabaseContract.Transfers.COLUMN_BLOCK_NUM)));

                transferEntry.setHistoricalTransfer(historicalTransfer);
                transferEntry.setTimestamp(cursor.getLong(cursor.getColumnIndex(SCWallDatabaseContract.Transfers.COLUMN_TIMESTAMP)));

                // Adding historical transfer entry to array
                transfers.add(transferEntry);
            }while(cursor.moveToNext());
        }else{
            Log.w(TAG, "No historical transactions");
        }
        cursor.close();
        long after = System.currentTimeMillis();
        Log.d(TAG, String.format("getTransactions took %d ms with %d transactions", (after - before), transfers.size()));
        return transfers;
    }

    /**
     * Making a query to fetch all unknown account names. That would be missing entries in the
     * user_accounts table.
     * @return: List of all accounts with missing names.
     */
    public List<UserAccount> getMissingAccountNames(){
        String sql = "SELECT DISTINCT %s FROM %s WHERE %s NOT IN (SELECT %s FROM %s)";

        String firstReplacedSql = String.format(sql,
                SCWallDatabaseContract.Transfers.COLUMN_TO,
                SCWallDatabaseContract.Transfers.TABLE_NAME,
                SCWallDatabaseContract.Transfers.COLUMN_TO,
                SCWallDatabaseContract.UserAccounts.COLUMN_ID,
                SCWallDatabaseContract.UserAccounts.TABLE_NAME);

        String secondReplacedSql = String.format(sql,
                SCWallDatabaseContract.Transfers.COLUMN_FROM,
                SCWallDatabaseContract.Transfers.TABLE_NAME,
                SCWallDatabaseContract.Transfers.COLUMN_FROM,
                SCWallDatabaseContract.UserAccounts.COLUMN_ID,
                SCWallDatabaseContract.UserAccounts.TABLE_NAME);


        String[] firstSelectionArgs = {
                SCWallDatabaseContract.Transfers.COLUMN_FROM,
                SCWallDatabaseContract.Transfers.COLUMN_FROM
        };

        String[] secondSelectionArgs = {
                SCWallDatabaseContract.Transfers.COLUMN_TO,
                SCWallDatabaseContract.Transfers.COLUMN_TO
        };

        Cursor firstCursor = db.rawQuery(firstReplacedSql, null);
        Cursor secondCursor = db.rawQuery(secondReplacedSql, null);

        ArrayList<UserAccount> accounts = new ArrayList<>();
        if(firstCursor.moveToFirst()){
            do{
                accounts.add(new UserAccount(firstCursor.getString(0)));
            }while(firstCursor.moveToNext());
        }

        if(secondCursor.moveToFirst()){
            do{
                accounts.add(new UserAccount(secondCursor.getString(0)));
            }while(secondCursor.moveToNext());
        }

        firstCursor.close();
        secondCursor.close();
        return accounts;
    }

    /**
     * @return: A HashMap connecting account ids to account names.
     */
    public HashMap<String, String> getUserMap(){
        HashMap<String, String> userMap = new HashMap<>();
        String[] columns = { SCWallDatabaseContract.UserAccounts.COLUMN_ID, SCWallDatabaseContract.UserAccounts.COLUMN_NAME };
        Cursor cursor = db.query(true, SCWallDatabaseContract.UserAccounts.TABLE_NAME, columns, null, null, null, null, null, null);
        if(cursor.moveToFirst()){
            do{
                userMap.put(cursor.getString(0), cursor.getString(1));
            }while(cursor.moveToNext());
        }
        cursor.close();
        return userMap;
    }

    /**
     * @return: A hashmap connecting asset ids to asset symbols.
     */
    public HashMap<String, Asset> getAssetMap(){
        HashMap<String, Asset> assetMap = new HashMap<>();
        String[] columns = {
                SCWallDatabaseContract.Assets.COLUMN_ID,
                SCWallDatabaseContract.Assets.COLUMN_SYMBOL,
                SCWallDatabaseContract.Assets.COLUMN_PRECISION
        };
        Cursor cursor = db.query(true, SCWallDatabaseContract.Assets.TABLE_NAME, columns, null, null, null, null, null, null);
        if(cursor.moveToFirst()){
            do{
                String id = cursor.getString(0);
                String symbol = cursor.getString(1);
                int precision = cursor.getInt(2);
                assetMap.put(cursor.getString(0), new Asset(id, symbol, precision));
            }while(cursor.moveToNext());
        }
        cursor.close();
        return assetMap;
    }

    /**
     * Returns all missing asset references from the transfers table.
     * @return: List of Asset instances.
     */
    public List<Asset> getMissingAssets(){
        String sql = "SELECT DISTINCT %s from %s where %s not in (select %s from %s)";
        String finalSql = String.format(sql,
                SCWallDatabaseContract.Transfers.COLUMN_TRANSFER_ASSET_ID,
                SCWallDatabaseContract.Transfers.TABLE_NAME,
                SCWallDatabaseContract.Transfers.COLUMN_TRANSFER_ASSET_ID,
                SCWallDatabaseContract.Assets.COLUMN_ID,
                SCWallDatabaseContract.Assets.TABLE_NAME);

        ArrayList<Asset> missingAssets = new ArrayList<>();
        Cursor cursor = db.rawQuery(finalSql, null);
        if(cursor.moveToFirst()){
            do{
                missingAssets.add(new Asset(cursor.getString(0)));
            }while(cursor.moveToNext());
        }

        cursor.close();
        return missingAssets;
    }

    /**
     * Stores a list of assets
     * @param assets: Assets to store
     */
    public int putAssets(List<Asset> assets){
        ContentValues contentValues = new ContentValues();
        int count = 0;
        for(Asset asset : assets){
            contentValues.put(SCWallDatabaseContract.Assets.COLUMN_ID, asset.getObjectId());
            contentValues.put(SCWallDatabaseContract.Assets.COLUMN_PRECISION, asset.getPrecision());
            contentValues.put(SCWallDatabaseContract.Assets.COLUMN_SYMBOL, asset.getSymbol());
            contentValues.put(SCWallDatabaseContract.Assets.COLUMN_ISSUER, asset.getIssuer());

            try {
                db.insertOrThrow(SCWallDatabaseContract.Assets.TABLE_NAME, null, contentValues);
                count++;
            } catch(SQLException e){

            }
        }
        return count;
    }

    /**
     * This method is used to obtain the list of transfer operations stored in the database
     * with the date and time information missing.
     * @return: A list of block numbers.
     */
    public LinkedList<Long> getMissingTransferTimes(){
        LinkedList<Long> missingTimes = new LinkedList<>();
        String table = SCWallDatabaseContract.Transfers.TABLE_NAME;
        String[] columns = { SCWallDatabaseContract.Transfers.COLUMN_BLOCK_NUM };
        String selection = SCWallDatabaseContract.Transfers.COLUMN_TIMESTAMP + "= ?";
        String[] selectionArgs = {"0"};
        Cursor cursor = db.query(table, columns, selection, selectionArgs, null, null, null, null);
        if(cursor.moveToFirst()){
            do{
                missingTimes.add(new Long(cursor.getLong(0)));
            }while(cursor.moveToNext());
        }
        cursor.close();
        Log.d(TAG, String.format("Got %d missing times", missingTimes.size()));
        return missingTimes;
    }

    /**
     * Sets a missing block time information.
     * @param blockHeader: The block header data of this transaction.
     * @param blockNum: The block number, which is not included in the block header
     *                and has to be passed separately.
     * @return: True if there was one transfer entry being updated, false otherwise.
     */
    public boolean setBlockTime(BlockHeader blockHeader, long blockNum){
        boolean updated = false;
        ContentValues values = new ContentValues();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        try {
            Date date = dateFormat.parse(blockHeader.timestamp);
            values.put(SCWallDatabaseContract.Transfers.COLUMN_TIMESTAMP, date.getTime() / 1000);

            String table = SCWallDatabaseContract.Transfers.TABLE_NAME;
            String whereClause = SCWallDatabaseContract.Transfers.COLUMN_BLOCK_NUM + "=?";
            String[] whereArgs = { String.format("%d", blockNum) };
            int count = db.update(table, values, whereClause, whereArgs);
            if(count == 1)
                updated = true;
        } catch (ParseException e) {
            Log.e(TAG, "ParseException. Msg: "+e.getMessage());
        }
        return updated;
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

    public int putUserAccounts(List<AccountProperties> accountProperties){
        ContentValues contentValues = new ContentValues();
        int count = 0;
        for(AccountProperties properties : accountProperties){
            contentValues.put(SCWallDatabaseContract.UserAccounts.COLUMN_ID, properties.id);
            contentValues.put(SCWallDatabaseContract.UserAccounts.COLUMN_NAME, properties.name);

            try {
                db.insertOrThrow(SCWallDatabaseContract.UserAccounts.TABLE_NAME, null, contentValues);
                count++;
            }catch(SQLException e){

            }
        }
        return count;
    }

    /**
     * DEBUG ONLY
     */
    public void clearTimestamps(){
        ContentValues values = new ContentValues();
        values.put(SCWallDatabaseContract.Transfers.COLUMN_TIMESTAMP, 0);
        int count = db.update(SCWallDatabaseContract.Transfers.TABLE_NAME, values, null, null);
        Log.d(TAG, String.format("%d timestamps where deleted", count));
    }
}
