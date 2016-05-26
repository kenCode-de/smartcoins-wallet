package de.bitshares_munich.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import de.bitshares_munich.smartcoinswallet.List_View_Activity;
import de.bitshares_munich.smartcoinswallet.R;

/**
 * Created by qasim on 5/10/16.
 */
public class ContactsFragment extends Fragment {
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

        //now you must initialize your list view
        ListView listview =(ListView)view.findViewById(R.id.contactslist);

        //EDITED Code
       // ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), R.layout.listview_contacts, items);


        listview.setAdapter(new List_View_Activity(getActivity()));

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



}
