package de.bitshares_munich.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.bitshares_munich.Interfaces.ContactsDelegate;
import de.bitshares_munich.smartcoinswallet.Add_Edit_Contacts;
import de.bitshares_munich.smartcoinswallet.List_View_Activity;
import de.bitshares_munich.smartcoinswallet.R;
import de.bitshares_munich.smartcoinswallet.Share_Contact;

/**
 * Created by qasim on 5/10/16.
 */
public class ContactsFragment extends Fragment implements ContactsDelegate {
    public static ContactsDelegate contactsDelegate;

    @Bind(R.id.contactslist)
    ListView contactslist;
    List_View_Activity adapter;


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
//        ArrayList<String> categories = new ArrayList<String>();
//        categories.add("Automobile");
//        categories.add("Business Services");
//        categories.add("Computers");
//        categories.add("Education");
//        categories.add("Personal");
//        categories.add("Travel");
        View view = inflater.inflate(R.layout.fragment_contacts, container, false);
        ButterKnife.bind(this, view);
        contactsDelegate=this;

        adapter = new List_View_Activity(getActivity());
        contactslist.setAdapter(adapter);
//        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            public void onItemClick(AdapterView<?> a, View v, int position, long id) {
//                AlertDialog.Builder adb=new AlertDialog.Builder(getActivity());
//                adb.setTitle("Delete?");
//                adb.setMessage("Are you sure you want to delete " + position);
//                final int positionToRemove = position;
//                adb.setNegativeButton("Cancel", null);
//                adb.setPositiveButton("Ok", new AlertDialog.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int which) {
//                        a.remove(positionToRemove);
//                        adapter.notifyDataSetChanged();
//                    }});
//                adb.show();
//            }
//        });
        //To have custom list view use this : you must define CustomeAdapter class
        // listview.setadapter(new CustomeAdapter(getActivity()));
        //getActivty is used instead of Context
        return view;

    }
    @OnClick(R.id.addcontact)
    public void AddContact(){
        Intent intent = new Intent(getActivity(), Add_Edit_Contacts.class);
        intent.putExtra("activity",99999);
        startActivity(intent);
    }
    @Override
    public void OnUpdate(String s,int id){
//        contactslist.destroyDrawingCache();
//        contactslist.setVisibility(ListView.INVISIBLE);
//        contactslist.setVisibility(ListView.VISIBLE);
//        adapter.clear();
      adapter.loadmore();
//        adapter = new List_View_Activity(getActivity());
//        adapter.clear();
//// Add data to collection..
//        collection.add();
//// Refresh your listview..
//        listview.getAdapter().notifyDataSetChanged();
        adapter = new List_View_Activity(getActivity());
      contactslist.setAdapter(adapter);
    }
    @OnClick(R.id.sharecontact)
    public void ShareContact() {
        Intent intent = new Intent(getActivity(), Share_Contact.class);
        startActivity(intent);
    }
}
