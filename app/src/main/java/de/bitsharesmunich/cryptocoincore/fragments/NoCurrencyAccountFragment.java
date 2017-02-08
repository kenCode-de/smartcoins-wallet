package de.bitsharesmunich.cryptocoincore.fragments;

import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;

import de.bitshares_munich.database.SCWallDatabase;
import de.bitshares_munich.smartcoinswallet.BackupBrainkeyActivity;
import de.bitshares_munich.smartcoinswallet.R;
import de.bitsharesmunich.cryptocoincore.adapters.ViewPagerAdapter;
import de.bitsharesmunich.cryptocoincore.base.AccountSeed;
import de.bitsharesmunich.cryptocoincore.base.Coin;
import de.bitsharesmunich.cryptocoincore.base.SeedType;
import de.bitsharesmunich.cryptocoincore.base.seed.BIP39;
import de.bitsharesmunich.cryptocoincore.bitcoin.BitcoinAccount;


/**
 * Created by hvarona on 05/02/17.
 */
public class NoCurrencyAccountFragment extends Fragment {
    private Coin coin;
    private ViewPager pager;

    public NoCurrencyAccountFragment() {
        // Required empty public constructor
    }

    public static NoCurrencyAccountFragment newInstance(Coin coin) {
        NoCurrencyAccountFragment noCurrencyAccountFragment = new NoCurrencyAccountFragment();

        Bundle args = new Bundle();
        args.putString("coin",coin.toString());
        noCurrencyAccountFragment.setArguments(args);

        return noCurrencyAccountFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View v = inflater.inflate(R.layout.fragment_no_currency_account, container, false);
        final Fragment f = this;

        this.coin = Coin.valueOf(getArguments().getString("coin", "BITSHARE"));

        TextView text = (TextView) v.findViewById(R.id.noCurrencyAccountText);
        text.setText(getString(R.string.no_currency_account,this.coin.toString()));

        if (container instanceof ViewPager) {
            pager = (ViewPager) container;
        }

        Button importButton = (Button) v.findViewById(R.id.importCurrencyAccount);
        Button createButton = (Button) v.findViewById(R.id.createCurrencyAccount);
        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (pager != null){
                    final SCWallDatabase db = new SCWallDatabase(getContext());
                    List<AccountSeed> seeds = db.getSeeds(SeedType.BIP39);

                    if (seeds.size() == 0) {
                        final Dialog dialog = new Dialog(getContext(), R.style.stylishDialog);
                        dialog.setTitle(getString(R.string.backup_master_seed));
                        dialog.setContentView(R.layout.activity_copybrainkey);
                        final EditText etBrainKey = (EditText) dialog.findViewById(R.id.etBrainKey);
                        final AccountSeed newSeed;
                        try {
                            BufferedReader reader = new BufferedReader(new InputStreamReader(getContext().getAssets().open("bip39dict.txt"), "UTF-8"));
                            String dictionary = reader.readLine();
                            newSeed = new BIP39(dictionary.split(","));

                            String masterSeedWords = newSeed.getMnemonicCodeString();
                            if (masterSeedWords.isEmpty()) {
                                Toast.makeText(getContext(), getResources().getString(R.string.unable_to_create_master_seed), Toast.LENGTH_LONG).show();
                                return;
                            } else {
                                etBrainKey.setText(masterSeedWords);
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
                                    db.putSeed(newSeed);
                                    BitcoinAccount bitcoinAccount = new BitcoinAccount(newSeed, "BTC Account");
                                    db.putGeneralCoinAccount(bitcoinAccount);

                                    Toast.makeText(getContext(), R.string.copied_to_clipboard, Toast.LENGTH_SHORT).show();
                                    ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                                    ClipData clip = ClipData.newPlainText("label", etBrainKey.getText().toString());
                                    clipboard.setPrimaryClip(clip);
                                    dialog.cancel();
                                    ((ViewPagerAdapter) pager.getAdapter()).changeBitcoinFragment();
                                }
                            });
                            dialog.setCancelable(false);

                            dialog.show();
                        } catch (Exception e) {

                        }
                    } else {
                        BitcoinAccount bitcoinAccount = new BitcoinAccount(seeds.get(0), "BTC Account");
                        db.putGeneralCoinAccount(bitcoinAccount);
                        ((ViewPagerAdapter) pager.getAdapter()).changeBitcoinFragment();
                    }
                }

                //((ViewPagerAdapter) pager.getAdapter()).changeBitcoinFragment();
            }
        });

        return v;
    }
}