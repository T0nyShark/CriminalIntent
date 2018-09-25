package com.industries.shark.criminalintent;


import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;


import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static android.view.View.*;
import static android.widget.CompoundButton.*;


public class CrimeFragment extends Fragment {
    private static final String ARG_CRIME_ID = "crime_id";
    private static final String DIALOG_DATE = "DialogDate";
    private static final String DIALOG_TIME = "DialogTime";
    private static final String DIALOG_IMAGE_PREVIEW = "ImagePreview";


   private static final int REQUEST_DATE = 0;
   private static final int REQUEST_TIME = 1;
   private static final int REQUEST_CONTACT = 2;
   private static final int REQUEST_PHOTO = 3;
   private static final int REQUEST_IMAGE_PREVIEW = 4;

    private Crime mCrime;
    private EditText mTitleField;
    private Button mDateButton;
    private CheckBox mSolvedCheckBox;
    private Button mSetTimeButton;
    private Button mReportButton;
    private Button mSuspectButton;
    private ImageButton mPhotoButton;
    private ImageView mPhotoView;

    private File mPhotoFile;

    private Point mPhotoSize;

    private Callbacks mCallbacks;


    //required interface for hosting activities

    public interface Callbacks
    {
        void onCrimeUpdated(Crime crime);
    }

    public static CrimeFragment newInstance(UUID crimeId){
        Bundle args = new Bundle();
        args.putSerializable(ARG_CRIME_ID, crimeId);

        CrimeFragment fragment = new CrimeFragment();
        fragment.setArguments(args);
        return fragment;

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mCallbacks = (Callbacks) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
          UUID crimeId = (UUID) getArguments().getSerializable(ARG_CRIME_ID);
        mCrime = CrimeLab.get(getActivity()).getCrime(crimeId);
        mPhotoFile = CrimeLab.get(getActivity()).getPhotoFile(mCrime);
        setHasOptionsMenu(true);
    }

    @Override
    public void onPause() {
        super.onPause();

        CrimeLab.get(getActivity()).updateCrime(mCrime);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_crime, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){

            case R.id.menu_delete_crime:

                createDeleteDialog();

                   return true;

                   default: return super.onOptionsItemSelected(item);






        }

    }

    public void createDeleteDialog() {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View mDeleteDialogView = inflater.inflate(R.layout.dialog_delete_crime, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(this.getContext());

        builder.setView(mDeleteDialogView);

        final CheckBox mDeleteCheckbox = (CheckBox) mDeleteDialogView.findViewById(R.id.full_delete_checkbox);


        builder.setCancelable(false)
                .setPositiveButton(R.string.dialog_delete_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                      if (mDeleteCheckbox.isChecked()){
                          CrimeLab.get(getActivity()).deleteCrime(mCrime);


                          if (getActivity() != null) {
                               dialog.cancel();
                               getActivity().finish();

                          }
                      }
                      else {
                          CrimeLab.get(getActivity()).clearCrime(mCrime);



                          if (getActivity() != null) {
                              dialog.cancel();
                              getActivity().finish();

                          }


                      }
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        AlertDialog dialog = builder.create();

        dialog.show();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_crime, container, false);

        mTitleField = (EditText) v.findViewById(R.id.crime_title);

        mTitleField.setText(mCrime.getTitle());
               mTitleField.addTextChangedListener(new TextWatcher() {


            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            mCrime.setTitle(s.toString());
            updateCrime();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mDateButton = (Button) v.findViewById(R.id.crime_date);
        updateDate();
        updateTime();

        mDateButton.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v) {
                FragmentManager manager = getFragmentManager();
                DatePickerFragment dialog = DatePickerFragment.newInstance(mCrime.getDate());
                dialog.setTargetFragment(CrimeFragment.this, REQUEST_DATE);
                dialog.show(manager, DIALOG_DATE);


            }
            });

        mSolvedCheckBox = (CheckBox) v.findViewById(R.id.crime_solved);
        mSolvedCheckBox.setChecked(mCrime.isSolved());
        mSolvedCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mCrime.setSolved(isChecked);
                updateCrime();
            }
        });
        mSetTimeButton = (Button) v.findViewById(R.id.crime_time);
        mSetTimeButton.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v) {
                FragmentManager manager = getFragmentManager();
                TimePickerFragment dialog = TimePickerFragment.newInstance(mCrime.getTime());
                dialog.setTargetFragment(CrimeFragment.this, REQUEST_TIME);
                dialog.show(manager, DIALOG_TIME);

            }
        });

        mReportButton = (Button) v.findViewById(R.id.crime_report);

        mReportButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = ShareCompat.IntentBuilder
                        .from(getActivity())
                        .setType("text/plain")
                        .setText(getCrimeReport())
                        .setSubject(getString(R.string.crime_report_subject))
                        .setChooserTitle(getString(R.string.send_report))
                        .createChooserIntent();

                startActivity(intent);
         /*
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("text/plain");
                i.putExtra(Intent.EXTRA_TEXT, getCrimeReport());
                i.putExtra(Intent.EXTRA_SUBJECT,
                        getString(R.string.crime_report_subject));
               i = Intent.createChooser(i, getString(R.string.send_report));
                startActivity(i); */
            }
        });

        final Intent pickContact = new Intent(Intent.ACTION_PICK,
                ContactsContract.Contacts.CONTENT_URI);

        mSuspectButton = (Button) v.findViewById(R.id.crime_suspect);

        mSuspectButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(pickContact, REQUEST_CONTACT);
            }
        });

        if (mCrime.getSuspect() != null){
            mSuspectButton.setText(mCrime.getSuspect());
        }

        PackageManager packageManager = getActivity().getPackageManager();

        if (packageManager.resolveActivity(pickContact, PackageManager.MATCH_DEFAULT_ONLY) == null){
            mSuspectButton.setEnabled(false);
        }

        mPhotoButton = (ImageButton) v.findViewById(R.id.crime_camera);

        final Intent captureImage = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        boolean canTakePhoto = (mPhotoFile != null &&
                captureImage.resolveActivity(packageManager) != null);

        mPhotoButton.setEnabled(canTakePhoto);

        mPhotoButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = FileProvider.getUriForFile(getActivity(),
                        "com.industries.shark.android.criminalintent.fileprovider",
                        mPhotoFile);

                captureImage.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                List<ResolveInfo> cameraActivities = getActivity()
                        .getPackageManager()
                        .queryIntentActivities(captureImage, PackageManager.MATCH_DEFAULT_ONLY);

                for (ResolveInfo activity : cameraActivities){
                    getActivity().grantUriPermission(activity.activityInfo.packageName,
                            uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                }

                startActivityForResult(captureImage, REQUEST_PHOTO);

            }
        });

        mPhotoView = (ImageView) v.findViewById(R.id.crime_photo);

        mPhotoView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager manager = getFragmentManager();
                ImagePreviewFragment fragment = ImagePreviewFragment.newInstance(mPhotoFile);
                fragment.setTargetFragment(CrimeFragment.this, REQUEST_IMAGE_PREVIEW);
                fragment.show(manager, DIALOG_IMAGE_PREVIEW);
            }
        });

        ViewTreeObserver observer = mPhotoButton.getViewTreeObserver();

        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {

                mPhotoView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                mPhotoSize = new Point(mPhotoView.getWidth(), mPhotoView.getHeight());

                updatePhotoView();
            }
        });



        return v;

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
           if (resultCode != Activity.RESULT_OK) {
               return;
           }

       if (requestCode == REQUEST_DATE) {
               Date date = (Date) data.getSerializableExtra(DatePickerFragment.EXTRA_DATE);
               mCrime.setDate(date);
               updateCrime();
               updateDate();
           }

          else if (requestCode == REQUEST_TIME){
               Date dateTime = (Date) data.getSerializableExtra(TimePickerFragment.EXTRA_TIME);
               mCrime.setTime(dateTime);
               updateCrime();
               updateTime();
           }
           else if (requestCode == REQUEST_CONTACT && data != null) {
                     Uri contactUri = data.getData();
                     String[] queryFields = new String[]{
                             ContactsContract.Contacts.DISPLAY_NAME
                     };

                     Cursor c = getActivity().getContentResolver()
                             .query(contactUri, queryFields, null, null, null);

                     try{
                         if (c.getCount() == 0){
                             return;
                         }
                         c.moveToFirst();
                         String suspect = c.getString(0);
                         mCrime.setSuspect(suspect);
                         updateCrime();
                         mSuspectButton.setText(suspect);
                     }
                     finally {
                         c.close();



                 }

           } else if (requestCode == REQUEST_PHOTO){
              Uri uri = FileProvider.getUriForFile(getActivity(),
                      "com.industries.shark.android.criminalintent.fileprovider"
              ,mPhotoFile);
              getActivity().revokeUriPermission(uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
              updateCrime();
              updatePhotoView();
       }

       }

       private void updateCrime(){
        CrimeLab.get(getActivity()).updateCrime(mCrime);
        mCallbacks.onCrimeUpdated(mCrime);
       }


    private void updateDate() {
        mDateButton.setText(mCrime.getDate().toString());
    }

    private void updateTime() {
        Date date = mCrime.getDate();
        Date time = mCrime.getTime();

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, time.getHours());
        calendar.set(Calendar.MINUTE, time.getMinutes());
        mDateButton.setText(calendar.getTime().toString());
    }

    private String getCrimeReport(){
        String solvedString = null;

        if (mCrime.isSolved()){
            solvedString = getString(R.string.crime_report_solved);
        }
        else {
            solvedString = getString(R.string.crime_report_unsolved);
        }

        String dateFormat = "EEE, MMM dd";
        String dateString = DateFormat.format(dateFormat, mCrime.getDate()).toString();

        String suspect = mCrime.getSuspect();

        if (suspect == null){
            suspect = getString(R.string.crime_report_no_suspect);
        }
        else {
            suspect = getString(R.string.crime_report_suspect, suspect);
        }

        String report = getString(R.string.crime_report, mCrime.getTitle(), dateString, solvedString, suspect);


        return report;
    }

    private void updatePhotoView(){
        if (mPhotoFile == null || !mPhotoFile.exists()){
            mPhotoView.setImageDrawable(null);
            mPhotoView.setClickable(false);
            mPhotoView.setContentDescription(getString(R.string.crime_photo_no_image_description));

        } else {
            mPhotoView.setClickable(true);
            Bitmap bitmap = PictureUtils.getScaledBitmap(mPhotoFile.getPath(), mPhotoSize.x, mPhotoSize.y);
            mPhotoView.setImageBitmap(bitmap);
            mPhotoView.setContentDescription(getString(R.string.crime_photo_image_description));
        }
    }
}

