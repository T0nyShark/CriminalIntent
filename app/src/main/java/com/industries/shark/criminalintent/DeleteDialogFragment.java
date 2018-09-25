package com.industries.shark.criminalintent;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;


import java.util.UUID;

public class DeleteDialogFragment extends AppCompatDialogFragment {

    public static final String EXTRA_IS_CRIME_DELETED = "com.industries.shark.criminalintent.deleteresult";
    private static final String ARG_CRIME_ID_TO_DELETE = "deletecrimeid";




    public static DeleteDialogFragment newInstance(UUID id){
        Bundle args = new Bundle();
        args.putSerializable(ARG_CRIME_ID_TO_DELETE, id);
        DeleteDialogFragment fragment = new DeleteDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final UUID uuid = (UUID) getArguments().getSerializable(ARG_CRIME_ID_TO_DELETE);

        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_delete_crime, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setView(view);
        builder.setTitle(R.string.delete_dialog_title_text);
        final CheckBox mDeleteCheckbox = (CheckBox) view.findViewById(R.id.full_delete_checkbox);

        TextView mDeleteTextView = (TextView) view.findViewById(R.id.check_to_delete_text);

        mDeleteTextView.setVisibility(View.INVISIBLE);
        mDeleteCheckbox.setVisibility(View.INVISIBLE);

        builder.setCancelable(false)
                .setPositiveButton(R.string.dialog_delete_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        CrimeLab.get(getActivity()).deleteCrime(uuid);
                           sendResult(Activity.RESULT_OK);
                        if (getActivity() != null) {
                            dialog.cancel(); }
                    }

                })
                .setNegativeButton(R.string.delete_dialog_cancel_btn, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sendResult(Activity.RESULT_OK);
                        dialog.cancel();
                    }
                });
        AlertDialog dialog = builder.create();

        dialog.show();
        return dialog;
    }

    private void sendResult(int resultCode) {
        if (getTargetFragment() == null) {
            return;
        }

        Intent intent = new Intent();


        getTargetFragment().onActivityResult(getTargetRequestCode(), resultCode, intent);
    }

}
