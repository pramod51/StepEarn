package step.earn.stepearn;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import step.earn.stepearn.Adopters.RewardsAdopter;
import step.earn.stepearn.Adopters.RewardsItems;

import com.firebase.ui.database.FirebaseRecyclerOptions;

import com.google.firebase.database.FirebaseDatabase;


public class RewardFragment extends Fragment {

    private RecyclerView recyclerView;
    private Context context;
    private RewardsAdopter rewardsAdopter;
    private RelativeLayout linearLayout;
    private ProgressBar progressbar;
    private final String KEY_RECYCLER_STATE = "recycler_state";
    private static final String BUNDLE_RECYCLER_LAYOUT = "classname.recycler.layout";


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view=inflater.inflate(R.layout.activity_rewards_fragment, container, false);
        context=getActivity();
        initView(view);



        FirebaseRecyclerOptions<RewardsItems> options =
                new FirebaseRecyclerOptions.Builder<RewardsItems>()
                        .setQuery(FirebaseDatabase.getInstance().getReference().child("RewardItems"), RewardsItems.class)
                        .build();
        rewardsAdopter=new RewardsAdopter(options);
        recyclerView.setAdapter(rewardsAdopter);

        linearLayout.setVisibility(View.VISIBLE);
        progressbar.setVisibility(View.GONE);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        rewardsAdopter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        rewardsAdopter.stopListening();
    }

    private void initView(View view) {

        recyclerView=view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        progressbar=view.findViewById(R.id.progressbar);
        progressbar.setVisibility(View.VISIBLE);
        linearLayout=view.findViewById(R.id.ll);
        linearLayout.setVisibility(View.INVISIBLE);

    }
    @Override
    public void onPause()
    {
        super.onPause();


    }

    @Override
    public void onResume()
    {
        super.onResume();

    }
    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);

        if(savedInstanceState != null)
        {
            Parcelable savedRecyclerLayoutState = savedInstanceState.getParcelable(BUNDLE_RECYCLER_LAYOUT);
            recyclerView.getLayoutManager().onRestoreInstanceState(savedRecyclerLayoutState);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(BUNDLE_RECYCLER_LAYOUT, recyclerView.getLayoutManager().onSaveInstanceState());
    }

}