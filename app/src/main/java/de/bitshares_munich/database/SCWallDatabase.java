package de.bitshares_munich.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.google.common.primitives.UnsignedLong;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;
import java.util.UUID;

import de.bitshares_munich.models.TransactionDetails;
import de.bitsharesmunich.cryptocoincore.base.AccountSeed;
import de.bitsharesmunich.cryptocoincore.base.Coin;
import de.bitsharesmunich.cryptocoincore.base.CryptoCoinFactory;
import de.bitsharesmunich.cryptocoincore.base.GeneralCoinAccount;
import de.bitsharesmunich.cryptocoincore.base.SeedType;
import de.bitsharesmunich.cryptocoincore.base.seed.BIP39;
import de.bitsharesmunich.cryptocoincore.base.seed.Brainkey;
import de.bitsharesmunich.graphenej.Asset;
import de.bitsharesmunich.graphenej.AssetAmount;
import de.bitsharesmunich.graphenej.TransferOperation;
import de.bitsharesmunich.graphenej.UserAccount;
import de.bitsharesmunich.graphenej.models.AccountProperties;
import de.bitsharesmunich.graphenej.models.BlockHeader;
import de.bitsharesmunich.graphenej.models.HistoricalTransfer;
import de.bitsharesmunich.graphenej.objects.Memo;

/**
 * Database wrapper class, providing access to the underlying database.
 *
 * Created by nelson on 12/13/16.
 */
public class SCWallDatabase {
    private String TAG = this.getClass().getName();

    /**
     * Constant used to specify an unlimited amount of transactions for the
     * second argument of the getTransactions method.
     */
    public static final int UNLIMITED_TRANSACTIONS = -1;

    /**
     * The default number of transactions to load in the transaction list
     */
    public static final int DEFAULT_TRANSACTION_BATCH_SIZE = 50;

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

            if(transferEntry.getEquivalentValue() != null){
                AssetAmount assetAmount = transferEntry.getEquivalentValue();
                contentValues.put(SCWallDatabaseContract.Transfers.COLUMN_EQUIVALENT_VALUE_ASSET_ID, assetAmount.getAsset().getObjectId());
                contentValues.put(SCWallDatabaseContract.Transfers.COLUMN_EQUIVALENT_VALUE, assetAmount.getAmount().longValue());
            }

            Memo memo = operation.getMemo();
            if(!memo.getPlaintextMessage().equals("")){
                contentValues.put(SCWallDatabaseContract.Transfers.COLUMN_MEMO_FROM, memo.getSource().toString());
                contentValues.put(SCWallDatabaseContract.Transfers.COLUMN_MEMO_TO, memo.getSource().toString());
                contentValues.put(SCWallDatabaseContract.Transfers.COLUMN_MEMO_MESSAGE, memo.getPlaintextMessage());
            }
            try{
                db.insertOrThrow(SCWallDatabaseContract.Transfers.TABLE_NAME, null, contentValues);
                count++;
            }catch (SQLException e){
                //Ignoring exception, usually throwed becase the UNIQUE constraint failed.
            }
        }
        Log.d(TAG,String.format("Inserved %d transactions in database", count));
        return count;
    }

    public int updateEquivalentValue(HistoricalTransferEntry transfer){
        String table = SCWallDatabaseContract.Transfers.TABLE_NAME;
        String whereClause = SCWallDatabaseContract.Transfers.COLUMN_ID + "=?";
        String[] whereArgs = new String[]{ transfer.getHistoricalTransfer().getId() };

        ContentValues contentValues = new ContentValues();
        Log.d(TAG,String.format("Updating eq value. asset id: %s, amount: %d", transfer.getEquivalentValue().getAsset().getObjectId(), transfer.getEquivalentValue().getAmount().longValue()));
        contentValues.put(SCWallDatabaseContract.Transfers.COLUMN_EQUIVALENT_VALUE, transfer.getEquivalentValue().getAmount().longValue());
        contentValues.put(SCWallDatabaseContract.Transfers.COLUMN_EQUIVALENT_VALUE_ASSET_ID, transfer.getEquivalentValue().getAsset().getObjectId());

        int updated = db.update(table, contentValues, whereClause, whereArgs);
        return updated;
    }

    /**
     * Retrieves the list of historical transfers.
     * @param userAccount: The user account whose transactions we're interested in.
     * @param max: The maximum number of transactions to fetch, if the value is <= 0, then the
     *           query will put no limits on the number of returned values.
     * @return: The list of historical transfer transactions.
     */
    public List<HistoricalTransferEntry> getTransactions(UserAccount userAccount, int max){
        long before = System.currentTimeMillis();
        HashMap<String, String> userMap = this.getUserMap();
        HashMap<String, Asset> assetMap = this.getAssetMap();

        String tableName = SCWallDatabaseContract.Transfers.TABLE_NAME;
        String orderBy = SCWallDatabaseContract.Transfers.COLUMN_BLOCK_NUM + " DESC";
        String selection = SCWallDatabaseContract.Transfers.COLUMN_FROM + " = ? OR " + SCWallDatabaseContract.Transfers.COLUMN_TO + " = ?";
        String[] selectionArgs = { userAccount.getObjectId(), userAccount.getObjectId() };
        String limit = max > 0 ? String.format("%d", max) : null;
        Cursor cursor = db.query(tableName, null, selection, selectionArgs, null, null, orderBy, limit);
        ArrayList<HistoricalTransferEntry> transfers = new ArrayList<>();
        if(cursor.moveToFirst()){
            do{
                HistoricalTransferEntry transferEntry = new HistoricalTransferEntry();
                HistoricalTransfer historicalTransfer = new HistoricalTransfer();

                // Getting origin and destination user account ids
                String fromId = cursor.getString(cursor.getColumnIndex(SCWallDatabaseContract.Transfers.COLUMN_FROM));
                String toId = cursor.getString(cursor.getColumnIndex(SCWallDatabaseContract.Transfers.COLUMN_TO));

                // Skipping transfer if we are missing users information
                if(userMap.get(fromId) == null || userMap.get(toId) == null){
                    cursor.moveToNext();
                    continue;
                }

                // Skipping transfer if we are missing timestamp information
                long t = cursor.getLong(cursor.getColumnIndex(SCWallDatabaseContract.Transfers.COLUMN_TIMESTAMP));
                if(t == 0){
                    cursor.moveToNext();
                    continue;
                }

                // Building UserAccount instances
                UserAccount from = new UserAccount(fromId, userMap.get(fromId));
                UserAccount to = new UserAccount(toId, userMap.get(toId));

                String transferAssetId = cursor.getString(cursor.getColumnIndex(SCWallDatabaseContract.Transfers.COLUMN_TRANSFER_ASSET_ID));
                String feeAssetId = cursor.getString(cursor.getColumnIndex(SCWallDatabaseContract.Transfers.COLUMN_FEE_ASSET_ID));

                // Transfer and fee assets
                Asset transferAsset = assetMap.get(transferAssetId);
                Asset feeAsset = assetMap.get(feeAssetId);

                // Skipping transfer if we are missing transfer and fee asset information
                if(transferAsset == null || feeAsset == null){
                    cursor.moveToNext();
                    continue;
                }

                // Transfer and fee amounts
                AssetAmount transferAmount = new AssetAmount(UnsignedLong.valueOf(cursor.getLong(cursor.getColumnIndex(SCWallDatabaseContract.Transfers.COLUMN_TRANSFER_AMOUNT))), transferAsset);
                AssetAmount feeAmount = new AssetAmount(UnsignedLong.valueOf(cursor.getLong(cursor.getColumnIndex(SCWallDatabaseContract.Transfers.COLUMN_FEE_AMOUNT))), feeAsset);

                // Building a TransferOperation
                TransferOperation transferOperation = new TransferOperation(from, to, transferAmount, feeAmount);

                // Building memo data
                String memoMessage = cursor.getString(cursor.getColumnIndex(SCWallDatabaseContract.Transfers.COLUMN_MEMO_MESSAGE));
                Memo memo = new Memo();
                memo.setPlaintextMessage(memoMessage);
                transferOperation.setMemo(memo);

                // Adding other historical transfer data
                historicalTransfer.setId(cursor.getString(cursor.getColumnIndex(SCWallDatabaseContract.Transfers.COLUMN_ID)));
                historicalTransfer.setOperation(transferOperation);
                historicalTransfer.setBlockNum(cursor.getInt(cursor.getColumnIndex(SCWallDatabaseContract.Transfers.COLUMN_BLOCK_NUM)));

                // Adding the HistoricalTransfer instance
                transferEntry.setHistoricalTransfer(historicalTransfer);

                // Setting the timestamp
                transferEntry.setTimestamp(cursor.getLong(cursor.getColumnIndex(SCWallDatabaseContract.Transfers.COLUMN_TIMESTAMP)));

                // Adding equivalent value data
                String id = cursor.getString(cursor.getColumnIndex(SCWallDatabaseContract.Transfers.COLUMN_EQUIVALENT_VALUE_ASSET_ID));
                long equivalentValue = cursor.getLong(cursor.getColumnIndex(SCWallDatabaseContract.Transfers.COLUMN_EQUIVALENT_VALUE));
                if(id != null){
                    Log.v(TAG,String.format("Eq value asset id: %s, value: %d", id, equivalentValue));
                    String table = SCWallDatabaseContract.Assets.TABLE_NAME;
                    String[] columns = new String[] {
                            SCWallDatabaseContract.Assets.COLUMN_SYMBOL,
                            SCWallDatabaseContract.Assets.COLUMN_PRECISION
                    };
                    String where = SCWallDatabaseContract.Assets.COLUMN_ID + "=?";
                    String[] whereArgs = new String[]{ id };
                    Cursor assetCursor = db.query(true, table, columns, where, whereArgs, null, null, null, null);
                    if(assetCursor.moveToFirst()){
                        String symbol = assetCursor.getString(0);
                        int precision = assetCursor.getInt(1);
                        AssetAmount eqValueAssetAmount = new AssetAmount(UnsignedLong.valueOf(equivalentValue), new Asset(id, symbol, precision));
                        transferEntry.setEquivalentValue(eqValueAssetAmount);
                    }else{
                        Log.w(TAG,"Got empty cursor while trying to fill asset data");
                    }
                    assetCursor.close();
                }else{
                    cursor.moveToNext();
                    continue;
                }

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
     * Gets the total number of recorded transactions from a given user.
     *
     * @param userAccount: User account we're interested in.
     * @return: The total number of transaction records in the database from a given user.
     */
    public int getTransactionCount(UserAccount userAccount){
        String sql = "SELECT COUNT(*) FROM " + SCWallDatabaseContract.Transfers.TABLE_NAME +
                " where " + SCWallDatabaseContract.Transfers.COLUMN_FROM + " = ? OR " + SCWallDatabaseContract.Transfers.COLUMN_TO + " = ?";
        String[] selectionArgs = new String[]{ userAccount.getObjectId(), userAccount.getObjectId() };
        Cursor cursor = db.rawQuery(sql, selectionArgs);
        cursor.moveToFirst();
        int count = cursor.getInt(0);
        cursor.close();
        return count;
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
     * @return: A hashmap connecting asset ids to Asset object instances.
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
    public LinkedList<Long> getMissingTransferTimes(int limitValue){
        LinkedList<Long> missingTimes = new LinkedList<>();
        String table = SCWallDatabaseContract.Transfers.TABLE_NAME;
        String[] columns = { SCWallDatabaseContract.Transfers.COLUMN_BLOCK_NUM };
        String selection = SCWallDatabaseContract.Transfers.COLUMN_TIMESTAMP + "= ?";
        String[] selectionArgs = {"0"};
        String limit = String.format("%d", limitValue);
        String orderBy = SCWallDatabaseContract.Transfers.COLUMN_BLOCK_NUM + " DESC";
        Cursor cursor = db.query(table, columns, selection, selectionArgs, null, null, orderBy, limit);
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
     * Method used to obtain a list of all historical transfers for which we don't have
     * an equivalent value.
     *
     * Every HistoricalTransferEntry object in the list returned is only
     * partially built, with just enough information to fill the 2 missing equivalent values
     * columns, which are the equivalent value asset id, and asset amount.
     *
     * @return: List of all historical transfers lacking an equivalent value.
     */
    public LinkedList<HistoricalTransferEntry> getMissingEquivalentValues(){
        LinkedList<HistoricalTransferEntry> historicalEntries = new LinkedList<>();
        String table = SCWallDatabaseContract.Transfers.TABLE_NAME;
        String[] columns = {
                SCWallDatabaseContract.Transfers.COLUMN_ID,
                SCWallDatabaseContract.Transfers.COLUMN_TIMESTAMP,
                SCWallDatabaseContract.Transfers.COLUMN_TRANSFER_ASSET_ID,
                SCWallDatabaseContract.Transfers.COLUMN_TRANSFER_AMOUNT
        };
        String selection  = SCWallDatabaseContract.Transfers.COLUMN_EQUIVALENT_VALUE_ASSET_ID + " is null and " +
                SCWallDatabaseContract.Transfers.COLUMN_TIMESTAMP + " != 0";
        Log.i(TAG, "Selection: "+selection);
        Cursor cursor = db.query(table, columns, selection, null, null, null, null, null);
        Log.i(TAG, String.format("Got cursor with %d entries", cursor.getCount()));
        if(cursor.moveToFirst()){
            do{
                String historicalTransferId = cursor.getString(0);
                long timestamp = cursor.getLong(1);
                String assetId = cursor.getString(2);
                long amount = cursor.getLong(3);

                TransferOperation operation = new TransferOperation();
                operation.setAmount(new AssetAmount(UnsignedLong.valueOf(amount), new Asset(assetId)));

                HistoricalTransfer transfer = new HistoricalTransfer();
                transfer.setId(historicalTransferId);
                transfer.setOperation(operation);

                HistoricalTransferEntry transferEntry = new HistoricalTransferEntry();
                transferEntry.setHistoricalTransfer(transfer);
                transferEntry.setTimestamp(timestamp);

                historicalEntries.add(transferEntry);
            }while(cursor.moveToNext());
            cursor.close();
            Log.i(TAG, String.format("Got %d transactions with missing equivalent value", historicalEntries.size()));
        }
        return historicalEntries;
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
            if(count > 0) {
                updated = true;
            }else{
                Log.w(TAG,String.format("Failed to update block time. block: %d", blockNum));
            }
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
     * Given an incomplete instance of the UserAccount object, this method performs a query and
     * fills in the missing details.
     *
     * The incomplete object passed as argument must have at least its object if set.
     * @param account: The incomplete UserAccount instance
     * @return: The same UserAccount instance, but with all the fields with valid data.
     */
    public UserAccount fillUserDetails(UserAccount account){
        String table = SCWallDatabaseContract.UserAccounts.TABLE_NAME;
        String selection = SCWallDatabaseContract.UserAccounts.COLUMN_ID + "=?";
        String[] selectionArgs = new String[] { account.getObjectId() };
        Cursor cursor = db.query(table, null, selection, selectionArgs, null, null, null, null);
        if(cursor.moveToFirst()){
            String accountName = cursor.getString(cursor.getColumnIndex(SCWallDatabaseContract.UserAccounts.COLUMN_NAME));
            account.setAccountName(accountName);
        }
        cursor.close();
        return account;
    }

    /**
     * Given an incomplete instance of the Asset object, performs a query and fills the asset
     * reference with 'precision', 'symbol' and 'description' data.
     *
     * The incomplete object passed as argument must have at least its object if set.
     * @param asset: Incomplete asset instance.
     * @return: Complete asset instance.
     */
    public Asset fillAssetDetails(Asset asset){
        String table = SCWallDatabaseContract.Assets.TABLE_NAME;
        String selection = SCWallDatabaseContract.Assets.COLUMN_ID + "=?";
        String[] selectionArgs = new String[]{ asset.getObjectId() };
        Cursor cursor = db.query(table, null, selection, selectionArgs, null, null, null, null);
        if(cursor.moveToFirst()){
            try{
                String symbol = cursor.getString(cursor.getColumnIndex(SCWallDatabaseContract.Assets.COLUMN_SYMBOL));
                int precision = cursor.getInt(cursor.getColumnIndex(SCWallDatabaseContract.Assets.COLUMN_PRECISION));
                String description = cursor.getString(cursor.getColumnIndex(SCWallDatabaseContract.Assets.COLUMN_DESCRIPTION));
                asset.setSymbol(symbol);
                asset.setPrecision(precision);
                asset.setDescription(description);
            }catch(Exception e){
                Log.e(TAG,"Exception: "+e.getMessage());
            }
        }
        cursor.close();
        return asset;
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

    public void clearTransfers(){
        db.execSQL("delete from "+SCWallDatabaseContract.Transfers.TABLE_NAME);
    }


    // CryptoCoinCore

    // Account Seed Section

    public String putSeed(final AccountSeed seed){
        ContentValues contentValues = new ContentValues();
        String newId = UUID.randomUUID().toString();
        contentValues.put(SCWallDatabaseContract.Seeds.COLUMN_ID, newId);
        contentValues.put(SCWallDatabaseContract.Seeds.COLUMN_TYPE, seed.getType().name());
        contentValues.put(SCWallDatabaseContract.Seeds.COLUMN_MNEMONIC, seed.getMnemonicCodeString());
        contentValues.put(SCWallDatabaseContract.Seeds.COLUMN_ADDITIONAL, seed.getAdditional());
        try{
            db.insertOrThrow(SCWallDatabaseContract.Seeds.TABLE_NAME, null, contentValues);
            seed.setId(newId);
            Log.d(TAG,String.format("Inserted %s seed seuccesfully transactions in database", newId));
            return newId;
            }catch (SQLException e){
            }
        Log.d(TAG,"Error inserting seed in database");
        return null;
    }

    public boolean updateSeed(AccountSeed seed){
        String table = SCWallDatabaseContract.Seeds.TABLE_NAME;
        String whereClause = SCWallDatabaseContract.Seeds.COLUMN_ID + "=?";
        String[] whereArgs = new String[]{ seed.getId() };
        ContentValues contentValues = new ContentValues();
        contentValues.put(SCWallDatabaseContract.Seeds.COLUMN_TYPE, seed.getType().name());
        contentValues.put(SCWallDatabaseContract.Seeds.COLUMN_MNEMONIC, seed.getMnemonicCodeString());
        contentValues.put(SCWallDatabaseContract.Seeds.COLUMN_ADDITIONAL, seed.getAdditional());
        int affected = db.update(table,contentValues,whereClause,whereArgs);
        return affected > 0;
    }

    public List<AccountSeed> getSeeds(SeedType type){
        List<AccountSeed> seeds = new ArrayList();
        String[] columns = {
                SCWallDatabaseContract.Seeds.COLUMN_ID,
                SCWallDatabaseContract.Seeds.COLUMN_MNEMONIC,
                SCWallDatabaseContract.Seeds.COLUMN_ADDITIONAL,
        };
        Cursor cursor = db.query(true, SCWallDatabaseContract.Seeds.TABLE_NAME, columns,
                SCWallDatabaseContract.Seeds.COLUMN_TYPE+ " = '" + type.name() + "'", null, null, null, null, null);
        if(cursor.moveToFirst()){
            AccountSeed seed;
            do{
                String id = cursor.getString(0);
                List<String> mnemonic = Arrays.asList(cursor.getString(1).split(" "));
                String additional = cursor.getString(2);
                switch (type.name()) {
                    case "BIP39":
                        seed = new BIP39(id, mnemonic, additional);
                        break;
                    case "BrainKey":
                        seed = new Brainkey(id, mnemonic, additional);
                        break;
                    default:
                        seed = null;
                }
                seeds.add(seed);
            }while(cursor.moveToNext());
        }
        cursor.close();
        return seeds;
    }

    public AccountSeed getSeed(String idSeed) {
        AccountSeed seed = null;
        String[] columns = {
                SCWallDatabaseContract.Seeds.COLUMN_ID,
                SCWallDatabaseContract.Seeds.COLUMN_MNEMONIC,
                SCWallDatabaseContract.Seeds.COLUMN_ADDITIONAL,
                SCWallDatabaseContract.Seeds.COLUMN_TYPE
        };
        Cursor cursor = db.query(true, SCWallDatabaseContract.Seeds.TABLE_NAME, columns,
                SCWallDatabaseContract.Seeds.COLUMN_ID + " = '" + idSeed + "'",
                null, null, null, null, null);
        if(cursor.moveToFirst()){
            do{
                String id = cursor.getString(0);
                List<String> mnemonic = Arrays.asList(cursor.getString(1).split(" "));
                String additional = cursor.getString(2);
                String type = cursor.getString(3);
                switch (type) {
                    case "BIP39":
                        seed = new BIP39(id, mnemonic, additional);
                        break;
                    case "BrainKey":
                        seed = new Brainkey(id, mnemonic, additional);
                        break;
                    default:
                        seed = null;
                }
                cursor.close();
                return seed;
            }while(cursor.moveToNext());
        }
        cursor.close();
        return null;
    }

    public String getIdSeed(AccountSeed seed) {
        String[] columns = {
                SCWallDatabaseContract.Seeds.COLUMN_ID,
        };
        Cursor cursor = db.query(true, SCWallDatabaseContract.Seeds.TABLE_NAME, columns,
                SCWallDatabaseContract.Seeds.COLUMN_MNEMONIC + " = '" + seed.getMnemonicCodeString()
                        + "' AND " + SCWallDatabaseContract.Seeds.COLUMN_ADDITIONAL + " = '" + seed.getAdditional() + "'",
                null, null, null, null, null);
        if(cursor.moveToFirst()){
            do{
                String id = cursor.getString(0);
                cursor.close();
                return id;
            }while(cursor.moveToNext());
        }
        cursor.close();
        return null;
    }

    public List<AccountSeed> getSeeds() {
        List<AccountSeed> seeds = new ArrayList();
        String[] columns = {
                SCWallDatabaseContract.Seeds.COLUMN_ID,
                SCWallDatabaseContract.Seeds.COLUMN_MNEMONIC,
                SCWallDatabaseContract.Seeds.COLUMN_ADDITIONAL,
                SCWallDatabaseContract.Seeds.COLUMN_TYPE
        };
        Cursor cursor = db.query(true, SCWallDatabaseContract.Seeds.TABLE_NAME, columns, null, null, null, null, null, null);
        if(cursor.moveToFirst()){
            AccountSeed seed;
            do{
                String id = cursor.getString(0);
                List<String> mnemonic = Arrays.asList(cursor.getString(1).split(" "));
                String additional = cursor.getString(2);
                String type = cursor.getString(3);
                switch (type) {
                    case "BIP39":
                        seed = new BIP39(id, mnemonic, additional);
                        break;
                    case "BrainKey":
                        seed = new Brainkey(id, mnemonic, additional);
                        break;
                    default:
                        seed = null;
                }
                seeds.add(seed);
            }while(cursor.moveToNext());
        }
        cursor.close();
        return seeds;
    }


    //General Coin Account section

    public String putGeneralCoinAccount(final GeneralCoinAccount account){
        ContentValues contentValues = new ContentValues();
        String newId = UUID.randomUUID().toString();
        contentValues.put(SCWallDatabaseContract.GeneralAccounts.COLUMN_ID, newId);
        contentValues.put(SCWallDatabaseContract.GeneralAccounts.COLUMN_TYPE, account.getCoin().name());
        if(account.getSeed() == null){
            Log.d(TAG,"Error inserting account  null seed in database");
            return null;
        }
        String idSeed = account.getSeed().getId();
        if(idSeed == null || idSeed.isEmpty() || idSeed.equalsIgnoreCase("null")){
            idSeed = getIdSeed(account.getSeed());
            if(idSeed == null || idSeed.isEmpty() || idSeed.equalsIgnoreCase("null")){
                idSeed = putSeed(account.getSeed());
                if(idSeed == null || idSeed.isEmpty() || idSeed.equalsIgnoreCase("null")) {
                    Log.d(TAG,"Error inserting account  null id seed in database");
                    return null;
                }
            }
            account.getSeed().setId(idSeed);
        }
        contentValues.put(SCWallDatabaseContract.GeneralAccounts.COLUMN_ID_SEED, idSeed);
        contentValues.put(SCWallDatabaseContract.GeneralAccounts.COLUMN_NAME, account.getName());
        contentValues.put(SCWallDatabaseContract.GeneralAccounts.COLUMN_ACCOUNT_INDEX, account.getAccountNumber());
        contentValues.put(SCWallDatabaseContract.GeneralAccounts.COLUMN_CHANGE_INDEX, account.getLastChangeIndex());
        contentValues.put(SCWallDatabaseContract.GeneralAccounts.COLUMN_EXTERNAL_INDEX, account.getLastExternalIndex());
        try{
            db.insertOrThrow(SCWallDatabaseContract.GeneralAccounts.TABLE_NAME, null, contentValues);
            account.setId(newId);
            Log.d(TAG,String.format("Inserted %s account seuccesfully transactions in database", newId));
            return newId;
        }catch (SQLException e){
        }
        Log.d(TAG,"Error inserting account in database");
        return null;
    }

    public boolean updateGeneralCoinAccount(GeneralCoinAccount account){
        String table = SCWallDatabaseContract.GeneralAccounts.TABLE_NAME;
        String whereClause = SCWallDatabaseContract.GeneralAccounts.COLUMN_ID + "=?";
        String[] whereArgs = new String[]{ account.getId()};

        ContentValues contentValues = new ContentValues();
        contentValues.put(SCWallDatabaseContract.GeneralAccounts.COLUMN_NAME, account.getName());
        contentValues.put(SCWallDatabaseContract.GeneralAccounts.COLUMN_ACCOUNT_INDEX, account.getAccountNumber());
        contentValues.put(SCWallDatabaseContract.GeneralAccounts.COLUMN_CHANGE_INDEX, account.getLastChangeIndex());
        contentValues.put(SCWallDatabaseContract.GeneralAccounts.COLUMN_EXTERNAL_INDEX, account.getLastExternalIndex());
        int affected = db.update(table,contentValues,whereClause,whereArgs);
        return affected > 0;
    }

    public GeneralCoinAccount getGeneralCoinAccount(String coinType) {
        String[] columns = {
                SCWallDatabaseContract.GeneralAccounts.COLUMN_ID,
                SCWallDatabaseContract.GeneralAccounts.COLUMN_NAME,
                SCWallDatabaseContract.GeneralAccounts.COLUMN_TYPE,
                SCWallDatabaseContract.GeneralAccounts.COLUMN_ID_SEED,
                SCWallDatabaseContract.GeneralAccounts.COLUMN_ACCOUNT_INDEX,
                SCWallDatabaseContract.GeneralAccounts.COLUMN_CHANGE_INDEX,
                SCWallDatabaseContract.GeneralAccounts.COLUMN_EXTERNAL_INDEX
        };
        Cursor cursor = db.query(true, SCWallDatabaseContract.GeneralAccounts.TABLE_NAME, columns,
                SCWallDatabaseContract.GeneralAccounts.COLUMN_TYPE + " = " + coinType,
                null, null, null, null, null);
        if(cursor.moveToFirst()){
            do{
                String id = cursor.getString(0);
                String name = cursor.getString(1);
                Coin type = Coin.valueOf(cursor.getString(2));
                String idSeed = cursor.getString(3);
                AccountSeed seed = getSeed(idSeed);
                if (seed == null) {
                    continue;
                }
                int accountIndex = cursor.getInt(4);
                int changeIndex = cursor.getInt(5);
                int externalIndex = cursor.getInt(6);
                GeneralCoinAccount account = CryptoCoinFactory
                        .getGeneralCoinManager(type)
                        .getAccount(id, name, seed, accountIndex,
                                externalIndex, changeIndex);
                cursor.close();
                return account;
            }while(cursor.moveToNext());
        }
        cursor.close();
        return null;
    }

    public GeneralCoinAccount getGeneralCoinAccount(AccountSeed seed, String coinType) {
        String[] columns = {
                SCWallDatabaseContract.GeneralAccounts.COLUMN_ID,
                SCWallDatabaseContract.GeneralAccounts.COLUMN_NAME,
                SCWallDatabaseContract.GeneralAccounts.COLUMN_ACCOUNT_INDEX,
                SCWallDatabaseContract.GeneralAccounts.COLUMN_CHANGE_INDEX,
                SCWallDatabaseContract.GeneralAccounts.COLUMN_EXTERNAL_INDEX
        };
        Cursor cursor = db.query(true, SCWallDatabaseContract.GeneralAccounts.TABLE_NAME, columns,
                SCWallDatabaseContract.GeneralAccounts.COLUMN_TYPE + " = " + coinType + " AND " + SCWallDatabaseContract.GeneralAccounts.COLUMN_ID_SEED + " = " + seed.getId(),
                null, null, null, null, null);
        if(cursor.moveToFirst()){
            do{
                String id = cursor.getString(0);
                String name = cursor.getString(1);
                Coin type = Coin.valueOf(coinType);
                int accountIndex = cursor.getInt(2);
                int changeIndex = cursor.getInt(3);
                int externalIndex = cursor.getInt(4);
                GeneralCoinAccount account = CryptoCoinFactory
                        .getGeneralCoinManager(type)
                        .getAccount(id, name, seed, accountIndex,
                                externalIndex, changeIndex);
                cursor.close();
                return account;
            }while(cursor.moveToNext());
        }
        cursor.close();
        return null;
    }

    public List<GeneralCoinAccount> getGeneralCoinAccounts() {

        List<GeneralCoinAccount> accounts = new ArrayList();

        String[] columns = {
                SCWallDatabaseContract.GeneralAccounts.COLUMN_ID,
                SCWallDatabaseContract.GeneralAccounts.COLUMN_NAME,
                SCWallDatabaseContract.GeneralAccounts.COLUMN_TYPE,
                SCWallDatabaseContract.GeneralAccounts.COLUMN_ID_SEED,
                SCWallDatabaseContract.GeneralAccounts.COLUMN_ACCOUNT_INDEX,
                SCWallDatabaseContract.GeneralAccounts.COLUMN_CHANGE_INDEX,
                SCWallDatabaseContract.GeneralAccounts.COLUMN_EXTERNAL_INDEX
        };
        Cursor cursor = db.query(true, SCWallDatabaseContract.GeneralAccounts.TABLE_NAME, columns,
                null, null, null, null, null, null);
        if(cursor.moveToFirst()){
            do{
                String id = cursor.getString(0);
                String name = cursor.getString(1);
                Coin type = Coin.valueOf(cursor.getString(2));
                String idSeed = cursor.getString(3);
                AccountSeed seed = getSeed(idSeed);
                if (seed == null) {
                    continue;
                }
                int accountIndex = cursor.getInt(4);
                int changeIndex = cursor.getInt(5);
                int externalIndex = cursor.getInt(6);
                GeneralCoinAccount account = CryptoCoinFactory
                        .getGeneralCoinManager(type)
                        .getAccount(id, name, seed, accountIndex,
                                externalIndex, changeIndex);
                accounts.add(account);
            }while(cursor.moveToNext());
        }
        cursor.close();
        return accounts;
    }


}
