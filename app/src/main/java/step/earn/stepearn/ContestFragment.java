package step.earn.stepearn;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import step.earn.stepearn.Adopters.SliderAdopter;
import step.earn.stepearn.Adopters.SliderItem;
import step.earn.stepearn.Adopters.WatchMoreAdopter;
import step.earn.stepearn.Adopters.WatchMoreItems;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdCallback;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.kofigyan.stateprogressbar.StateProgressBar;
import com.smarteist.autoimageslider.IndicatorView.animation.type.IndicatorAnimationType;
import com.smarteist.autoimageslider.SliderAnimations;
import com.smarteist.autoimageslider.SliderView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class ContestFragment extends Fragment {

    private TextView loadingAd;
    private WatchMoreAdopter watchMoreAdopter;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView watchMoreRecyclerView;
    private RewardedAd rewardedAd;
    private Button watchAd;
    private Context context;
    private StateProgressBar stateProgressBar;
    private String uId= FirebaseAuth.getInstance().getCurrentUser().getUid();//"xyz";
    private DatabaseReference mRef= FirebaseDatabase.getInstance().getReference().child("Users").child(uId).child("Steps");
    private CountDownTimer cTimer = null;
    private SliderView sliderView;
    private SliderAdopter adapter;
    private AdView mAdView;
    private InterstitialAd mInterstitialAd;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view=inflater.inflate(R.layout.activity_contest_fragment, container, false);
        context=getActivity();
        initView(view);
        Log.v("Tag",getTime());
        String[] descriptionData ={"one","",};
        rewardedAdFun();
        bannerAd();
        SlidingImages(view);

        //stateProgressBar.setStateDescriptionData(descriptionData);
        Query query=FirebaseDatabase.getInstance().getReference().child("Users")
                                .child(uId).child("RewardHistory");
        FirebaseRecyclerOptions<WatchMoreItems> options =
                new FirebaseRecyclerOptions.Builder<WatchMoreItems>()
                        .setQuery(query, WatchMoreItems.class)
                        .build();
        watchMoreAdopter=new WatchMoreAdopter(options);
        watchMoreRecyclerView.setAdapter(watchMoreAdopter);



        final String start=getTime();
        mRef.child(getCurrDate()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull final DataSnapshot snapshot) {
                final long i=  snapshot.child("AdWatchCount").getChildrenCount();
                StateProgressBar.StateNumber[] progress={StateProgressBar.StateNumber.ONE,StateProgressBar.StateNumber.ONE,StateProgressBar.StateNumber.TWO,
                        StateProgressBar.StateNumber.THREE,StateProgressBar.StateNumber.FOUR,StateProgressBar.StateNumber.FIVE};
                if (i<=5&&i!=0)
                stateProgressBar.setCurrentStateNumber(progress[(int) i]);
                else if (i>5)
                    stateProgressBar.setCurrentStateNumber(progress[5]);

                if (i>=5&&i<=7){
                    FirebaseDatabase.getInstance().getReference().child("Users").child(uId)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.child("TrackHistory").exists()){
                                        Log.v("tag","t");
                                        HashMap<String, Object> map=new HashMap<>();
                                        map.put("Day",i);
                                        map.put("Date",getCurrDate());
                                        FirebaseDatabase.getInstance().getReference().child("Users").child(uId).
                                                child("RewardHistory").child(snapshot.child("TrackHistory").
                                                getValue(String.class)).setValue(map);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                }

                cTimer = new CountDownTimer(300000, 1000) {
                    public void onTick(long millisUntilFinished) {
                        if (snapshot.child("isTimerRunning").exists()&&snapshot.child("isTimerRunning").getValue(String.class).equals("Yes")){
                            try {
                                String timeDiff=getTimeDiffrence(snapshot.child("startTime").getValue(String.class));
                                String[] data=timeDiff.split(":");

                                if (Integer.parseInt(data[0])>0||(19-Integer.parseInt(data[1])<0)){
                                    mRef.child(getCurrDate()).child("isTimerRunning").setValue("No");
                                }
                                //Log.v("tag",data[0]+"--"+data[1]+"--"+data[2]);

                            } catch (ParseException e) {
                                e.printStackTrace();
                            }


                        }


                        if (!snapshot.child("isTimerRunning").exists()||(snapshot.child("isTimerRunning").getValue(String.class)).equals("No")){

                            loadingAd.setVisibility(View.VISIBLE);
                            if (!rewardedAd.isLoaded()){
                                loadingAd.setText("Loading Reward...");
                            }else{
                                loadingAd.setText("Reward loaded, Now you can Watch!");
                            }
                            watchAd.setText("Get Free Coins");
                            //cTimer.cancel();
                        }else {
                            loadingAd.setVisibility(View.INVISIBLE);
                            try {
                                String[] string=getTimeDiffrenceInMin(snapshot.child("startTime").getValue(String.class)).split(":");
                                if (19-Integer.parseInt(string[0])>0)
                                watchAd.setText((19 - Integer.parseInt(string[0])) +":"+ (59 - Integer.parseInt(string[1])));
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }



                    }
                    public void onFinish() {
                        cTimer.start();
                    }
                };
                cTimer.start();







            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });





        watchAd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                if (rewardedAd.isLoaded()&&watchAd.getText().toString().equals("Get Free Coins")) {
                    loadingAd.setVisibility(View.GONE);
                    RewardedAdCallback adCallback = new RewardedAdCallback() {
                        @Override
                        public void onRewardedAdOpened() {
                            AdmobIntertitial();
                            // Ad opened.
                            //Toast.makeText(context,"Ad Opened",Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onRewardedAdClosed() {
                            if (mInterstitialAd.isLoaded())
                                mInterstitialAd.show();
                            // Ad closed.
                            //Toast.makeText(context,"Ad Closed",Toast.LENGTH_SHORT).show();
                            rewardedAd = createAndLoadRewardedAd();
                        }

                        @Override
                        public void onUserEarnedReward(@NonNull RewardItem reward) {
                            if (mInterstitialAd.isLoaded())
                                mInterstitialAd.show();
                            // User earned reward.
                            Toast.makeText(getContext(),"1 Coin has been Added to your Account :)",Toast.LENGTH_LONG).show();
                            HashMap<String, Object> map=new HashMap<>();
                            map.put("startTime",getTime());
                            map.put("isTimerRunning","Yes");
                            mRef.child(getCurrDate()).updateChildren(map);
                            //mRef.child(getCurrDate()).child().setValue();
                            mRef.child(getCurrDate()).child("AdWatchCount").push().setValue(1);
                            //Toast.makeText(context,"Ad rewared="+reward.getAmount(),Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onRewardedAdFailedToShow(AdError adError) {
                            // Ad failed to display.
                            rewardedAd = createAndLoadRewardedAd();

                            Toast.makeText(context,"Ad failed to play",Toast.LENGTH_SHORT).show();
                        }
                    };
                    rewardedAd.show(getActivity(), adCallback);
                }else if (!watchAd.getText().toString().equals("Get Free Coins")){
                    Toast.makeText(context,"Please wait till timer ends",Toast.LENGTH_SHORT).show();
                }
                else {
                    //Log.d("TAG", "The rewarded ad wasn't loaded yet.");

                    Toast.makeText(context,"Reward Loaded yet",Toast.LENGTH_SHORT).show();
                }
            }
        });







        return view;
    }

    private void initView(View view) {
        watchMoreRecyclerView=view.findViewById(R.id.watch_more_ad_recycler_view);
        LinearLayoutManager layoutManager=new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        //layoutManager.setStackFromEnd(true);
        watchMoreRecyclerView.setLayoutManager(layoutManager);
        stateProgressBar = (StateProgressBar) view.findViewById(R.id.state_progress_bar);
        watchAd=view.findViewById(R.id.watch_ad);
        loadingAd=view.findViewById(R.id.loading_ad);
        mAdView = view.findViewById(R.id.adView);

    }


    private String getTime(){
        Calendar c = Calendar.getInstance();
        SimpleDateFormat dateformat = new SimpleDateFormat("hh:mm:ss aa");
        String datetime = dateformat.format(c.getTime());
        return datetime;
    }
    private String getTimeDiffrence(String time) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss aa");

        Date systemDate = Calendar.getInstance().getTime();
        String myDate = sdf.format(systemDate);
//                  txtCurrentTime.setText(myDate);

        Date Date1 = sdf.parse(myDate);
        Date Date2 = sdf.parse(time);

        long millse = Date1.getTime() - Date2.getTime();
        long mills = Math.abs(millse);

        int Hours = (int) (mills/(1000 * 60 * 60));
        int Mins = (int) (mills/(1000*60)) % 60;
        long Secs = (int) (mills / 1000) % 60;

        String diff = Hours+":"+Mins + ":" + Secs;
        return diff;

    }
    private String getTimeDiffrenceInMin(String time) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss aa");

        Date systemDate = Calendar.getInstance().getTime();
        String myDate = sdf.format(systemDate);
//                  txtCurrentTime.setText(myDate);

        Date Date1 = sdf.parse(myDate);
        Date Date2 = sdf.parse(time);

        long millse = Date1.getTime() - Date2.getTime();
        long mills = Math.abs(millse);

        int Hours = (int) (mills/(1000 * 60 * 60));
        int Mins = (int) (mills/(1000*60)) % 60;
        long Secs = (int) (mills / 1000) % 60;

        String diff = Mins + ":" + Secs;
        return diff;

    }
    private void rewardedAdFun() {
        rewardedAd = new RewardedAd(context,
                "ca-app-pub-4725607852978925/8387065792");
        RewardedAdLoadCallback adLoadCallback = new RewardedAdLoadCallback() {
            @Override
            public void onRewardedAdLoaded() {
                // Ad successfully loaded.
                //Toast.makeText(context,"Ad Loadedfn",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onRewardedAdFailedToLoad(LoadAdError adError) {
                // Ad failed to load.
                rewardedAd = createAndLoadRewardedAd();
                //Toast.makeText(context,"Ad Load failed",Toast.LENGTH_SHORT).show();
            }
        };
        rewardedAd.loadAd(new AdRequest.Builder().build(), adLoadCallback);
    }

    public RewardedAd createAndLoadRewardedAd() {
        RewardedAd rewardedAd1 = new RewardedAd(context,
                "ca-app-pub-4725607852978925/8387065792");
        RewardedAdLoadCallback adLoadCallback = new RewardedAdLoadCallback() {
            @Override
            public void onRewardedAdLoaded() {
                // Ad successfully loaded.
            }

            @Override
            public void onRewardedAdFailedToLoad(LoadAdError adError) {
                // Ad failed to load.
                rewardedAd = createAndLoadRewardedAd();
            }
        };
        rewardedAd1.loadAd(new AdRequest.Builder().build(), adLoadCallback);
        return rewardedAd1;
    }
    public String getCurrDate()  {
        Date d = new Date();
        CharSequence s  = DateFormat.format("yyyy-MM-dd", d.getTime());
        return s.toString();
    }

    @Override
    public void onStart() {
        super.onStart();
        watchMoreAdopter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        watchMoreAdopter.stopListening();
    }

    private void SlidingImages(View view){

        sliderView = view.findViewById(R.id.imageSlider);
        adapter = new SliderAdopter(context);
        FirebaseDatabase.getInstance().getReference().child("BasicRecords").child("SlidingImages")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            List<SliderItem> sliderItemList = new ArrayList<>();

                            for (DataSnapshot ds:snapshot.getChildren()){
                                if (sliderItemList.size()<snapshot.getChildrenCount()){
                                    sliderItemList.add(new SliderItem("",ds.getValue(String.class)));
                                }
                            }
                            sliderView.setSliderAdapter(adapter);
                            sliderView.setIndicatorAnimation(IndicatorAnimationType.DROP); //set indicator animation by using SliderLayout.IndicatorAnimations. :WORM or THIN_WORM or COLOR or DROP or FILL or NONE or SCALE or SCALE_DOWN or SLIDE and SWAP!!
                            sliderView.setSliderTransformAnimation(SliderAnimations.CUBEOUTDEPTHTRANSFORMATION);//SIMPLETRANSFORMATION , DEPTHTRANSFORMATION
                            sliderView.setAutoCycleDirection(SliderView.AUTO_CYCLE_DIRECTION_RIGHT);//AUTO_CYCLE_DIRECTION_BACK_AND_FORTH
                            sliderView.setIndicatorSelectedColor(Color.WHITE);
                            sliderView.setIndicatorUnselectedColor(Color.GRAY);
                            sliderView.setScrollTimeInSec(3);
                            sliderView.setAutoCycle(true);
                            sliderView.startAutoCycle();

                            adapter.renewItems(sliderItemList);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });









    }
    public void addNewItem(View view) {
        //SliderItem sliderItem = new SliderItem();
        //sliderItem.setDescription("Slider Item Added Manually");
        //sliderItem.setImageUrl("https://images.pexels.com/photos/929778/pexels-photo-929778.jpeg?auto=compress&cs=tinysrgb&dpr=2&h=750&w=1260");
        List<SliderItem> sliderItemList = new ArrayList<>();
        sliderItemList.add(new SliderItem("","https://dietcarry.com/wp-content/uploads/2020/11/IMG-20201106-WA0005-1.jpg"));

        sliderItemList.add(new SliderItem("","https://dietcarry.com/wp-content/uploads/2020/11/IMG-20201106-WA0024.jpg"));
        sliderItemList.add(new SliderItem("","https://dietcarry.com/wp-content/uploads/2020/11/Screenshot_2020-11-10-14-38-37-37_fcda2657b11af9cf0a49a9d2ac2c6d74-1.jpg"));
        //sliderItemList.add("https://dietcarry.com/wp-content/uploads/2020/11/IMG-20201106-WA0006-1.jpg");
        //adapter.addItem(sliderItem);
        adapter.renewItems(sliderItemList);
    }
    private void bannerAd() {
        MobileAds.initialize(getActivity());

        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        mAdView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                // Code to be executed when an ad finishes loading.
                //Toast.makeText(getActivity(),"BannerAd loaded",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAdFailedToLoad(LoadAdError adError) {
                // Code to be executed when an ad request fails.
                //Toast.makeText(getActivity(),"BannerAd error",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAdOpened() {
                // Code to be executed when an ad opens an overlay that
                // covers the screen.
                //Toast.makeText(getActivity(),"Ad opened",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAdClicked() {
                // Code to be executed when the user clicks on an ad.
                //Toast.makeText(getActivity(),"Ad clicked",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAdLeftApplication() {
                // Code to be executed when the user has left the app.
            }

            @Override
            public void onAdClosed() {
                // Code to be executed when the user is about to return
                // to the app after tapping on an ad.
                //Toast.makeText(getActivity(),"Ad closed",Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void AdmobIntertitial() {

        mInterstitialAd = new InterstitialAd(context);
        mInterstitialAd.setAdUnitId("ca-app-pub-4725607852978925/9321517717");
        mInterstitialAd.loadAd(new AdRequest.Builder().build());
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                // Code to be executed when an ad finishes loading.

            }

            @Override
            public void onAdFailedToLoad(LoadAdError adError) {
                // Code to be executed when an ad request fails.
            }

            @Override
            public void onAdOpened() {
                // Code to be executed when the ad is displayed.
            }

            @Override
            public void onAdClicked() {
                // Code to be executed when the user clicks on an ad.
            }

            @Override
            public void onAdLeftApplication() {
                // Code to be executed when the user has left the app.
            }

            @Override
            public void onAdClosed() {
                // Code to be executed when the interstitial ad is closed.
            }
        });
    }
}