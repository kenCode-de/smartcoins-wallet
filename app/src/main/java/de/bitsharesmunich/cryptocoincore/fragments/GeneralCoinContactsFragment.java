package de.bitsharesmunich.cryptocoincore.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ListView;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.bitshares_munich.interfaces.ContactsDelegate;
import de.bitsharesmunich.cryptocoincore.smartcoinwallets.AddEditContacts;
import de.bitshares_munich.smartcoinswallet.ContactListAdapter;
import de.bitshares_munich.smartcoinswallet.R;
import de.bitshares_munich.smartcoinswallet.ShareContact;
import de.bitsharesmunich.cryptocoincore.base.Coin;
import de.bitsharesmunich.cryptocoincore.smartcoinwallets.GeneralCoinContactListAdapter;

/**
 * Created by qasim on 5/10/16.
 */

/**
 * Shows a list of user contacts. Allows the user to add, modify or remove contacts.
 */
public class GeneralCoinContactsFragment extends Fragment implements ContactsDelegate {
    public static ContactsDelegate contactsDelegate;

    @Bind(R.id.contactslist)
    ListView contactslist; /**< List view for the contacts*/
    GeneralCoinContactListAdapter adapter; /**< Adapter for the contacts list view*/

    @Bind(R.id.sharecontact)
    ImageView sharecontact; /**< Touchable option for sharing a contact*/

    @Bind(R.id.addcontact)
    ImageView addcontact; /**< Touchable option for adding a contact*/

    Coin coin; /**< Coin parameters passed on this fragment instanciation*/

    boolean viewCreated = false; /**< tells if the fragment was already created*/
    boolean isVisible = false; /**< tells if the fragment is already visible*/

    public GeneralCoinContactsFragment() {
        // Required empty public constructor
    }

    /**
     * Creates an instance of this fragment passing a specific Coin.
     * The coin parameter is no longer used, because the contacts have many addresses
     * with different coins.
     *
     * @param coin the coin to pass to the fragment
     * @return an instance of this fragment
     */
    public static GeneralCoinContactsFragment newInstance(Coin coin) {
        GeneralCoinContactsFragment generalCoinContactsFragment = new GeneralCoinContactsFragment();

        Bundle args = new Bundle();
        args.putString("coin",coin.toString());
        generalCoinContactsFragment.setArguments(args);

        return generalCoinContactsFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_contacts, container, false);
        ButterKnife.bind(this, view);
        contactsDelegate=this;

        if (this.getArguments().getString("coin") == null){
            coin = Coin.BITSHARE;
        } else {
            coin = Coin.valueOf(this.getArguments().getString("coin", "BITSHARE"));
        }

        adapter = new GeneralCoinContactListAdapter(getActivity(), this.coin);
        contactslist.setAdapter(adapter);

        this.viewCreated = true;
        onInterfaceStartedAndVisible();

        return view;

    }

    /**
     * Captures when the user press the "Add Contact" button
     */
    @OnClick(R.id.addcontact)
    public void AddContact(){
        Intent intent = new Intent(getActivity(), AddEditContacts.class);
        intent.putExtra("activity",99999);
        intent.putExtra("coin",this.coin.name());
        startActivity(intent);
    }
    @Override
    public void OnUpdate(String s,int id){
        adapter.loadmore();
        adapter = new GeneralCoinContactListAdapter(getActivity(), this.coin);
        contactslist.setAdapter(adapter);
    }
    /**
     * Captures when the user press the "Share Contact" button
     */
    @OnClick(R.id.sharecontact)
    public void ShareContact() {
        Intent intent = new Intent(getActivity(), ShareContact.class);
        startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();

    }
    @Override
    public void setUserVisibleHint(boolean visible){
        super.setUserVisibleHint(visible);
        this.isVisible = visible;
        if (visible) {
            this.onInterfaceStartedAndVisible();
        }
    }

    /**
     * Starts the animations whenever the fragment is created and visible to the user
     */
    public void onInterfaceStartedAndVisible(){
        if (viewCreated && isVisible) {
            addcontact.setVisibility(View.INVISIBLE);
            final Animation animationRigthtoLeft = AnimationUtils.loadAnimation(getContext(), R.anim.animation);
            animationRigthtoLeft.setInterpolator(new AccelerateDecelerateInterpolator());
            animationRigthtoLeft.setDuration(333);
            addcontact.postDelayed(new Runnable() {
                public void run() {
                    addcontact.startAnimation(animationRigthtoLeft);
                    addcontact.setVisibility(View.VISIBLE);
                }
            }, 333);
        }
    }
}
