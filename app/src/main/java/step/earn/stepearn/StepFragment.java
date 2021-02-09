package step.earn.stepearn;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import step.earn.stepearn.Adopters.HistoryAdopter;
import step.earn.stepearn.Adopters.HistoryItems;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.shinelw.library.ColorArcProgressBar;

import java.util.Calendar;
import java.util.Date;

public class StepFragment extends Fragment implements SensorEventListener {
    private ColorArcProgressBar progressBar;
    private TextView stepPerDay,km,calories,toolbarText,inviteAFriend,totalCoin,dailyRewards,Level,levelRemark,stepText,goal;
    private static final String TAG = "MainActivity";
    private String uId= FirebaseAuth.getInstance().getCurrentUser().getUid();//"xyz";
    private AdView mAdView;
    boolean running=false;
    private DatabaseReference ref=FirebaseDatabase.getInstance().getReference().child("Users").child(uId).child("Steps");
    private DatabaseReference headRef=FirebaseDatabase.getInstance().getReference();
    private DatabaseReference userRef=FirebaseDatabase.getInstance().getReference().child("Users").child(uId);
    private SensorManager sensorManager;
    private RecyclerView recyclerView;
    private HistoryAdopter historyAdopter;
    private String[] levels ={"Level 0","Level 1","Level 2","Level 3","Level 4"};
    private ProgressBar progressbar;
    private LinearLayout linearLayout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view=inflater.inflate(R.layout.activity_step_fragment, container, false);
        init(view);
        bannerAd();

        headRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child("BasicRecords").exists()){
                    inviteAFriend.setText(snapshot.child("BasicRecords").child("InviteCoin").getValue(String.class));

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        sensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        stepCounter();


        FirebaseRecyclerOptions<HistoryItems> options =
                new FirebaseRecyclerOptions.Builder<HistoryItems>()
                        .setQuery(ref.orderByKey().limitToLast(10), HistoryItems.class)
                        .build();
        historyAdopter=new HistoryAdopter(options);
        recyclerView.setAdapter(historyAdopter);



        return view;
    }


    private String yesterdayDate(){

        final Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        CharSequence s  = DateFormat.format("yyyy-MM-dd", cal.getTime());
        return s.toString();
    }
    private String getYesterdayDates(int i){

        final Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -i);
        CharSequence s  = DateFormat.format("yyyy-MM-dd", cal.getTime());
        return s.toString();
    }

    private void init(View view) {
        Level=view.findViewById(R.id.level);
        levelRemark=view.findViewById(R.id.level_remark);
        mAdView = view.findViewById(R.id.adView);
        recyclerView=view.findViewById(R.id.recycler_view);
        LinearLayoutManager layoutManager=new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, true);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);
        progressBar=view.findViewById(R.id.progress_bar);
        stepPerDay=view.findViewById(R.id.coin_per_day);
        km=view.findViewById(R.id.km);
        calories=view.findViewById(R.id.calorie);
        toolbarText=view.findViewById(R.id.toolbar);
        totalCoin=view.findViewById(R.id.total_coins);
        inviteAFriend=view.findViewById(R.id.invite_a_friend);
        dailyRewards=view.findViewById(R.id.daily_reward);
        toolbarText.setText(getCurrDate1());
        stepText=view.findViewById(R.id.step);
        goal=view.findViewById(R.id.goal);
        progressbar=view.findViewById(R.id.progressbar);
        progressbar.setVisibility(View.VISIBLE);
        linearLayout=view.findViewById(R.id.ll);
        linearLayout.setVisibility(View.INVISIBLE);
    }

    private String getCurrDate1() {
        Date d = new Date();
        CharSequence s  = DateFormat.format("yyyy-MM-dd", d.getTime());
        return s.toString();
    }

    @Override
    public void onResume() {
        super.onResume();
        running=true;
        Sensor sensor=sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        if (sensor!=null){
            sensorManager.registerListener(this,sensor,SensorManager.SENSOR_DELAY_FASTEST);
        }
        else{
            Toast.makeText(getActivity(),"Sensor Not Found",Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        running=false;
    }

    private void stepCounter() {
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshots) {
                DataSnapshot snapshot=dataSnapshots.child("Steps");
                if (!snapshot.child(getCurrDate()).exists())
                    return;

                String s=getCurrDate();
                if (snapshot.child(s).child("AdWatchCount").exists()&&snapshot.child(s).child("AdWatchCount").getChildrenCount()<=7)
                    dailyRewards.setText(String.valueOf(snapshot.child(s).child("AdWatchCount").getChildrenCount()));
                else if (snapshot.child(s).child("AdWatchCount").exists())
                    dailyRewards.setText("7");
                if (snapshot.child(s).exists()&&snapshot.child(s).child("Today").exists()) {
                    final int step=snapshot.child(s).child("Today").getValue(Integer.class);


                    new CountDownTimer(step,2){

                        @Override
                        public void onTick(long l) {

                            stepText.setText(""+(step-l));
                            Log.v("tag",""+l);
                            //resend.setEnabled(false);
                        }

                        @Override
                        public void onFinish() {
                            //resend.setText("Resend");
                            //resend.setEnabled(true);
                            stepText.setText(""+step);

                        }
                    }.start();








                    //Log.v("tag",String.valueOf(step));
                    km.setText(getUpToTwoDigits(String.valueOf(getDistanceRun(step))));

                    calories.setText(getUpToTwoDigits(String.valueOf(getCalories(step))));
                    if (snapshot.child(getCurrDate()).child("Level").exists()){
                        String level=snapshot.child(getCurrDate()).child("Level").getValue(String.class);
                        if (level.equals(levels[1])&&step<=3000)
                        ref.child(getCurrDate()).child("Coins").setValue(step/1000);
                        else if (level.equals(levels[2])&&step<=5000)
                            ref.child(getCurrDate()).child("Coins").setValue(step/1000);
                        else if (level.equals(levels[3])&&step<=10000)
                            ref.child(getCurrDate()).child("Coins").setValue(step/1000);
                        stepPerDay.setText("0"+String.valueOf(snapshot.child(s).child("Coins").getValue(Integer.class)));
                    }

                }
                if (snapshot.child(s).child("DailyRewards").exists())
                    dailyRewards.setText(snapshot.child(s).child("DailyRewards").getValue(String.class));
                int sum=0;
                if (snapshot.child(s).child("Today").exists()){

                    for (DataSnapshot ds:snapshot.getChildren()){
                        if (ds.child("Coins").exists()){
                            sum+=ds.child("Coins").getValue(Integer.class);
                        }
                    }
                }
                int RCoins=0;
                if (snapshot.exists()){
                    for (DataSnapshot dataSnapshot:snapshot.getChildren()){
                        if (dataSnapshot.child("AdWatchCount").exists()&&dataSnapshot.child("AdWatchCount").getChildrenCount()<=5) {
                            sum+=dataSnapshot.child("AdWatchCount").getChildrenCount();
                            RCoins+=dataSnapshot.child("AdWatchCount").getChildrenCount();
                        } else if (dataSnapshot.child("AdWatchCount").exists()){
                            sum+=5;
                            RCoins+=5;
                        }
                    }
                    userRef.child("RewardCoin").setValue(RCoins);
                }
                int investedCoins=0;
                for (DataSnapshot ds:dataSnapshots.child("InvestedCoin").getChildren()){
                    if (ds.exists()){
                        investedCoins+=Integer.parseInt(ds.getValue(String.class));
                    }
                }
                if (dataSnapshots.child("YourReferral").exists())
                    sum+=dataSnapshots.child("YourReferral").getChildrenCount()*5;
                userRef.child("TotalCoin").setValue(sum-investedCoins);
                totalCoin.setText(String.valueOf(sum-investedCoins));
                String level=levels[1];
                if (snapshot.child(getCurrDate()).child("Level").exists())
                level=snapshot.child(getCurrDate()).child("Level").getValue(String.class);
                if (level.equals(levels[1])) {
                    progressBar.setMaxValues(3000);
                    goal.setText("Goal 3000");
                } else if (level.equals(levels[2])) {
                    goal.setText("Goal 5000");
                    progressBar.setMaxValues(5000);
                } else {
                    goal.setText("Goal 10000");
                    progressBar.setMaxValues(10000);

                }
                //progressBar.setBackgroundColor(getResources().getColor(R.color.light_blue));
                if (snapshot.child(s).child("Today").exists())
                progressBar.setCurrentValues(snapshot.child(s).child("Today").getValue(Integer.class));

                progressbar.setVisibility(View.GONE);
                linearLayout.setVisibility(View.VISIBLE);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        Query lastQuery = ref.orderByKey().limitToLast(10);
        lastQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int i=3,j=10;
                for (DataSnapshot ds:snapshot.getChildren()){
                    if (ds.child("Today").exists()){
                        if (ds.getKey().equals(getYesterdayDates(i))&&ds.child("Today").getValue(Integer.class)>=3000){
                            i--;
                        }
                        if (ds.getKey().equals(getYesterdayDates(j))&&ds.child("Today").getValue(Integer.class)>=10000){
                            j--;
                        }
                    }
                }
                if (i==0) {
                        userRef.child("CurrentLevel").setValue(levels[2]);
                        ref.child(getCurrDate()).child("Level").setValue(levels[2]);
                        Level.setText(levels[2]);
                        levelRemark.setText("Reach 5 coin 2 days in a Row");
                    }
                else if (i<=3){
                    userRef.child("CurrentLevel").setValue(levels[1]);
                    ref.child(getCurrDate()).child("Level").setValue(levels[1]);
                    Level.setText(levels[1]);
                }

                if (j==0){
                    userRef.child("CurrentLevel").setValue(levels[3]);
                    ref.child(getCurrDate()).child("Level").setValue(levels[3]);
                    Level.setText(levels[3]);
                    levelRemark.setText("You reached highest level, keep it up");
                    levelRemark.setTextColor(getResources().getColor(R.color.red) );
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    @Override
    public void onStart() {
        super.onStart();
        historyAdopter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        historyAdopter.startListening();
    }

    @Override
    public void onSensorChanged(final SensorEvent sensorEvent) {
        if (running){
            ref.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    String s=getCurrDate();
                    int i=1;
                     if (!snapshot.exists()||!snapshot.child(yesterdayDate()).exists())
                     {
                         //ref.child(yesterdayDate()).child("Today").setValue(sensorEvent.values[0]);
                         ref.child(yesterdayDate()).child("Total").setValue(sensorEvent.values[0]);
                         //SystemClock.sleep(1000);
                     }
                    if (!snapshot.child(s).child("Today").exists())
                    {
                        i=1;
                        int ds1=0;
                        for (DataSnapshot ds: snapshot.getChildren()){
                            if (i==snapshot.getChildrenCount())
                            {
                                if (ds.child("Total").exists())
                                ds1= ds.child("Total").getValue(Integer.class);
                                ref.child(yesterdayDate()).child("Total").setValue(sensorEvent.values[0]);

                                ds1=(int) sensorEvent.values[0];
                                break;
                            }
                            i++;
                        }
                        if (sensorEvent.values[0]<ds1){
                            ref.child(yesterdayDate()).child("Total").setValue(sensorEvent.values[0]);
                            ds1= (int) sensorEvent.values[0];
                        }
                        ref.child(s).child("Today").setValue(sensorEvent.values[0]-ds1);
                        //ref.child(s).child("Coins").setValue(((sensorEvent.values[0]-ds1)/1000));
                        ref.child(s).child("Total").setValue(sensorEvent.values[0]);

                    }
                    else
                     {
                         int x=1;
                         for (DataSnapshot ds: snapshot.getChildren()){
                             if (x==snapshot.getChildrenCount()-1){
                                 if (ds.child("Total").exists())
                                     if (sensorEvent.values[0]>=ds.child("Total").getValue(Integer.class))
                                        ref.child(s).child("Today").setValue(sensorEvent.values[0]-ds.child("Total").getValue(Integer.class));
                                     else
                                         ref.child(yesterdayDate()).child("Total").setValue(sensorEvent.values[0]);
                                 break;
                             }
                             x++;
                         }
                         ref.child(s).child("Total").setValue(sensorEvent.values[0]);
                     }
                    if (snapshot.child(s).exists()&&snapshot.child(s).child("Today").exists()){
                        int step=snapshot.child(s).child("Today").getValue(Integer.class);
                        //j;
                        //stepPerDay.setText(String.valueOf(snapshot.child(s).getValue(Integer.class)));
                        km.setText(getUpToTwoDigits(String.valueOf(getDistanceRun(step))));
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        }
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
    public String getCurrDate()  {
        Date d = new Date();
        CharSequence s  = DateFormat.format("yyyy-MM-dd", d.getTime());
        return s.toString();
    }
    public float getDistanceRun(long steps){
        float distance = (float)(steps*78)/(float)100000;
        return distance;
    }
    private float getCalories(int steps){
        return (float) (0.04*(float)steps);
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

    private String getUpToTwoDigits(String number){
        String ans="";
        int i=0;
        while(i<number.length()&&number.charAt(i)!='.'){
            ans+=number.charAt(i);
            i++;
        }
        if (i<number.length()&&number.charAt(i+1)!='0')
        {
            ans+='.';
            ans+=number.charAt(i+1);
            return ans;
        }
        return ans;



    }
}