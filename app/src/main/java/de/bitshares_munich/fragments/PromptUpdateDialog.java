package de.bitshares_munich.fragments;

import android.app.Activity;
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
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;

import com.google.gson.Gson;
import de.bitsharesmunich.graphenej.UserAccount;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.bitshares_munich.models.AccountDetails;
import de.bitshares_munich.smartcoinswallet.R;

/**
 * Dialog that will be displayed to the users after the security update prompting to
 * update the potentially unsecure accounts.
 *
 * Created by nelson on 12/16/16.
 */
public class PromptUpdateDialog extends DialogFragment {
    public static final String TAG = "PromptUpdateDialog";
    public static final String KEY_ACCOUNTS = "key_accounts_to_update";

    private AccountDetails[] accountList;
    private UpdateAccountsListListener mListener;
    private ListView accountListView;

    public static PromptUpdateDialog newInstance(List<AccountDetails> accounts){
        PromptUpdateDialog dialog = new PromptUpdateDialog();
        Gson gson = new Gson();
        String json = gson.toJson(accounts);
        Bundle bundle = new Bundle();
        bundle.putString(KEY_ACCOUNTS, json);
        dialog.setArguments(bundle);
        return dialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Light);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if(activity instanceof UpdateAccountsListListener){
            mListener = (UpdateAccountsListListener) activity;
        }else{
            throw new RuntimeException("UpdateAccountsListListener not implemented by the activity");
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().setTitle(R.string.security_update_title);
        View view = inflater.inflate(R.layout.dialog_prompt_account_update, container);
        Button proceed = (Button) view.findViewById(R.id.proceed);
        Button later = (Button) view.findViewById(R.id.later);
        later.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onAccountList(new ArrayList<UserAccount>());
                getDialog().dismiss();
            }
        });
        proceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onAccountList(((AccountListAdapter) accountListView.getAdapter()).getSelectedAccounts());
                getDialog().dismiss();
            }
        });

        // Deserializing account list and giving it to the AccountListAdapter.
        accountListView = (ListView) view.findViewById(R.id.account_list);
        Gson gson = new Gson();
        String jsonAccounts = getArguments().getString(KEY_ACCOUNTS);
        accountList = gson.fromJson(jsonAccounts, AccountDetails[].class);
        Log.d(TAG, "onCreate. accountList: "+accountList);
        accountListView.setAdapter(new AccountListAdapter(getActivity(), R.layout.account_update_prompt_item, accountList));
        return view;
    }

    /**
     * Adapter used to fill in the list of accounts to be updated.
     */
    private class AccountListAdapter extends ArrayAdapter<AccountDetails> implements CompoundButton.OnCheckedChangeListener{

        private HashMap<Integer, UserAccount> selectedAccounts;

        public AccountListAdapter(Context context, int resource, AccountDetails[] objects) {
            super(context, resource, objects);
            selectedAccounts = new HashMap<>();

            // All accounts selected by default
            for(int i = 0; i < objects.length; i++){
                Log.d(TAG, String.format("account id: %s, name: %s", objects[i].account_id, objects[i].account_name));
                selectedAccounts.put(i, new UserAccount(objects[i].account_id, objects[i].account_name));
            }
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if(convertView == null){
                LayoutInflater vi = (LayoutInflater) getContext().getSystemService(
                        Context.LAYOUT_INFLATER_SERVICE);
                convertView = vi.inflate(R.layout.account_update_prompt_item, null);
                CheckBox checkbox = (CheckBox) convertView.findViewById(R.id.update);
                checkbox.setTag(new Integer(position));
                checkbox.setText(getItem(position).account_name);
                checkbox.setOnCheckedChangeListener(this);
            }
            return convertView;
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            UserAccount userAccount = null;
            if(isChecked){
                AccountDetails details = accountList[((Integer)buttonView.getTag()).intValue()];
                userAccount = new UserAccount(details.account_id, details.account_name);
            }
            selectedAccounts.put(((Integer)buttonView.getTag()).intValue(), userAccount);
        }

        public List<UserAccount> getSelectedAccounts(){
            ArrayList<UserAccount> selectedAccountList = new ArrayList<>();
            for(Integer position : selectedAccounts.keySet()){
                if(selectedAccounts.get(position) != null){
                    selectedAccountList.add(selectedAccounts.get(position));
                }
            }
            return selectedAccountList;
        }
    }

    /**
     * Interface to be implemented by the class interested in receive the update from the user
     * interaction with this fragment.
     */
    public interface UpdateAccountsListListener {

        /**
         * The list of accounts the user has selected to be updated, can be empty.
         * @param accountList
         */
        public void onAccountList(List<UserAccount> accountList);
    }
}
