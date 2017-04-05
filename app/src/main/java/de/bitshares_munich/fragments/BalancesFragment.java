package de.bitshares_munich.fragments;

import android.Manifest;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.primitives.UnsignedLong;

import org.bitcoinj.core.DumpedPrivateKey;
import org.bitcoinj.core.ECKey;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Currency;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.bitshares_munich.adapters.TransferAmountComparator;
import de.bitshares_munich.adapters.TransferDateComparator;
import de.bitshares_munich.adapters.TransferSendReceiveComparator;
import de.bitshares_munich.adapters.TransfersTableAdapter;
import de.bitshares_munich.database.HistoricalTransferEntry;
import de.bitshares_munich.database.SCWallDatabase;
import de.bitshares_munich.interfaces.AssetDelegate;
import de.bitshares_munich.interfaces.ISound;
import de.bitshares_munich.interfaces.PdfGeneratorListener;
import de.bitshares_munich.models.AccountAssets;
import de.bitshares_munich.models.AccountDetails;
import de.bitshares_munich.models.BalanceItem;
import de.bitshares_munich.models.BalanceItems;
import de.bitshares_munich.models.BalanceItemsEvent;
import de.bitshares_munich.models.BalanceItemsListener;
import de.bitshares_munich.models.Smartcoins;
import de.bitshares_munich.models.TransactionDetails;
import de.bitshares_munich.smartcoinswallet.AssetsActivity;
import de.bitshares_munich.smartcoinswallet.AssetsSymbols;
import de.bitshares_munich.smartcoinswallet.AudioFilePath;
import de.bitshares_munich.smartcoinswallet.Constants;
import de.bitshares_munich.smartcoinswallet.MediaService;
import de.bitshares_munich.smartcoinswallet.QRCodeActivity;
import de.bitshares_munich.smartcoinswallet.R;
import de.bitshares_munich.smartcoinswallet.ReceiveActivity;
import de.bitshares_munich.smartcoinswallet.SendScreen;
import de.bitshares_munich.smartcoinswallet.WebsocketWorkerThread;
import de.bitshares_munich.utils.Application;
import de.bitshares_munich.utils.Crypt;
import de.bitshares_munich.utils.Helper;
import de.bitshares_munich.utils.PdfGeneratorTask;
import de.bitshares_munich.utils.PermissionManager;
import de.bitshares_munich.utils.SupportMethods;
import de.bitshares_munich.utils.TableViewClickListener;
import de.bitshares_munich.utils.TinyDB;
import de.bitshares_munich.utils.webSocketCallHelper;
import de.bitsharesmunich.graphenej.Address;
import de.bitsharesmunich.graphenej.Asset;
import de.bitsharesmunich.graphenej.AssetAmount;
import de.bitsharesmunich.graphenej.Converter;
import de.bitsharesmunich.graphenej.PublicKey;
import de.bitsharesmunich.graphenej.TransferOperation;
import de.bitsharesmunich.graphenej.UserAccount;
import de.bitsharesmunich.graphenej.api.GetAccounts;
import de.bitsharesmunich.graphenej.api.GetAssets;
import de.bitsharesmunich.graphenej.api.GetBlockHeader;
import de.bitsharesmunich.graphenej.api.GetLimitOrders;
import de.bitsharesmunich.graphenej.api.GetMarketHistory;
import de.bitsharesmunich.graphenej.api.GetRelativeAccountHistory;
import de.bitsharesmunich.graphenej.api.LookupAssetSymbols;
import de.bitsharesmunich.graphenej.errors.ChecksumException;
import de.bitsharesmunich.graphenej.interfaces.WitnessResponseListener;
import de.bitsharesmunich.graphenej.models.AccountProperties;
import de.bitsharesmunich.graphenej.models.BaseResponse;
import de.bitsharesmunich.graphenej.models.BlockHeader;
import de.bitsharesmunich.graphenej.models.BucketObject;
import de.bitsharesmunich.graphenej.models.HistoricalTransfer;
import de.bitsharesmunich.graphenej.models.Market;
import de.bitsharesmunich.graphenej.models.WitnessResponse;
import de.bitsharesmunich.graphenej.objects.Memo;
import de.codecrafters.tableview.SortableTableView;
import de.codecrafters.tableview.toolkit.SimpleTableHeaderAdapter;
import de.codecrafters.tableview.toolkit.SortStateViewProviders;


/**
 * Created by qasim on 5/10/16.
 */
public class BalancesFragment extends Fragment implements AssetDelegate, ISound, PdfGeneratorListener, BalanceItemsListener {
    /* Permission flag */
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private final static List<String> SMARTCOINS = Arrays.asList(new String[]{"CNY", "BTC", "USD",
            "GOLD", "EUR", "SILVER", "ARS", "CAD", "GBP", "KRW", "CHF", "JPY", "HKD", "SGD", "AUD",
            "RUB", "SBK"});
    public static Activity balanceActivity;
    //    Boolean isLoading = false;
    public static Boolean onClicked = false;
    public static ISound iSound;
    static Boolean audioSevice = false;
    public final String TAG = this.getClass().getName();
    // Debug flags
    private final boolean DEBUG_DATE_LOADING = false;
    private final boolean DEBUG_EQ_VALUES = false;
    int accountDetailsId;
    String accountId = "";
    DecimalFormat df = new DecimalFormat("0.0");
    Handler myHandler = new Handler();
    String to = "";
    String wifkey = "";
    String finalFiatCurrency;
    @Bind(R.id.load_more_values)
    Button loadMoreButton;
    @Bind(R.id.scrollViewBalances)
    ScrollView scrollViewBalances;
    @Bind(R.id.backLine)
    View backLine;
    @Bind(R.id.progressBar)
    ProgressBar progressBar;
    @Bind(R.id.progressBar1)
    ProgressBar progressBar1;
    @Bind(R.id.qrCamera)
    ImageView qrCamera;
    @Bind(R.id.tvBalances)
    TextView tvBalances;
    @Bind(R.id.tvUpgradeLtm)
    TextView tvUpgradeLtm;
    @Bind(R.id.llBalances)
    LinearLayout llBalances;
    @Bind(R.id.whiteSpaceAfterBalances)
    LinearLayout whiteSpaceAfterBalances;
    TinyDB tinyDB;
    @Bind(R.id.tableViewparent)
    LinearLayout tableViewparent;
    @Bind(R.id.account_name)
    TextView tvAccountName;
    @Bind(R.id.receivebtn)
    ImageView receivebtn;
    @Bind(R.id.sendbtn)
    ImageView sendbtn;
    @Bind(R.id.ivLifeTime)
    ImageView ivLifeTime;
    @Bind(R.id.ivMultiAccArrow)
    ImageView ivMultiAccArrow;
    ProgressDialog progressDialog;
    Boolean sentCallForTransactions = false;
    BalanceItems balanceItems;
    Locale locale;
    NumberFormat format;
    String language;
    //Cache for equivalent component BTS to EUR
    double BTSCurrencyPriceCache;
    Date BTSCurrencyPriceCacheDate;


    webSocketCallHelper myWebSocketHelper;
    Handler updateEquivalentAmount;
    Handler animateNsoundHandler = new Handler();
    Handler updateTransactionsList;
    int counterRepeatTransactionLoad = 0;
    Handler loadOndemand = new Handler();
    boolean updateTriggerFromNetworkBroadcast = false;
    AssetsActivity myAssetsActivity;
    boolean firstTimeLoad = true;
    String transactionsLoadedAccountName = "";
    /**
     * SortableTableView displaying the list of transactions.
     */
    private SortableTableView<HistoricalTransferEntry> transfersView;
    /**
     * Adapter for the transaction list.
     */
    private TransfersTableAdapter tableAdapter;
    /**
     * Counter used to keep track of how many times the 'load more' button was pressed
     */
    private int loadMoreCounter = 1;
    /* AsyncTask used to process the PDF generation job in the background */
    private PdfGeneratorTask pdfGeneratorTask;
    /* Dialog with a pdfProgress bar used to display pdfProgress while generating a new PDF file */
    private ProgressDialog pdfProgress;
    /* Constant used to fix the number of historical transfers to fetch from the network in one batch */
    private int HISTORICAL_TRANSFER_BATCH_SIZE = 50;
    /* Parameters to be used as the start and stop arguments in the 'get_relative_account_history' API call */
    private int start = 1;
    private int stop = HISTORICAL_TRANSFER_BATCH_SIZE;
    private int historicalTransferCount = 0;
    private int HISTORICAL_TRANSFER_MAX = 10;
    /* Constant used to split the missing times and equivalent values in batches of constant time */
    private int SECONDARY_LOAD_BATCH_SIZE = 2;
    /*
    * Attribute used when trying to make a 2-step equivalent value calculation
    * This variable will hold the equivalent value of the UIA in BTS, that will in turn
    * have to be converted to the smartcoin of choice for the user */
    private AssetAmount coreCurrencyEqValue;
    /* Websocket handler */
    private GetMarketHistory getMarketHistory;
    /*
    * This is the smartcoin that matches the user's selected fiat currency.
    * If no smartcoin exists for a user's specific local currency, the bitUSD
    * will be used instead.*/
    private Asset mSmartcoin;
    /* List of transactions for which we don't have the equivalent value data */
    private LinkedList<HistoricalTransferEntry> missingEquivalentValues;
    /* List of block numbers with missing date information in the database */
    private LinkedList<Long> missingTimes;
    /* Smarcoins Wallet database instance */
    private SCWallDatabase database;
    /* Websocket threads */
    private WebsocketWorkerThread transferHistoryThread;
    private WebsocketWorkerThread getMissingAccountsThread;
    private WebsocketWorkerThread getMissingAssets;
    private WebsocketWorkerThread getMissingTimes;
    private WebsocketWorkerThread getMissingEquivalentValues;
    /**
     * Callback activated once we get a block header response.
     */
    private WitnessResponseListener mGetMissingTimesListener = new WitnessResponseListener() {

        @Override
        public void onSuccess(final WitnessResponse response) {
            if (getActivity() == null) {
                Log.w(TAG, "Got no activity, quitting..");
                return;
            }
            if (missingTimes.size() > 1) {
                Log.d(TAG, "getMissingTime. onSuccess. remaining: " + (missingTimes.size() - 1));
            }

            BlockHeader blockHeader = (BlockHeader) response.result;
            boolean updated = database.setBlockTime(blockHeader, missingTimes.peek());
            if (!updated) {
                Log.w(TAG, "Failed to update time from transaction at block: " + missingTimes.peek());
            }
            missingTimes.poll();

            // If we still have missing times in the queue, work on them
            if (missingTimes.size() > 0) {
                long blockNum = missingTimes.peek();
                getMissingTimes = new WebsocketWorkerThread(new GetBlockHeader(blockNum, mGetMissingTimesListener));
                getMissingTimes.start();
            } else {
                // If we're done loading missing transfer times, we check for missing equivalent values.
                // By calling the 'getMissingEquivalentValues' method we should get a list of all transfer
                // entries that are missing just the equivalent values, but DO HAVE time information.
                missingEquivalentValues = database.getMissingEquivalentValues();
                if (missingEquivalentValues.size() > 0) {
                    Log.i(TAG, "Finished loading missing times, now we can safely proceed to missing eq values");
                    processNextEquivalentValue();
                } else {
                    Log.w(TAG, "Got no missing equivalent value to fetch");
                }
            }
        }

        @Override
        public void onError(BaseResponse.Error error) {
            Log.e(TAG, "missingTimes. onError");
            missingTimes.poll();

            // If we still have missing times in the queue, work on them
            if (missingTimes.size() > 0) {
                long blockNum = missingTimes.peek();
                getMissingTimes = new WebsocketWorkerThread(new GetBlockHeader(blockNum, mGetMissingTimesListener));
                getMissingTimes.start();
            }
        }
    };
    /**
     * Called when we get a response from the 'get_market_history' API call
     */
    private WitnessResponseListener mHistoricalMarketListener = new WitnessResponseListener() {
        @Override
        public void onSuccess(WitnessResponse response) {
            Log.d(TAG, "mHistoricalMarketListener.onSuccess");
            if (getActivity() == null) {
                Log.w(TAG, "Got no activity, quitting..");
                return;
            }

            List<BucketObject> buckets = (List<BucketObject>) response.result;
            HistoricalTransferEntry transferEntry = missingEquivalentValues.peek();
            if (buckets.size() > 0) {
                BucketObject bucket = buckets.get(buckets.size() - 1);

                AssetAmount transferAmount = transferEntry.getHistoricalTransfer().getOperation().getTransferAmount();

                Asset base = database.fillAssetDetails(bucket.key.base);
                Asset quote = database.fillAssetDetails(bucket.key.quote);

                if (quote.equals(mSmartcoin)) {
                    // Doing conversion and updating the database
                    Converter converter = new Converter(base, quote, bucket);
                    long convertedBaseValue = converter.convert(transferAmount, Converter.CLOSE_VALUE);
                    AssetAmount equivalentValue = new AssetAmount(UnsignedLong.valueOf(convertedBaseValue), mSmartcoin);

                    transferEntry.setEquivalentValue(equivalentValue);
                    database.updateEquivalentValue(transferEntry);

                    // Removing the now solved equivalent value
                    missingEquivalentValues.poll();

                    // Process the next equivalent value, in case we have one
                    processNextEquivalentValue();
                } else {
                    AssetAmount originalTransfer = transferEntry.getHistoricalTransfer().getOperation().getTransferAmount();

                    // Doing conversion and updating the database
                    Converter converter = new Converter(base, quote, bucket);
                    long convertedBaseValue = converter.convert(originalTransfer, Converter.CLOSE_VALUE);
                    coreCurrencyEqValue = new AssetAmount(UnsignedLong.valueOf(convertedBaseValue), base);

                    base = database.fillAssetDetails(Constants.getCoreCurrency());
                    quote = database.fillAssetDetails(mSmartcoin);

                    Log.d(TAG, String.format("Requesting conversion from %s -> %s", base.getSymbol(), quote.getSymbol()));

                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(transferEntry.getTimestamp() * 1000);
                    calendar.set(Calendar.MINUTE, 0);
                    calendar.set(Calendar.SECOND, 0);
                    Date startDate = calendar.getTime();
                    Date endDate = calendar.getTime();

                    // Performing the 2nd step of the equivalent value calculation. We already hold the
                    // relationship UIA -> BTS, now we need the BTS -> Smartcoin for this time bucket.
                    getMarketHistory = new GetMarketHistory(
                            base,
                            quote,
                            Constants.DEFAULT_BUCKET_SIZE,
                            startDate,
                            endDate,
                            mHistoricalMarketSecondStepListener);
                    getMissingEquivalentValues = new WebsocketWorkerThread(getMarketHistory);
                    getMissingEquivalentValues.start();
                }
            } else {
                // Got no bucket for the specified time window. In this case we just expand the time
                // window by pushing the 'start' field further into the past using exponential increments.
                // The idea is not to waste time and network data transfer performing a sequential time search
                // of what seems to be a very inactive asset market.
                Asset transferAsset = transferEntry.getHistoricalTransfer().getOperation().getTransferAmount().getAsset();
                Log.w(TAG, String.format("Got no bucket from the requested time period for asset: %s , id: %s", transferAsset.getSymbol(), transferAsset.getObjectId()));
                Date currentStart = getMarketHistory.getStart();
                Calendar calendar = Calendar.getInstance();
                int previousCount = getMarketHistory.getCount() > 0 ? getMarketHistory.getCount() - 1 : 0;
                int currentCount = getMarketHistory.getCount();
                long previousExponentialFactor = (long) Math.pow(2, previousCount) * Constants.DEFAULT_BUCKET_SIZE * 1000;
                long newExponentialFactor = (long) Math.pow(2, currentCount) * Constants.DEFAULT_BUCKET_SIZE * 1000;
                long adjustedStartValue = currentStart.getTime() + previousExponentialFactor - newExponentialFactor;
                calendar.setTimeInMillis(adjustedStartValue);
                getMarketHistory.setStart(calendar.getTime());
                getMarketHistory.retry();
            }
        }

        @Override
        public void onError(BaseResponse.Error error) {
            Log.e(TAG, "historicalMarketListener.onError. Msg: " + error.message);
            // Removing this equivalent value task, even though it was not resolved
            missingEquivalentValues.poll();

            // Process the next equivalent value, in case we have one
            processNextEquivalentValue();
        }
    };
    private WitnessResponseListener mHistoricalMarketSecondStepListener = new WitnessResponseListener() {
        @Override
        public void onSuccess(WitnessResponse response) {
            Log.d(TAG, "historicalMarketSecondStepListener.onSuccess");
            if (getActivity() == null) {
                Log.w(TAG, "Got no activity, quitting..");
                return;
            }
            List<BucketObject> buckets = (List<BucketObject>) response.result;
            HistoricalTransferEntry transferEntry = missingEquivalentValues.peek();

            if (buckets.size() > 0) {
                // Fetching the last bucket, just in case we have more than one.
                BucketObject bucket = buckets.get(buckets.size() - 1);

                Asset base = database.fillAssetDetails(bucket.key.base);
                Asset quote = database.fillAssetDetails(bucket.key.quote);

                // Doing conversion and updating the database
                Converter converter = new Converter(base, quote, bucket);
                long convertedBaseValue = converter.convert(coreCurrencyEqValue, Converter.CLOSE_VALUE);
                AssetAmount equivalentValue = new AssetAmount(UnsignedLong.valueOf(convertedBaseValue), mSmartcoin);

                // Updating equivalent value entry
                transferEntry.setEquivalentValue(equivalentValue);
                database.updateEquivalentValue(transferEntry);

                // Removing the now solved equivalent value
                missingEquivalentValues.poll();

                // Processing next value, if there is one.
                // Process the next equivalent value, in case we have one
                processNextEquivalentValue();
            } else {
                Date currentStart = getMarketHistory.getStart();
                int previousCount = getMarketHistory.getCount() > 0 ? getMarketHistory.getCount() - 1 : 0;
                int currentCount = getMarketHistory.getCount();
                long previousExponentialFactor = (long) Math.pow(2, previousCount) * Constants.DEFAULT_BUCKET_SIZE * 1000;
                long newExponentialFactor = (long) Math.pow(2, currentCount) * Constants.DEFAULT_BUCKET_SIZE * 1000;
                long adjustedStartValue = currentStart.getTime() + previousExponentialFactor - newExponentialFactor;

                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(adjustedStartValue);
                getMarketHistory.setStart(calendar.getTime());
                getMarketHistory.retry();
            }
        }

        @Override
        public void onError(BaseResponse.Error error) {
            Log.e(TAG, "mHistoricalMarketSecondStepListener.onError. Msg: " + error.message);

            // Removing the now solved equivalent value
            missingEquivalentValues.poll();

            // Processing next value, if there is one.
            // Process the next equivalent value, in case we have one
            processNextEquivalentValue();
        }
    };
    /**
     * Callback activated whenever we get information about missing assets in the database.
     * If the missing asset happens to be the user's current base smartcoin, we update the
     * mSmartcoin reference, since this will be a new and complete Asset instance.
     */
    private WitnessResponseListener mLookupAssetsSymbolsListener = new WitnessResponseListener() {

        @Override
        public void onSuccess(final WitnessResponse response) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "assetsUpdater.onSuccess");
                    List<Asset> assets = (List<Asset>) response.result;
                    // Updating the database
                    int count = database.putAssets(assets);

                    // Looking for smartcoin asset
                    for (Asset asset : assets) {
                        if (asset.getObjectId().equals(mSmartcoin.getObjectId().toString())) {
                            mSmartcoin = database.fillAssetDetails(asset);
                        }
                    }

                    // If we has missing equivalent values that could not be processed until
                    // we had all the missing assets in the database, start processing them now.
                    if (missingEquivalentValues != null) {
                        processNextEquivalentValue();
                    }
                }
            });
        }

        @Override
        public void onError(BaseResponse.Error error) {
            Log.e(TAG, "assetsUpdater.onError");
        }
    };
    /**
     * Callback activated once we get a response back from the full node telling us about missing
     * user account data we previously requested.
     */
    private WitnessResponseListener mGetmissingAccountsListener = new WitnessResponseListener() {
        @Override
        public void onSuccess(final WitnessResponse response) {
            getActivity().runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    List<AccountProperties> missingAccounts = (List<AccountProperties>) response.result;
                    database.putUserAccounts(missingAccounts);
                }
            });
        }

        @Override
        public void onError(BaseResponse.Error error) {
            Log.d(TAG, "missing accounts. onError");
        }
    };
    /**
     * Callback activated once we get a response back from the full node telling us about the
     * transfer history of the current account.
     */
    private WitnessResponseListener mTransferHistoryListener = new WitnessResponseListener() {

        @Override
        public void onSuccess(final WitnessResponse response) {
            if (getActivity() == null) {
                Log.w(TAG, "Got no activity, quitting..");
                return;
            }
            historicalTransferCount++;
            WitnessResponse<List<HistoricalTransfer>> resp = response;
            List<HistoricalTransferEntry> historicalTransferEntries = new ArrayList<>();

            // Getting decrypted private key in WIF format
            String wif = decryptWif();

            ECKey privateKey = DumpedPrivateKey.fromBase58(null, wif).getKey();
            PublicKey publicKey = new PublicKey(ECKey.fromPublicOnly(privateKey.getPubKey()));
            Address myAddress = new Address(publicKey.getKey());

            // Decrypting memo messages
            for (HistoricalTransfer historicalTransfer : resp.result) {
                HistoricalTransferEntry entry = new HistoricalTransferEntry();
                TransferOperation op = historicalTransfer.getOperation();
                if (op != null) {
                    Memo memo = op.getMemo();

                    if (memo.getByteMessage() != null) {

                        Address destinationAddress = memo.getDestination();
                        try {
                            if (destinationAddress.toString().equals(myAddress.toString())) {
                                String decryptedMessage = Memo.decryptMessage(privateKey, memo.getSource(), memo.getNonce(), memo.getByteMessage());
                                memo.setPlaintextMessage(decryptedMessage);
                            }
                        } catch (ChecksumException e) {
                            Log.e(TAG, "ChecksumException. Msg: " + e.getMessage());
                        } catch (NullPointerException e) {
                            // This is expected in case the decryption fails, so no need to log this event.
                        }
                    }
                } else {
                    continue;
                }
                entry.setHistoricalTransfer(historicalTransfer);
                historicalTransferEntries.add(entry);
            }


            int inserted = database.putTransactions(historicalTransferEntries);
            Log.d(TAG, String.format("Inserted %d out of %d obtained operations", inserted, resp.result.size()));
            List<HistoricalTransferEntry> transactions = database.getTransactions(new UserAccount(accountId), loadMoreCounter * SCWallDatabase.DEFAULT_TRANSACTION_BATCH_SIZE);


            // Updating table view
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updateTableView(false);
                }
            });

            // If we got exactly the requested amount of historical transfers, it means we
            // MUST have more to fetch.
            if (resp.result.size() == HISTORICAL_TRANSFER_BATCH_SIZE && historicalTransferCount < HISTORICAL_TRANSFER_MAX) {
                Log.v(TAG, String.format("Got %d transactions, which is exactly the requested amount, so we might have more.", resp.result.size()));
                start = transactions.size() + (historicalTransferCount * HISTORICAL_TRANSFER_BATCH_SIZE);
                stop = start + HISTORICAL_TRANSFER_BATCH_SIZE + 1;
                Log.v(TAG, String.format("Calling get_relative_account_history. start: %d, limit: %d, stop: %d", start, HISTORICAL_TRANSFER_BATCH_SIZE, stop));
                transferHistoryThread = new WebsocketWorkerThread(new GetRelativeAccountHistory(new UserAccount(accountId), start, HISTORICAL_TRANSFER_BATCH_SIZE, stop, mTransferHistoryListener));
                transferHistoryThread.start();
            } else {
                // If we got less than the requested amount of historical transfers, it means we
                // are done importing old transactions. We can proceed to get other missing attributes
                // like transaction timestamps, asset references and equivalent values.
                Log.d(TAG, String.format("Got %d transfers, which is less than what we asked for, so that must be it", resp.result.size()));
                List<UserAccount> missingAccountNames = database.getMissingAccountNames();
                if (missingAccountNames.size() > 0) {
                    // Got some missing user names, so we request them to the network.
                    getMissingAccountsThread = new WebsocketWorkerThread(new GetAccounts(missingAccountNames, mGetmissingAccountsListener));
                    getMissingAccountsThread.start();
                }

                List<Asset> missingAssets = database.getMissingAssets();
                if (missingAssets.size() > 0) {
                    // Got some missing asset symbols, so we request them to the network.
                    getMissingAssets = new WebsocketWorkerThread(new LookupAssetSymbols(missingAssets, mLookupAssetsSymbolsListener));
                    getMissingAssets.start();
                }

                missingTimes = database.getMissingTransferTimes(SECONDARY_LOAD_BATCH_SIZE);
                if (missingTimes.size() > 0) {
                    Long blockNum = missingTimes.peek();
                    getMissingTimes = new WebsocketWorkerThread(new GetBlockHeader(blockNum, mGetMissingTimesListener));
                    getMissingTimes.start();
                }

                missingEquivalentValues = database.getMissingEquivalentValues();
                processNextEquivalentValue();
            }
        }

        @Override
        public void onError(BaseResponse.Error error) {
            Log.e(TAG, "mTransferHistoryListener. onError. Msg: " + error.message);
        }
    };

    public BalancesFragment() {
        // Required empty public constructor
    }

    /**
     * Assuming we have a list of missing equivalent values, this method will be called
     * to start the procedure needed to resolve a single missing equivalent value.
     * <p>
     * Since this procedure might have to be called repeated times, it was better isolated
     * in a private method.
     */
    private void processNextEquivalentValue() {
        if (missingEquivalentValues.size() > 0) {
            List<Asset> missingAssets = database.getMissingAssets();
            if (missingAssets.size() == 0) {
                HistoricalTransferEntry transferEntry = missingEquivalentValues.peek();
                Asset transferredAsset = transferEntry.getHistoricalTransfer()
                        .getOperation()
                        .getTransferAmount()
                        .getAsset();

                while (transferredAsset.equals(mSmartcoin)) {
                    // In case the transferred asset is the smartcoin itself, there is no need for
                    // a equivalent value calculation, and as such we just fill in the equivalent
                    // value fields and .
                    transferEntry.setEquivalentValue(new AssetAmount(transferEntry.getHistoricalTransfer().getOperation().getTransferAmount().getAmount(), transferredAsset));
                    database.updateEquivalentValue(transferEntry);

                    missingEquivalentValues.poll();

                    transferEntry = missingEquivalentValues.peek();
                    transferredAsset = transferEntry.getHistoricalTransfer()
                            .getOperation()
                            .getTransferAmount()
                            .getAsset();
                }

                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(transferEntry.getTimestamp() * 1000);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                Date startDate = calendar.getTime();
                Date endDate = calendar.getTime();

                Asset base = null;
                Asset quote = null;
                if (transferredAsset.equals(Constants.getCoreCurrency())) {
                    // Directly fetch the BTS <-> smartcoin
                    base = database.fillAssetDetails(transferredAsset);
                    quote = database.fillAssetDetails(mSmartcoin);
                } else {
                    // We need to perform 2 conversions, namely
                    // Token <-> BTS <-> smartcoin
                    base = database.fillAssetDetails(transferredAsset);
                    quote = database.fillAssetDetails(Constants.getCoreCurrency());
                }
//                Log.d(TAG, String.format("initial times. start: %d, end: %d", startDate.getTime(), endDate.getTime()));
                if (base != null && quote != null) {
                    getMarketHistory = new GetMarketHistory(
                            base,
                            quote,
                            Constants.DEFAULT_BUCKET_SIZE,
                            startDate,
                            endDate,
                            mHistoricalMarketListener);
                    getMissingEquivalentValues = new WebsocketWorkerThread(getMarketHistory);
                    getMissingEquivalentValues.start();
                } else {
                    Log.w(TAG, "Either base or quote is null");
                }
            } else {
                // Don't do anything, the lookup_asset_symbol callback will check for missing
                // equivalent values again and pick up this series of tasks.
                Log.w(TAG, "We have some missing assets");
            }
        } else {
            // In case we're done loading missing times and equivalent values for this batch,
            // we can check if we have another batch of times and consequently missing equivalent
            // values to process.
            missingTimes = database.getMissingTransferTimes(SECONDARY_LOAD_BATCH_SIZE);
            if (missingTimes.size() > 0) {
                Log.d(TAG, String.format("Got a new batch of %d missing times, so we're now going to process them", missingTimes.size()));
                Long blockNum = missingTimes.peek();
                getMissingTimes = new WebsocketWorkerThread(new GetBlockHeader(blockNum, mGetMissingTimesListener));
                getMissingTimes.start();
            } else {
                Log.d(TAG, "We're done with missing times, so this must be it...");
            }

            // Updating table view either way
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updateTableView(true);
                }
            });
        }
    }

    private String decryptWif() {
        String wif = null;
        try {
            wif = Crypt.getInstance().decrypt_string(wifkey);
        } catch (InvalidKeyException e) {
            Log.e(TAG, "InvalidKeyException. Msg: " + e.getMessage());
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "NoSuchAlgorithmException. Msg: " + e.getMessage());
        } catch (NoSuchPaddingException e) {
            Log.e(TAG, "NoSuchPaddingException. Msg: " + e.getMessage());
        } catch (InvalidAlgorithmParameterException e) {
            Log.e(TAG, "InvalidAlgorithmParameterException. Msg: " + e.getMessage());
        } catch (IllegalBlockSizeException e) {
            Log.e(TAG, "IllegalBlockSizeException. Msg: " + e.getMessage());
        } catch (BadPaddingException e) {
            Log.e(TAG, "BadPaddingException. Msg: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "ClassNotFoundException. Msg: " + e.getMessage());
        } catch (IOException e) {
            Log.e(TAG, "IOException. Msg: " + e.getMessage());
        }
        return wif;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tinyDB = new TinyDB(getContext());
        iSound = this;
        updateEquivalentAmount = new Handler();
        myWebSocketHelper = new webSocketCallHelper(getContext());
        database = new SCWallDatabase(getContext());
        if (DEBUG_DATE_LOADING) {
            database.clearTimestamps();
        }
        if (DEBUG_EQ_VALUES) {
            database.clearTransfers();
        }

        // Setting the "base" smartcoin for this user
        String countryCode = Helper.fetchStringSharePref(getContext(), getString(R.string.pref_country));

        this.mSmartcoin = Smartcoins.getMap().get(countryCode.toUpperCase());

        // Getting the system's configuration locale
        locale = getResources().getConfiguration().locale;

        HashMap<String, Asset> knownAssets = database.getAssetMap();
        if (!knownAssets.containsKey(this.mSmartcoin.getObjectId())) {
            // If the smartcoin asset details are not known, we schedule an update from the full node.
            ArrayList<Asset> assetList = new ArrayList<>();
            assetList.add(mSmartcoin);
            getMissingAssets = new WebsocketWorkerThread(new LookupAssetSymbols(assetList, mLookupAssetsSymbolsListener));
            getMissingAssets.start();
        } else {
            mSmartcoin = database.fillAssetDetails(mSmartcoin);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Application.registerAssetDelegate(this);
        //TODO: Evaluate removal after finish refactoring transactions logic
        // getMissingAssets is a WebsocketWorkerThread and should be null only at first
        // time BalanceFragment run, at creation
        /*if (getMissingAssets == null) {
            Log.d(TAG, "Got no missing assets, checking for new transactions");
            List<HistoricalTransferEntry> transactions = database.getTransactions(new UserAccount(accountId), HISTORICAL_TRANSFER_BATCH_SIZE);
            start = transactions.size();
            stop = start + HISTORICAL_TRANSFER_BATCH_SIZE + 1;
            Log.v(TAG, String.format("Calling get_relative_account_history. start: %d, limit: %d, stop: %d", start, HISTORICAL_TRANSFER_BATCH_SIZE, stop));
            transferHistoryThread = new WebsocketWorkerThread(new GetRelativeAccountHistory(new UserAccount(accountId), start, HISTORICAL_TRANSFER_BATCH_SIZE, stop, mTransferHistoryListener));
            transferHistoryThread.start();
        } else {
            Log.w(TAG, "getMissingAssets is not null");
        }*/
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View rootView = inflater.inflate(R.layout.fragment_balances, container, false);
        ButterKnife.bind(this, rootView);
        language = Helper.fetchStringSharePref(getActivity(), getString(R.string.pref_language), "");
        locale = new Locale(language);
        balanceActivity = getActivity();
        format = NumberFormat.getInstance(locale);
        tvUpgradeLtm.setPaintFlags(tvUpgradeLtm.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        progressDialog = new ProgressDialog(getActivity());

        transfersView = (SortableTableView<HistoricalTransferEntry>) rootView.findViewById(R.id.tableView);
        transfersView.addDataClickListener(new TableViewClickListener(getContext()));

        AssetsSymbols assetsSymbols = new AssetsSymbols(getContext());
        assetsSymbols.getAssetsFromServer();
        final Handler handler = new Handler();
        final Runnable updateTask = new Runnable() {
            @Override
            public void run() {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        public void run() {
                            setSortableTableViewHeight(rootView, handler, this);
                        }
                    });
                }
            }
        };

        final Runnable createFolder = new Runnable() {
            @Override
            public void run() {
                createFolder();
            }
        };


        loadBasic(false, true, false);
        loadBalancesFromSharedPref();
//        TransactionUpdateOnStartUp(to);

        handler.postDelayed(updateTask, 2000);

        handler.postDelayed(createFolder, 5000);
        if (!Helper.containKeySharePref(getActivity(), "ltmAmount")) {
            Helper.storeStringSharePref(getActivity(), "ltmAmount", "17611.7");
        }
//        getLtmPrice(getActivity(), tvAccountName.getText().toString());
        return rootView;
    }

    @Override
    public void onPause() {
        super.onPause();
        Application.registerAssetDelegate(null);
    }

    private void setSortableTableViewHeight(View rootView, Handler handler, Runnable task) {
        try {
            View scrollViewBalances = rootView.findViewById(R.id.scrollViewBalances);
            int height1 = scrollViewBalances.getHeight();

            if (height1 == 0) {
                handler.postDelayed(task, 2000);
                return;
            }

            View transactionsExportHeader = rootView.findViewById(R.id.transactionsExportHeader);
            int height2 = transactionsExportHeader.getHeight();
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) transfersView.getLayoutParams();
            params.height = height1 - height2;
            tableViewparent.setLayoutParams(params);
        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
            handler.postDelayed(task, 2000);
        }
    }

    private void createFolder() {
        try {
            PermissionManager manager = new PermissionManager();
            manager.verifyStoragePermissions(getActivity());

            final File folder = new File(Environment.getExternalStorageDirectory() + File.separator + getResources().getString(R.string.folder_name));

            boolean success = false;

            if (!folder.exists()) {
                success = folder.mkdir();
            }

            if (success) {
                // Do something on success
                Toast.makeText(getContext(), getResources().getString(R.string.txt_folder_created) + " : " + folder.getAbsolutePath(), Toast.LENGTH_LONG).show();
            }

            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        File file2 = new File(folder.getAbsolutePath(), "Woohoo.wav");

                        if (!file2.exists()) {
                            FileOutputStream save = new FileOutputStream(file2);

                            byte[] buffer = null;
                            InputStream fIn = getResources().openRawResource(R.raw.woohoo);
                            int size = 0;

                            try {
                                size = fIn.available();
                                buffer = new byte[size];
                                fIn.read(buffer);
                                fIn.close();
                                save.write(buffer);

                            } catch (FileNotFoundException e) {
                                // TODO Auto-generated catch block
                            } catch (IOException e) {
                                // TODO Auto-generated catch block
                            }

                            save.flush();
                            save.close();
                        }
                    } catch (Exception e) {

                    }
                }
            });
        } catch (Exception e) {
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        // Inflate the layout for this fragment
        scrollViewBalances.fullScroll(View.FOCUS_UP);
        scrollViewBalances.pageScroll(View.FOCUS_UP);
        onClicked = false;
        final String hide_donations_isChanged = "hide_donations_isChanged";
        Boolean isHideDonationsChanged = false;
        if (Helper.containKeySharePref(getContext(), hide_donations_isChanged)) {
            if (Helper.fetchBoolianSharePref(getContext(), hide_donations_isChanged)) {
                isHideDonationsChanged = true;
                Helper.storeBoolianSharePref(getContext(), hide_donations_isChanged, false);
            }
        }
        Boolean isCheckedTimeZone = false;
        isCheckedTimeZone = Helper.fetchBoolianSharePref(getActivity(), getString(R.string.pre_ischecked_timezone));
        Boolean accountNameChange = checkIfAccountNameChange();

        if (accountNameChange || (finalFiatCurrency != null && !Helper.getFadeCurrency(getContext()).equals(finalFiatCurrency)))
            llBalances.removeAllViews();

        if (isHideDonationsChanged || accountNameChange || (finalFiatCurrency != null && !Helper.getFadeCurrency(getContext()).equals(finalFiatCurrency))) {
            if (finalFiatCurrency != null && !Helper.getFadeCurrency(getContext()).equals(finalFiatCurrency)) {
                loadBasic(true, accountNameChange, true);
            } else {
                loadBasic(true, accountNameChange, false);
            }

        }
        else {
            //If Balance change (send funds) this will run to update it.
            Application app = (Application)this.getContext().getApplicationContext();
            if((app.getUpdateFunds())){
                Log.d(TAG, "Updating funds (getUpdateFunds() is true)");
                app.setUpdateFunds(false);
                //Update Balances
                loadBasic(false, true, false);
                //Update Table Views getting data from database
                updateTableView(true);
            }
        }


        if (!accountId.equals("")) {
            UserAccount me = new UserAccount(accountId);
            start = (historicalTransferCount * HISTORICAL_TRANSFER_BATCH_SIZE);
            stop = start + HISTORICAL_TRANSFER_BATCH_SIZE + 1;
            Log.i(TAG, String.format("Calling get_relative_account_history. start: %d, limit: %d, stop: %d", start, HISTORICAL_TRANSFER_BATCH_SIZE, stop));
            transferHistoryThread = new WebsocketWorkerThread(new GetRelativeAccountHistory(me, start, HISTORICAL_TRANSFER_BATCH_SIZE, stop, mTransferHistoryListener));
            transferHistoryThread.start();
        } else {
            Log.d(TAG, "account id is empty");
        }

        // Loading transfers from database
        //updateTableView(true);

    }

    @OnClick(R.id.receivebtn)
    public void GoToReceiveActivity() {
        final Intent intent = new Intent(getActivity(), ReceiveActivity.class);
        intent.putExtra(getString(R.string.to), to);
        intent.putExtra(getString(R.string.account_id), accountId);
        Animation coinAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.coin_animation);
        coinAnimation.setAnimationListener(new android.view.animation.Animation.AnimationListener() {

            @Override
            public void onAnimationEnd(Animation animation) {
                startActivity(intent);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationStart(Animation animation) {
            }
        });
        receivebtn.startAnimation(coinAnimation);
    }

    @OnClick(R.id.sendbtn)
    public void GoToSendActivity() {
        final Intent intent = new Intent(getActivity(), SendScreen.class);
        Animation coinAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.coin_animation);
        coinAnimation.setAnimationListener(new android.view.animation.Animation.AnimationListener() {

            @Override
            public void onAnimationEnd(Animation animation) {
                startActivity(intent);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationStart(Animation animation) {
            }
        });
        sendbtn.startAnimation(coinAnimation);
    }

    @OnClick(R.id.tvUpgradeLtm)
    public void updateLtm() {

        final boolean[] balanceValid = {true};
        final Dialog dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.alert_confirmation_dialog);
        final Button btnDone = (Button) dialog.findViewById(R.id.btnDone);
        final TextView alertMsg = (TextView) dialog.findViewById(R.id.alertMsg);
        alertMsg.setText(getString(R.string.help_message));
        final Button btnCancel = (Button) dialog.findViewById(R.id.btnCancel);
        btnCancel.setBackgroundColor(Color.RED);
        btnCancel.setText(getString(R.string.txt_no));
        btnDone.setText(getString(R.string.next));

        btnDone.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("StringFormatInvalid")
            @Override
            public void onClick(View v) {
                String ltmAmount = Helper.fetchStringSharePref(getActivity(), "ltmAmount");
                //Check Balance
                if (btnDone.getText().equals(getString(R.string.next))) {
                    alertMsg.setText(getString(R.string.upgrade_to_ltm) + ltmAmount + getString(R.string.bts_will_be_deducted) + tvAccountName.getText().toString() + getString(R.string.account));
                    btnDone.setText(getString(R.string.txt_yes));
                    btnCancel.setText(getString(R.string.txt_back));
                } else {
                    dialog.cancel();
                    ArrayList<AccountDetails> accountDetails = tinyDB.getListObject(getString(R.string.pref_wallet_accounts), AccountDetails.class);

                    try {
                        for (int i = 0; i < accountDetails.size(); i++) {
                            if (accountDetails.get(i).isSelected) {
                                ArrayList<AccountAssets> arrayListAccountAssets = accountDetails.get(i).AccountAssets;
                                for (int j = 0; j < arrayListAccountAssets.size(); j++) {
                                    AccountAssets accountAssets = arrayListAccountAssets.get(j);
                                    if (accountAssets.symbol.equalsIgnoreCase("BTS")) {
                                        Double amount = Double.valueOf(SupportMethods.ConvertValueintoPrecision(accountAssets.precision, accountAssets.ammount));
                                        if (amount < Double.parseDouble(ltmAmount)) {
                                            balanceValid[0] = false;
                                            Toast.makeText(getActivity(), getString(R.string.insufficient_funds), Toast.LENGTH_LONG).show();
                                        }
                                        break;
                                    }
                                }

                            }
                        }
                    } catch (Exception e) {
                    }
                    if (balanceValid[0]) {
                        showDialog("", getString(R.string.upgrading));
                    }

                }
            }

        });
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (btnCancel.getText().equals(getString(R.string.txt_back))) {
                    alertMsg.setText(getString(R.string.help_message));
                    btnCancel.setText(getString(R.string.txt_no));
                    btnDone.setText(getString(R.string.next));
                } else {
                    dialog.cancel();
                }
            }
        });
        dialog.show();
    }

    @OnClick(R.id.qrCamera)
    public void QrCodeActivity() {
        final Intent intent = new Intent(getContext(), QRCodeActivity.class);
        intent.putExtra("id", 1);
        Animation coinAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.coin_animation);
        coinAnimation.setAnimationListener(new android.view.animation.Animation.AnimationListener() {

            @Override
            public void onAnimationEnd(Animation animation) {
                startActivity(intent);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationStart(Animation animation) {
            }
        });
        qrCamera.startAnimation(coinAnimation);
    }

    @OnClick(R.id.exportButton)
    public void onExportButton() {
        int permission = ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    getActivity(),
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_EXTERNAL_STORAGE
            );
        } else {
            UserAccount currentUser = new UserAccount(accountId);
            List<HistoricalTransferEntry> transfers = database.getTransactions(currentUser, SCWallDatabase.UNLIMITED_TRANSACTIONS);
            pdfGeneratorTask = new PdfGeneratorTask(getContext(), currentUser, this);
            pdfGeneratorTask.execute(transfers.toArray(new HistoricalTransferEntry[transfers.size()]));

            if (pdfProgress == null) {
                pdfProgress = new ProgressDialog(getContext());
            }
            pdfProgress.setMessage(getResources().getString(R.string.progress_pdf_generation));
            pdfProgress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            pdfProgress.setIndeterminate(false);
            pdfProgress.setMax(100);
            pdfProgress.setProgress(0);
            pdfProgress.show();
        }
    }

    public void loadBalancesFromSharedPref() {
        try {
            ArrayList<AccountDetails> accountDetails = tinyDB.getListObject(getString(R.string.pref_wallet_accounts), AccountDetails.class);

            if (accountDetails.size() > 1) {
                ivMultiAccArrow.setVisibility(View.VISIBLE);
            } else {
                ivMultiAccArrow.setVisibility(View.GONE);
            }


            for (int i = 0; i < accountDetails.size(); i++) {
                if (accountDetails.get(i).isSelected) {
                    ArrayList<AccountAssets> accountAsset = accountDetails.get(i).AccountAssets;

                    if ((accountAsset != null) && (accountAsset.size() > 0)) {
                        ArrayList<String> sym = new ArrayList<>();
                        ArrayList<String> pre = new ArrayList<>();
                        ArrayList<String> am = new ArrayList<>();

                        for (int j = 0; j < accountAsset.size(); j++) {
                            pre.add(j, accountAsset.get(j).precision);
                            sym.add(j, accountAsset.get(j).symbol);
                            am.add(j, accountAsset.get(j).ammount);
                        }

                        BalanceAssetsUpdate(sym, pre, am, true);
                    }

                    break;
                }
            }
        } catch (Exception e) {

        }


    }

    @Override
    public void isUpdate(ArrayList<String> ids, ArrayList<String> sym, ArrayList<String> pre, ArrayList<String> am) {
        if (isAdded()) {
            ArrayList<AccountDetails> accountDetails = tinyDB.getListObject(getString(R.string.pref_wallet_accounts), AccountDetails.class);
            ArrayList<AccountAssets> accountAssets = new ArrayList<>();

            for (int i = 0; i < ids.size(); i++) {
                long amount = Long.parseLong(am.get(i));

                if (amount != 0) {
                    AccountAssets accountAsset = new AccountAssets();
                    accountAsset.id = ids.get(i);
                    if (pre.size() > i) accountAsset.precision = pre.get(i);
                    if (sym.size() > i) accountAsset.symbol = sym.get(i);
                    if (am.size() > i) accountAsset.ammount = am.get(i);
                    accountAssets.add(accountAsset);
                }
            }

            try {
                for (int i = 0; i < accountDetails.size(); i++) {
                    if (accountDetails.get(i).isSelected) {
                        accountDetails.get(i).AccountAssets = accountAssets;
                        getEquivalentComponents(accountAssets);
                        break;
                    }
                }
            } catch (Exception w) {
                SupportMethods.testing("Assets", w, "Asset Activity");
            }

            tinyDB.putListObject(getString(R.string.pref_wallet_accounts), accountDetails);
            BalanceAssetsUpdate(sym, pre, am, false);
        }
    }

    public BalanceItems getBalanceItems() {
        if (this.balanceItems == null) {
            this.balanceItems = new BalanceItems();
            this.balanceItems.addListener(this);
        }

        return this.balanceItems;
    }

    public void BalanceAssetsUpdate(final ArrayList<String> sym, final ArrayList<String> pre, final ArrayList<String> am, final Boolean onStartUp) {
        int count = llBalances.getChildCount();

        // use standard asset names (like add bit etc)
        ArrayList<String> symbols = new ArrayList();
        for (String symbol : sym) {
            if (SMARTCOINS.contains(symbol)) {
                symbols.add("bit" + symbol);
            } else {
                symbols.add(symbol);
            }
        }

        if (count <= 0)
            BalanceAssetsLoad(symbols, pre, am, onStartUp);
        if (count > 0)
            BalanceAssetsUpdate(symbols, pre, am);
    }

    public void processAssets(final HashMap<String, ArrayList<String>> currencies, final HashMap<String, Asset> assets, final Runnable getEquivalentCompRunnable) {
        for (final String base : currencies.keySet()) {
            if (assets.containsKey(base)) {
                for (final String quote : currencies.get(base)) {
                    if (assets.containsKey(quote)) {
                        WebsocketWorkerThread glo = new WebsocketWorkerThread(new GetLimitOrders(assets.get(base).getObjectId(), assets.get(quote).getObjectId(), 20, new WitnessResponseListener() {
                            @Override
                            public void onSuccess(WitnessResponse response) {
                                if (response.result.getClass() == ArrayList.class) {
                                    ArrayList list = (ArrayList) response.result;
                                    for (Object listObject : list) {
                                        if (listObject.getClass() == Market.class) {
                                            Market market = ((Market) listObject);
                                            if (!market.sell_price.base.asset_id.equalsIgnoreCase(assets.get(base).getObjectId())) {
                                                double price = (double) market.sell_price.base.amount / (double) market.sell_price.quote.amount;
                                                int exp = assets.get(base).getPrecision() - assets.get(quote).getPrecision();
                                                price = (price * Math.pow(10, exp));
                                                updateEquivalentValue(base, Double.toString(price), getEquivalentCompRunnable);
                                                return;
                                            }
                                        }
                                    }
                                }
                                getEquivalentValueIndirect(assets.get(base), assets.get(quote), assets.get("BTS"));
                            }

                            @Override
                            public void onError(BaseResponse.Error error) {
                                Log.e(TAG, "Error getLimitOrder " + error.message);
                                //TODO handle limitOrder error
                            }
                        }));
                        glo.start();
                    } else {
                        Log.e(TAG, "Quote is not in assetlist");
                        //TODO handle quote null error
                    }
                }
            } else {
                Log.e(TAG, "Base is not in assetlist");
                //TODO handle base error
            }
        }
    }

    public void getEquivalentComponent(final HashMap<String, ArrayList<String>> currencies, final Runnable getEquivalentCompRunnable) {

        ArrayList<String> assetList = new ArrayList();
        for (String key : currencies.keySet()) {
            if (!assetList.contains(key)) {
                assetList.add(key);
            }
            for (String values : currencies.get(key)) {
                if (!assetList.contains(values)) {
                    assetList.add(values);
                }
            }
        }
        WebsocketWorkerThread wwThread = new WebsocketWorkerThread(new GetAssets(assetList, new WitnessResponseListener() {
            @Override
            public void onSuccess(WitnessResponse response) {
                if (response.result.getClass() == ArrayList.class) {
                    ArrayList list = (ArrayList) response.result;
                    final HashMap<String, Asset> assets = new HashMap();
                    for (Object listObject : list) {
                        if (listObject != null) {
                            if (listObject.getClass() == Asset.class) {
                                Asset asset = (Asset) listObject;
                                assets.put(asset.getSymbol(), asset);
                            }
                        }
                    }
                    processAssets(currencies, assets, getEquivalentCompRunnable);
                }
            }

            @Override
            public void onError(BaseResponse.Error error) {
                Log.e(TAG, "Error in GetAssets " + error.message);
                //TODO error handle getasset errror
            }
        }));
        wwThread.start();
    }

    private void updateEquivalentValue(String assetName, String value, Runnable getEquivalentCompRunnable) {
        if (this.balanceItems.findBalanceItemBySymbol(assetName) != null) {
            this.balanceItems.updateFiatBalanceItem(assetName, value);
        } else {
            Log.i(TAG, "tvAsset tv Amount tvFiatAmount nulls");
            updateEquivalentAmount.postDelayed(getEquivalentCompRunnable, 500);
        }
    }

    private void getEquivalentComponents(final ArrayList<AccountAssets> accountAssets) {
        final Runnable getEquivalentCompRunnable = new Runnable() {
            @Override
            public void run() {
                getEquivalentComponents(accountAssets);
            }
        };

        String fiatCurrency = Helper.getFadeCurrency(getContext());

        if (fiatCurrency.isEmpty()) {
            fiatCurrency = "EUR";
        }
        finalFiatCurrency = fiatCurrency;

        HashMap<String, ArrayList<String>> currenciesChange = new HashMap();

        for (int i = 0; i < accountAssets.size(); i++) {
            AccountAssets accountAsset = accountAssets.get(i);
            if (!accountAsset.symbol.equals(fiatCurrency)) {
                if (!currenciesChange.containsKey(accountAsset.symbol)) {
                    currenciesChange.put(accountAsset.symbol, new ArrayList());
                }
                currenciesChange.get(accountAsset.symbol).add(fiatCurrency);
            }
        }
        this.getEquivalentComponent(currenciesChange, getEquivalentCompRunnable);
    }

    private void getEquivalentValueIndirect(final Asset indirectAsset, final Asset fiatCurrency, final Asset reference) {

        final WebsocketWorkerThread glo = new WebsocketWorkerThread(new GetLimitOrders(indirectAsset.getObjectId(), reference.getObjectId(), 20, new WitnessResponseListener() {
            @Override
            public void onSuccess(WitnessResponse response) {
                if (response.result.getClass() == ArrayList.class) {
                    ArrayList list = (ArrayList) response.result;
                    for (Object listObject : list) {
                        if (listObject.getClass() == Market.class) {
                            Market market = ((Market) listObject);

                            if (!market.sell_price.base.asset_id.equalsIgnoreCase(indirectAsset.getObjectId())) {
                                double price = (double) market.sell_price.base.amount / (double) market.sell_price.quote.amount;
                                int exp = indirectAsset.getPrecision() - reference.getPrecision();
                                price = price * Math.pow(10, exp) * BTSCurrencyPriceCache;
                                updateEquivalentValue(indirectAsset.getSymbol(), Double.toString(price), null);
                                return;
                            }
                        }
                    }
                }
            }

            @Override
            public void onError(BaseResponse.Error error) {

            }
        }));


        Date now = new Date();
        if ((BTSCurrencyPriceCacheDate != null) && (now.getTime() - BTSCurrencyPriceCacheDate.getTime() <= 300000)) { //if the cache date of the asset is too old, 300000 = 5 minutes
            glo.start();
        } else {
            WebsocketWorkerThread middle = new WebsocketWorkerThread(new GetLimitOrders(reference.getObjectId(), fiatCurrency.getObjectId(), 20, new WitnessResponseListener() {
                @Override
                public void onSuccess(WitnessResponse response) {
                    if (response.result.getClass() == ArrayList.class) {
                        ArrayList list = (ArrayList) response.result;
                        for (Object listObject : list) {
                            if (listObject.getClass() == Market.class) {
                                Market market = ((Market) listObject);
                                if (!market.sell_price.base.asset_id.equalsIgnoreCase(reference.getObjectId())) {
                                    double price = (double) market.sell_price.base.amount / (double) market.sell_price.quote.amount;
                                    int exp = reference.getPrecision() - fiatCurrency.getPrecision();
                                    final double middlePrice = price * Math.pow(10, exp);
                                    BTSCurrencyPriceCache = middlePrice;
                                    BTSCurrencyPriceCacheDate = new Date();

                                    glo.start();
                                    return;
                                }
                            }
                        }
                    }
                }

                @Override
                public void onError(BaseResponse.Error error) {
                }
            }));
            middle.start();
        }
    }

    private void updateBalanceArrays(final ArrayList<String> sym, final ArrayList<String> pre, final ArrayList<String> am, boolean startUp) {
        try {
            this.getBalanceItems().clear();

            for (int i = 0; i < sym.size(); i++) {
                Long _amount = Long.parseLong(am.get(i));

                // remove balances which are zero
                if (_amount != 0) {
                    this.getBalanceItems().addBalanceItem(sym.get(i), pre.get(i), am.get(i), startUp);
                }
            }
        } catch (Exception e) {

        }
    }

    public void onNewBalanceItem(BalanceItemsEvent event) {
        final BalanceItem item = event.getBalanceItem();
        final boolean isInitialLoad = event.isInitialLoad();

        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                progressBar1.setVisibility(View.VISIBLE);
                addNewBalanceView(item, isInitialLoad);
                progressBar1.setVisibility(View.INVISIBLE);
            }
        });
    }

    public void onBalanceItemRemoved(BalanceItemsEvent event) {
        final BalanceItem item = event.getBalanceItem();
        final int index = event.getIndex();
        final int size = event.getNewSize();

        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                progressBar1.setVisibility(View.VISIBLE);
                removeBalanceItemView(item, index, size);
                progressBar1.setVisibility(View.INVISIBLE);
            }
        });
    }

    public void onBalanceItemUpdated(BalanceItemsEvent event) {
        final BalanceItem oldItem = event.getOldItem();
        final BalanceItem newItem = event.getBalanceItem();
        final int index = event.getIndex();

        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                progressBar1.setVisibility(View.VISIBLE);
                updateBalanceItem(oldItem, newItem, index);
                progressBar1.setVisibility(View.INVISIBLE);
            }

        });
    }

    public void removeBalanceItemView(BalanceItem item, int index, int newSize) {
        if (index < llBalances.getChildCount() * 2) {
            TextView symbolTextViewToOccupy;
            TextView ammountTextViewToOccupy;
            TextView fiatTextViewToOccupy;
            TextView symbolTextViewToMoveOut;
            TextView ammountTextViewToMoveOut;
            TextView fiatTextViewToMoveOut;

            for (int i = index; i < llBalances.getChildCount() * 2 - 1; i++) {
                View rowView = llBalances.getChildAt(i / 2);

                if (i % 2 == 0) {
                    symbolTextViewToOccupy = (TextView) rowView.findViewById(R.id.symbol_child_one);
                    ammountTextViewToOccupy = (TextView) rowView.findViewById(R.id.amount_child_one);
                    fiatTextViewToOccupy = (TextView) rowView.findViewById(R.id.fiat_child_one);
                    symbolTextViewToMoveOut = (TextView) rowView.findViewById(R.id.symbol_child_two);
                    ammountTextViewToMoveOut = (TextView) rowView.findViewById(R.id.amount_child_two);
                    fiatTextViewToMoveOut = (TextView) rowView.findViewById(R.id.fiat_child_two);
                } else {
                    symbolTextViewToOccupy = (TextView) rowView.findViewById(R.id.symbol_child_two);
                    ammountTextViewToOccupy = (TextView) rowView.findViewById(R.id.amount_child_two);
                    fiatTextViewToOccupy = (TextView) rowView.findViewById(R.id.fiat_child_two);

                    View nextRowView = llBalances.getChildAt((i / 2) + 1);

                    symbolTextViewToMoveOut = (TextView) nextRowView.findViewById(R.id.symbol_child_one);
                    ammountTextViewToMoveOut = (TextView) nextRowView.findViewById(R.id.amount_child_one);
                    ;
                    fiatTextViewToMoveOut = (TextView) nextRowView.findViewById(R.id.fiat_child_one);
                }

                symbolTextViewToOccupy.setText(symbolTextViewToMoveOut.getText());
                ammountTextViewToOccupy.setText(ammountTextViewToMoveOut.getText());
                fiatTextViewToOccupy.setText(fiatTextViewToMoveOut.getText());
                symbolTextViewToOccupy.setVisibility(View.VISIBLE);
                ammountTextViewToOccupy.setVisibility(View.VISIBLE);
                fiatTextViewToOccupy.setVisibility(View.VISIBLE);
                symbolTextViewToMoveOut.setVisibility(View.INVISIBLE);
                ammountTextViewToMoveOut.setVisibility(View.INVISIBLE);
                fiatTextViewToMoveOut.setVisibility(View.INVISIBLE);
            }

            if (newSize % 2 == 0) {
                View rowView = llBalances.getChildAt(llBalances.getChildCount() - 1);
                llBalances.removeView(rowView);

                if (llBalances.getChildCount() == 0) {
                    whiteSpaceAfterBalances.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    public void addNewBalanceView(final BalanceItem item, boolean initialLoad) {
        TextView textView2 = null;
        TextView symbolTextView;
        final TextView ammountTextView;
        View lastChild = null;
        boolean theresNoChild = true;

        if (llBalances.getChildCount() > 0) {//if there's items in balances
            //we take the right side of the last child of the balances
            lastChild = llBalances.getChildAt(llBalances.getChildCount() - 1);
            textView2 = (TextView) lastChild.findViewById(R.id.symbol_child_two);
            theresNoChild = false;
        }

        //if there's no items in the balances or the right side of the last child
        //is already occupied, then we have to create a new View and occupy the left side
        if (theresNoChild || !textView2.getText().equals("")) {
            LayoutInflater layoutInflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View customView = layoutInflater.inflate(R.layout.items_rows_balances, null);
            symbolTextView = (TextView) customView.findViewById(R.id.symbol_child_one);
            ammountTextView = (TextView) customView.findViewById(R.id.amount_child_one);

            TextView rightSymbolTextView = (TextView) customView.findViewById(R.id.symbol_child_two);
            TextView rightAmmountTextView = (TextView) customView.findViewById(R.id.amount_child_two);
            rightSymbolTextView.setText("");
            rightAmmountTextView.setText("");

            llBalances.addView(customView);
        } else {
            //In this point the right side is free, so we can use it
            symbolTextView = (TextView) lastChild.findViewById(R.id.symbol_child_two);
            ammountTextView = (TextView) lastChild.findViewById(R.id.amount_child_two);
        }

        String finalSymbol = "";
        if (SMARTCOINS.contains(item.getSymbol())) {
            finalSymbol = "bit" + item.getSymbol();
        } else {
            finalSymbol = item.getSymbol();
        }


        final AssetsSymbols assetsSymbols = new AssetsSymbols(getContext());
        assetsSymbols.displaySpannable(symbolTextView, finalSymbol);

        float b = powerInFloat(item.getPrecision(), item.getAmount());
        if (SMARTCOINS.contains(item.getSymbol().replace("bit", ""))) {
            ammountTextView.setText(String.format(locale, "%.2f", b));
        } else if (assetsSymbols.isUiaSymbol(item.getSymbol()))
            ammountTextView.setText(String.format(locale, "%.4f", b));
        else if (assetsSymbols.isSmartCoinSymbol(item.getSymbol()))
            ammountTextView.setText(String.format(locale, "%.2f", b));
        else ammountTextView.setText(String.format(locale, "%.4f", b));

        //if it's not the initial load, then is a balance received, then we must show an animation and sound
        if (!initialLoad) {
            Log.d("Balances Update", "Balance received");

            ammountTextView.setTypeface(ammountTextView.getTypeface(), Typeface.BOLD);
            ammountTextView.setTextColor(getResources().getColor(R.color.green));
            AudioFilePath audioFilePath = new AudioFilePath(getContext());
            if (!audioFilePath.fetchAudioEnabled()) {
                audioSevice = true;
                getActivity().startService(new Intent(getActivity(), MediaService.class));
            }

            final Runnable rotateTask = new Runnable() {
                @Override
                public void run() {
                    try {
                        getActivity().runOnUiThread(new Runnable() {
                            public void run() {
                                rotateReceiveButton();
                            }
                        });

                    } catch (Exception e) {

                    }
                }
            };
            animateNsoundHandler.postDelayed(rotateTask, 200);
            Log.d("Balances Update", "Animation initiated");
            animateText(ammountTextView, 0, convertLocalizeStringToFloat(returnFromPower(item.getPrecision(), item.getAmount())));
            Log.d("Balances Update", "Text Animated");

            final Runnable updateTask = new Runnable() {
                @Override
                public void run() {
                    try {
                        ammountTextView.setTextColor(getResources().getColor(R.color.receive_amount));
                    } catch (Exception e) {

                    }
                }
            };
            animateNsoundHandler.postDelayed(updateTask, 4000);
        }
    }

    public void updateBalanceItem(final BalanceItem oldItem, final BalanceItem newItem, final int index) {
        final Runnable reloadBalances = new Runnable() {
            @Override
            public void run() {
                removeZeroedBalanceViews();
            }
        };

        if (index < llBalances.getChildCount() * 2) {
            View rowView = llBalances.getChildAt(index / 2);
            final TextView symbolTextView;
            final TextView ammountTextView;
            final TextView fiatTextView;

            if (index % 2 == 0) {
                symbolTextView = (TextView) rowView.findViewById(R.id.symbol_child_one);
                ammountTextView = (TextView) rowView.findViewById(R.id.amount_child_one);
                fiatTextView = (TextView) rowView.findViewById(R.id.fiat_child_one);
            } else {
                symbolTextView = (TextView) rowView.findViewById(R.id.symbol_child_two);
                ammountTextView = (TextView) rowView.findViewById(R.id.amount_child_two);
                fiatTextView = (TextView) rowView.findViewById(R.id.fiat_child_two);
            }

            String finalSymbol = "";
            if (SMARTCOINS.contains(newItem.getSymbol())) {
                finalSymbol = "bit" + newItem.getSymbol();
            } else {
                finalSymbol = newItem.getSymbol();
            }

            final AssetsSymbols assetsSymbols = new AssetsSymbols(getContext());
            assetsSymbols.displaySpannable(symbolTextView, finalSymbol);

            Long oldAmmount = Long.parseLong(oldItem.getAmount());
            Long newAmmount = Long.parseLong(newItem.getAmount());

            if (oldAmmount > newAmmount) {
                Log.d("Balances Update", "Balance sent");

                ammountTextView.setTypeface(ammountTextView.getTypeface(), Typeface.BOLD);
                ammountTextView.setTextColor(getResources().getColor(R.color.red));

                animateText(ammountTextView, convertLocalizeStringToFloat(ammountTextView.getText().toString()), convertLocalizeStringToFloat(returnFromPower(newItem.getPrecision(), newItem.getAmount())));

                final Runnable updateTask = new Runnable() {
                    @Override
                    public void run() {
                        try {
                            ammountTextView.setTextColor(getResources().getColor(R.color.receive_amount));
                        } catch (Exception e) {

                        }
                    }
                };
                animateNsoundHandler.postDelayed(updateTask, 4000);

                if (newAmmount == 0) {
                    final Runnable zeroAmount = new Runnable() {
                        @Override
                        public void run() {
                            try {
                                ammountTextView.setText("");
                                symbolTextView.setText("");
                                fiatTextView.setText("");
                            } catch (Exception e) {

                            }
                        }
                    };

                    animateNsoundHandler.postDelayed(zeroAmount, 4200);
                    animateNsoundHandler.postDelayed(reloadBalances, 5000);
                }

                Log.d("Balances Update", "Animation initiated");
            } else if (oldAmmount < newAmmount) {

                Log.d("Balances Update", "Balance received");

                ammountTextView.setTypeface(ammountTextView.getTypeface(), Typeface.BOLD);
                ammountTextView.setTextColor(getResources().getColor(R.color.green));

                // run animation
                //if (animateOnce) {
                AudioFilePath audioFilePath = new AudioFilePath(getContext());
                if (!audioFilePath.fetchAudioEnabled()) {
                    audioSevice = true;
                    getActivity().startService(new Intent(getActivity(), MediaService.class));
                }

                final Runnable rotateTask = new Runnable() {
                    @Override
                    public void run() {
                        try {
                            getActivity().runOnUiThread(new Runnable() {
                                public void run() {
                                    rotateReceiveButton();
                                }
                            });

                        } catch (Exception e) {

                        }
                    }
                };

                animateNsoundHandler.postDelayed(rotateTask, 200);

                //animateOnce = false;

                Log.d("Balances Update", "Animation initiated");
                //}

                animateText(ammountTextView, convertLocalizeStringToFloat(ammountTextView.getText().toString()), convertLocalizeStringToFloat(returnFromPower(newItem.getPrecision(), newItem.getAmount())));

                Log.d("Balances Update", "Text Animated");

                final Runnable updateTask = new Runnable() {
                    @Override
                    public void run() {
                        try {
                            ammountTextView.setTextColor(getResources().getColor(R.color.receive_amount));
                        } catch (Exception e) {

                        }
                    }
                };
                animateNsoundHandler.postDelayed(updateTask, 4000);

                if (newAmmount == 0) {
                    final Runnable zeroAmount = new Runnable() {
                        @Override
                        public void run() {
                            try {
                                ammountTextView.setText("");
                                symbolTextView.setText("");
                                fiatTextView.setText("");
                            } catch (Exception e) {

                            }
                        }
                    };

                    animateNsoundHandler.postDelayed(zeroAmount, 4200);
                    animateNsoundHandler.postDelayed(reloadBalances, 5000);
                }
            }

            //Now, we update the fiat (EquivalentComponent)
            if ((newAmmount != 0) && (!newItem.getFiat().equals(""))) {
                try {
                    Log.d("Equivalent Value Update", "Changing Fiat Text: " + newItem.getSymbol());
                    final Currency currency = Currency.getInstance(finalFiatCurrency);
                    double d = convertLocalizeStringToDouble(returnFromPower(newItem.getPrecision(), newItem.getAmount()));
                    final Double eqAmount = d * convertLocalizeStringToDouble(newItem.getFiat());
                    String fiatString = "";
                    if (Helper.isRTL(locale, currency.getSymbol())) {
                        fiatString = String.format(locale, "%.2f %s", eqAmount, currency.getSymbol());
                    } else {
                        fiatString = String.format(locale, "%s %.2f", currency.getSymbol(), eqAmount);
                    }

                    fiatTextView.setText(fiatString);

                    fiatTextView.setVisibility(View.VISIBLE);
                } catch (Exception e) {
                    Log.e(TAG, "Error in updateEquivalentValue : " + e.getMessage());
                    for (StackTraceElement element : e.getStackTrace()) {
                        Log.e(TAG, element.toString());
                    }
                    fiatTextView.setVisibility(View.GONE);
                }
            }
        }
    }

    public void BalanceAssetsLoad(final ArrayList<String> sym, final ArrayList<String> pre, final ArrayList<String> am, final Boolean onStartUp) {

        final AssetsSymbols assetsSymbols = new AssetsSymbols(getContext());


        updateBalanceArrays(sym, pre, am, onStartUp);

        //TODO this shouldn't be loading in the UI Thread, there's nothing UI here
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                try {
                    ArrayList<AccountDetails> accountDetails = tinyDB.getListObject(getString(R.string.pref_wallet_accounts), AccountDetails.class);
                    for (int i = 0; i < accountDetails.size(); i++) {
                        if (accountDetails.get(i).isSelected) {
                            getEquivalentComponents(accountDetails.get(i).AccountAssets);
                            break;
                        }
                    }
                } catch (Exception w) {
                    SupportMethods.testing("Assets", w, "Asset Activity");
                }
                /*}

                whiteSpaceAfterBalances.setVisibility(View.GONE);*/
            }
        });
    }

    private void rotateReceiveButton() {
        ImageView rcvBtn = (ImageView) getActivity().findViewById(R.id.receivebtn);
        final Animation rotAnim = AnimationUtils.loadAnimation(getContext(), R.anim.rotate360);
        rcvBtn.startAnimation(rotAnim);
    }

    public void playSound() {
        Log.d(TAG, "playSound");
        try {
            AudioFilePath audioFilePath = new AudioFilePath(getContext());
            MediaPlayer mediaPlayer = audioFilePath.fetchMediaPlayer();
            if (mediaPlayer != null)
                mediaPlayer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void animateText(final TextView tvCounter, float startValue, float endValue) {
        ValueAnimator animator = new ValueAnimator();
        animator.setFloatValues(startValue, endValue);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float animateValue = Float.parseFloat(String.valueOf(animation.getAnimatedValue()));
                tvCounter.setText(Helper.setLocaleNumberFormat(locale, animateValue));
            }
        });
        animator.setEvaluator(new TypeEvaluator<Float>() {
            public Float evaluate(float fraction, Float startValue, Float endValue) {
                return startValue + (endValue - startValue) * fraction;
            }
        });
        animator.setDuration(2000);
        animator.start();
    }

    public void removeZeroedBalanceViews() {
        this.getBalanceItems().removeZeroBalanceItems();
    }

    public void BalanceAssetsUpdate(final ArrayList<String> sym, final ArrayList<String> pre, final ArrayList<String> am) {
        for (int i = 0; i < sym.size(); i++) {
            Long _amount = Long.parseLong(am.get(i));
            BalanceItem balanceItem = this.getBalanceItems().findBalanceItemBySymbol(sym.get(i));

            if ((balanceItem != null) || (_amount != 0)) {
                this.getBalanceItems().addOrUpdateBalanceItem(sym.get(i), pre.get(i), am.get(i));
            }
        }
    }

    String returnFromPower(String i, String str) {
        Double ok = 1.0;
        Double pre = Double.valueOf(i);
        Double value = Double.valueOf(str);
        for (int k = 0; k < pre; k++) {
            ok = ok * 10;
        }
        return Double.toString(value / ok);
    }

    float powerInFloat(String i, String str) {
        float ok = 1.0f;
        float pre = Float.parseFloat(i);
        float value = Float.parseFloat(str);
        for (int k = 0; k < pre; k++) {
            ok = ok * 10;
        }
        return (value / ok);
    }

    /**
     * Updating the sort strategy
     */
    private void updateSortTable() {
        SimpleTableHeaderAdapter simpleTableHeaderAdapter = new SimpleTableHeaderAdapter(getContext(), getContext().getString(R.string.date), getContext().getString(R.string.all), getContext().getString(R.string.to_from), getContext().getString(R.string.amount));
        simpleTableHeaderAdapter.setPaddingLeft(getResources().getDimensionPixelSize(R.dimen.transactionsheaderpading));
        transfersView.setHeaderAdapter(simpleTableHeaderAdapter);

        transfersView.setHeaderSortStateViewProvider(SortStateViewProviders.darkArrows());
        transfersView.setColumnWeight(0, 20);
        transfersView.setColumnWeight(1, 12);
        transfersView.setColumnWeight(2, 27);
        transfersView.setColumnWeight(3, 22);
        transfersView.setColumnComparator(0, new TransferDateComparator());
        transfersView.setColumnComparator(1, new TransferSendReceiveComparator(new UserAccount(accountId)));
        transfersView.setColumnComparator(3, new TransferAmountComparator());
    }

    @Override
    public void soundFinish() {
        if (audioSevice) {
            try{
                getActivity().stopService(new Intent(getActivity(), MediaService.class));
            }
            catch (Exception e) {
                Log.e(TAG, "Error when stopping sound service. Exception Msg: " + e.getMessage());
            }
        }
        audioSevice = false;
    }

    /**
     * Saves transaction list into the shared preferences storage.
     *
     * @param transactionDetails: List of TransactionDetails to store
     * @param accountName:        Account name.
     */
    private void putTransactions(List<TransactionDetails> transactionDetails, String accountName) {
        tinyDB.putTransactions(getResources().getString(R.string.pref_local_transactions) + accountName, new ArrayList<>(transactionDetails));
    }

    /**
     * Retrieves the transaction list from the shared preferences storage.
     *
     * @param accountName: Account name.
     * @return: List of transactions from a given account.
     */
    private ArrayList<TransactionDetails> getTransactions(String accountName) {
        ArrayList<TransactionDetails> mySavedList = tinyDB.getTransactions(getResources().getString(R.string.pref_local_transactions) + accountName, TransactionDetails.class);

        AssetsSymbols assetsSymbols = new AssetsSymbols(getContext());
        assetsSymbols.updatedTransactionDetails(mySavedList);

        for (TransactionDetails td : mySavedList) {
            td.updateContext(getContext());
        }

        return mySavedList;
    }

    @Override
    public void TransactionUpdate(final List<TransactionDetails> transactionDetails, final int number_of_transactions_in_queue) {

    }

    @Override
    public void transactionsLoadComplete(List<TransactionDetails> transactionDetails, int newTransactionsLoaded) {
        try {
            if (updateTriggerFromNetworkBroadcast && (newTransactionsLoaded == 0) && (counterRepeatTransactionLoad++ < 15)) {
                if (Application.isReady) {
                    Application.disconnect();
                }
                sentCallForTransactions = false;
                return;
            }

            updateTriggerFromNetworkBroadcast = false;
            counterRepeatTransactionLoad = 0;
            sentCallForTransactions = false;

            Context context = getContext();
            // update context
            for (TransactionDetails td : transactionDetails) {
                td.updateContext(context);
            }

            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (updateTransactionsList == null) {
                        updateTransactionsList = new Handler();
                    }

                    if (tableViewparent.getVisibility() != View.VISIBLE)
                        tableViewparent.setVisibility(View.VISIBLE);
                }
            });

        } catch (Exception e) {
            SupportMethods.testing("TransactionUpdate", e, "try/catch");
        }
    }

    @Override
    public void transactionsLoadMessageStatus(String message) {

    }

    @Override
    public void transactionsLoadFailure(String reason) {
        Log.d("LogTransactions", "transactionsLoadFailure");
        sentCallForTransactions = false;
    }

    @Override
    public void loadAgain() {
        if (updateTriggerFromNetworkBroadcast) {
            sentCallForTransactions = false;
        }
    }

    @OnClick(R.id.load_more_values)
    public void loadMoreTransactions() {
        loadMoreCounter++;
        updateTableView(false);
        int loadedTransaction = loadMoreCounter * SCWallDatabase.DEFAULT_TRANSACTION_BATCH_SIZE;
        int transactionCount = database.getTransactionCount(new UserAccount(accountId));
        if (loadedTransaction >= transactionCount) {
            loadMoreButton.setVisibility(View.GONE);
        }
    }

    void isLifeTime(final String name_id, final String id) {

        String getDetails = "{\"id\":" + id + ",\"method\":\"call\",\"params\":[";
        String getDetails2 = ",\"get_accounts\",[[\"" + name_id + "\"]]]}";
        myWebSocketHelper.make_websocket_call(getDetails, getDetails2, webSocketCallHelper.api_identifier.database);
    }

    @Override
    public void getLifetime(String s, int id) {
        myWebSocketHelper.cleanUpTransactionsHandler();

        ArrayList<AccountDetails> accountDetails = tinyDB.getListObject(getString(R.string.pref_wallet_accounts), AccountDetails.class);

        String result = SupportMethods.ParseJsonObject(s, "result");
        String nameObject = SupportMethods.ParseObjectFromJsonArray(result, 0);
        String expiration = SupportMethods.ParseJsonObject(nameObject, "membership_expiration_date");
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        try {
            Date date1 = dateFormat.parse(expiration);
            Date date2 = dateFormat.parse("1969-12-31T23:59:59");
            if (date2.getTime() >= date1.getTime()) {
                if (accountDetails.size() > accountDetailsId) {
                    accountDetails.get(accountDetailsId).isLifeTime = true;
                    showHideLifeTime(true);
                } else if (accountDetails.size() == 1) {
                    accountDetails.get(0).isLifeTime = true;
                    showHideLifeTime(true);
                }
                tinyDB.putListObject(getString(R.string.pref_wallet_accounts), accountDetails);

            }
        } catch (Exception e) {
            SupportMethods.testing("getLifetime", e, "Exception");

        }

    }

    void startAnimation() {
        scrollViewBalances.fullScroll(View.FOCUS_UP);
        scrollViewBalances.pageScroll(View.FOCUS_UP);
        qrCamera.setVisibility(View.INVISIBLE);
        backLine.setVisibility(View.INVISIBLE);
        final Animation animationFadeIn = AnimationUtils.loadAnimation(getContext(), R.anim.fade_in);
        final Animation animationRigthtoLeft = AnimationUtils.loadAnimation(getContext(), R.anim.home_anim);
        animationRigthtoLeft.setInterpolator(new AccelerateDecelerateInterpolator());
        qrCamera.postDelayed(new Runnable() {
            public void run() {
                qrCamera.startAnimation(animationRigthtoLeft);
                qrCamera.setVisibility(View.VISIBLE);
            }
        }, 333);


        backLine.postDelayed(new Runnable() {
            public void run() {
                backLine.setVisibility(View.VISIBLE);
                backLine.startAnimation(animationFadeIn);
            }
        }, 999);

    }

    @Override
    public void setUserVisibleHint(boolean visible) {
        super.setUserVisibleHint(visible);
        if (visible) {

            if (qrCamera != null && backLine != null) {
                startAnimation();
            } else {
                myHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (qrCamera != null && backLine != null) {
                            startAnimation();
                        } else myHandler.postDelayed(this, 333);
                    }
                }, 333);
            }
        }
    }

    private void loadOnDemand(final Activity _activity) {
        try {
            loadOndemand.removeCallbacksAndMessages(null);

            Runnable loadOnDemandRunnable = new Runnable() {
                @Override
                public void run() {

                    try {
                        _activity.runOnUiThread(new Runnable() {
                            public void run() {
                                loadViews(false, true, false);
                            }
                        });
                    } catch (Exception e) {
                    }
                }
            };
            loadOndemand.postDelayed(loadOnDemandRunnable, 1000);
        } catch (Exception e) {
        }
    }

    @Override
    public void loadAll() {
        updateTriggerFromNetworkBroadcast = true;
        loadOnDemand(getActivity());
    }

    void loadViews(Boolean onResume, Boolean accountNameChanged, boolean fiatCurrencyChanged) {

        if (firstTimeLoad) {

//            tableViewparent.setVisibility(View.GONE);
//            myTransactions = new ArrayList<>();
            //TODO: Implement this
//            updateSortTableView(tableView, myTransactions);

            //TODO: Implement this
//            tableView.addDataClickListener(new TableViewClickListener(getContext()));
//            progressBar.setVisibility(View.VISIBLE);

            firstTimeLoad = false;
        }

        whiteSpaceAfterBalances.setVisibility(View.VISIBLE);

        if (myAssetsActivity == null) {
            myAssetsActivity = new AssetsActivity(getContext(), to, this, null);
            myAssetsActivity.registerDelegate();
        }

        if (!onResume || accountNameChanged || fiatCurrencyChanged) {
            myAssetsActivity.loadBalances(to);
        }
    }

    void loadBasic(boolean onResume, boolean accountNameChanged, boolean fiatCurrencyChanged) {

        ArrayList<AccountDetails> accountDetails = tinyDB.getListObject(getString(R.string.pref_wallet_accounts), AccountDetails.class);
        if (accountDetails.size() == 1) {
            accountDetailsId = 0;
            accountDetails.get(0).isSelected = true;
            to = accountDetails.get(0).account_name;
            accountId = accountDetails.get(0).account_id;
            wifkey = accountDetails.get(0).wif_key;
            showHideLifeTime(accountDetails.get(0).isLifeTime);
            tinyDB.putListObject(getString(R.string.pref_wallet_accounts), accountDetails);
        } else {
            for (int i = 0; i < accountDetails.size(); i++) {
                if (accountDetails.get(i).isSelected) {
                    accountDetailsId = i;
                    to = accountDetails.get(i).account_name;
                    accountId = accountDetails.get(i).account_id;
                    wifkey = accountDetails.get(i).wif_key;
                    showHideLifeTime(accountDetails.get(i).isLifeTime);
                    break;
                }
            }
        }
        Application.monitorAccountId = accountId;
        tvAccountName.setText(to);
        isLifeTime(accountId, "15");

        if (onResume && accountNameChanged) {
            loadBalancesFromSharedPref();
        }

        loadViews(onResume, accountNameChanged, fiatCurrencyChanged);
    }

    Boolean checkIfAccountNameChange() {
        ArrayList<AccountDetails> accountDetails = tinyDB.getListObject(getString(R.string.pref_wallet_accounts), AccountDetails.class);
        String checkAccountName = "";
        if (accountDetails.size() == 1) {
            checkAccountName = accountDetails.get(0).account_name;
            ivMultiAccArrow.setVisibility(View.GONE);
        } else {
            ivMultiAccArrow.setVisibility(View.VISIBLE);
            for (int i = 0; i < accountDetails.size(); i++) {
                if (accountDetails.get(i).isSelected) {
                    checkAccountName = accountDetails.get(i).account_name;
                    break;
                }
            }
        }
        return !checkAccountName.equals(to);
    }

    void onChangedAccount() {
        Log.d(TAG,"onChangedAccount");
        final ArrayList<AccountDetails> accountDetailsList;

        accountDetailsList = tinyDB.getListObject(getString(R.string.pref_wallet_accounts), AccountDetails.class);

        List<String> accountlist = new ArrayList<String>();

        for (int i = 0; i < accountDetailsList.size(); i++) {
            accountlist.add(accountDetailsList.get(i).account_name);
        }

        AlertDialog.Builder builderSingle = new AlertDialog.Builder(getContext());

        builderSingle.setTitle(getString(R.string.imported_created_accounts));


        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                getContext(), android.R.layout.simple_list_item_1, accountlist);

        builderSingle.setAdapter(
                arrayAdapter,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String strName = arrayAdapter.getItem(which);

                        for (int i = 0; i < accountDetailsList.size(); i++) {

                            if (strName.equals(accountDetailsList.get(i).account_name)) {
                                accountDetailsList.get(i).isSelected = true;
                            } else {
                                accountDetailsList.get(i).isSelected = false;
                            }

                        }
                        tinyDB.putListObject(getString(R.string.pref_wallet_accounts), accountDetailsList);
                        Helper.storeStringSharePref(getContext(), getString(R.string.pref_account_name), strName);

                        onResume();
                        dialog.dismiss();
                    }
                });
        builderSingle.show();
    }

    @OnClick(R.id.ivMultiAccArrow)
    public void ivOnChangedAccount(View view) {
        onChangedAccount();
    }

    @OnClick(R.id.account_name)
    public void tvOnChangedAccount(View view) {
        onChangedAccount();
    }

    private void showHideLifeTime(final Boolean show) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (show) {
                    ivLifeTime.setVisibility(View.VISIBLE);
                    tvUpgradeLtm.setVisibility(View.GONE);

                } else {
                    ivLifeTime.setVisibility(View.GONE);
                    tvUpgradeLtm.setVisibility(View.VISIBLE);
                }
            }
        });

    }

    private void hideDialog() {
        try {

            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (progressDialog != null) {
                        if (progressDialog.isShowing()) {
                            progressDialog.cancel();
                        }
                    }
                }
            });
        } catch (Exception e) {
        }
    }

    private void showDialog(String title, String msg) {
        if (progressDialog != null) {
            if (!progressDialog.isShowing()) {
                progressDialog.setTitle(title);
                progressDialog.setMessage(msg);
                progressDialog.show();
            }
        }
    }

    private void updateLifeTimeModel(String accountName) {
        ArrayList<AccountDetails> accountDetails = tinyDB.getListObject(getString(R.string.pref_wallet_accounts), AccountDetails.class);
        try {
            for (int i = 0; i < accountDetails.size(); i++) {
                if (accountDetails.get(i).account_name.equals(accountName)) {
                    accountDetails.get(i).isLifeTime = true;
                    break;
                }
            }
        } catch (Exception e) {
        }

        tinyDB.putListObject(getString(R.string.pref_wallet_accounts), accountDetails);
        showHideLifeTime(true);
    }

    private float convertLocalizeStringToFloat(String text) {
        float txtAmount_d = 0;
        try {
            NumberFormat format = NumberFormat.getInstance(Locale.ENGLISH);
            Number number = format.parse(text);
            txtAmount_d = number.floatValue();
        } catch (Exception e) {
            try {
                NumberFormat format = NumberFormat.getInstance(locale);
                Number number = format.parse(text);
                txtAmount_d = number.floatValue();

            } catch (Exception e1) {

            }
        }
        return txtAmount_d;
    }

    private double convertLocalizeStringToDouble(String text) {
        double txtAmount_d = 0;
        try {
            NumberFormat format = NumberFormat.getInstance(Locale.ENGLISH);
            Number number = format.parse(text);
            txtAmount_d = number.doubleValue();
        } catch (Exception e) {
            try {
                NumberFormat format = NumberFormat.getInstance(locale);
                Number number = format.parse(text);
                txtAmount_d = number.doubleValue();

            } catch (Exception e1) {

            }
        }
        return txtAmount_d;
    }

    private int convertLocalizeStringToInt(String text) {
        int txtAmount_d = 0;
        try {
            NumberFormat format = NumberFormat.getInstance(Locale.ENGLISH);
            Number number = format.parse(text);
            txtAmount_d = number.intValue();
        } catch (Exception e) {
            try {
                NumberFormat format = NumberFormat.getInstance(locale);
                Number number = format.parse(text);
                txtAmount_d = number.intValue();

            } catch (Exception e1) {

            }
        }
        return txtAmount_d;
    }

    public void isAssets() {
//        progressBar.setVisibility(View.GONE);
        progressBar1.setVisibility(View.GONE);
    }

    /**
     * Refreshes table data by assigning a new adapter.
     * This method should be called whenever there is fresh data in the transfers database table.
     *
     * @param reset: If true, the current transfer list is discarded, and a new query is made to the database.
     */
    private void updateTableView(boolean reset) {
        UserAccount account = new UserAccount(accountId);

        if (reset) {
            Log.d(TAG, "updateTableView Reset");
            loadMoreCounter = 1;
        }

        // Calculate how many items to fetch depending on how many times
        // the 'load more' button has been pressed. Maybe later we can modify the
        // getTransactions method to accept ranges and simplify this code.
        int limit = SCWallDatabase.DEFAULT_TRANSACTION_BATCH_SIZE * loadMoreCounter;
        List<HistoricalTransferEntry> newData = database.getTransactions(account, limit);

        // Here we check if the SortableTableView has its default adapter or our own instance.
        if (transfersView.getDataAdapter() instanceof TransfersTableAdapter && !reset) {
            Log.d(TAG, "updating table view");
            tableAdapter = (TransfersTableAdapter) transfersView.getDataAdapter();
            List<HistoricalTransferEntry> existingData = tableAdapter.getData();
            boolean found = true;
            for (HistoricalTransferEntry newEntry : newData) {
                for (HistoricalTransferEntry existingEntry : existingData) {
                    if (newEntry.getHistoricalTransfer().getId().equals(existingEntry.getHistoricalTransfer().getId())) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    existingData.add(newEntry);
                }
                found = false;
            }

        } else {
            tableAdapter = new TransfersTableAdapter(getContext(), account, newData.toArray(new HistoricalTransferEntry[newData.size()]));

            transfersView.setDataAdapter(tableAdapter);
        }
        //Notifies the attached observers that the underlying data has been changed and any View
        // reflecting the data set should refresh itself.
        tableAdapter.notifyDataSetChanged();

        if (transfersView.getColumnComparator(0) == null) {
            updateSortTable();
        }
    }

    /**
     * PdfGeneratorListener interface method. Used to update the pdfProgress view.
     *
     * @param percentage
     */
    @Override
    public void onUpdate(float percentage) {
        if (pdfProgress != null) {
            int progress = (int) (percentage * 100);
            pdfProgress.setProgress(progress);
        }
    }

    /**
     * PdfGeneratorListener interface method. Used to dismiss the pdfProgress view.
     */
    @Override
    public void onReady(String message) {
        if (pdfProgress != null && pdfProgress.isShowing()) {
            pdfProgress.dismiss();
        }
        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
    }

    /**
     * PdfGeneratorListener interface method. Used to inform the user about an error.
     */
    @Override
    public void onError(String message) {
        Toast.makeText(getContext(), getActivity().getText(R.string.pdf_generated_msg_error) + message, Toast.LENGTH_LONG).show();
    }
}