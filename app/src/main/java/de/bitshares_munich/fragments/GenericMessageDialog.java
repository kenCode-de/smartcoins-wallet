package de.bitshares_munich.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import de.bitshares_munich.smartcoinswallet.R;

/**
 * This is just a generic dialog implementation that will display a title and a message.
 *
 * Created by nelson on 4/20/17.
 */
public class GenericMessageDialog extends DialogFragment {
    public static final String KEY_TITLE = "key_title";
    public static final String KEY_MESSAGE = "key_message";

    private GenericMessageDialogListener mListener;

    public static GenericMessageDialog newInstance(String title, String message){
        GenericMessageDialog dialog = new GenericMessageDialog();
        Bundle bundle = new Bundle();
        bundle.putString(KEY_TITLE, title);
        bundle.putString(KEY_MESSAGE, message);
        dialog.setArguments(bundle);
        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        getDialog().getWindow().setLayout(LinearLayoutCompat.LayoutParams.MATCH_PARENT, LinearLayoutCompat.LayoutParams.WRAP_CONTENT);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Bundle args = getArguments();
        getDialog().setTitle(args.getString(KEY_TITLE));
        View view = inflater.inflate(R.layout.generic_message_dialog, container);
        ((TextView) view.findViewById(R.id.generic_message)).setText(args.getString(KEY_MESSAGE));
        view.findViewById(R.id.generic_positive).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onOptionSelected(true);
                getDialog().dismiss();
            }
        });

        view.findViewById(R.id.generic_negative).setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                mListener.onOptionSelected(false);
                getDialog().dismiss();
            }
        });
        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof GenericMessageDialogListener) {
            mListener = (GenericMessageDialogListener) activity;
        }else{
            throw new RuntimeException("GenericMessageDialogListener not implemented by the activity");
        }
    }

    /**
     * Interface to be implemented by the class interested in receive the update from the user
     * interaction with this fragment.
     */
    public interface GenericMessageDialogListener {

        /**
         * Method that will inform the listener about the decision taken by the user
         */
        public void onOptionSelected(boolean accepted);
    }
}
