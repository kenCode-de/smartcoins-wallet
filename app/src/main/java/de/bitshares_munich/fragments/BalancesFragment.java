package de.bitshares_munich.fragments;

import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import de.bitsharesmunich.graphenej.Address;
import de.bitsharesmunich.graphenej.Asset;
import de.bitsharesmunich.graphenej.PublicKey;
import de.bitsharesmunich.graphenej.TransferOperation;
import de.bitsharesmunich.graphenej.UserAccount;
import de.bitsharesmunich.graphenej.errors.ChecksumException;
import de.bitsharesmunich.graphenej.interfaces.WitnessResponseListener;
import de.bitsharesmunich.graphenej.models.AccountProperties;
import de.bitsharesmunich.graphenej.models.BaseResponse;
import de.bitsharesmunich.graphenej.models.BlockHeader;
import de.bitsharesmunich.graphenej.models.HistoricalTransfer;
import de.bitsharesmunich.graphenej.models.Market;
import de.bitsharesmunich.graphenej.models.WitnessResponse;
import de.bitsharesmunich.graphenej.objects.Memo;
import de.bitsharesmunich.graphenej.api.GetAccounts;
import de.bitsharesmunich.graphenej.api.GetAssets;
import de.bitsharesmunich.graphenej.api.GetBlockHeader;
import de.bitsharesmunich.graphenej.api.GetLimitOrders;
import de.bitsharesmunich.graphenej.api.GetRelativeAccountHistory;
import de.bitsharesmunich.graphenej.api.LookupAssetSymbols;

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
import java.util.Comparator;
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
import de.bitshares_munich.Interfaces.AssetDelegate;
import de.bitshares_munich.Interfaces.ISound;
import de.bitshares_munich.Interfaces.InternalMovementListener;
import de.bitshares_munich.adapters.TransactionsTableAdapter;
import de.bitshares_munich.adapters.TransferAmountComparator;
import de.bitshares_munich.adapters.TransferDateComparator;
import de.bitshares_munich.adapters.TransferSendReceiveComparator;
import de.bitshares_munich.adapters.TransfersTableAdapter;
import de.bitshares_munich.database.HistoricalTransferEntry;
import de.bitshares_munich.database.SCWallDatabase;
import de.bitshares_munich.models.AccountAssets;
import de.bitshares_munich.models.AccountDetails;
import de.bitshares_munich.models.TransactionDetails;
import de.bitshares_munich.smartcoinswallet.AssestsActivty;
import de.bitshares_munich.smartcoinswallet.AssetsSymbols;
import de.bitshares_munich.smartcoinswallet.AudioFilePath;
import de.bitshares_munich.smartcoinswallet.MediaService;
import de.bitshares_munich.smartcoinswallet.PdfTable;
import de.bitshares_munich.smartcoinswallet.QRCodeActivity;
import de.bitshares_munich.smartcoinswallet.R;
import de.bitshares_munich.smartcoinswallet.RecieveActivity;
import de.bitshares_munich.smartcoinswallet.SendScreen;
import de.bitshares_munich.smartcoinswallet.WebsocketWorkerThread;
import de.bitshares_munich.utils.Application;
import de.bitshares_munich.utils.Crypt;
import de.bitshares_munich.utils.Helper;
import de.bitshares_munich.utils.PermissionManager;
import de.bitshares_munich.utils.SupportMethods;
import de.bitshares_munich.utils.TableViewClickListener;
import de.bitshares_munich.utils.TinyDB;
import de.bitshares_munich.utils.TransactionsHelper;
import de.bitshares_munich.utils.webSocketCallHelper;
import de.codecrafters.tableview.SortableTableView;
import de.codecrafters.tableview.TableDataAdapter;
import de.codecrafters.tableview.toolkit.SimpleTableHeaderAdapter;
import de.codecrafters.tableview.toolkit.SortStateViewProviders;


/**
 * Created by qasim on 5/10/16.
 */
public class BalancesFragment extends Fragment implements AssetDelegate, ISound {
    public final String TAG = this.getClass().getName();
    public static Activity balanceActivity;

    //TODO: REMOVE THIS
    private final boolean DEBUG_DATE_LOADING = false;

    static Boolean audioSevice = false;

    int accountDetailsId;
    String accountId = "";
    DecimalFormat df = new DecimalFormat("0.0");

    Boolean isLoading = false;
    public static Boolean onClicked = false;
    Handler myHandler = new Handler();

    String to = "";

    String wifkey = "";
    String finalFaitCurrency;

    @Bind(R.id.load_more_values)
    Button load_more_values;

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
    int number_of_transactions_loaded = 0;
    int number_of_transactions_to_load = 0;

    @Bind(R.id.whiteSpaceAfterBalances)
    LinearLayout whiteSpaceAfterBalances;

    //    private SortableTableView<TransactionDetails> tableView;
    private SortableTableView<HistoricalTransferEntry> transfersView;
    private ArrayList<TransactionDetails> myTransactions;

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


    Locale locale;
    NumberFormat format;
    String language;

    public static ISound iSound;

    public BalancesFragment() {
        // Required empty public constructor
    }

    webSocketCallHelper myWebSocketHelper;

    /* List of block numbers with missing date information in the database */
    private LinkedList<Long> missingTimes;

    /* Smarcoins Wallet database instance */
    private SCWallDatabase database;

    /* Websocket threads */
    private WebsocketWorkerThread transferHistoryThread;
    private WebsocketWorkerThread getMissingAccountsThread;
    private WebsocketWorkerThread getMissingAssets;
    private WebsocketWorkerThread getMissingTimes;

    private final static List<String> SMARTCOINS = Arrays.asList(new String[] {"CNY","BTC","USD","GOLD","EUR","SILVER",
            "ARS","CAD","GBP","KRW","CHF","JPY","HKD","SGD","AUD","RUB","SBK"});

    /**
     * Callback activated once we get a block header response.
     */
    private WitnessResponseListener mGetMissingTimesListener = new WitnessResponseListener() {

        @Override
        public void onSuccess(final WitnessResponse response) {
            Log.d(TAG, "getMissingTime. onSuccess");
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    BlockHeader blockHeader = (BlockHeader) response.result;
                    boolean updated = database.setBlockTime(blockHeader, missingTimes.peek());
                    if (updated) {
                        Log.d(TAG, "Block time updated in transaction at block: " + missingTimes.peek());
                    } else {
                        Log.w(TAG, "Failed to update time from transaction at block: " + missingTimes.peek());
                    }
                    missingTimes.poll();

                    // If we still have missing times in the queue, work on them
                    if (missingTimes.size() > 0) {
                        long blockNum = missingTimes.peek();
                        getMissingTimes = new WebsocketWorkerThread(new GetBlockHeader(blockNum, mGetMissingTimesListener));
                        getMissingTimes.start();
                    }

                    updateTableView();
                }
            });
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
     * Callback activated once we get a response back from the network informing us about some
     * missing asset details.
     */
    private WitnessResponseListener mLookupAssetsSymbolsListener = new WitnessResponseListener() {

        @Override
        public void onSuccess(final WitnessResponse response) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "assetsUpdater.onSuccess");
                    List<Asset> assets = (List<Asset>) response.result;
                    int count = database.putAssets(assets);
                    if (count > 0) {
                        // Assets updated, refresh table adapter.
                        updateTableView();
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
                    int count = database.putUserAccounts(missingAccounts);
                    if (count > 0) {
                        // User accounts updated, refresh table adapter.
                        updateTableView();
                    }
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
            Log.d(TAG, "mTransferHistoryListener. onSuccess");
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    WitnessResponse<List<HistoricalTransfer>> resp = response;
                    List<HistoricalTransferEntry> historicalTransferEntries = new ArrayList<>();
                    String wif = "";
                    ECKey privateKey = null;
                    PublicKey publicKey = null;
                    Address myAddress = null;
                    try {
                        wif = Crypt.getInstance().decrypt_string(wifkey);
                        privateKey = DumpedPrivateKey.fromBase58(null, wif).getKey();
                        publicKey = new PublicKey(ECKey.fromPublicOnly(privateKey.getPubKey()));
                        myAddress = new Address(publicKey.getKey());
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
                    for(HistoricalTransfer historicalTransfer : resp.result){
                        HistoricalTransferEntry entry = new HistoricalTransferEntry();
                        TransferOperation op = historicalTransfer.getOperation();
                        if(op != null){
                            Memo memo = op.getMemo();
                            if(memo.getByteMessage() != null){
                                Address destinationAddress = memo.getDestination();
                                try {
                                    if(destinationAddress.toString().equals(myAddress.toString())){
                                        Log.d(TAG, String.format("Trying to decrypt message from %s -> %s", memo.getSource().toString(), memo.getDestination().toString()));
                                        String decryptedMessage = Memo.decryptMessage(privateKey, memo.getSource(), memo.getNonce(), memo.getByteMessage());
                                        Log.d(TAG, String.format("Plaintext version: %s", decryptedMessage));
                                        memo.setPlaintextMessage(decryptedMessage);
                                    }
                                } catch (ChecksumException e) {
                                    Log.e(TAG, "ChecksumException. Msg: "+e.getMessage());
                                } catch (NullPointerException e){
                                    Log.e(TAG, "NullPointerException. Msg: "+e.getMessage());
                                }
                            }
                        }else{
                            continue;
                        }
                        entry.setHistoricalTransfer(historicalTransfer);
                        historicalTransferEntries.add(entry);
                    }
                    Log.d(TAG, String.format("Got %d transactions from network request", resp.result.size()));
                    int inserted = database.putTransactions(historicalTransferEntries);
                    Log.d(TAG, String.format("Inserted %d of those into the database", inserted));

                    List<UserAccount> missingAccountNames = database.getMissingAccountNames();
                    if (missingAccountNames.size() > 0) {
                        // Got some missing user names, so we request them to the network.
                        getMissingAccountsThread = new WebsocketWorkerThread(new GetAccounts(missingAccountNames, mGetmissingAccountsListener));
                        getMissingAccountsThread.start();
                        Log.d(TAG, "Requesting missing account names");
                    }

                    List<Asset> missingAssets = database.getMissingAssets();
                    if (missingAssets.size() > 0) {
                        // Got some missing asset symbols, so we request them to the network.
                        getMissingAssets = new WebsocketWorkerThread(new LookupAssetSymbols(missingAssets, mLookupAssetsSymbolsListener));
                        getMissingAssets.start();
                        Log.d(TAG, "Requesting missing assets");
                    }

                    missingTimes = database.getMissingTransferTimes();
                    if (missingTimes.size() > 0) {
                        Long blockNum = missingTimes.peek();
                        getMissingTimes = new WebsocketWorkerThread(new GetBlockHeader(blockNum, mGetMissingTimesListener));
                        getMissingTimes.start();
                        Log.d(TAG, "Requesting missing block times");
                    }
                }
            });
        }

        @Override
        public void onError(BaseResponse.Error error) {
            Log.e(TAG, "mTransferHistoryListener. onError. Msg: " + error.message);
        }
    };

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
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View rootView = inflater.inflate(R.layout.fragment_balances, container, false);
        ButterKnife.bind(this, rootView);
        language = Helper.fetchStringSharePref(getActivity(), getString(R.string.pref_language));
        locale = new Locale(language);
        balanceActivity = getActivity();
        format = NumberFormat.getInstance(locale);
        tvUpgradeLtm.setPaintFlags(tvUpgradeLtm.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        progressDialog = new ProgressDialog(getActivity());

        transfersView = (SortableTableView<HistoricalTransferEntry>) rootView.findViewById(R.id.tableView);
        transfersView.addDataClickListener(new TableViewClickListener(getContext(), (InternalMovementListener) getActivity()));

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
        TransactionUpdateOnStartUp(to);

        handler.postDelayed(updateTask, 2000);

        handler.postDelayed(createFolder, 5000);
        if (!Helper.containKeySharePref(getActivity(), "ltmAmount")) {
            Helper.storeStringSharePref(getActivity(), "ltmAmount", "17611.7");
        }
        getLtmPrice(getActivity(), tvAccountName.getText().toString());
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

        if (accountNameChange) {
            //firstTimeLoad = true;
            if (pendingTransactionsLoad != null)
                pendingTransactionsLoad.removeCallbacksAndMessages(null);
        }

        if (isCheckedTimeZone && !accountNameChange) {
            TransactionUpdateOnStartUp(to);
        }

        if (accountNameChange || (finalFaitCurrency != null && !Helper.getFadeCurrency(getContext()).equals(finalFaitCurrency)))
            llBalances.removeAllViews();

        if (isHideDonationsChanged || accountNameChange || (finalFaitCurrency != null && !Helper.getFadeCurrency(getContext()).equals(finalFaitCurrency))) {
            if (finalFaitCurrency != null && !Helper.getFadeCurrency(getContext()).equals(finalFaitCurrency)) {
                loadBasic(true, accountNameChange, true);
            } else {
                loadBasic(true, accountNameChange, false);
            }

        }

        if (!accountId.equals("")) {
            transferHistoryThread = new WebsocketWorkerThread(new GetRelativeAccountHistory(new UserAccount(accountId), mTransferHistoryListener));
            transferHistoryThread.start();
        } else {
            Log.d(TAG, "account id is empty");
        }

        // Loading transfers from database
        updateTableView();
    }

    @OnClick(R.id.recievebtn)
    public void GoToRecieveActivity() {
        final Intent intent = new Intent(getActivity(), RecieveActivity.class);
        intent.putExtra(getString(R.string.to), to);
        intent.putExtra(getString(R.string.account_id), accountId);
        Animation coinAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.coin_animation);
        coinAnimation.setAnimationListener(new android.view.animation.Animation.AnimationListener() {

            @Override
            public void onAnimationEnd(Animation animation) {
                ((InternalMovementListener)getActivity()).onInternalAppMove();
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
        if (isLoading) {
            final Intent intent = new Intent(getActivity(), SendScreen.class);
            Animation coinAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.coin_animation);
            coinAnimation.setAnimationListener(new android.view.animation.Animation.AnimationListener() {

                @Override
                public void onAnimationEnd(Animation animation) {
                    ((InternalMovementListener) getActivity()).onInternalAppMove();
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
        } else Toast.makeText(getContext(), R.string.loading_msg, Toast.LENGTH_LONG).show();
    }

    @OnClick(R.id.tvUpgradeLtm)
    public void updateLtm() {

        final boolean[] balanceValid = {true};
        final Dialog dialog = new Dialog(getActivity());
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
                        getAccountUpgradeInfo(getActivity(), tvAccountName.getText().toString());
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
        if (isLoading) {
            final Intent intent = new Intent(getContext(), QRCodeActivity.class);
            intent.putExtra("id", 1);
            Animation coinAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.coin_animation);
            coinAnimation.setAnimationListener(new android.view.animation.Animation.AnimationListener() {

                @Override
                public void onAnimationEnd(Animation animation) {
                    ((InternalMovementListener)getActivity()).onInternalAppMove();
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
        } else Toast.makeText(getContext(), R.string.loading_msg, Toast.LENGTH_LONG).show();
    }

    @OnClick(R.id.exportButton)
    public void onExportButton() {
        if (isLoading) {
            Log.i(TAG,"Exporting");
            TableDataAdapter myAdapter = transfersView.getDataAdapter();
            List<HistoricalTransferEntry> data = myAdapter.getData();
            Log.i(TAG,"Constructor");
            PdfTable myTable = new PdfTable(getContext(), getActivity(), "Transactions-scwall");
            myTable.createTable(getContext(), data, new UserAccount(accountId));
        } else {
            Log.i(TAG,"else is loading");
            Toast.makeText(getContext(), R.string.loading_msg, Toast.LENGTH_LONG).show();
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

        SupportMethods.testing("Assets", "Assets views 3", "Asset Activity");

        tinyDB.putListObject(getString(R.string.pref_wallet_accounts), accountDetails);
        SupportMethods.testing("Assets", "Assets views 4", "Asset Activity");
        BalanceAssetsUpdate(sym, pre, am, false);
    }

    public void BalanceAssetsUpdate(final ArrayList<String> sym, final ArrayList<String> pre, final ArrayList<String> am, final Boolean onStartUp) {
        int count = llBalances.getChildCount();

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
            BalanceAssetsUpdate(symbols, pre, am);

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
        Log.d(TAG,"getEquivalentComponent. asset list");
        for(String assetString : assetList) Log.d(TAG, "asset: "+assetString);

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
                    for (final String base : currencies.keySet()) {
                        if (assets.containsKey(base)) {
                            for (final String quote : currencies.get(base)) {
                                if (assets.containsKey(quote)) {
                                    WebsocketWorkerThread glo = new WebsocketWorkerThread(new GetLimitOrders(assets.get(base).getId(), assets.get(quote).getId(), 20, new WitnessResponseListener() {
                                        @Override
                                        public void onSuccess(WitnessResponse response) {
                                            if (response.result.getClass() == ArrayList.class) {
                                                ArrayList list = (ArrayList) response.result;
                                                for (Object listObject : list) {
                                                    if (listObject.getClass() == Market.class) {
                                                        Market market = ((Market) listObject);
                                                        if (!market.sell_price.base.asset_id.equalsIgnoreCase(assets.get(base).getId())) {
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
        for (int i = 0; i < llBalances.getChildCount(); i++) {
            LinearLayout llRow = (LinearLayout) llBalances.getChildAt(i);

            for (int j = 1; j <= 2; j++) {

                TextView tvAsset;
                TextView tvAmount;
                final TextView tvFaitAmount;

                if (j == 1) {
                    tvAsset = (TextView) llRow.findViewById(R.id.symbol_child_one);
                    tvAmount = (TextView) llRow.findViewById(R.id.amount_child_one);
                    tvFaitAmount = (TextView) llRow.findViewById(R.id.fait_child_one);
                } else {
                    tvAsset = (TextView) llRow.findViewById(R.id.symbol_child_two);
                    tvAmount = (TextView) llRow.findViewById(R.id.amount_child_two);
                    tvFaitAmount = (TextView) llRow.findViewById(R.id.fait_child_two);
                }

                if (tvAsset == null || tvAmount == null || tvFaitAmount == null) {
                    Log.i(TAG, "tvAsset tv Amount tvFaitAmount nulls");
                    //TODO
                    updateEquivalentAmount.postDelayed(getEquivalentCompRunnable, 500);
                    return;
                }
                String asset = tvAsset.getText().toString();
                String amount = tvAmount.getText().toString();
                asset = asset.replace("bit", "");

                if (amount.isEmpty()) {
                    amount = "0.0";
                }
                if (!amount.isEmpty() && assetName.equals(asset)) {
                    final Currency currency = Currency.getInstance(finalFaitCurrency);
                    try {
                        double d = convertLocalizeStringToDouble(amount);
                        final Double eqAmount = d * convertLocalizeStringToDouble(value);
                        if (Helper.isRTL(locale, currency.getSymbol())) {
                            getActivity().runOnUiThread(
                                    new Runnable() {
                                        @Override
                                        public void run() {
                                            tvFaitAmount.setText(String.format(locale, "%.2f %s", eqAmount, currency.getSymbol()));
                                        }
                                    });
                        } else {
                            getActivity().runOnUiThread(
                                    new Runnable() {
                                        @Override
                                        public void run() {
                                            tvFaitAmount.setText(String.format(locale, "%s %.2f", currency.getSymbol(), eqAmount));
                                        }
                                    });
                        }

                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tvFaitAmount.setVisibility(View.VISIBLE);
                            }
                        });

                    } catch (Exception e) {
                        Log.e(TAG, "Error in updateEquivalentValue : " + e.getMessage());
                        for (StackTraceElement element : e.getStackTrace()) {
                            Log.e(TAG, element.toString());
                        }
                        getActivity().runOnUiThread(
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        tvFaitAmount.setVisibility(View.GONE);
                                    }
                                }
                        );
                    }
                } else {
                    /*getActivity().runOnUiThread(
                            new Runnable() {
                                @Override
                                public void run() {
                                    tvFaitAmount.setVisibility(View.GONE);
                                }
                            }
                    );*/
                }
            }
        }
    }

    private void getEquivalentComponents(final ArrayList<AccountAssets> accountAssets) {
        Log.d(TAG, "getEquivalentComponents");
        final Runnable getEquivalentCompRunnable = new Runnable() {
            @Override
            public void run() {
                getEquivalentComponents(accountAssets);
            }
        };

        String faitCurrency = Helper.getFadeCurrency(getContext());

        if (faitCurrency.isEmpty()) {
            faitCurrency = "EUR";
        }
        finalFaitCurrency = faitCurrency;

        HashMap<String, ArrayList<String>> currenciesChange = new HashMap();

        for (int i = 0; i < accountAssets.size(); i++) {
            AccountAssets accountAsset = accountAssets.get(i);
            if (!accountAsset.symbol.equals(faitCurrency)) {
                if (!currenciesChange.containsKey(accountAsset.symbol)) {
                    currenciesChange.put(accountAsset.symbol, new ArrayList());
                }
                Log.d(TAG,"Creating mapping: "+accountAsset.symbol+" -> "+faitCurrency);
                currenciesChange.get(accountAsset.symbol).add(faitCurrency);
            }
        }
        this.getEquivalentComponent(currenciesChange, getEquivalentCompRunnable);
    }

    private void getEquivalentValueIndirect(final Asset indirectAsset, final Asset faitCurrency, final Asset reference){
        WebsocketWorkerThread middle = new WebsocketWorkerThread(new GetLimitOrders(reference.getId(), faitCurrency.getId(), 20, new WitnessResponseListener() {
            @Override
            public void onSuccess(WitnessResponse response) {
                if (response.result.getClass() == ArrayList.class) {
                    ArrayList list = (ArrayList) response.result;
                    for (Object listObject : list) {
                        if (listObject.getClass() == Market.class) {
                            Market market = ((Market) listObject);
                            if (!market.sell_price.base.asset_id.equalsIgnoreCase(reference.getId())) {
                                double price = (double)market.sell_price.base.amount / (double)market.sell_price.quote.amount;
                                int exp = reference.getPrecision()- faitCurrency.getPrecision() ;
                                final double middlePrice = price * Math.pow(10, exp);
                                WebsocketWorkerThread glo = new WebsocketWorkerThread(new GetLimitOrders(indirectAsset.getId(), reference.getId(), 20, new WitnessResponseListener() {
                                    @Override
                                    public void onSuccess(WitnessResponse response) {
                                        if (response.result.getClass() == ArrayList.class) {
                                            ArrayList list = (ArrayList) response.result;
                                            for (Object listObject : list) {
                                                if (listObject.getClass() == Market.class) {
                                                    Market market = ((Market) listObject);
                                                    if (!market.sell_price.base.asset_id.equalsIgnoreCase(indirectAsset.getId())) {
                                                        double price = (double)market.sell_price.base.amount / (double)market.sell_price.quote.amount;
                                                        int exp = indirectAsset.getPrecision() - reference.getPrecision();
                                                        price = price * Math.pow(10, exp)* middlePrice;
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

    ArrayList<String> symbolsArray;
    ArrayList<String> precisionsArray;
    ArrayList<String> amountsArray;

    private void updateBalanceArrays(final ArrayList<String> sym, final ArrayList<String> pre, final ArrayList<String> am) {
        try {
            symbolsArray = new ArrayList<>();
            precisionsArray = new ArrayList<>();
            amountsArray = new ArrayList<>();

            for (int i = 0; i < sym.size(); i++) {
                Long _amount = Long.parseLong(am.get(i));

                // remove balances which are zero
                if (_amount != 0) {
                    amountsArray.add(am.get(i));
                    precisionsArray.add(pre.get(i));
                    symbolsArray.add(sym.get(i));
                }
            }
        } catch (Exception e) {

        }
    }

    public void BalanceAssetsLoad(final ArrayList<String> sym, final ArrayList<String> pre, final ArrayList<String> am, final Boolean onStartUp) {

        final AssetsSymbols assetsSymbols = new AssetsSymbols(getContext());


        updateBalanceArrays(sym, pre, am);

        sym.clear();
        sym.addAll(symbolsArray);

        pre.clear();
        pre.addAll(precisionsArray);

        am.clear();
        am.addAll(amountsArray);


        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                SupportMethods.testing("Assets", "Assets views ", "Asset Activity");
                LayoutInflater layoutInflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                llBalances.removeAllViews();

                for (int i = 0; i < sym.size(); i += 2) {
                    int counter = 1;
                    int op = sym.size();
                    int pr;

                    if ((op - i) > 2) {
                        pr = 2;
                    } else {
                        pr = op - i;
                    }

                    View customView = layoutInflater.inflate(R.layout.items_rows_balances, null);
                    for (int l = i; l < i + pr; l++) {
                        if (counter == 1) {
                            TextView textView = (TextView) customView.findViewById(R.id.symbol_child_one);
                            assetsSymbols.displaySpannable(textView, sym.get(l));

                            TextView textView1 = (TextView) customView.findViewById(R.id.amount_child_one);

                            float b = powerInFloat(pre.get(l), am.get(i));
                            if(SMARTCOINS.contains(sym.get(l).replace("bit",""))) {
                                textView1.setText(String.format(locale, "%.2f", b));
                            }else if (assetsSymbols.isUiaSymbol(sym.get(l)))
                                textView1.setText(String.format(locale, "%.4f", b));
                            else if (assetsSymbols.isSmartCoinSymbol(sym.get(l)))
                                textView1.setText(String.format(locale, "%.2f", b));
                            else textView1.setText(String.format(locale, "%.4f", b));

                        }

                        if (counter == 2) {
                            TextView textView2 = (TextView) customView.findViewById(R.id.symbol_child_two);
                            assetsSymbols.displaySpannable(textView2, sym.get(l));

                            TextView textView3 = (TextView) customView.findViewById(R.id.amount_child_two);
                            String r = returnFromPower(pre.get(l), am.get(l));

                            if(SMARTCOINS.contains(sym.get(l).replace("bit",""))) {
                                textView3.setText(String.format(locale, "%.2f", Float.parseFloat(r)));
                            }else if (assetsSymbols.isUiaSymbol(sym.get(l)))
                                textView3.setText(String.format(locale, "%.4f", Float.parseFloat(r)));
                            else if (assetsSymbols.isSmartCoinSymbol(sym.get(l)))
                                textView3.setText(String.format(locale, "%.2f", Float.parseFloat(r)));
                            else
                                textView3.setText(String.format(locale, "%.4f", Float.parseFloat(r)));

                            llBalances.addView(customView);
                        }

                        if (counter == 1 && i == sym.size() - 1) {
                            TextView textView2 = (TextView) customView.findViewById(R.id.symbol_child_two);
                            textView2.setText("");
                            TextView textView3 = (TextView) customView.findViewById(R.id.amount_child_two);
                            textView3.setVisibility(View.GONE);
                            llBalances.addView(customView);
                        }

                        if (counter == 1) {
                            counter = 2;
                        } else counter = 1;
                    }
                }

                if (!onStartUp) {
                    progressBar1.setVisibility(View.GONE);
                    isLoading = true;
                } else {
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
                }

                whiteSpaceAfterBalances.setVisibility(View.GONE);
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

    public void removeZeroedBalanceViews() {

        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                try {

                    for (int i = 0; i < llBalances.getChildCount(); i++) {

                        View row = llBalances.getChildAt(i);

                        TextView tvSymOne = (TextView) row.findViewById(R.id.symbol_child_one);
                        TextView tvAmOne = (TextView) row.findViewById(R.id.amount_child_one);
                        TextView tvfaitOne = (TextView) row.findViewById(R.id.fait_child_one);

                        TextView tvSymtwo = (TextView) row.findViewById(R.id.symbol_child_two);
                        TextView tvAmtwo = (TextView) row.findViewById(R.id.amount_child_two);
                        TextView tvFaitTwo = (TextView) row.findViewById(R.id.fait_child_two);

                        // If first balance in row is zeroed then update it
                        if (tvSymOne.getText().toString().equals("")) {
                            // shift balances from next child here
                            String symbol = "";
                            String amount = "";
                            String fait = "";

                            // Get next non-zero balance
                            if (tvSymtwo.getText().toString().isEmpty()) {
                                // if second balance in row is also empty then get next non-zero balance
                                for (int j = i + 1; j < llBalances.getChildCount(); j++) {
                                    View nextrow = llBalances.getChildAt(j);

                                    TextView tvSymOnenextrow = (TextView) nextrow.findViewById(R.id.symbol_child_one);
                                    TextView tvAmOnenextrow = (TextView) nextrow.findViewById(R.id.amount_child_one);
                                    TextView tvfaitOnenextrow = (TextView) nextrow.findViewById(R.id.fait_child_one);

                                    if (!tvSymOnenextrow.getText().toString().isEmpty()) {
                                        symbol = tvSymOnenextrow.getText().toString();
                                        amount = tvAmOnenextrow.getText().toString();
                                        fait = tvfaitOnenextrow.getText().toString();
                                        tvSymOnenextrow.setText("");
                                        tvAmOnenextrow.setText("");
                                        tvfaitOnenextrow.setText("");
                                        break;
                                    }

                                    TextView tvSymtwonextrow = (TextView) nextrow.findViewById(R.id.symbol_child_two);
                                    TextView tvAmtwonextrow = (TextView) nextrow.findViewById(R.id.amount_child_two);
                                    TextView tvFaitTwonextrow = (TextView) nextrow.findViewById(R.id.fait_child_two);

                                    if (!tvSymtwonextrow.getText().toString().isEmpty()) {
                                        symbol = tvSymtwonextrow.getText().toString();
                                        amount = tvAmtwonextrow.getText().toString();
                                        fait = tvFaitTwonextrow.getText().toString();
                                        tvSymtwonextrow.setText("");
                                        tvAmtwonextrow.setText("");
                                        tvFaitTwonextrow.setText("");
                                        break;
                                    }
                                }
                            } else {
                                // if second balance is row is non-empty then move it to first balance
                                symbol = tvSymtwo.getText().toString();
                                amount = tvAmtwo.getText().toString();
                                fait = tvFaitTwo.getText().toString();
                                tvSymtwo.setText("");
                                tvAmtwo.setText("");
                                tvFaitTwo.setText("");
                            }

                            // update first balance amount
                            AssetsSymbols assetsSymbols = new AssetsSymbols(getContext());
                            if(SMARTCOINS.contains(symbol.replace("bit",""))) {
                                tvAmOne.setText(String.format(locale, "%.2f", Float.parseFloat(amount)));
                            }else if (assetsSymbols.isUiaSymbol(symbol))
                                tvAmOne.setText(String.format(locale, "%.4f", Float.parseFloat(amount)));
                            else if (assetsSymbols.isSmartCoinSymbol(symbol))
                                tvAmOne.setText(String.format(locale, "%.2f", Float.parseFloat(amount)));
                            else
                                tvAmOne.setText(String.format(locale, "%.4f", Float.parseFloat(amount)));

                            assetsSymbols.displaySpannable(tvSymOne, symbol);
                            tvfaitOne.setText(fait);

                            if (fait.isEmpty()) {
                                tvfaitOne.setVisibility(View.GONE);
                            } else {
                                tvfaitOne.setVisibility(View.VISIBLE);
                            }
                        }

                        if (tvSymtwo.getText().toString().isEmpty()) {
                            String symbol = "";
                            String amount = "";
                            String fait = "";

                            // Get next non-zero balance
                            for (int j = i + 1; j < llBalances.getChildCount(); j++) {
                                View nextrow = llBalances.getChildAt(j);

                                TextView tvSymOnenextrow = (TextView) nextrow.findViewById(R.id.symbol_child_one);
                                TextView tvAmOnenextrow = (TextView) nextrow.findViewById(R.id.amount_child_one);
                                TextView tvfaitOnenextrow = (TextView) nextrow.findViewById(R.id.fait_child_one);

                                if (!tvSymOnenextrow.getText().toString().isEmpty()) {
                                    symbol = tvSymOnenextrow.getText().toString();
                                    amount = tvAmOnenextrow.getText().toString();
                                    fait = tvfaitOnenextrow.getText().toString();
                                    tvSymOnenextrow.setText("");
                                    tvAmOnenextrow.setText("");
                                    tvfaitOnenextrow.setText("");
                                    break;
                                }

                                TextView tvSymtwonextrow = (TextView) nextrow.findViewById(R.id.symbol_child_two);
                                TextView tvAmtwonextrow = (TextView) nextrow.findViewById(R.id.amount_child_two);
                                TextView tvFaitTwonextrow = (TextView) nextrow.findViewById(R.id.fait_child_two);

                                if (!tvSymtwonextrow.getText().toString().isEmpty()) {
                                    symbol = tvSymtwonextrow.getText().toString();
                                    amount = tvAmtwonextrow.getText().toString();
                                    fait = tvFaitTwonextrow.getText().toString();
                                    tvSymtwonextrow.setText("");
                                    tvAmtwonextrow.setText("");
                                    tvFaitTwonextrow.setText("");
                                    break;
                                }
                            }

                            AssetsSymbols assetsSymbols = new AssetsSymbols(getContext());
                            if(SMARTCOINS.contains(symbol.replace("bit",""))) {
                                tvAmtwo.setText(String.format(locale, "%.2f", Float.parseFloat(amount)));
                            }else if (assetsSymbols.isUiaSymbol(symbol))
                                tvAmtwo.setText(String.format(locale, "%.4f", Float.parseFloat(amount)));
                            else if (assetsSymbols.isSmartCoinSymbol(symbol))
                                tvAmtwo.setText(String.format(locale, "%.2f", Float.parseFloat(amount)));
                            else
                                tvAmtwo.setText(String.format(locale, "%.4f", Float.parseFloat(amount)));

                            assetsSymbols.displaySpannable(tvSymtwo, symbol);
                            tvFaitTwo.setText(fait);

                            if (fait.isEmpty()) {
                                tvFaitTwo.setVisibility(View.GONE);
                            } else {
                                tvFaitTwo.setVisibility(View.VISIBLE);
                            }
                        }


                    }

                    // remove empty rows
                    for (int i = 0; i < llBalances.getChildCount(); i++) {
                        View row = llBalances.getChildAt(i);

                        TextView tvSymOne = (TextView) row.findViewById(R.id.symbol_child_one);
                        TextView tvSymtwo = (TextView) row.findViewById(R.id.symbol_child_two);

                        if (tvSymOne.getText().toString().isEmpty() && tvSymtwo.getText().toString().isEmpty()) {
                            llBalances.removeView(row);
                        }
                    }

                    if (llBalances.getChildCount() == 0) {
                        whiteSpaceAfterBalances.setVisibility(View.VISIBLE);
                    }
                } catch (Exception e) {
                }
            }
        });
    }

    Handler animateNsoundHandler = new Handler();

    public void BalanceAssetsUpdate(final ArrayList<String> sym, final ArrayList<String> pre, final ArrayList<String> am) {
        final Runnable reloadBalances = new Runnable() {
            @Override
            public void run() {
                removeZeroedBalanceViews();
            }
        };

        getActivity().runOnUiThread(new Runnable() {
            public void run() {

                try {
                    // remove zero balances not in previously loaded balances
                    List<Integer> indexesToRemove = new ArrayList<>();

                    for (int i = 0; i < sym.size(); i++) {
                        Long _amount = Long.parseLong(am.get(i));

                        if (_amount == 0) {
                            Boolean matchFound = symbolsArray.contains(sym.get(i));

                            if (!matchFound) {
                                indexesToRemove.add(i);
                                sym.remove(i);
                                am.remove(i);
                                pre.remove(i);

                                sym.trimToSize();
                                am.trimToSize();
                                pre.trimToSize();

                                i--;
                            }
                        }
                    }
                } catch (Exception e) {

                }


                try {

                    LayoutInflater layoutInflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    int count = llBalances.getChildCount();
                    int m = 0;

                    try {

                        Log.d("Balances Update", "Start");
                        Boolean animateOnce = true;

                        for (int i = 0; i < count; i++) {

                            // count == number of row
                            // m == number of child in the row
                            // Get balances row
                            LinearLayout linearLayout = (LinearLayout) llBalances.getChildAt(i);
                            TextView tvSymOne = (TextView) linearLayout.findViewById(R.id.symbol_child_one);
                            TextView tvAmOne = (TextView) linearLayout.findViewById(R.id.amount_child_one);
                            TextView tvfaitOne = (TextView) linearLayout.findViewById(R.id.fait_child_one);

                            TextView tvSymtwo = (TextView) linearLayout.findViewById(R.id.symbol_child_two);
                            TextView tvAmtwo = (TextView) linearLayout.findViewById(R.id.amount_child_two);
                            TextView tvFaitTwo = (TextView) linearLayout.findViewById(R.id.fait_child_two);

                            // First child updation
                            if (sym.size() > m) {

                                Log.d("Balances Update", "sym size 1 : " + Long.toString(m));

                                String symbol = sym.get(m);

                                Log.d("Balances Update", "symbol : " + symbol);

                                String amount = "";


                                if (pre.size() > m && am.size() > m) {
                                    amount = returnFromPower(pre.get(m), am.get(m));
                                }

                                Log.d("Balances Update", "amount : " + symbol);

                                String amountInInt = am.get(m);

                                Log.d("Balances Update", "amount in int : " + amountInInt);

                                String txtSymbol = "";
                                String txtAmount = "";

                                if (symbolsArray.size() > m) {
                                    txtSymbol = symbolsArray.get(m);
                                    txtAmount = amountsArray.get(m);
                                }

                                Log.d("Balances Update", "old symbol : " + txtSymbol);

                                Log.d("Balances Update", "old amount : " + txtAmount);

                                if (!symbol.equals(txtSymbol)) {
                                    AssetsSymbols assetsSymbols = new AssetsSymbols(getContext());
                                    assetsSymbols.displaySpannable(tvSymOne, symbol);
                                }

                                if (!amountInInt.equals(txtAmount)) {
                                    // previous amount

                                    if (txtAmount.isEmpty()) {
                                        txtAmount = "0";
                                    }

                                    Long txtAmount_d = Long.parseLong(txtAmount);

                                    // New amount
                                    Long amount_d = Long.parseLong(amountInInt);

                                    // Balance is sent
                                    if (txtAmount_d > amount_d) {

                                        Log.d("Balances Update", "Balance sent");

                                        SupportMethods.testing("float", txtAmount_d, "txtamount");
                                        SupportMethods.testing("float", amount_d, "amount");
                                        tvAmOne.setTypeface(tvAmOne.getTypeface(), Typeface.BOLD);
                                        tvAmOne.setTextColor(getResources().getColor(R.color.red));

                                        animateText(tvAmOne, convertLocalizeStringToFloat(tvAmOne.getText().toString()), convertLocalizeStringToFloat(amount));

                                        final TextView cView = tvAmOne;
                                        final TextView aView = tvSymOne;
                                        final TextView bView = tvfaitOne;

                                        final Runnable updateTask = new Runnable() {
                                            @Override
                                            public void run() {
                                                try {
                                                    cView.setTextColor(getResources().getColor(R.color.recieveamount));
                                                } catch (Exception e) {

                                                }
                                            }
                                        };
                                        animateNsoundHandler.postDelayed(updateTask, 4000);

                                        if (amount_d == 0) {
                                            final Runnable zeroAmount = new Runnable() {
                                                @Override
                                                public void run() {
                                                    try {
                                                        cView.setText("");
                                                        aView.setText("");
                                                        bView.setText("");
                                                    } catch (Exception e) {

                                                    }
                                                }
                                            };

                                            animateNsoundHandler.postDelayed(zeroAmount, 4200);
                                            animateNsoundHandler.postDelayed(reloadBalances, 5000);
                                        }

                                        Log.d("Balances Update", "Animation initiated");
                                    }
                                    // Balance is rcvd
                                    else if (amount_d > txtAmount_d) {

                                        Log.d("Balances Update", "Balance received");

                                        tvAmOne.setTypeface(tvAmOne.getTypeface(), Typeface.BOLD);
                                        tvAmOne.setTextColor(getResources().getColor(R.color.green));

                                        // run animation
                                        if (animateOnce) {
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

                                            animateOnce = false;

                                            Log.d("Balances Update", "Animation initiated");
                                        }

                                        animateText(tvAmOne, convertLocalizeStringToFloat(tvAmOne.getText().toString()), convertLocalizeStringToFloat(amount));

                                        Log.d("Balances Update", "Text Animated");

                                        final TextView cView = tvAmOne;
                                        final TextView aView = tvSymOne;
                                        final TextView bView = tvfaitOne;

                                        final Runnable updateTask = new Runnable() {
                                            @Override
                                            public void run() {
                                                try {
                                                    cView.setTextColor(getResources().getColor(R.color.recieveamount));
                                                } catch (Exception e) {

                                                }
                                            }
                                        };
                                        animateNsoundHandler.postDelayed(updateTask, 4000);

                                        if (amount_d == 0) {
                                            final Runnable zeroAmount = new Runnable() {
                                                @Override
                                                public void run() {
                                                    try {
                                                        cView.setText("");
                                                        aView.setText("");
                                                        bView.setText("");
                                                    } catch (Exception e) {

                                                    }
                                                }
                                            };

                                            animateNsoundHandler.postDelayed(zeroAmount, 4200);
                                            animateNsoundHandler.postDelayed(reloadBalances, 5000);
                                        }
                                        Log.d("Balances Update", "Rcv done");
                                    }
                                }
                                m++;
                                Log.d("Balances Update", "m++");
                            } else {
                                Log.d("Balances Update", "linearLayout.removeAllViews");
                                linearLayout.removeAllViews();
                            }

                            // Second child updation
                            if (sym.size() > m) {
                                Log.d("Balances Update", "sym size 2 : " + Long.toString(m));

                                String symbol = sym.get(m);
                                String amount = "";

                                Log.d("Balances Update", "symbol : " + symbol);

                                if (pre.size() > m && am.size() > m) {
                                    amount = returnFromPower(pre.get(m), am.get(m));
                                }

                                Log.d("Balances Update", "amount : " + amount);

                                String amountInInt = am.get(m);

                                Log.d("Balances Update", "amount in int : " + amountInInt);

                                String txtSymbol = "";
                                String txtAmount = "";
                                if (symbolsArray.size() > m) {
                                    txtSymbol = symbolsArray.get(m);
                                    txtAmount = amountsArray.get(m);
                                }

                                Log.d("Balances Update", "old symbol : " + txtSymbol);
                                Log.d("Balances Update", "old amount : " + txtAmount);

                                if (txtAmount.isEmpty()) {
                                    txtAmount = "0";
                                }

                                Long txtAmount_d = Long.parseLong(txtAmount);

                                Long amount_d = Long.parseLong(amountInInt);

                                if (!symbol.equals(txtSymbol)) {
                                    AssetsSymbols assetsSymbols = new AssetsSymbols(getContext());
                                    assetsSymbols.displaySpannable(tvSymtwo, symbol);

                                }

                                if (!amountInInt.equals(txtAmount)) {
                                    tvAmtwo.setVisibility(View.VISIBLE);

                                    // balance is sent
                                    if (txtAmount_d > amount_d) {
                                        Log.d("Balances Update", "Balance sent");
                                        tvAmtwo.setTextColor(getResources().getColor(R.color.red));
                                        tvAmtwo.setTypeface(tvAmtwo.getTypeface(), Typeface.BOLD);

                                        animateText(tvAmtwo, convertLocalizeStringToFloat(tvAmtwo.getText().toString()), convertLocalizeStringToFloat(amount));
                                        Log.d("Balances Update", "Text animated");

                                        final TextView cView = tvAmtwo;
                                        final TextView aView = tvSymtwo;
                                        final TextView bView = tvFaitTwo;

                                        final Runnable updateTask = new Runnable() {
                                            @Override
                                            public void run() {
                                                try {
                                                    cView.setTextColor(getResources().getColor(R.color.recieveamount));
                                                } catch (Exception e) {

                                                }
                                            }
                                        };

                                        animateNsoundHandler.postDelayed(updateTask, 4000);

                                        if (amount_d == 0) {
                                            final Runnable zeroAmount = new Runnable() {
                                                @Override
                                                public void run() {
                                                    try {
                                                        cView.setText("");
                                                        aView.setText("");
                                                        bView.setText("");
                                                    } catch (Exception e) {

                                                    }
                                                }
                                            };

                                            animateNsoundHandler.postDelayed(zeroAmount, 4200);
                                            animateNsoundHandler.postDelayed(reloadBalances, 5000);
                                        }

                                        Log.d("Balances Update", "Animation done");

                                    }
                                    // Balance is recieved
                                    else if (amount_d > txtAmount_d) {
                                        Log.d(TAG, "Balance is received. amount_d: "+amount_d+", txtAmount_d: "+txtAmount_d);
                                        tvAmtwo.setTextColor(getResources().getColor(R.color.green));
                                        tvAmtwo.setTypeface(tvAmtwo.getTypeface(), Typeface.BOLD);

                                        // run animation
                                        if (animateOnce) {
                                            final Runnable playSOund = new Runnable() {
                                                @Override
                                                public void run() {
                                                    try {
                                                        playSound();
                                                    } catch (Exception e) {

                                                    }
                                                }
                                            };

                                            final Runnable rotateTask = new Runnable() {
                                                @Override
                                                public void run() {
                                                    try {
                                                        getActivity().runOnUiThread(new Runnable() {
                                                            public void run() {
                                                                try {
                                                                    rotateRecieveButton();
                                                                } catch (Exception e) {

                                                                }
                                                            }
                                                        });

                                                    } catch (Exception e) {

                                                    }
                                                }
                                            };

                                            animateNsoundHandler.postDelayed(playSOund, 100);
                                            animateNsoundHandler.postDelayed(rotateTask, 200);

                                            animateOnce = false;

                                            Log.d("Balances Update", "Animation initiated");
                                        }

                                        animateText(tvAmtwo, convertLocalizeStringToFloat(tvAmtwo.getText().toString()), convertLocalizeStringToFloat(amount));
                                        Log.d("Balances Update", "Text animated");

                                        final TextView cView = tvAmtwo;
                                        final TextView aView = tvSymtwo;
                                        final TextView bView = tvFaitTwo;

                                        //final Handler handler = new Handler();

                                        final Runnable updateTask = new Runnable() {
                                            @Override
                                            public void run() {
                                                try {
                                                    cView.setTextColor(getResources().getColor(R.color.recieveamount));
                                                } catch (Exception e) {

                                                }
                                            }
                                        };

                                        animateNsoundHandler.postDelayed(updateTask, 4000);

                                        if (amount_d == 0) {
                                            final Runnable zeroAmount = new Runnable() {
                                                @Override
                                                public void run() {
                                                    try {
                                                        cView.setText("");
                                                        aView.setText("");
                                                        bView.setText("");
                                                    } catch (Exception e) {

                                                    }
                                                }
                                            };

                                            animateNsoundHandler.postDelayed(zeroAmount, 4200);
                                            animateNsoundHandler.postDelayed(reloadBalances, 5000);
                                        }
                                        Log.d("Balances Update", "rcv done");
                                    }
                                }
                                m++;
                                Log.d("Balances Update", "m updated");
                            } else {
                                Log.d("Balances Update", "else when sym > m");
                                // i == number of row
                                if (i == (count - 1)) // if its the last row
                                {
                                    if (sym.size() > m) // if number of balances is more than traversed
                                        m--;            // then minus 1 from m
                                }
                            }
                        }


                        // Calculate m : number of balances loaded in ui
                        m = 0;
                        for (int i = 0; i < llBalances.getChildCount(); i++) {
                            LinearLayout linearLayout = (LinearLayout) llBalances.getChildAt(i);
                            TextView tvSymOne = (TextView) linearLayout.findViewById(R.id.symbol_child_one);
                            TextView tvSymtwo = (TextView) linearLayout.findViewById(R.id.symbol_child_two);

                            if (!tvSymOne.getText().toString().isEmpty()) {
                                m++;
                            }

                            if (!tvSymtwo.getText().toString().isEmpty()) {
                                m++;
                            }
                        }


                        Log.d(TAG, "Number of balances loaded : " + Long.toString(m));

                        // Insert/remove balance objects if updated
                        Log.d("Balances Update", "Insert or remove balance objects if needed");
                        AssetsSymbols assetsSymbols = new AssetsSymbols(getContext());
                        int loop = sym.size() - m; // number of extra balances to be loaded

                        if (loop > 0) {
                            Log.d("Balances Update", "Yes updation required : " + Long.toString(loop));

                            for (int i = m; i < sym.size(); i += 2) {
                                int counter = 1;
                                int totalNumberOfBalances = sym.size(); // total number of balances 6
                                int pr;

                                if ((totalNumberOfBalances - i) > 2) {
                                    pr = 2;
                                } else {
                                    pr = totalNumberOfBalances - i;
                                }

                                View customView = layoutInflater.inflate(R.layout.items_rows_balances, null);

                                for (int l = i; l < (i + pr); l++) {
                                    if (counter == 1) {
                                        TextView textView = (TextView) customView.findViewById(R.id.symbol_child_one);
                                        assetsSymbols.displaySpannable(textView, sym.get(l));

                                        TextView textView1 = (TextView) customView.findViewById(R.id.amount_child_one);

                                        if ((pre.size() > l) && (am.size() > i)) {
                                            String r = returnFromPower(pre.get(l), am.get(i));
                                            textView1.setText(r);
                                            textView1.setText(String.format(locale, "%.4f", Float.parseFloat(r)));
                                        } else textView1.setText("");
                                    }

                                    if (counter == 2) {
                                        TextView textView2 = (TextView) customView.findViewById(R.id.symbol_child_two);
                                        //  textView2.setText(sym.get(l));
                                        assetsSymbols.displaySpannable(textView2, sym.get(l));

                                        TextView textView3 = (TextView) customView.findViewById(R.id.amount_child_two);
                                        if ((pre.size() > l) && (am.size() > l)) {
                                            String r = returnFromPower(pre.get(l), am.get(l));
                                            textView3.setText(String.format(locale, "%.4f", Float.parseFloat(r)));
                                        }

                                        llBalances.addView(customView);

                                        // run animation
                                        if (animateOnce) {
                                            final Runnable playSOund = new Runnable() {
                                                @Override
                                                public void run() {
                                                    try {
                                                        Log.d(TAG,"a");
                                                        playSound();
                                                    } catch (Exception e) {

                                                    }
                                                }
                                            };

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

                                            animateNsoundHandler.postDelayed(playSOund, 100);
                                            animateNsoundHandler.postDelayed(rotateTask, 200);

                                            animateOnce = false;

                                            Log.d("Balances Update", "Animation initiated");
                                        }
                                    }

                                    if ((counter == 1) && (i == (sym.size() - 1))) {
                                        llBalances.addView(customView);

                                        // run animation
                                        if (animateOnce) {
                                            final Runnable playSOund = new Runnable() {
                                                @Override
                                                public void run() {
                                                    try {
                                                        Log.d(TAG,"would be playing sound");
//                                                        playSound();
                                                    } catch (Exception e) {

                                                    }
                                                }
                                            };

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

                                            animateNsoundHandler.postDelayed(playSOund, 100);
                                            animateNsoundHandler.postDelayed(rotateTask, 200);

                                            animateOnce = false;

                                            Log.d("Balances Update", "Animation initiated");
                                        }
                                    }

                                    if (counter == 1) {
                                        counter = 2;
                                    } else counter = 1;
                                }
                            }
                        }
                    } catch (Exception e) {
                        Log.d("Balances Update", e.getMessage());
                    }
                } catch (Exception e) {
                    Log.d("Balances Load", e.getMessage());
                }

                progressBar1.setVisibility(View.GONE);
                whiteSpaceAfterBalances.setVisibility(View.GONE);
                isLoading = true;

                updateBalanceArrays(sym, pre, am);
            }
        });
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
        Log.d(TAG, "updateSortTable");
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
            getActivity().stopService(new Intent(getActivity(), MediaService.class));
        }
        audioSevice = false;
    }

    private static class TransactionsDateComparator implements Comparator<TransactionDetails> {
        @Override
        public int compare(TransactionDetails one, TransactionDetails two) {
            return one.getDate().compareTo(two.getDate());
        }
    }

    private static class TransactionsSendRecieveComparator implements Comparator<TransactionDetails> {
        @Override
        public int compare(TransactionDetails one, TransactionDetails two) {
            return one.getSent().compareTo(two.getSent());
        }
    }

    private static int compareFloats(float change1, float change2) {
        if (change1 < change2) {
            return -1;
        } else if (change1 == change2) {
            return 0; // Fails on NaN however, not sure what you want
        } else if (change2 > change2) {
            return 1;
        } else {
            return 1;
        }
    }

    private static int compareDoubles(double change1, double change2) {
        if (change1 < change2) {
            return -1;
        } else if (change1 == change2) {
            return 0; // Fails on NaN however, not sure what you want
        } else if (change2 > change2) {
            return 1;
        } else {
            return 1;
        }
    }

    private static class TransactionsAmountComparator implements Comparator<TransactionDetails> {
        @Override
        public int compare(TransactionDetails one, TransactionDetails two) {
            return compareDoubles(one.getAmount(), two.getAmount());
        }
    }


    /**
     * Saves transaction list into the shared preferences storage.
     *
     * @param transactionDetails: List of TransactionDetails to store
     * @param accountName:        Account name.
     */
    private void putTransactions(List<TransactionDetails> transactionDetails, String accountName) {
        Log.d(TAG, "putTransactions. account name: " + accountName + ", number of tx: " + transactionDetails.size());
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

    TransactionsTableAdapter myTransactionsTableAdapter;

    public void TransactionUpdateOnStartUp(String accountName) {
        Log.d(TAG, "TransactionUpdateOnStartUp. account name: " + accountName);

        final List<TransactionDetails> localTransactionDetails = getTransactions(accountName);

        if (localTransactionDetails != null && localTransactionDetails.size() > 0) {

            getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    //isSavedTransactions = true;
                    if (myTransactionsTableAdapter == null) {
                        myTransactionsTableAdapter = new TransactionsTableAdapter(getContext(), localTransactionDetails);
                    } else {
                        myTransactionsTableAdapter.clear();
                        myTransactionsTableAdapter.addAll(localTransactionDetails);
                    }
//                    tableView.setDataAdapter(myTransactionsTableAdapter);
                    tableViewparent.setVisibility(View.VISIBLE);
                }
            });
        }
    }

    Handler updateTransactionsList;

    @Override
    public void TransactionUpdate(final List<TransactionDetails> transactionDetails, final int number_of_transactions_in_queue) {

    }

    int counterRepeatTransactionLoad = 0;

    @Override
    public void transactionsLoadComplete(List<TransactionDetails> transactionDetails, int newTransactionsLoaded) {
        Log.d(TAG, "transactionLoadComplete. new tx: " + newTransactionsLoaded);
        try {
            if (updateTriggerFromNetworkBroadcast && (newTransactionsLoaded == 0) && (counterRepeatTransactionLoad++ < 15)) {
                if (Application.isReady) {
                    Application.disconnect();
                }

                sentCallForTransactions = false;

                loadTransactions(getContext(), accountId, this, wifkey, number_of_transactions_loaded, number_of_transactions_to_load, myTransactions);
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

            myTransactions.clear();
            myTransactions.addAll(transactionDetails);


            AssetsSymbols assetsSymbols = new AssetsSymbols(getContext());
            myTransactions = assetsSymbols.updatedTransactionDetails(myTransactions);

            putTransactions(myTransactions, to);

            number_of_transactions_loaded += number_of_transactions_to_load;

            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (updateTransactionsList == null) {
                        updateTransactionsList = new Handler();
                    }

                    if (myTransactionsTableAdapter == null) {
                        myTransactionsTableAdapter = new TransactionsTableAdapter(getContext(), myTransactions);
//                        tableView.setDataAdapter(myTransactionsTableAdapter);
                    } else {
                        myTransactionsTableAdapter = new TransactionsTableAdapter(getContext(), myTransactions);
//                        tableView.setDataAdapter(myTransactionsTableAdapter);
                    }

                    if (myTransactionActivity.finalBlockRecieved) {
                        load_more_values.setVisibility(View.GONE);
                    } else {
                        load_more_values.setVisibility(View.VISIBLE);
                        load_more_values.setEnabled(true);
                    }

                    if (progressBar.getVisibility() != View.GONE)
                        progressBar.setVisibility(View.GONE);

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

        loadTransactions(getContext(), accountId, this, wifkey, number_of_transactions_loaded, number_of_transactions_to_load, myTransactions);

    }

    Boolean isTransactionUpdating = false;

    @Override
    public void loadAgain() {

        Log.d("LogTransactions", "loadAgain");

        Log.d("LogTransactions", updateTriggerFromNetworkBroadcast + "");

        if (updateTriggerFromNetworkBroadcast || myTransactions.size() <= 0) {

            Log.d("LogTransactions", "updateTriggerFromNetworkBroadcast");

//            progressBar.setVisibility(View.VISIBLE);
            load_more_values.setVisibility(View.GONE);
            number_of_transactions_loaded = 0;
            number_of_transactions_to_load = 20;
            loadTransactions(getContext(), accountId, this, wifkey, number_of_transactions_loaded, number_of_transactions_to_load, myTransactions);
            sentCallForTransactions = false;
            pendingTransactionsLoad.removeCallbacksAndMessages(null);

            final AssetDelegate assetDelegate = this;

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {

                    loadTransactions(getContext(), accountId, assetDelegate, wifkey, number_of_transactions_loaded, number_of_transactions_to_load, myTransactions);

                }
            }, 5000);

        } else {
            if (myTransactionsTableAdapter == null) {
                myTransactionsTableAdapter = new TransactionsTableAdapter(getContext(), myTransactions);
            } else
                myTransactionsTableAdapter = new TransactionsTableAdapter(getContext(), myTransactions);
//            tableView.setDataAdapter(myTransactionsTableAdapter);
            load_more_values.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
        }
    }

    @OnClick(R.id.load_more_values)
    public void Load_more_Values() {
        load_more_values.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);
        number_of_transactions_to_load = 20;
        loadTransactions(getContext(), accountId, this, wifkey, number_of_transactions_loaded, number_of_transactions_to_load, myTransactions);
    }

    void isLifeTime(final String name_id, final String id) {

        String getDetails = "{\"id\":" + id + ",\"method\":\"call\",\"params\":[";
        String getDetails2 = ",\"get_accounts\",[[\"" + name_id + "\"]]]}";
        myWebSocketHelper.make_websocket_call(getDetails, getDetails2, webSocketCallHelper.api_identifier.database);
    }

    @Override
    public void getLifetime(String s, int id) {
        myWebSocketHelper.cleanUpTransactionsHandler();

        SupportMethods.testing("getLifetime", s, "s");

        ArrayList<AccountDetails> accountDetails = tinyDB.getListObject(getString(R.string.pref_wallet_accounts), AccountDetails.class);
        SupportMethods.testing("getAccountID", s, "s");

        String result = SupportMethods.ParseJsonObject(s, "result");
        String nameObject = SupportMethods.ParseObjectFromJsonArray(result, 0);
        String expiration = SupportMethods.ParseJsonObject(nameObject, "membership_expiration_date");
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        try {
            Date date1 = dateFormat.parse(expiration);
            Date date2 = dateFormat.parse("1969-12-31T23:59:59");
            if (date2.getTime() >= date1.getTime()) {
                SupportMethods.testing("getLifetime", "true", "s");
                if (accountDetails.size() > accountDetailsId) {
                    accountDetails.get(accountDetailsId).isLifeTime = true;
                    showHideLifeTime(true);
                } else if (accountDetails.size() == 1) {
                    accountDetails.get(0).isLifeTime = true;
                    showHideLifeTime(true);
                }
                tinyDB.putListObject(getString(R.string.pref_wallet_accounts), accountDetails);

            } else {
                SupportMethods.testing("getLifetime", "false", "s");
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

        if (firstTimeLoad) {

//            tableViewparent.setVisibility(View.GONE);
            myTransactions = new ArrayList<>();
            //TODO: Implement this
//            updateSortTableView(tableView, myTransactions);

            //TODO: Implement this
//            tableView.addDataClickListener(new TableViewClickListener(getContext()));
//            progressBar.setVisibility(View.VISIBLE);


            load_more_values.setVisibility(View.GONE);

            firstTimeLoad = false;
        }

        whiteSpaceAfterBalances.setVisibility(View.VISIBLE);

        if (myAssetsActivity == null) {
            myAssetsActivity = new AssestsActivty(getContext(), to, this, null);
            myAssetsActivity.registerDelegate();
        }

        // get transactions from sharedPref


        myTransactions = getTransactions(to);

        if (!onResume || accountNameChanged || faitCurrencyChanged) {
            progressBar1.setVisibility(View.VISIBLE);
            myAssetsActivity.loadBalances(to);

//            progressBar.setVisibility(View.VISIBLE);
            load_more_values.setVisibility(View.GONE);
            number_of_transactions_loaded = 0;
            number_of_transactions_to_load = 20;
            loadTransactions(getContext(), accountId, this, wifkey, number_of_transactions_loaded, number_of_transactions_to_load, myTransactions);
        }
    }

    void loadBasic(boolean onResume, boolean accountNameChanged, boolean faitCurrencyChanged) {

        if (!onResume) {
            isLoading = false;
        }

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
            TransactionUpdateOnStartUp(to);
        }

        loadViews(onResume, accountNameChanged, faitCurrencyChanged);
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

    public void getAccountUpgradeInfo(final Activity activity, final String accountName) {

        //Toast.makeText(activity, activity.getString(R.string.feature_unavaible), Toast.LENGTH_SHORT).show();
        //TODO evaluate removal

        /*ServiceGenerator sg = new ServiceGenerator(getString(R.string.account_from_brainkey_url));
        IWebService service = sg.getService(IWebService.class);
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("method", "upgrade_account");
        hashMap.put("account", accountName);
        try {
            hashMap.put("wifkey", Crypt.getInstance().decrypt_string(wifkey));
        } catch (Exception e) {
        }

        final Call<AccountUpgrade> postingService = service.getAccountUpgrade(hashMap);
        postingService.enqueue(new Callback<AccountUpgrade>() {
            @Override
            public void onResponse(Response<AccountUpgrade> response) {
                if (response.isSuccess()) {
                    AccountUpgrade accountDetails = response.body();
                    if (accountDetails.status.equals("success")) {
                        updateLifeTimeModel(accountName);
                        hideDialog();
                        Toast.makeText(activity, getString(R.string.upgrade_success), Toast.LENGTH_SHORT).show();
                    } else {
                        hideDialog();
                        Toast.makeText(activity, getString(R.string.upgrade_failed), Toast.LENGTH_SHORT).show();
                    }

                } else {
                    hideDialog();
                    Toast.makeText(activity, getString(R.string.upgrade_failed), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Throwable t) {
                hideDialog();
                Toast.makeText(activity, activity.getString(R.string.txt_no_internet_connection), Toast.LENGTH_SHORT).show();
            }
        });*/
    }

    public void getLtmPrice(final Activity activity, final String accountName) {
        //Toast.makeText(activity, activity.getString(R.string.feature_unavaible), Toast.LENGTH_SHORT).show();
        //TODO implement
        /*ServiceGenerator sg = new ServiceGenerator(getString(R.string.account_from_brainkey_url));
        IWebService service = sg.getService(IWebService.class);
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("method", "upgrade_account_fees");
        hashMap.put("account", accountName);
        try {
            hashMap.put("wifkey", Crypt.getInstance().decrypt_string(wifkey));
        } catch (Exception e) {
        }

        final Call<LtmFee> postingService = service.getLtmFee(hashMap);
        postingService.enqueue(new Callback<LtmFee>() {
            @Override
            public void onResponse(Response<LtmFee> response) {
                if (response.isSuccess()) {
                    hideDialog();
                    LtmFee ltmFee = response.body();
                    if (ltmFee.status.equals("success")) {
                        try {
                            JSONObject jsonObject = new JSONObject(ltmFee.transaction);
                            JSONObject jsonObject1 = jsonObject.getJSONArray("operations").getJSONArray(0).getJSONObject(1);
                            JSONObject jsonObject2 = jsonObject1.getJSONObject("fee");
                            String amount = jsonObject2.getString("amount");
                            String temp = SupportMethods.ConvertValueintoPrecision("5", amount);
                            Helper.storeStringSharePref(getActivity(), "ltmAmount", temp);
                        } catch (Exception e) {
                        }
                    }
                }

            }

            @Override
            public void onFailure(Throwable t) {
                hideDialog();
                Toast.makeText(activity, activity.getString(R.string.txt_no_internet_connection), Toast.LENGTH_SHORT).show();
            }
        });*/
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

    TransactionsHelper myTransactionActivity;
    Handler pendingTransactionsLoad;

    void loadTransactions(final Context context, final String id, final AssetDelegate in, final String wkey, final int loaded, final int toLoad, final ArrayList<TransactionDetails> alreadyLoadedTransactions) {
        if (sentCallForTransactions) {
            if (pendingTransactionsLoad == null) {
                pendingTransactionsLoad = new Handler(Looper.getMainLooper());
            }

            pendingTransactionsLoad.removeCallbacksAndMessages(null);

            pendingTransactionsLoad.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (context != null)
                        loadTransactions(context, id, in, wkey, loaded, toLoad, alreadyLoadedTransactions);
                }
            }, 500);

        } else {
            sentCallForTransactions = true;

            if (myTransactionActivity == null) {
                myTransactionActivity = new TransactionsHelper(context, id, in, wkey, loaded, toLoad, alreadyLoadedTransactions);
            } else {
                myTransactionActivity.context = null;
                myTransactionActivity = new TransactionsHelper(context, id, in, wkey, loaded, toLoad, alreadyLoadedTransactions);
            }
        }

    }

    public void isAssets() {
        progressBar.setVisibility(View.GONE);
        progressBar1.setVisibility(View.GONE);
    }

    /**
     * Refreshes table data by assigning a new adapter.
     * This method should be called whenever there is fresh data in the transfers database table.
     */
    private void updateTableView() {
        UserAccount account = new UserAccount(accountId);
        List<HistoricalTransferEntry> transfers = database.getTransactions(account);
        Log.d(TAG, String.format("Updating the table view with %d transactions", transfers.size()));
        transfersView.setDataAdapter(new TransfersTableAdapter(getContext(), account, transfers.toArray(new HistoricalTransferEntry[transfers.size()])));

        if (transfersView.getColumnComparator(0) == null) {
            updateSortTable();
        }
    }
}


