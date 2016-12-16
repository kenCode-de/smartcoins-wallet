package de.bitshares_munich.fragments;

import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.google.gson.Gson;

import java.util.List;

import de.bitshares_munich.models.AccountDetails;
import de.bitshares_munich.smartcoinswallet.R;

/**
 * Created by nelson on 12/16/16.
 */
public class UpdatingAccountsDialog extends DialogFragment {
    public static final String KEY_ACCOUNTS = "key_accounts_to_update";

    private AccountDetails[] accountList;

    static UpdatingAccountsDialog newInstance(List<AccountDetails> accounts){
        UpdatingAccountsDialog dialog = new UpdatingAccountsDialog();
        Gson gson = new Gson();
        String json = gson.toJson(accounts);
        Bundle bundle = new Bundle();
        bundle.putString(KEY_ACCOUNTS, json);
        dialog.setArguments(bundle);
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_updating_accounts, container);
        Button proceed = (Button) view.findViewById(R.id.later);
        Button later = (Button) view.findViewById(R.id.proceed);
        ListView accountListView = (ListView) view.findViewById(R.id.account_list);

        Gson gson = new Gson();
        String jsonAccounts = getArguments().getString(KEY_ACCOUNTS);
        accountList = gson.fromJson(jsonAccounts, AccountDetails[].class);
        accountListView.setAdapter(new AccountListAdapter(getActivity(), R.layout.account_update_list, accountList));

        return view;
    }

    private class AccountListAdapter extends ArrayAdapter<AccountDetails> {

        public AccountListAdapter(Context context, int resource, AccountDetails[] objects) {
            super(context, resource, objects);
        }
    }
}
