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

import de.bitshares_munich.smartcoinswallet.BackupBrainkeyActivity;
import de.bitshares_munich.smartcoinswallet.R;
import de.bitsharesmunich.cryptocoincore.adapters.ViewPagerAdapter;
import de.bitsharesmunich.cryptocoincore.base.Coin;


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
                    ((ViewPagerAdapter)pager.getAdapter()).changeBitcoinFragment();

                    final Dialog dialog = new Dialog(getContext() this, R.style.stylishDialog);
                    dialog.setTitle(getString(R.string.backup_brainkey));
                    dialog.setContentView(R.layout.activity_copybrainkey);
                    final EditText etBrainKey = (EditText) dialog.findViewById(R.id.etBrainKey);
                    try {
                        String brainKey = getBrainKey();
                        if (brainKey.isEmpty()) {
                            Toast.makeText(getApplicationContext(), getResources().getString(R.string.unable_to_load_brainkey), Toast.LENGTH_LONG).show();
                            return;
                        } else {
                            etBrainKey.setText(brainKey);
                        }
                    } catch (Exception e) {

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
                            Toast.makeText(BackupBrainkeyActivity.this, R.string.copied_to_clipboard, Toast.LENGTH_SHORT).show();
                            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                            ClipData clip = ClipData.newPlainText("label", etBrainKey.getText().toString());
                            clipboard.setPrimaryClip(clip);
                            dialog.cancel();
                        }
                    });
                    dialog.setCancelable(false);

                    dialog.show();

                }

            }
        });

        return v;
    }
}