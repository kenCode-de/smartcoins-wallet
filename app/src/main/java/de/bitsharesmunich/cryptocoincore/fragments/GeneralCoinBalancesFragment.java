package de.bitsharesmunich.cryptocoincore.fragments;

import android.Manifest;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
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
import android.support.v4.content.ContextCompat;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.primitives.UnsignedLong;

import org.bitcoinj.core.DumpedPrivateKey;
import org.bitcoinj.core.ECKey;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
import de.bitshares_munich.models.BalancesItems;
import de.bitshares_munich.models.GeneralCoinSettingEvent;
import de.bitshares_munich.models.Smartcoins;
import de.bitshares_munich.models.TransactionDetails;
import de.bitshares_munich.smartcoinswallet.AssestsActivty;
import de.bitshares_munich.smartcoinswallet.AssetsSymbols;
import de.bitshares_munich.smartcoinswallet.AudioFilePath;
import de.bitshares_munich.smartcoinswallet.Constants;
import de.bitshares_munich.smartcoinswallet.MediaService;
import de.bitshares_munich.smartcoinswallet.R;
import de.bitsharesmunich.cryptocoincore.base.ChangeSettingListener;
import de.bitsharesmunich.cryptocoincore.base.GeneralCoinFactory;
import de.bitsharesmunich.cryptocoincore.base.GeneralCoinSettings;
import de.bitsharesmunich.cryptocoincore.base.TransactionLog;
import de.bitsharesmunich.cryptocoincore.adapters.ArrayListCoinAdapter;
import de.bitsharesmunich.cryptocoincore.base.AccountSeed;
import de.bitsharesmunich.cryptocoincore.base.SeedType;
import de.bitsharesmunich.cryptocoincore.base.seed.BIP39;
import de.bitsharesmunich.cryptocoincore.smartcoinwallets.QRCodeActivity;
import de.bitsharesmunich.cryptocoincore.smartcoinwallets.RecieveActivity;
import de.bitsharesmunich.cryptocoincore.smartcoinwallets.SendScreen;
import de.bitshares_munich.smartcoinswallet.WebsocketWorkerThread;
import de.bitshares_munich.utils.Application;
import de.bitshares_munich.utils.Crypt;
import de.bitshares_munich.utils.Helper;
import de.bitshares_munich.utils.PdfGeneratorTask;
import de.bitshares_munich.utils.PermissionManager;
import de.bitshares_munich.utils.SupportMethods;
import de.bitshares_munich.utils.TinyDB;
import de.bitshares_munich.utils.webSocketCallHelper;
import de.bitsharesmunich.cryptocoincore.adapters.CryptoCoinTransferAmountComparator;
import de.bitsharesmunich.cryptocoincore.adapters.CryptoCoinTransferDateComparator;
import de.bitsharesmunich.cryptocoincore.adapters.CryptoCoinTransferSendReceiveComparator;
import de.bitsharesmunich.cryptocoincore.adapters.CryptoCoinTransfersTableAdapter;
import de.bitsharesmunich.cryptocoincore.base.Balance;
import de.bitsharesmunich.cryptocoincore.base.ChangeBalanceListener;
import de.bitsharesmunich.cryptocoincore.base.Coin;
import de.bitsharesmunich.cryptocoincore.base.GeneralCoinAccount;
import de.bitsharesmunich.cryptocoincore.base.GeneralCoinAddress;
import de.bitsharesmunich.cryptocoincore.base.GeneralTransaction;
import de.bitsharesmunich.cryptocoincore.insightapi.AccountActivityWatcher;
import de.bitsharesmunich.cryptocoincore.insightapi.GetTransactionByAddress;
import de.bitsharesmunich.cryptocoincore.utils.CryptoCoinTableViewClickListener;
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
import de.codecrafters.tableview.listeners.TableHeaderClickListener;
import de.codecrafters.tableview.toolkit.SimpleTableHeaderAdapter;
import de.codecrafters.tableview.toolkit.SortStateViewProviders;


/**
 * Shows the balances of all the user coin accounts and all the transactions made in this accounts.
 *
 * There are two types of accounts: Bitshares accounts and Bitcoin alike accounts.
 * Must functions in this fragment take different actions for these two.
 */
public class GeneralCoinBalancesFragment extends Fragment implements AssetDelegate, ISound, PdfGeneratorListener, BalanceItemsListener, ChangeSettingListener {
    public final String TAG = this.getClass().getName();
    public static Activity balanceActivity;

    // Debug flags
    private final boolean DEBUG_DATE_LOADING = false;
    private final boolean DEBUG_EQ_VALUES = false;

    /* Permission flag */
    private static final int REQUEST_EXTERNAL_STORAGE = 1;

    static Boolean audioSevice = false;

    int accountDetailsId;
    String accountId = "";
    DecimalFormat df = new DecimalFormat("0.0");

//    Boolean isLoading = false;
    public static Boolean onClicked = false;
    Handler myHandler = new Handler();

    String to = "";

    String wifkey = "";
//    String finalFaitCurrency;

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

    @Bind(R.id.recievebtn)
    ImageView recievebtn;

    @Bind(R.id.sendbtn)
    ImageView sendbtn;

    @Bind(R.id.ivLifeTime)
    ImageView ivLifeTime;

    @Bind(R.id.ivMultiAccArrow)
    ImageView ivMultiAccArrow;

    ProgressDialog progressDialog;

    Boolean sentCallForTransactions = false;

    BalancesItems balancesItems; /**< the data of every coin balance. Every operation (creation, modification or removal)
                                   *< with the balances must be with this object*/

    Locale locale;
    NumberFormat format;
    String language;

    public static ISound iSound;

    List<Coin> coinsUsed = new ArrayList<Coin>(); //this will be used for enabling/disabling coins in spinner selector

    public GeneralCoinBalancesFragment() {
        // Required empty public constructor
    }

    /**
     * Creates a new instance of this fragment specifying the coin type of the balances to show.
     *
     * NOTE: This method is still used by the ViewPagerAdapter to create an instance of this fragment, but
     * the coin takes no effect since this fragment shows the balances of all the coin types
     *
     * @param coin the coin to associate with the balances in this fragment
     * @return a instance of this fragment
     */
    public static GeneralCoinBalancesFragment newInstance(Coin coin) {
        GeneralCoinBalancesFragment generalCoinBalancesFragment = new GeneralCoinBalancesFragment();

        Bundle args = new Bundle();
        args.putString("coin",coin.toString());
        generalCoinBalancesFragment.setArguments(args);

        return generalCoinBalancesFragment;
    }

    double BTSCurrencyPriceCache; /**< cache for equivalent component BTS to EUR */
    Date BTSCurrencyPriceCacheDate; /**< datetime of the BTS to EUR cache*/


    webSocketCallHelper myWebSocketHelper;

    private SortableTableView<TransactionLog> cryptoCoinTransfersView; /**< SortableTableView displaying the list of transactions.*/
    private CryptoCoinTransfersTableAdapter cryptoCoinTableAdapter; /**< Adapter for the transaction list.*/
    private int loadMoreCounter = 1; /**< Counter used to keep track of how many times the 'load more' button was pressed*/

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

    private SCWallDatabase database; /**< Database instance that manages smartcoin wallets and
                                       *< bitcoin alike accounts*/

    /* Websocket threads */
    private WebsocketWorkerThread transferHistoryThread;
    private WebsocketWorkerThread getMissingAccountsThread;
    private WebsocketWorkerThread getMissingAssets;
    private WebsocketWorkerThread getMissingTimes;
    private WebsocketWorkerThread getMissingEquivalentValues;

    private final static List<String> SMARTCOINS = Arrays.asList(new String[] {"CNY","BTC","USD","GOLD","EUR","SILVER",
            "ARS","CAD","GBP","KRW","CHF","JPY","HKD","SGD","AUD","RUB","SBK"});

    private Coin coin; /**< The coin parameters passed in the instanciation of this fragment*/


    private int lastTransferTableColumnIndexPressed = -1; /**< The index of the last column in the transaction list pressed by the user*/
    private boolean lastTransferTableColumnIndexSortUp = true; /**< Indicates whether the last column pressed must be in ascending order*/

    private WitnessResponseListener mHistoricalMarketSecondStepListener = new WitnessResponseListener() {
        @Override
        public void onSuccess(WitnessResponse response) {
            Log.d(TAG,"historicalMarketSecondStepListener.onSuccess");
            if(getActivity() == null){
                Log.w(TAG, "Got no activity, quitting..");
                return;
            }
            List<BucketObject> buckets = (List<BucketObject>) response.result;
            HistoricalTransferEntry transferEntry = missingEquivalentValues.peek();

            if(buckets.size() > 0){
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
            }else{
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
            Log.e(TAG,"mHistoricalMarketSecondStepListener.onError. Msg: "+error.message);

            // Removing the now solved equivalent value
            missingEquivalentValues.poll();

            // Processing next value, if there is one.
            // Process the next equivalent value, in case we have one
            processNextEquivalentValue();
        }
    };

    /**
     * Called when we get a response from the 'get_market_history' API call
     */
    private WitnessResponseListener mHistoricalMarketListener = new WitnessResponseListener() {
        @Override
        public void onSuccess(WitnessResponse response) {
            Log.d(TAG,"mHistoricalMarketListener.onSuccess");
            if(getActivity() == null){
                Log.w(TAG, "Got no activity, quitting..");
                return;
            }

            List<BucketObject> buckets = (List<BucketObject>) response.result;
            HistoricalTransferEntry transferEntry = missingEquivalentValues.peek();
            if(buckets.size() > 0){
                BucketObject bucket = buckets.get(buckets.size() - 1);

                AssetAmount transferAmount = transferEntry.getHistoricalTransfer().getOperation().getTransferAmount();

                Asset base = database.fillAssetDetails(bucket.key.base);
                Asset quote = database.fillAssetDetails(bucket.key.quote);

                if(quote.equals(mSmartcoin)){
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
                }else{
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
            }else{
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
            Log.e(TAG,"historicalMarketListener.onError. Msg: "+error.message);
            // Removing this equivalent value task, even though it was not resolved
            missingEquivalentValues.poll();

            // Process the next equivalent value, in case we have one
            processNextEquivalentValue();
        }
    };

    /**
     * Callback activated once we get a block header response.
     */
    private WitnessResponseListener mGetMissingTimesListener = new WitnessResponseListener() {

        @Override
        public void onSuccess(final WitnessResponse response) {
            if(getActivity() == null){
                Log.w(TAG, "Got no activity, quitting..");
                return;
            }
            if(missingTimes.size() > 1){
                Log.d(TAG, "getMissingTime. onSuccess. remaining: "+(missingTimes.size() - 1));
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
            }else{
                // If we're done loading missing transfer times, we check for missing equivalent values.
                // By calling the 'getMissingEquivalentValues' method we should get a list of all transfer
                // entries that are missing just the equivalent values, but DO HAVE time information.
                missingEquivalentValues = database.getMissingEquivalentValues();
                if(missingEquivalentValues.size() > 0){
                    Log.i(TAG,"Finished loading missing times, now we can safely proceed to missing eq values");
                    processNextEquivalentValue();
                }else{
                    Log.w(TAG,"Got no missing equivalent value to fetch");
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
                    database.putAssets(assets);

                    // Looking for smartcoin asset
                    for(Asset asset : assets){
                        if(asset.getObjectId().equals(mSmartcoin.getObjectId().toString())){
                            mSmartcoin = database.fillAssetDetails(asset);
                            Log.d(TAG,"Setting smartcoin as: "+mSmartcoin.getSymbol());
                        }
                    }

                    // If we has missing equivalent values that could not be processed until
                    // we had all the missing assets in the database, start processing them now.
                    if(missingEquivalentValues != null){
                        processNextEquivalentValue();
                    }

                    //TODO: Remove this from here and replace the balance update procedure with something better
                    loadBalancesFromSharedPref();
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
            Log.v(TAG, "mTransferHistoryListener. onSuccess");
            if(getActivity() == null){
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
            for(HistoricalTransfer historicalTransfer : resp.result){
                HistoricalTransferEntry entry = new HistoricalTransferEntry();
                TransferOperation op = historicalTransfer.getOperation();
                if(op != null){
                    Memo memo = op.getMemo();
                    if(memo.getByteMessage() != null){
                        Address destinationAddress = memo.getDestination();
                        try {
                            if(destinationAddress.toString().equals(myAddress.toString())){
                                String decryptedMessage = Memo.decryptMessage(privateKey, memo.getSource(), memo.getNonce(), memo.getByteMessage());
                                memo.setPlaintextMessage(decryptedMessage);
                            }
                        } catch (ChecksumException e) {
                            Log.e(TAG, "ChecksumException. Msg: "+e.getMessage());
                        } catch (NullPointerException e){
                            // This is expected in case the decryption fails, so no need to log this event.
                        }
                    }
                }else{
                    continue;
                }
                entry.setHistoricalTransfer(historicalTransfer);
                historicalTransferEntries.add(entry);
            }

            int inserted = database.putTransactions(historicalTransferEntries);
            Log.d(TAG,String.format("Inserted %d out of %d obtained operations", inserted, resp.result.size()));
            List<HistoricalTransferEntry> transactions = database.getTransactions(new UserAccount(accountId), loadMoreCounter * SCWallDatabase.DEFAULT_TRANSACTION_BATCH_SIZE);
            // If we got exactly the requested amount of historical transfers, it means we
            // must have more to fetch.
            if(resp.result.size() == HISTORICAL_TRANSFER_BATCH_SIZE){
                Log.v(TAG,String.format("Got %d transactions, which es exactly the requested amount, so we might have more.", resp.result.size()));
                start = transactions.size() + (historicalTransferCount * HISTORICAL_TRANSFER_BATCH_SIZE);
                stop = start + HISTORICAL_TRANSFER_BATCH_SIZE + 1;
                Log.v(TAG,String.format("Calling get_relative_account_history. start: %d, limit: %d, stop: %d", start, HISTORICAL_TRANSFER_BATCH_SIZE, stop));
                transferHistoryThread = new WebsocketWorkerThread(new GetRelativeAccountHistory(new UserAccount(accountId), start, HISTORICAL_TRANSFER_BATCH_SIZE, stop, mTransferHistoryListener));
                transferHistoryThread.start();
            }else{
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

    /**
     * Assuming we have a list of missing equivalent values, this method will be called
     * to start the procedure needed to resolve a single missing equivalent value.
     *
     * Since this procedure might have to be called repeated times, it was better isolated
     * in a private method.
     */
    private void processNextEquivalentValue(){
        if(missingEquivalentValues.size() > 0){
            List<Asset> missingAssets = database.getMissingAssets();
            if(missingAssets.size() == 0){
                HistoricalTransferEntry transferEntry = missingEquivalentValues.peek();
                Asset transferredAsset = transferEntry.getHistoricalTransfer()
                        .getOperation()
                        .getTransferAmount()
                        .getAsset();

                while(transferredAsset.equals(mSmartcoin)){
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
                if(transferredAsset.equals(Constants.getCoreCurrency())){
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
                if(base != null && quote != null){
                    getMarketHistory = new GetMarketHistory(
                            base,
                            quote,
                            Constants.DEFAULT_BUCKET_SIZE,
                            startDate,
                            endDate,
                            mHistoricalMarketListener);
                    getMissingEquivalentValues = new WebsocketWorkerThread(getMarketHistory);
                    getMissingEquivalentValues.start();
                }else{
                    Log.w(TAG,"Either base or quote is null");
                }
            }else{
                // Don't do anything, the lookup_asset_symbol callback will check for missing
                // equivalent values again and pick up this series of tasks.
                Log.w(TAG, "We have some missing assets");
            }
        }else{
            // In case we're done loading missing times and equivalent values for this batch,
            // we can check if we have another batch of times and consequently missing equivalent
            // values to process.
            missingTimes = database.getMissingTransferTimes(SECONDARY_LOAD_BATCH_SIZE);
            if (missingTimes.size() > 0) {
                Log.d(TAG,String.format("Got a new batch of %d missing times, so we're now going to process them", missingTimes.size()));
                Long blockNum = missingTimes.peek();
                getMissingTimes = new WebsocketWorkerThread(new GetBlockHeader(blockNum, mGetMissingTimesListener));
                getMissingTimes.start();
            }else{
                Log.d(TAG,"We're done with missing times, so this must be it...");
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

    private String decryptWif(){
        String wif = null;
        try {
            wif = Crypt.getInstance().decrypt_string(wifkey);
        } catch (InvalidKeyException e) {
            Log.e(TAG, "InvalidKeyException. Msg: "+e.getMessage());
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "NoSuchAlgorithmException. Msg: "+e.getMessage());
        } catch (NoSuchPaddingException e) {
            Log.e(TAG, "NoSuchPaddingException. Msg: "+e.getMessage());
        } catch (InvalidAlgorithmParameterException e) {
            Log.e(TAG, "InvalidAlgorithmParameterException. Msg: "+e.getMessage());
        } catch (IllegalBlockSizeException e) {
            Log.e(TAG, "IllegalBlockSizeException. Msg: "+e.getMessage());
        } catch (BadPaddingException e) {
            Log.e(TAG, "BadPaddingException. Msg: "+e.getMessage());
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "ClassNotFoundException. Msg: "+e.getMessage());
        } catch (IOException e) {
            Log.e(TAG, "IOException. Msg: "+e.getMessage());
        }
        return wif;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tinyDB = new TinyDB(getContext());
        Application.registerAssetDelegate(this);
        iSound = this;
        updateEquivalentAmount = new Handler();
        myWebSocketHelper = new webSocketCallHelper(getContext());
        database = new SCWallDatabase(getContext());
        if (DEBUG_DATE_LOADING) {
            database.clearTimestamps();
        }
        if(DEBUG_EQ_VALUES){
            database.clearTransfers();
        }

        // Getting the system's configuration locale
        locale = getResources().getConfiguration().locale;

        // Checking the app's configuration to override the system configuration
        // country locale if it is other than an empty string.
        String configurationCountry = Helper.fetchStringSharePref(getContext(), getString(R.string.pref_country), "");
        if(!configurationCountry.equals("")){
            locale = new Locale.Builder().setLocale(locale).setRegion(configurationCountry).build();
        }

        if (this.getArguments().getString("coin") == null){
            coin = Coin.BITSHARE;
        } else {
            coin = Coin.valueOf(this.getArguments().getString("coin", "BITSHARE"));
        }

        // Checking the app's configuration to override the system configuration
        // language locale if it is other than an empty string.
        language = Helper.fetchStringSharePref(getActivity(), getString(R.string.pref_language), "");
        if(!language.equals("")){
            locale = new Locale.Builder().setLocale(locale).setLanguage(language).build();
        }

        /**
         * Just checking if we still don't have a country setup in the locale, in which case
         * we try the telephony manager and then if that too fails we just setup Germany as
         * the default country.
         */
        String localeCountry = locale.getCountry();
        if(localeCountry.equals("")){
            Log.w(TAG, "Could not resolve country information, trying with the telephony manager");
            // If the locale mechanism fails to give us a country, we try
            // to get it from the TelephonyManager.
            //String telephonyCountry = Helper.getUserCountry(getContext());
            String telephonyCountry = "";
            if(telephonyCountry == null || telephonyCountry.equals("")){
                Log.w(TAG,"Could not resolve country information again, falling back to the default");
                telephonyCountry = Constants.DEFAULT_COUNTRY_CODE;
            }
            locale = new Locale.Builder().setRegion(telephonyCountry).build();
        }

        /**
         * Just checking if we still don't have a language setup in the locale, in which
         * case we fallback to english as the default.
         */
        String localeLanguage = locale.getLanguage();
        if(localeLanguage.equals("")){
            Log.w(TAG,"Could not resolve language information, falling back to english");
            locale = new Locale.Builder().setLocale(locale).setLanguage(Constants.DEFAULT_LANGUAGE_CODE).build();
        }

        this.mSmartcoin = Smartcoins.getMap().get(locale.getCountry());
        HashMap<String, Asset> knownAssets = database.getAssetMap();
        if(!knownAssets.containsKey(this.mSmartcoin.getObjectId())){
            // If the smartcoin asset details are not known, we schedule an update from the full node.
            ArrayList<Asset> assetList = new ArrayList<>();
            assetList.add(mSmartcoin);
            getMissingAssets = new WebsocketWorkerThread(new LookupAssetSymbols(assetList, mLookupAssetsSymbolsListener));
            getMissingAssets.start();
            Log.d(TAG,"Don't know much about this smartcoin, making a network query");
        }else{
            Log.d(TAG, "Already have the details in database, just filling them");
            mSmartcoin = database.fillAssetDetails(mSmartcoin);
        }
        Log.d(TAG, String.format("Selected smartcoin: %s", mSmartcoin.getSymbol()));
    }

    @Override
    public void onStart() {
        super.onStart();
        if(getMissingAssets == null){
            Log.d(TAG, "Got no missing assets, checking for new transactions");
            List<HistoricalTransferEntry> transactions = database.getTransactions(new UserAccount(accountId), HISTORICAL_TRANSFER_BATCH_SIZE);
            start = transactions.size();
            stop = start + HISTORICAL_TRANSFER_BATCH_SIZE + 1;
            Log.v(TAG,String.format("Calling get_relative_account_history. start: %d, limit: %d, stop: %d", start, HISTORICAL_TRANSFER_BATCH_SIZE, stop));
            transferHistoryThread = new WebsocketWorkerThread(new GetRelativeAccountHistory(new UserAccount(accountId), start, HISTORICAL_TRANSFER_BATCH_SIZE, stop, mTransferHistoryListener));
            transferHistoryThread.start();
        }else{
            Log.w(TAG, "getMissingAssets is not null");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View rootView = inflater.inflate(R.layout.fragment_balances, container, false);
        ButterKnife.bind(this, rootView);
        balanceActivity = getActivity();
        format = NumberFormat.getInstance(locale);
        tvUpgradeLtm.setPaintFlags(tvUpgradeLtm.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        progressDialog = new ProgressDialog(getActivity());

        //if (this.coin == Coin.BITSHARE) {
        //    transfersView = (SortableTableView<HistoricalTransferEntry>) rootView.findViewById(R.id.tableView);
        //    transfersView.addDataClickListener(new TableViewClickListener(getContext()));
        //} else {
            cryptoCoinTransfersView = (SortableTableView<TransactionLog>) rootView.findViewById(R.id.tableView);
            cryptoCoinTransfersView.addDataClickListener(new CryptoCoinTableViewClickListener(getContext()));
        //}

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

            LinearLayout.LayoutParams params;
            //if (this.coin == Coin.BITSHARE) {
             //   params = (LinearLayout.LayoutParams) transfersView.getLayoutParams();
            //} else {
                params = (LinearLayout.LayoutParams) cryptoCoinTransfersView.getLayoutParams();
            //}
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
        Log.d(TAG,"onResume");
        // Inflate the layout for this fragment
        scrollViewBalances.fullScroll(View.FOCUS_UP);
        scrollViewBalances.pageScroll(View.FOCUS_UP);
        onClicked = false;

        if (this.coin == Coin.BITSHARE) {
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

            String smartcoinSymbol = mSmartcoin.getSymbol();
            if (accountNameChange || (smartcoinSymbol != null && !Helper.getFadeCurrency(getContext()).equals(smartcoinSymbol)))
                this.balancesItems.getBalancesItems(Coin.BITSHARE).clear();
                //dfgdfgdsfllBalances.removeAllViews();

            if (isHideDonationsChanged || accountNameChange || (smartcoinSymbol != null && !Helper.getFadeCurrency(getContext()).equals(mSmartcoin.getSymbol()))) {
                if (smartcoinSymbol != null && !Helper.getFadeCurrency(getContext()).equals(smartcoinSymbol)) {
                    loadBasic(true, accountNameChange, true);
                } else {
                    loadBasic(true, accountNameChange, false);
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
        }

        // Loading transfers from database
        updateTableView(true);
    }

    @OnClick(R.id.recievebtn)
    public void GotoRecieveActivity(){
        this.GoToRecieveActivity(this.coin);
    }

    public void GoToRecieveActivity(Coin coin) {
        final Intent intent = new Intent(getActivity(), RecieveActivity.class);
        if(coin == Coin.BITSHARE) {
            intent.putExtra(getString(R.string.to), to);
            intent.putExtra(getString(R.string.account_id), accountId);
        }else{
            SCWallDatabase db = new SCWallDatabase(getContext());
            final GeneralCoinAccount account = db.getGeneralCoinAccount(coin.name());
            intent.putExtra(getString(R.string.to), account.getNextReceiveAddress());
            intent.putExtra(getString(R.string.account_id), Long.toString(account.getId()));
        }
        intent.putExtra(getString(R.string.coin),coin.name());
        Animation coinAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.coin_animation);
        coinAnimation.setAnimationListener(new Animation.AnimationListener() {

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
        recievebtn.startAnimation(coinAnimation);
    }

    @OnClick(R.id.sendbtn)
    public void GoToSendActivity() {
        this.GoToSendActivity(this.coin);
    }

    public void GoToSendActivity(Coin coin) {
        final Intent intent = new Intent(getActivity(), SendScreen.class);
        Bundle b = new Bundle();
        b.putString(getString(R.string.coin), coin.name());
        intent.putExtras(b);

        Animation coinAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.coin_animation);
        coinAnimation.setAnimationListener(new Animation.AnimationListener() {

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
        intent.putExtra(getString(R.string.coin),coin.name());
        Animation coinAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.coin_animation);
        coinAnimation.setAnimationListener(new Animation.AnimationListener() {

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
        }else{
            UserAccount currentUser = new UserAccount(accountId);
            List<HistoricalTransferEntry> transfers = database.getTransactions(currentUser, SCWallDatabase.UNLIMITED_TRANSACTIONS);
            pdfGeneratorTask = new PdfGeneratorTask(getContext(), currentUser, this);
            pdfGeneratorTask.execute(transfers.toArray(new HistoricalTransferEntry[transfers.size()]));

            if(pdfProgress == null){
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

    /**
     * Loads the balances for all coins
     */
    public void loadBalancesFromSharedPref() {
        //Loads the Bitshares balance
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

                        BalanceAssetsUpdate(Coin.BITSHARE, sym, pre, am, true);
                    }

                    break;
                }
            }
        } catch (Exception e) {
            Log.e("","Error loading bitshares balance");
        }

        //Loads the balances from the rest of the coins (Bitcoin, Litecoin, Dash, etc)
        SCWallDatabase db = new SCWallDatabase(getContext());
        List<GeneralCoinAccount> accountList = db.getActiveAccounts();

        for (final GeneralCoinAccount account : accountList) {
            loadGeneralCoinAccount(account);
        }
    }

    /**
     * Initialize the balance of an existing account
     *
     * @param account the account from which the balance will be initialized     *
     */
    public void loadGeneralCoinAccount(final GeneralCoinAccount account){
        SCWallDatabase db = new SCWallDatabase(getContext());
        //Getting the addresses from the account (The already used and the next 20 available).
        List<GeneralCoinAddress> addresses = account.getAddresses(db);

        //The balance is created with the cache info from the database
        getBalanceItems().addBalancesItems(account.getCoin()).addDetailedBalanceItem(account.getCoin().getLabel(), "" + account.getCoin().getPrecision(), "" + account.getBalance().get(0).getConfirmedAmount(), account.getBalance().get(0).getLessConfirmed(), true);
        Log.i("test", account.getCoin().name()+" account balance: " + account.getBalance().get(0).getAmmount());

        //this listener will refresh the data of the balance item when the balance of the account has changed
        account.addChangeBalanceListener(new ChangeBalanceListener() {
            @Override
            public void balanceChange(Balance balance) {
                if (account != null) {
                    //Refresh the balance item data
                    getBalanceItems().getBalancesItems(account.getCoin()).addOrUpdateDetailedBalanceItem(account.getCoin().getLabel(), "" + account.getCoin().getPrecision(), "" + balance.getConfirmedAmount(), balance.getLessConfirmed());
                    //Refresh the transfers history
                    getActivity().runOnUiThread(new Runnable() {
                        public void run() {
                            updateTableView(false);
                        }
                    });
                }
            }
        });

        //loads the settings from the specific account and sets a listener in case the user changes them
        GeneralCoinSettings accountSettings = GeneralCoinFactory.getSettings(getContext(), account.getCoin());
        accountSettings.addChangeSettingListener(this);

        //adds the coin of the account in the used list. This is for when the user wants to open a new coin account,
        //so the list won't show coins of accounts already open
        this.coinsUsed.add(account.getCoin());

        //creates the watcher to get new transaction for every address of this account from the server (Real Time)
        AccountActivityWatcher watcher = new AccountActivityWatcher(account, getContext());
        for (GeneralCoinAddress address : addresses) {
            watcher.addAddress(address.getAddressString(account.getNetworkParam()));
        }
        //and starts the watcher
        watcher.connect();

        //Once the watcher is active, new transactions will be notified.
        //Now the GetTransactionByAddress is started to get the old transaction obtained by the server
        GetTransactionByAddress getTransactionByAddress = new GetTransactionByAddress(account, getContext());
        for (GeneralCoinAddress address : addresses) {
            getTransactionByAddress.addAddress(address);
        }
        getTransactionByAddress.start();
    }

    Handler updateEquivalentAmount;

    @Override
    public void isUpdate(ArrayList<String> ids, ArrayList<String> sym, ArrayList<String> pre, ArrayList<String> am) {
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
        BalanceAssetsUpdate(Coin.BITSHARE, sym, pre, am, false);
    }

    /**
     * Singleton for BalancesItems.
     *
     * @return the balances items for this fragment
     */
    public BalancesItems getBalanceItems(){
        if (this.balancesItems == null){
            this.balancesItems = new BalancesItems();
            //Listen to every change in the balances items, so the fragment can change the views respectively
            this.balancesItems.addListener(this);
        }

        return this.balancesItems;
    }

    public void BalanceAssetsUpdate(Coin coin, final ArrayList<String> sym, final ArrayList<String> pre, final ArrayList<String> am, final Boolean onStartUp) {

        int count = 0;

        //if there's no balances items then the fragment view has not been created yet
        if (this.balancesItems != null) {
            BalanceItems bitshareBalanceItems = this.balancesItems.getBalancesItems(Coin.BITSHARE);
        if (bitshareBalanceItems != null)
            count = bitshareBalanceItems.count();
        }

        // use standard asset names (like add bit etc)
        ArrayList<String> symbols = new ArrayList();
        for(String symbol : sym){
            if(SMARTCOINS.contains(symbol)){
                symbols.add("bit"+symbol);
            }else{
                symbols.add(symbol);
            }
        }

        if (count <= 0)
            BalanceAssetsLoad(symbols, pre, am, onStartUp);
        if (count > 0)
            BalanceAssetsUpdate(coin, symbols, pre, am);
    }

    public void processAssets(final HashMap<String, ArrayList<String>> currencies, final HashMap<String, Asset> assets, final Runnable getEquivalentCompRunnable){
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
                                                double price = (double)market.sell_price.base.amount / (double)market.sell_price.quote.amount;
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
        BalanceItems bitshareBalanceItems = this.getBalanceItems().getBalancesItems(Coin.BITSHARE);

        if (bitshareBalanceItems.findBalanceItemBySymbol(assetName) != null){
            bitshareBalanceItems.updateFaitBalanceItem(assetName, value);
        } else {
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

        HashMap<String, ArrayList<String>> currenciesChange = new HashMap();

        for (int i = 0; i < accountAssets.size(); i++) {
            AccountAssets accountAsset = accountAssets.get(i);
            if (!accountAsset.symbol.equals(mSmartcoin.getSymbol())) {
                if (!currenciesChange.containsKey(accountAsset.symbol)) {
                    currenciesChange.put(accountAsset.symbol, new ArrayList());
                }
                currenciesChange.get(accountAsset.symbol).add(mSmartcoin.getSymbol());
            }
        }
        this.getEquivalentComponent(currenciesChange, getEquivalentCompRunnable);
    }

    private void getEquivalentValueIndirect(final Asset indirectAsset, final Asset faitCurrency, final Asset reference) {

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
        //If the BTS to EUR cache is valid, then starts the calculation of the equivalent component right away
        if ((BTSCurrencyPriceCacheDate != null) && (now.getTime() - BTSCurrencyPriceCacheDate.getTime() <= 300000)) { //if the cache date of the asset is too old, 300000 = 5 minutes
            glo.start();
        } else { //If not, then the value of BTS to EUR must be calculated first
            WebsocketWorkerThread middle = new WebsocketWorkerThread(new GetLimitOrders(reference.getObjectId(), faitCurrency.getObjectId(), 20, new WitnessResponseListener() {
                @Override
                public void onSuccess(WitnessResponse response) {
                    if (response.result.getClass() == ArrayList.class) {
                        ArrayList list = (ArrayList) response.result;
                        for (Object listObject : list) {
                            if (listObject.getClass() == Market.class) {
                                Market market = ((Market) listObject);
                                if (!market.sell_price.base.asset_id.equalsIgnoreCase(reference.getObjectId())) {
                                    double price = (double) market.sell_price.base.amount / (double) market.sell_price.quote.amount;
                                    int exp = reference.getPrecision() - faitCurrency.getPrecision();
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

    private void updateBitshareBalanceArrays(final ArrayList<String> sym, final ArrayList<String> pre, final ArrayList<String> am, boolean startUp) {
        try {
            BalanceItems bitshareBalanceItems = this.getBalanceItems().addBalancesItems(Coin.BITSHARE);
            GeneralCoinSettings accountSettings = GeneralCoinFactory.getSettings(getContext(), Coin.BITSHARE);
            accountSettings.addChangeSettingListener(this);

            bitshareBalanceItems.clear();

            for (int i = 0; i < sym.size(); i++) {
                Long _amount = Long.parseLong(am.get(i));

                // remove balances which are zero
                if (_amount != 0) {
                    bitshareBalanceItems.addBalanceItem(sym.get(i), pre.get(i), am.get(i), startUp);
                }
            }
        } catch (Exception e) {

        }
    }

    //Links the "Add Account" button click to led the user to add new coin accounts
    @OnClick(R.id.addCoinAccountButton)
    public void onAddCoinAccountButton() {
        this.showNewCoinAccountDialog();
    }

    private String getBrainKey() {
        TinyDB tinyDB;
        tinyDB = new TinyDB(getContext());
        ArrayList<AccountDetails> accountDetails = tinyDB.getListObject(getString(R.string.pref_wallet_accounts), AccountDetails.class);
        for (int i = 0; i < accountDetails.size(); i++) {
            if (accountDetails.get(i).isSelected) {
                return accountDetails.get(i).brain_key;
            }
        }

        return "";
    }

    /**
     * Show a dialog to led the user decide the new coin account to create
     */
    public void showNewCoinAccountDialog(){
        final Dialog dialogNewCoin = new Dialog(getContext(), R.style.stylishDialog);
        dialogNewCoin.setTitle(getString(R.string.add_new_coin_account_dialog));
        dialogNewCoin.setContentView(R.layout.add_new_currency_account);

        final Spinner coinSpinner = (Spinner)dialogNewCoin.findViewById(R.id.coinSpinner);
        Button createButton = (Button)dialogNewCoin.findViewById(R.id.createCurrencyAccount);

        ArrayList<Coin> data = new ArrayList<Coin>();

        for (Coin coin : Coin.values()){
            data.add(coin);
        }

        //The adapter is initialized with all the coins and disabling those that are already used (used - the coin account is already created)
        final ArrayListCoinAdapter coinAdapter = new ArrayListCoinAdapter(this.getActivity(),R.layout.coin_spinner_row,data,coinsUsed,getResources());
        coinSpinner.setAdapter(coinAdapter);

        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final SCWallDatabase db = new SCWallDatabase(getContext());
                final Coin coinSelected = (Coin)coinSpinner.getSelectedItem();

                //gets the bip39 seed already created in the database
                List<AccountSeed> seeds = db.getSeeds(SeedType.BIP39);

                //if there's no seed, then it must be created
                if (seeds.size() == 0) {
                    final Dialog dialog = new Dialog(getContext(), R.style.stylishDialog);
                    dialog.setTitle(getString(R.string.backup_master_seed));
                    dialog.setContentView(R.layout.activity_copybrainkey);
                    final EditText etBrainKey = (EditText) dialog.findViewById(R.id.etBrainKey);
                    final AccountSeed newSeed;
                    try {
                        //using the bip39 dictionary, the new seed is created along with a new mnemonic
                        BufferedReader reader = new BufferedReader(new InputStreamReader(getContext().getAssets().open("bip39dict.txt"), "UTF-8"));
                        String dictionary = reader.readLine();
                        newSeed = new BIP39(dictionary.split(","));
                        String masterSeedWords = newSeed.getMnemonicCodeString().toUpperCase();

                        //the bitshares brainkey is obtained also
                        String brainKey = getBrainKey();

                        //both mnemonic and brain key are concatenated
                        if (masterSeedWords.isEmpty() || brainKey.isEmpty()) {
                            Toast.makeText(getContext(), getResources().getString(R.string.unable_to_create_master_seed), Toast.LENGTH_LONG).show();
                            return;
                        } else {
                            etBrainKey.setText(brainKey + " " + masterSeedWords);
                        }


                        Button btnCancel = (Button) dialog.findViewById(R.id.btnCancel);
                        btnCancel.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.cancel();
                            }
                        });
                        Button btnCopy = (Button) dialog.findViewById(R.id.btnCopy);
                        btnCopy.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                //if the user agreeds and saves the new set of words, then the seed and the new account are created and stored in the db
                                db.putSeed(newSeed);
                                GeneralCoinAccount generalAccount = GeneralCoinFactory.getGeneralCoinAccount(coinSelected, newSeed, coinSelected.getLabel()+" Account");
                                db.putGeneralCoinAccount(generalAccount);

                                Toast.makeText(getContext(), R.string.copied_to_clipboard, Toast.LENGTH_SHORT).show();
                                ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                                ClipData clip = ClipData.newPlainText("label", etBrainKey.getText().toString());
                                clipboard.setPrimaryClip(clip);
                                dialog.cancel();
                                dialogNewCoin.cancel();

                                //the account is loaded in the fragment
                                loadGeneralCoinAccount(generalAccount);
                            }
                        });
                        dialog.setCancelable(false);

                        dialog.show();
                    } catch (Exception e) {

                    }
                } else {
                    //if there is a seed then it is used to create the new account and save it into the database
                    GeneralCoinAccount generalAccount = GeneralCoinFactory.getGeneralCoinAccount(coinSelected, seeds.get(0), coinSelected.getLabel()+" Account");
                    db.putGeneralCoinAccount(generalAccount);
                    dialogNewCoin.cancel();
                    //the account is loaded in the fragment
                    loadGeneralCoinAccount(generalAccount);
                }
            }
        });


        dialogNewCoin.show();
    }

    /**
     * This event gets fired when a new balance item is added to BalanceItems
     *
     * @param event event data with the new item added
     */
    public void onNewBalanceItem(BalanceItemsEvent event){
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

    /**
     * This event gets fired when a balance item is removed from BalanceItems
     *
     * @param event event data with the item removed
     */
    public void onBalanceItemRemoved(BalanceItemsEvent event){
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

    /**
     * This event gets fired when all balances items from a specific coin are removed from BalanceItems
     *
     * @param coin the coin from which the balances were removed
     */
    @Override
    public void onBalanceItemsRemoved(final Coin coin) {
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                progressBar1.setVisibility(View.VISIBLE);
                removeBalanceItemsView(coin);
                progressBar1.setVisibility(View.INVISIBLE);
            }

        });
    }

    /**
     * This event gets fired when a balance item is modified in BalanceItems
     *
     * @param event event data with the item modified
     */
    public void onBalanceItemUpdated(BalanceItemsEvent event){
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

    /**
     * Removes a balance item view from a specific coin from the fragment
     *
     * @param coin the coin from which the balance item view will be removed
     */
    public void removeBalanceItemsView(Coin coin){
        LinearLayout balanceGroup = null;

        //Find if coin section its already in llBalances
        for (int i=0;i<llBalances.getChildCount();i++) {
            View boxBase = llBalances.getChildAt(i);
            if (((Coin)boxBase.getTag()) == coin){
                balanceGroup = (LinearLayout)boxBase.findViewById(R.id.coin_balance);
            }
        }

        if (balanceGroup != null){
            balanceGroup.removeAllViews();
        }
    }

    /**
     * Removes a balance item view from the fragment
     *
     * @param item the balance item removed from BalanceItems
     * @param index the index where the balance item was in BalanceItems
     * @param newSize the new size of BalanceItems after the removal
     */
    public void removeBalanceItemView(BalanceItem item, int index, int newSize){
        LinearLayout balanceGroup = null;

        //Find if coin section its already in llBalances
        for (int i=0;i<llBalances.getChildCount();i++) {
            View boxBase = llBalances.getChildAt(i);
            if (((Coin)boxBase.getTag()) == item.getCoin()){
                balanceGroup = (LinearLayout)boxBase.findViewById(R.id.coin_balance);
            }
        }

        //the balance items view are distributed in two columns
        //if one item is removed, the next items must be relocated
        if (index < balanceGroup.getChildCount()*2){
            TextView symbolTextViewToOccupy;
            TextView ammountTextViewToOccupy;
            TextView faitTextViewToOccupy;
            TextView symbolTextViewToMoveOut;
            TextView ammountTextViewToMoveOut;
            TextView faitTextViewToMoveOut;

            for(int i=index;i<balanceGroup.getChildCount()*2-1;i++){
                View rowView = balanceGroup.getChildAt(i/2);

                if (i % 2 == 0){
                    symbolTextViewToOccupy = (TextView) rowView.findViewById(R.id.symbol_child_one);
                    ammountTextViewToOccupy = (TextView) rowView.findViewById(R.id.amount_child_one);
                    faitTextViewToOccupy = (TextView) rowView.findViewById(R.id.fait_child_one);
                    symbolTextViewToMoveOut = (TextView) rowView.findViewById(R.id.symbol_child_two);
                    ammountTextViewToMoveOut = (TextView) rowView.findViewById(R.id.amount_child_two);
                    faitTextViewToMoveOut = (TextView) rowView.findViewById(R.id.fait_child_two);
                } else {
                    symbolTextViewToOccupy = (TextView) rowView.findViewById(R.id.symbol_child_two);
                    ammountTextViewToOccupy = (TextView) rowView.findViewById(R.id.amount_child_two);
                    faitTextViewToOccupy = (TextView) rowView.findViewById(R.id.fait_child_two);

                    View nextRowView = balanceGroup.getChildAt((i/2)+1);

                    symbolTextViewToMoveOut = (TextView) nextRowView.findViewById(R.id.symbol_child_one);
                    ammountTextViewToMoveOut = (TextView) nextRowView.findViewById(R.id.amount_child_one);;
                    faitTextViewToMoveOut = (TextView) nextRowView.findViewById(R.id.fait_child_one);
                }

                symbolTextViewToOccupy.setText(symbolTextViewToMoveOut.getText());
                ammountTextViewToOccupy.setText(ammountTextViewToMoveOut.getText());
                faitTextViewToOccupy.setText(faitTextViewToMoveOut.getText());
                symbolTextViewToOccupy.setVisibility(View.VISIBLE);
                ammountTextViewToOccupy.setVisibility(View.VISIBLE);
                faitTextViewToOccupy.setVisibility(View.VISIBLE);
                symbolTextViewToMoveOut.setVisibility(View.INVISIBLE);
                ammountTextViewToMoveOut.setVisibility(View.INVISIBLE);
                faitTextViewToMoveOut.setVisibility(View.INVISIBLE);
            }

            //the new size tells if the last row of views must be eliminated
            if (newSize % 2 == 0){
                View rowView = balanceGroup.getChildAt(balanceGroup.getChildCount()-1);
                balanceGroup.removeView(rowView);

                if (balanceGroup.getChildCount() == 0) {
                    whiteSpaceAfterBalances.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    /**
     * Adds a new balance item view to the fragment
     *
     * @param item the new item to add as a view
     * @param initialLoad is true if the view has not been initialized, false if it was already initialized
     */
    public void addNewBalanceView(final BalanceItem item, boolean initialLoad){
        GeneralCoinSettings coinSettings = GeneralCoinFactory.getSettings(getContext(),item.getCoin());
        GeneralCoinSettings.GeneralCoinSetting precisionSetting = coinSettings.getSetting("precision");

        LinearLayout balanceGroup = null;

        //Find if coin section its already in llBalances
        for (int i=0;i<llBalances.getChildCount();i++) {
            View boxBase = llBalances.getChildAt(i);
            if (((Coin)boxBase.getTag()) == item.getCoin()){
                balanceGroup = (LinearLayout)boxBase.findViewById(R.id.coin_balance);
            }
        }

        //If the balance group view for the new balance item coin doesn't exists, then it's created
        if (balanceGroup == null){
            LayoutInflater layoutInflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            balanceGroup = (LinearLayout)layoutInflater.inflate(R.layout.items_balances_box, null);
            balanceGroup.setTag(item.getCoin());
            TextView balanceGroupTitle = (TextView)balanceGroup.findViewById(R.id.coin_title);
            ImageView balanceGroupIcon = (ImageView)balanceGroup.findViewById(R.id.coin_icon);
            ImageView coinSendButton = (ImageView)balanceGroup.findViewById(R.id.coin_send_btn);
            ImageView coinReceiveButton = (ImageView)balanceGroup.findViewById(R.id.coin_receive_btn);

            //Adds the button "Send coins" to this specific coin group view
            coinSendButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    GoToSendActivity(item.getCoin());
                }
            });

            //Adds the button "Receive coins" to this specific coin group view
            coinReceiveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    GoToRecieveActivity(item.getCoin());
                }
            });

            balanceGroupTitle.setText(item.getCoin().name());
            balanceGroupIcon.setImageResource(item.getCoin().getIcon());

            llBalances.addView(balanceGroup);
            balanceGroup = (LinearLayout)balanceGroup.findViewById(R.id.coin_balance);
        }

        TextView textView2 = null;
        TextView symbolTextView;
        final TextView ammountTextView;
        final TextView faitTextView;
        View lastChild = null;
        boolean theresNoChild = true;

        //if there are items in the balance group view
        //we take the right side of the last child of the balances
        if (balanceGroup.getChildCount() > 0) {
            lastChild = balanceGroup.getChildAt(balanceGroup.getChildCount() - 1);
            textView2 = (TextView) lastChild.findViewById(R.id.symbol_child_two);
            theresNoChild = false;
        }

        //if there are no items in the balances or the right side of the last child
        //is already occupied, then we have to create a new View and occupy the left side
        if (theresNoChild || !textView2.getText().equals("")) {
            LayoutInflater layoutInflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View customView = layoutInflater.inflate(R.layout.items_rows_balances, null);
            symbolTextView = (TextView) customView.findViewById(R.id.symbol_child_one);
            ammountTextView = (TextView) customView.findViewById(R.id.amount_child_one);
            faitTextView =  (TextView) customView.findViewById(R.id.fait_child_one);

            TextView rightSymbolTextView = (TextView) customView.findViewById(R.id.symbol_child_two);
            TextView rightAmmountTextView = (TextView) customView.findViewById(R.id.amount_child_two);
            TextView rightFaitTextView = (TextView) customView.findViewById(R.id.fait_child_two);
            rightSymbolTextView.setText("");
            rightAmmountTextView.setText("");
            rightFaitTextView.setText("");

            balanceGroup.addView(customView);
        } else {
            //In this point the right side is free, so we can use it
            symbolTextView = (TextView) lastChild.findViewById(R.id.symbol_child_two);
            ammountTextView = (TextView) lastChild.findViewById(R.id.amount_child_two);
            faitTextView = (TextView) lastChild.findViewById(R.id.fait_child_two);
        }

        String finalSymbol = "";
        if ((item.getCoin() == Coin.BITSHARE) && (SMARTCOINS.contains(item.getSymbol()))) {
            //adding "bit" if the coin group is Bitshares and the item is a Smartcoin
            finalSymbol = "bit" + item.getSymbol();
        } else {
            finalSymbol = item.getSymbol();

            //this adds "m" or "μ" to the coin label according to the user settings
            //for the precision of this specific coin
            if (precisionSetting != null) {
                switch (precisionSetting.getValue()) {
                    case "5":
                        finalSymbol = "m" + finalSymbol;
                        break;
                    case "2":
                        finalSymbol = "μ" + finalSymbol;
                        break;
                }
            }
        }

        /*If the precision were set by the user, then we have to used that one */
        String precision = "";
        if (precisionSetting != null){
            precision = precisionSetting.getValue();

            //Adjusting bitshares precision to be equal to the user setting.
            //When the user selects precision "8", due to bitshares having already a precision of "3",
            //this precision must be reduced to 3 less, ergo "5"
            if (item.getCoin() == Coin.BITSHARE){
                precision = ""+(Integer.parseInt(precision)-3);
            }
        } else {
            precision = item.getPrecision();
        }

        final AssetsSymbols assetsSymbols = new AssetsSymbols(getContext());
        assetsSymbols.displaySpannable(symbolTextView, finalSymbol);

        double b = powerInFloat(precision, item.getAmmount());
        if ((item.getCoin() == Coin.BITSHARE) && (SMARTCOINS.contains(item.getSymbol().replace("bit", "")))) {
            ammountTextView.setText(String.format(locale, "%.2f", b));
        } else if (assetsSymbols.isUiaSymbol(item.getSymbol()))
            ammountTextView.setText(String.format(locale, "%.4f", b));
        else if (assetsSymbols.isSmartCoinSymbol(item.getSymbol()))
            ammountTextView.setText(String.format(locale, "%.2f", b));
        else ammountTextView.setText(String.format(locale, "%.4f", b));

        //If there are confirmations needed, then print how many in the equivalent value text
        if ((item.getConfirmations() != -1) && (item.getConfirmations() < item.getCoin().getConfirmationsNeeded())){
            int percentageDone = (item.getConfirmations()+1)*100/item.getCoin().getConfirmationsNeeded();
            int confirmationColor = 0;

            if (percentageDone < 34){
                confirmationColor = ContextCompat.getColor(getContext(),R.color.color_confirmations_starting);
            } else if (percentageDone < 67){
                confirmationColor = ContextCompat.getColor(getContext(),R.color.color_confirmations_half);
            } else {
                confirmationColor = ContextCompat.getColor(getContext(),R.color.color_confirmations_almost_complete);
            }

            faitTextView.setTextColor(confirmationColor);
            faitTextView.setText(item.getConfirmations()+" of "+item.getCoin().getConfirmationsNeeded()+" conf");

        }

        //if it's not the initial load, then is a balance received, so it must show an animation and sound
        if (!initialLoad){
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
                                rotateRecieveButton();
                            }
                        });

                    } catch (Exception e) {

                    }
                }
            };
            animateNsoundHandler.postDelayed(rotateTask, 200);
            Log.d("Balances Update", "Animation initiated");
            animateText(ammountTextView, 0, convertLocalizeStringToFloat(returnFromPower(precision, item.getAmmount())));
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

    /**
     * Modifies a balance item view in the fragment
     *
     * @param oldItem a copy of how the balance item was
     * @param newItem the balance item changed
     * @param index the index of the balance item that must be changed
     */
    public void updateBalanceItem(final BalanceItem oldItem, final BalanceItem newItem, final int index){
        GeneralCoinSettings coinSettings = GeneralCoinFactory.getSettings(getContext(),newItem.getCoin());
        GeneralCoinSettings.GeneralCoinSetting precisionSetting = coinSettings.getSetting("precision");

        LinearLayout balanceGroup = null;

        //Find if coin section its already in llBalances
        for (int i=0;i<llBalances.getChildCount();i++) {
            View boxBase = llBalances.getChildAt(i);
            if (((Coin)boxBase.getTag()) == newItem.getCoin()){
                balanceGroup = (LinearLayout)boxBase.findViewById(R.id.coin_balance);
            }
        }

        final Runnable reloadBalances = new Runnable() {
            @Override
            public void run() {
                removeZeroedBalanceViews(newItem.getCoin());
            }
        };

        if (index < balanceGroup.getChildCount() * 2) {
            View rowView = balanceGroup.getChildAt(index / 2);
            final TextView symbolTextView;
            final TextView ammountTextView;
            final TextView faitTextView;

            if (index % 2 == 0) {
                symbolTextView = (TextView) rowView.findViewById(R.id.symbol_child_one);
                ammountTextView = (TextView) rowView.findViewById(R.id.amount_child_one);
                faitTextView = (TextView) rowView.findViewById(R.id.fait_child_one);
            } else {
                symbolTextView = (TextView) rowView.findViewById(R.id.symbol_child_two);
                ammountTextView = (TextView) rowView.findViewById(R.id.amount_child_two);
                faitTextView = (TextView) rowView.findViewById(R.id.fait_child_two);
            }

            String finalSymbol = "";
            if ((newItem.getCoin() == Coin.BITSHARE) && (SMARTCOINS.contains(newItem.getSymbol()))) {
                //adding "bit" if the coin group is Bitshares and the item is a Smartcoin
                finalSymbol = "bit" + newItem.getSymbol();
            } else {
                finalSymbol = newItem.getSymbol();

                //this adds "m" or "μ" to the coin label according to the user settings
                //for the precision of this specific coin
                if (precisionSetting != null) {
                    switch (precisionSetting.getValue()) {
                        case "5":
                            finalSymbol = "m" + finalSymbol;
                            break;
                        case "2":
                            finalSymbol = "μ" + finalSymbol;
                            break;
                    }
                }
            }

            final AssetsSymbols assetsSymbols = new AssetsSymbols(getContext());
            assetsSymbols.displaySpannable(symbolTextView, finalSymbol);

            //Long oldAmmount = Long.parseLong(oldItem.getAmmount());
            //Long newAmmount = Long.parseLong(newItem.getAmmount());
            double oldAmmount = Double.parseDouble(oldItem.getAmmount());
            double newAmmount = Double.parseDouble(newItem.getAmmount());

            /*If the precision were set by the user, then we have to used that one */
            String precision = "";
            if (precisionSetting != null){
                precision = precisionSetting.getValue();

                if (newItem.getCoin() == Coin.BITSHARE){
                    precision = ""+(Integer.parseInt(precision)-3);
                }
            } else {
                precision = newItem.getPrecision();
            }

            //This activates when a coin settings are set by the user
            //because even when the oldAmount is equal to the newAmount
            //the precisions are not
            if (oldAmmount == newAmmount){
                animateText(ammountTextView, convertLocalizeStringToFloat(ammountTextView.getText().toString()), convertLocalizeStringToFloat(returnFromPower(precision, newItem.getAmmount())));
            } else if (oldAmmount > newAmmount) { //loosing money (because the user send some)
                ammountTextView.setTypeface(ammountTextView.getTypeface(), Typeface.BOLD);
                ammountTextView.setTextColor(getResources().getColor(R.color.red));

                animateText(ammountTextView, convertLocalizeStringToFloat(ammountTextView.getText().toString()), convertLocalizeStringToFloat(returnFromPower(precision, newItem.getAmmount())));

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
                                faitTextView.setText("");
                            } catch (Exception e) {

                            }
                        }
                    };

                    animateNsoundHandler.postDelayed(zeroAmount, 4200);
                    animateNsoundHandler.postDelayed(reloadBalances, 5000);
                }

                Log.d("Balances Update", "Animation initiated");
            } else if (oldAmmount < newAmmount) { //gaining money

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
                                    rotateRecieveButton();
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

                animateText(ammountTextView, convertLocalizeStringToFloat(ammountTextView.getText().toString()), convertLocalizeStringToFloat(returnFromPower(precision, newItem.getAmmount())));

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
                                faitTextView.setText("");
                            } catch (Exception e) {

                            }
                        }
                    };

                    animateNsoundHandler.postDelayed(zeroAmount, 4200);
                    animateNsoundHandler.postDelayed(reloadBalances, 5000);
                }
            }

            //The confirmations labels are updated here when confirmations are still needed
            if ((newItem.getConfirmations() != -1) && (newItem.getConfirmations() < newItem.getCoin().getConfirmationsNeeded())) {
                int percentageDone = (newItem.getConfirmations() + 1) * 100 / newItem.getCoin().getConfirmationsNeeded();
                int confirmationColor = 0;

                if (percentageDone < 34) {
                    confirmationColor = ContextCompat.getColor(getContext(), R.color.color_confirmations_starting);
                } else if (percentageDone < 67) {
                    confirmationColor = ContextCompat.getColor(getContext(), R.color.color_confirmations_half);
                } else {
                    confirmationColor = ContextCompat.getColor(getContext(), R.color.color_confirmations_almost_complete);
                }

                faitTextView.setTextColor(confirmationColor);
                faitTextView.setText(newItem.getConfirmations() + " of " + newItem.getCoin().getConfirmationsNeeded() + " conf");

            } else if ((newAmmount != 0) && (!newItem.getFait().equals(""))) {//If there aren't confirmations to show, the fait (EquivalentComponent) gets updated then
                faitTextView.setText("");
                faitTextView.setTextColor(ContextCompat.getColor(getContext(), R.color.receive_amount));

                try {
                    final Currency currency = Currency.getInstance(mSmartcoin.getSymbol());
                    double d = convertLocalizeStringToDouble(returnFromPower(newItem.getPrecision(), newItem.getAmmount()));
                    final Double eqAmount = d * convertLocalizeStringToDouble(newItem.getFait());

                    NumberFormat currencyFormatter = Helper.newCurrencyFormat(getContext(), currency, locale);
                    Log.i(TAG, currencyFormatter.format(eqAmount));

                    String fiatString = String.format(locale, "%s", currencyFormatter.format(eqAmount));

                    faitTextView.setText(fiatString);
                    faitTextView.setVisibility(View.VISIBLE);
                } catch (Exception e) {
                    Log.e(TAG, "Error in updateEquivalentValue : " + e.getMessage());
                    for (StackTraceElement element : e.getStackTrace()) {
                        Log.e(TAG, element.toString());
                    }
                    faitTextView.setVisibility(View.GONE);
                }
            } else {
                faitTextView.setText("");
            }
        }
    }

    public void BalanceAssetsLoad(final ArrayList<String> sym, final ArrayList<String> pre, final ArrayList<String> am, final Boolean onStartUp) {

        final AssetsSymbols assetsSymbols = new AssetsSymbols(getContext());


        updateBitshareBalanceArrays(sym, pre, am, onStartUp);

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

    private void rotateRecieveButton() {
        ImageView rcvBtn = (ImageView) getActivity().findViewById(R.id.recievebtn);
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

    /**
     * Removes all balances items from a specified coin having zero balance
     * @param coin the coin from which to remove zero balances items
     */
    public void removeZeroedBalanceViews(Coin coin) {
        this.getBalanceItems().getBalancesItems(coin).removeZeroBalanceItems();
    }

    Handler animateNsoundHandler = new Handler();

    public void BalanceAssetsUpdate(Coin coin, final ArrayList<String> sym, final ArrayList<String> pre, final ArrayList<String> am) {
        for (int i = 0; i < sym.size(); i++) {
            Long _amount = Long.parseLong(am.get(i));
            BalanceItem balanceItem = this.getBalanceItems().getBalancesItems(coin).findBalanceItemBySymbol(sym.get(i));

            if ((balanceItem != null) || (_amount != 0)) {
                this.getBalanceItems().getBalancesItems(coin).addOrUpdateBalanceItem(sym.get(i), pre.get(i), am.get(i));
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

    double powerInFloat(String i, String str) {
        //float ok = 1.0f;
        float pre = Float.parseFloat(i);
        float value = Float.parseFloat(str);
        //for (int k = 0; k < pre; k++) {
        //}ok = ok * 10;
        return (value / Math.pow(10,pre));
    }

    /**
     * Initializes the transaction table view settings: adjusts the columns title and width,
     * assigns the columns comparators and sets the onclick headers listeners
     *
     */
    private void updateSortTable() {
        SimpleTableHeaderAdapter simpleTableHeaderAdapter = new SimpleTableHeaderAdapter(getContext(), getContext().getString(R.string.date), getContext().getString(R.string.all), getContext().getString(R.string.to_from), getContext().getString(R.string.amount));
        simpleTableHeaderAdapter.setPaddingLeft(getResources().getDimensionPixelSize(R.dimen.transactionsheaderpading));

        cryptoCoinTransfersView.setHeaderAdapter(simpleTableHeaderAdapter);

        cryptoCoinTransfersView.setHeaderSortStateViewProvider(SortStateViewProviders.darkArrows());
        cryptoCoinTransfersView.setColumnWeight(0, 20);
        cryptoCoinTransfersView.setColumnWeight(1, 12);
        cryptoCoinTransfersView.setColumnWeight(2, 27);
        cryptoCoinTransfersView.setColumnWeight(3, 22);
        cryptoCoinTransfersView.setColumnComparator(0, new CryptoCoinTransferDateComparator());
        cryptoCoinTransfersView.setColumnComparator(1, new CryptoCoinTransferSendReceiveComparator());
        cryptoCoinTransfersView.setColumnComparator(3, new CryptoCoinTransferAmountComparator());
        cryptoCoinTransfersView.addHeaderClickListener(new TableHeaderClickListener() {
            @Override
            public void onHeaderClicked(int columnIndex) {
                if (lastTransferTableColumnIndexPressed == columnIndex) {
                    lastTransferTableColumnIndexSortUp = !lastTransferTableColumnIndexSortUp;
                } else {
                    lastTransferTableColumnIndexSortUp = true;
                }

                lastTransferTableColumnIndexPressed = columnIndex;

                updateTableView(true);
            }
        });
    }

    @Override
    public void soundFinish() {
        if (audioSevice) {
            getActivity().stopService(new Intent(getActivity(), MediaService.class));
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

    Handler updateTransactionsList;

    @Override
    public void TransactionUpdate(final List<TransactionDetails> transactionDetails, final int number_of_transactions_in_queue) {

    }

    int counterRepeatTransactionLoad = 0;

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
        if (updateTriggerFromNetworkBroadcast ) {
            sentCallForTransactions = false;
        }
    }

    @OnClick(R.id.load_more_values)
    public void loadMoreTransactions() {
        loadMoreCounter++;
        updateTableView(false);
        int loadedTransaction = loadMoreCounter * SCWallDatabase.DEFAULT_TRANSACTION_BATCH_SIZE;
        int bitshareTransactionCount = database.getTransactionCount(new UserAccount(accountId));
        long bitcoinTransactionCount = database.getGeneralTransactionCount();

        if(loadedTransaction >= bitshareTransactionCount+bitcoinTransactionCount){
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

    Handler loadOndemand = new Handler();

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


    boolean updateTriggerFromNetworkBroadcast = false;

    @Override
    public void loadAll() {
        updateTriggerFromNetworkBroadcast = true;
        loadOnDemand(getActivity());
    }

    AssestsActivty myAssetsActivity;

    boolean firstTimeLoad = true;
    String transactionsLoadedAccountName = "";

    void loadViews(Boolean onResume, Boolean accountNameChanged, boolean faitCurrencyChanged) {
        if (this.coin == Coin.BITSHARE) {
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
                myAssetsActivity = new AssestsActivty(getContext(), to, this, null);
                myAssetsActivity.registerDelegate();
            }

            // get transactions from sharedPref


            //        myTransactions = getTransactions(to);

            if (!onResume || accountNameChanged || faitCurrencyChanged) {
                //            progressBar1.setVisibility(View.VISIBLE);
                myAssetsActivity.loadBalances(to);

                //            number_of_transactions_loaded = 0;
                //            number_of_transactions_to_load = 20;
                //            loadTransactions(getContext(), accountId, this, wifkey, number_of_transactions_loaded, number_of_transactions_to_load, myTransactions);
            }
        } else {
            if (firstTimeLoad) {
                firstTimeLoad = false;
            }

            whiteSpaceAfterBalances.setVisibility(View.VISIBLE);
        }
    }

    void loadBasic(boolean onResume, boolean accountNameChanged, boolean faitCurrencyChanged) {
        if (this.coin == Coin.BITSHARE) {
            Log.d(TAG, "loadBasic");
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
                //            TransactionUpdateOnStartUp(to);
            }

            loadViews(onResume, accountNameChanged, faitCurrencyChanged);
        } else {
            loadViews(onResume, false, false);
        }
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
     * Refreshes the transaction table data by assigning a new adapter.
     * This method should be called whenever there is fresh data in the transfers database table.
     * @param reset: If true, the current transfer list is discarded, and a new query is made to the database.
     */
    private void updateTableView(boolean reset) {
        SCWallDatabase.GeneralTransactionOrder order = SCWallDatabase.GeneralTransactionOrder.DATE;

        switch (lastTransferTableColumnIndexPressed){
            case 0:
                order = SCWallDatabase.GeneralTransactionOrder.DATE;
                break;
            case 1:
                order = SCWallDatabase.GeneralTransactionOrder.IN_OUT;
                break;
            case 4:
                order = SCWallDatabase.GeneralTransactionOrder.AMOUNT;
                break;
        }


        UserAccount account = new UserAccount(accountId);

        if (reset) {
            loadMoreCounter = 1;
            loadMoreButton.setVisibility(View.VISIBLE);
        }

        // Calculate how many items to fetch depending on how many times
        // the 'load more' button has been pressed. Maybe later we can modify the
        // getTransactions method to accept ranges and simplify this code.
        int limit = SCWallDatabase.DEFAULT_TRANSACTION_BATCH_SIZE * loadMoreCounter;
        List<HistoricalTransferEntry> newData = database.getTransactions(account, limit);

        // Here we check if the SortableTableView has its default adapter or our own instance.
        if(cryptoCoinTransfersView.getDataAdapter() instanceof CryptoCoinTransfersTableAdapter && !reset){
            Log.d(TAG,"updating table view");
            cryptoCoinTableAdapter = (CryptoCoinTransfersTableAdapter) cryptoCoinTransfersView.getDataAdapter();
            List<TransactionLog> existingData = cryptoCoinTableAdapter.getData();
            boolean found = true;
            for(HistoricalTransferEntry newEntry : newData){
                for(TransactionLog existingEntry : existingData){
                    if (existingEntry.getType() == TransactionLog.TransactionType.TRANSACTION_TYPE_BITSHARE) {
                        if (newEntry.getHistoricalTransfer().getId().equals(existingEntry.getBitshareTransactionLog().getHistoricalTransfer().getId())) {
                            found = true;
                            break;
                        }
                    }
                }
                if(!found){
                    existingData.add(new TransactionLog(newEntry, account));
                }
                found = false;
            }

        }else{
            Log.d(TAG, "resetting table view");
            TransactionLog newDataArray[] = new TransactionLog[newData.size()];

            HistoricalTransferEntry hte;
            for (int i=0;i<newData.size();i++){
                hte = newData.get(i);
                newDataArray[i] = new TransactionLog(hte,account);
            }

            cryptoCoinTableAdapter = new CryptoCoinTransfersTableAdapter(getContext(), locale, newDataArray);
            cryptoCoinTransfersView.setDataAdapter(cryptoCoinTableAdapter);
        }

        /*
        * LOADING BITCOIN TYPE TRANSACTION HISTORY
        *
        * */

        SCWallDatabase db = new SCWallDatabase(getContext());
        List<GeneralCoinAccount> accountList = db.getActiveAccounts();

        //If reset is false, then we start from the last transaction fetched from the database
        int offset = 0;
        if (!reset){
            offset = SCWallDatabase.DEFAULT_TRANSACTION_BATCH_SIZE * (loadMoreCounter-1);
        }

        List<GeneralTransaction> transactions = db.getGeneralTransactions(accountList, order, lastTransferTableColumnIndexSortUp,offset,SCWallDatabase.DEFAULT_TRANSACTION_BATCH_SIZE);
        TransactionLog newDataArray[] = new TransactionLog[transactions.size()];

        GeneralTransaction gt;
        for (int i=0;i<transactions.size();i++){
            gt = transactions.get(i);
            newDataArray[i] = new TransactionLog(gt,gt.getAccount());
        }

        if (cryptoCoinTransfersView.getDataAdapter() instanceof CryptoCoinTransfersTableAdapter) {
            Log.d(TAG, "updating " + this.coin.name() + " table view");
            cryptoCoinTableAdapter = (CryptoCoinTransfersTableAdapter) cryptoCoinTransfersView.getDataAdapter();
            cryptoCoinTableAdapter.addOrReplaceData(newDataArray);
        } else {
            Log.d(TAG, "resetting " + this.coin.name() + " table view");
            cryptoCoinTableAdapter = new CryptoCoinTransfersTableAdapter(getContext(), locale, newDataArray);
            cryptoCoinTransfersView.setDataAdapter(cryptoCoinTableAdapter);
        }

        cryptoCoinTableAdapter.notifyDataSetChanged();

        if (cryptoCoinTransfersView.getColumnComparator(0) == null) {
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
        if(pdfProgress != null){
            int progress = (int) (percentage * 100);
            pdfProgress.setProgress(progress);
        }
    }

    /**
     * PdfGeneratorListener interface method. Used to dismiss the pdfProgress view.
     */
    @Override
    public void onReady(String message) {
        if(pdfProgress != null && pdfProgress.isShowing()){
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

    @Override
    public void settingChange(GeneralCoinSettingEvent e) {
        final GeneralCoinSettings settings = (GeneralCoinSettings) e.getSource();

        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                BalanceItems balanceItems = getBalanceItems().getBalancesItems(settings.getCoinType());
                balanceItems.fireAllItemsUpdateEvent();
                updateTableView(false);
            }
        });
    }
}