package de.bitshares_munich.fragments;

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
import de.bitshares_munich.Interfaces.ContactsDelegate;
import de.bitshares_munich.smartcoinswallet.AddEditContacts;
import de.bitshares_munich.smartcoinswallet.ListViewActivity;
import de.bitshares_munich.smartcoinswallet.R;
import de.bitshares_munich.smartcoinswallet.ShareContact;

/**
 * Created by qasim on 5/10/16.
 */
public class ContactsFragment extends Fragment implements ContactsDelegate {
    public static ContactsDelegate contactsDelegate;

    @Bind(R.id.contactslist)
    ListView contactslist;
    ListViewActivity adapter;

    @Bind(R.id.sharecontact)
    ImageView sharecontact;

    @Bind(R.id.addcontact)
    ImageView addcontact;

    public ContactsFragment() {
        // Required empty public constructor
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
        adapter = new ListViewActivity(getActivity());
        contactslist.setAdapter(adapter);
        return view;

    }
    @OnClick(R.id.addcontact)
    public void AddContact(){
        Intent intent = new Intent(getActivity(), AddEditContacts.class);
        intent.putExtra("activity",99999);
        startActivity(intent);
    }
    @Override
    public void OnUpdate(String s,int id){
        adapter.loadmore();
        adapter = new ListViewActivity(getActivity());
        contactslist.setAdapter(adapter);
    }
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
        if (visible){
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
