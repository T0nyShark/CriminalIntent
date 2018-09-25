package com.industries.shark.criminalintent;


import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import java.io.File;

public class ImagePreviewFragment extends DialogFragment
{
    public static final String EXTRA_PREVIEW_IMG_FILE = "com.industries.shark.criminalintent.preview_image";
    private static final String ARG_PREVIEW_IMG_FILE = "preview_image";

    private Bitmap mBitmapPrev;
    private ImageView mPreviewImage;

    public static ImagePreviewFragment newInstance(File imgFile){
        Bundle args = new Bundle();
       args.putSerializable(ARG_PREVIEW_IMG_FILE, imgFile);

        ImagePreviewFragment fragment = new ImagePreviewFragment();
          fragment.setArguments(args);

        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_image_preview, null);

        File imageFile = (File) getArguments().getSerializable(ARG_PREVIEW_IMG_FILE);

        mBitmapPrev = PictureUtils.convertFileToBitmap(imageFile);

        mPreviewImage = (ImageView) view.findViewById(R.id.image_preview);

        mPreviewImage.setImageBitmap(mBitmapPrev);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());


        builder.setView(view)
                .setPositiveButton(R.string.prev_dialog_btn, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });


         return builder.create();

    }












}
