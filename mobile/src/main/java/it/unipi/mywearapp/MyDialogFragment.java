package it.unipi.mywearapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;

/**
 * This class implements the DialogFragment, simply build an AlertDialog, while the implementation of the Positive Response & the Negative one is realized
 * into the each activity the make use of the dialog
 */
public class MyDialogFragment extends DialogFragment {
    NoticeDialogListener mListener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        //Recovering the argument passed from the activity
        Bundle args = getArguments();
        int selectedPeriod = args.getInt( GlobalInfoContainer.SELECTED_PERIOD_KEY, 0);
        boolean isClearButton = args.getBoolean( GlobalInfoContainer.IS_CLEAR_BUTTON_KEY, false);
        boolean isClearAllSwings = args.getBoolean( GlobalInfoContainer.IS_CLEAR_ALL_SWINGS_KEY, false);
        int itemID = args.getInt( GlobalInfoContainer.ITEM_ID_KEY, 0 );

        AlertDialog.Builder builder;
        if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP )
            builder = new AlertDialog.Builder( getActivity(), android.R.style.Theme_Material_Light_Dialog_Alert);
        else
            builder = new AlertDialog.Builder( getActivity() );

        builder.setTitle( GlobalInfoContainer.getAlertDialogTitle() )
                .setIcon( android.R.drawable.ic_dialog_alert )
                .setNegativeButton( android.R.string.no,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                mListener.onDialogNegativeClick( MyDialogFragment.this); }
                        })
                .setPositiveButton(android.R.string.yes,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                mListener.onDialogPositiveClick( MyDialogFragment.this); }
                        });

        if( isClearButton == true )
            builder.setMessage( ListActivity.ALERT_DIALOG_LIST_PERIOD + " " + SwingPeriod.getStringFromIntegerPeriod( selectedPeriod ) + " ?");
        else if( isClearAllSwings == false )
            builder.setMessage( ListActivity.ALERT_DIALOG_LIST_ITEM + itemID + "?");
        else//is isClearAllSwing for settings
            builder.setMessage( SettingsActivity.ALERT_DIALOG_SETTINGS_TEXT );

        return builder.create();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        Activity activity = null;
        if (context instanceof Activity){
            activity=(Activity) context;
        }
        try {
            mListener = (NoticeDialogListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException( activity.toString() + " must implement NoticeDialogListener");
        }
    }

    //This interface is used to communicate with the Activity
    public interface NoticeDialogListener {
        public void onDialogPositiveClick( DialogFragment dialog);
        public void onDialogNegativeClick( DialogFragment dialog);
    }
}
