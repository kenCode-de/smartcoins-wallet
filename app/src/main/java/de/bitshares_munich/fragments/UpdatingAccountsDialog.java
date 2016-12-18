package de.bitshares_munich.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.Gson;
import com.luminiasoft.bitshares.UserAccount;

import java.util.List;

import de.bitshares_munich.Interfaces.UpdatedAccountListener;
import de.bitshares_munich.smartcoinswallet.R;

/**
 * Created by nelson on 12/17/16.
 */

public class UpdatingAccountsDialog extends DialogFragment implements UpdatedAccountListener {
    public static final String TAG = "UpdatingAccountsDialog";
    public static final String KEY_ACCOUNTS = "key_accounts_to_update";

    private ListView accountListView;
    private UpdatingAccountListAdapter adapter;
    private UserAccount[] accountList;

    public static UpdatingAccountsDialog newInstance(List<UserAccount> accountList) {
        UpdatingAccountsDialog dialog = new UpdatingAccountsDialog();
        Gson gson = new Gson();
        String json = gson.toJson(accountList);
        Bundle bundle = new Bundle();
        bundle.putString(KEY_ACCOUNTS, json);
        dialog.setArguments(bundle);
        return dialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_updating_account, container);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getDialog().setTitle(R.string.updating_account_title);
        getDialog().setCanceledOnTouchOutside(false);
        getDialog().setCancelable(false);

        // Deserializing account list and giving it to the AccountListAdapter.
        accountListView = (ListView) view.findViewById(R.id.account_list);
        Gson gson = new Gson();
        String jsonAccounts = getArguments().getString(KEY_ACCOUNTS);
        accountList = gson.fromJson(jsonAccounts, UserAccount[].class);
        adapter = new UpdatingAccountListAdapter(getActivity(), R.layout.account_updating_item, accountList);
        accountListView.setAdapter(adapter);

        Button doneButton = (Button) view.findViewById(R.id.done_button);
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().dismiss();
            }
        });
    }

    @Override
    public void onUpdateStatusChange(UserAccount account, int resultCode) {
        UserAccount[] accounts = adapter.getAccounts();
        Log.d(TAG, "onUpdateStatusChange. account: "+account.getAccountName());
        for(int i = 0; i < accounts.length; i++){
            UserAccount userAccount = accounts[i];
            if(userAccount.getAccountName().equals(account.getAccountName())){
                ImageView done = (ImageView) accountListView.getChildAt(i).findViewById(R.id.done);
                ImageView error = (ImageView) accountListView.getChildAt(i).findViewById(R.id.error);
                ProgressBar progress = (ProgressBar) accountListView.getChildAt(i).findViewById(R.id.progress);
                if(resultCode == UPDATING){
                    Log.d(TAG, "Setting progress view visible");
                    progress.setVisibility(View.VISIBLE);
                }else if(resultCode == SUCCESS){
                    Log.d(TAG, "Setting check mark visible");
                    done.setVisibility(View.VISIBLE);
                    progress.setVisibility(View.GONE);
                }else if(resultCode == FAILURE){
                    error.setVisibility(View.VISIBLE);
                    progress.setVisibility(View.GONE);
                }
                break;
            }
        }

    }

    @Override
    public void onUpdateFinished() {
        Button doneButton = (Button) getDialog().findViewById(R.id.done_button);
        doneButton.setEnabled(true);
    }

    private class UpdatingAccountListAdapter extends ArrayAdapter<UserAccount> {
        private UserAccount[] accounts;

        public UpdatingAccountListAdapter(Context context, int resource, UserAccount[] objects) {
            super(context, resource, objects);
            accounts = objects;
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater vi = (LayoutInflater) getContext().getSystemService(
                        Context.LAYOUT_INFLATER_SERVICE);
                convertView = vi.inflate(R.layout.account_updating_item, null);
                TextView accountName = (TextView) convertView.findViewById(R.id.account_name);
                accountName.setText(getItem(position).getAccountName());
            }
            return convertView;
        }

        public UserAccount[] getAccounts(){
            return accounts;
        }
    }
}
