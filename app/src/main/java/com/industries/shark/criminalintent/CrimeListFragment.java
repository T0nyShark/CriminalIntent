package com.industries.shark.criminalintent;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;


import java.util.Date;
import java.util.List;
import java.util.UUID;

public class CrimeListFragment extends Fragment{

    private static final String SAVED_SUBTITLE_VISIBLE = "subtitle";

    private static final String DELETE_DIALOG_TAG = "subtitle";
    private static final int REQUEST_DELETE_DIALOG = 7;




    private int mLastAdapterClickPosition = -1;

    private RecyclerView mCrimeRecyclerView;

    private CrimeAdapter mAdapter;

    private ImageView mSolvedImageView;

    private boolean mSubtitleVisible;

    private View mEmptyLayoutView;

    private TextView mEmptyListText;

    private Button mEmptyListButton;

    private Callbacks mCallbacks;

    private ItemTouchHelper.Callback mRecyclerTouchHelper;

    private int mItemPositionToDelete;

    

    //Required interface for hosting activities
    public interface Callbacks {
        void onCrimeSelected(Crime crime);
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
        setHasOptionsMenu(true);

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_crime_list, container, false);

        mCrimeRecyclerView = (RecyclerView)
                view.findViewById(R.id.crime_recycler_view);

        mEmptyLayoutView = (View) view.findViewById(R.id.empty_layout_view);

        mEmptyListText = (TextView) view.findViewById(R.id.empty_layout_text);

        mEmptyListButton = (Button) view.findViewById(R.id.empty_layout_add_button);

        mEmptyListButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                createNewCrime();
            }
        });

        mCrimeRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        mRecyclerTouchHelper = new RecyclerTouchHelper(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
        ItemTouchHelper helper = new ItemTouchHelper(mRecyclerTouchHelper);
        helper.attachToRecyclerView(mCrimeRecyclerView);

        if (savedInstanceState != null){
            mSubtitleVisible = savedInstanceState.getBoolean(SAVED_SUBTITLE_VISIBLE);
        }

        updateUI();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        updateUI();
        
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(SAVED_SUBTITLE_VISIBLE, mSubtitleVisible);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.fragment_crime_list, menu);

        MenuItem subtitleItem = menu.findItem(R.id.show_subtitle);
           if (mSubtitleVisible){
               subtitleItem.setTitle(R.string.hide_subtitle);
           }
           else {
               subtitleItem.setTitle(R.string.show_subtitle);
           }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.new_crime:
                Crime crime = new Crime();
                CrimeLab.get(getActivity()).addCrime(crime);
                 updateUI();
                 mCallbacks.onCrimeSelected(crime);
                return true;

            case R.id.show_subtitle:
                mSubtitleVisible = !mSubtitleVisible;
                getActivity().invalidateOptionsMenu();
                updateSubtitle();

                return true;

            default:

                return super.onOptionsItemSelected(item);



        }

    }

    private void createNewCrime() {
        Crime crime = new Crime();

        CrimeLab.get(getActivity()).addCrime(crime);

        Intent intent = CrimePagerActivity.newIntent(getActivity(), crime.getId());

        startActivity(intent);
    }

    private void updateSubtitle(){
        CrimeLab crimeLab = CrimeLab.get(getActivity());

        int crimeCount = crimeLab.getCrimes().size();

        String subtitle = getResources().getQuantityString(R.plurals.subtitle_plural, crimeCount, crimeCount);

        if (!mSubtitleVisible){
            subtitle = null;
        }

        AppCompatActivity activity = (AppCompatActivity) getActivity();

        activity.getSupportActionBar().setSubtitle(subtitle);
    }

        public void updateUI(){
        CrimeLab crimeLab = CrimeLab.get(getActivity());

        List<Crime> crimes = crimeLab.getCrimes();

       if (mAdapter == null){

        mAdapter = new CrimeAdapter(crimes);

        mCrimeRecyclerView.setAdapter(mAdapter);
       }

     else {
        mAdapter.setCrimes(crimes);
        if (crimes.size() < mAdapter.getItemCount()) {
            mAdapter.notifyItemRemoved(mLastAdapterClickPosition);
        } else {
            mAdapter.notifyItemChanged(mLastAdapterClickPosition);
        }

       mAdapter.notifyDataSetChanged();
    }



       mEmptyLayoutView.setVisibility(crimes.isEmpty() ? View.VISIBLE : View.GONE);


         updateSubtitle();
    }

    private class CrimeHolder extends ViewHolder implements OnClickListener {

      private TextView mTitleTextView;

      private TextView mDateTextView;

      private Crime mCrime;


        public void bind(Crime crime){
           mCrime = crime;

           mTitleTextView.setText(mCrime.getTitle());

           mDateTextView.setText(formatDate(mCrime.getDate()));

       mSolvedImageView.setVisibility(
               crime.isSolved() ? View.VISIBLE : View.GONE);

      }
        public String formatDate(Date date){
            String s = DateFormat.format("EEEE, MMM dd, yyyy", date).toString();


            return s;
        }

        public CrimeHolder(LayoutInflater inflater, ViewGroup parent, int viewType){
          super(inflater.inflate(viewType, parent, false));


             mTitleTextView = (TextView) itemView.findViewById(R.id.crime_title);

             mDateTextView = (TextView) itemView.findViewById(R.id.crime_date);

             mSolvedImageView = (ImageView) itemView.findViewById(R.id.crime_solved);



             itemView.setOnClickListener(this);


        }



        @Override
        public void onClick(View v) {

            mLastAdapterClickPosition = getAdapterPosition();

           mCallbacks.onCrimeSelected(mCrime);

        }

    }


    private class CrimeAdapter extends RecyclerView.Adapter<CrimeHolder>{

        private List<Crime> mCrimes;

        public CrimeAdapter(List<Crime> crimes){
            mCrimes = crimes;
        }

        @NonNull
        @Override
        public CrimeHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());

           return new CrimeHolder(layoutInflater, parent, viewType);

        }

        @Override
        public void onBindViewHolder(@NonNull CrimeHolder holder, int position) {

             Crime crime = mCrimes.get(position);

             holder.bind(crime);
        }

        @Override
        public int getItemCount() {
            return mCrimes.size();
        }

        @Override
        public int getItemViewType(int position) {
           return R.layout.list_item_crime;
        }
        public void setCrimes(List<Crime> crimes) {
            mCrimes = crimes;
        }

        public List<Crime> getCrimes() {
            return mCrimes;
        }
    }

    public class RecyclerTouchHelper extends ItemTouchHelper.SimpleCallback {

        public RecyclerTouchHelper(int dragDirs, int swipeDirs) {
            super(dragDirs, swipeDirs);

        }


        @Override
        public int getMovementFlags(RecyclerView recyclerView, ViewHolder viewHolder) {
            int swipeFlags = 0;
            int dragflags = 0;
            if (viewHolder instanceof CrimeListFragment.CrimeHolder){
                swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END;
            }
           return makeMovementFlags(dragflags, swipeFlags);
        }

        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
           if (viewHolder instanceof CrimeListFragment.CrimeHolder){
               FragmentManager manager = getFragmentManager();
               mItemPositionToDelete = viewHolder.getAdapterPosition();
               UUID uuid = mAdapter.getCrimes().get(mItemPositionToDelete).getId();
               DeleteDialogFragment fragment = DeleteDialogFragment.newInstance(uuid);
               fragment.setTargetFragment(CrimeListFragment.this, REQUEST_DELETE_DIALOG);
               fragment.show(manager, DELETE_DIALOG_TAG);

           }

        }


        @Override
        public boolean isLongPressDragEnabled() {
            return false;
        }

        @Override
        public boolean isItemViewSwipeEnabled() {
            return true;
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            updateUI();
            return;
        }

    }

}
