package step.earn.stepearn;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;
import com.etebarian.meowbottomnavigation.MeowBottomNavigation;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.Date;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    MeowBottomNavigation bottomNavigation;
    private TextView s,f,r,c,p;
    private String uId= FirebaseAuth.getInstance().getCurrentUser().getUid();//"xyz";
    private DatabaseReference ref= FirebaseDatabase.getInstance().getReference();
    private AdView mAdView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
        mAdView = findViewById(R.id.adView);
        checkInternetConnection(getWindow().getDecorView().getRootView());
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                DataSnapshot dataSnapshot=snapshot.child("Users").child(uId);
                if (snapshot.child("BasicRecords").child("bannerAd").getValue(String.class).equals("Yes"))
                    bannerAd();
                int x=snapshot.child("BasicRecords").child("RewardPlane").getValue(Integer.class);
                if (!dataSnapshot.child("RewardHistory").exists()||dataSnapshot.child("RewardHistory").getChildrenCount()<x){
                    HashMap<String, HashMap<String, Integer>> map=new HashMap<>();
                    int start=0;
                    if (dataSnapshot.child("RewardHistory").exists())
                        start= (int) dataSnapshot.child("RewardHistory").getChildrenCount();
                    for (int i=start+1;i<=x;i++)
                    {
                       ref.child("Users").child(uId).child("RewardHistory").child(""+i).child("Day").setValue(0);
                    }



                }
                boolean taskCompleted=false;
                for (DataSnapshot ds:dataSnapshot.child("RewardHistory").getChildren()){
                    if (ds.child("Day").getValue(Integer.class)<7||(ds.child("Date").exists()&&ds.child("Date").getValue(String.class).equals(getCurrDate()))) {
                        ref.child("Users").child(uId).child("TrackHistory").setValue(ds.getKey());
                        taskCompleted=true;
                        break;
                    }
                }
                if (dataSnapshot.child("RewardHistory").child(""+x).child("Day").exists()
                        &&dataSnapshot.child("RewardHistory").child(""+x).child("Day").getValue(Integer.class)>=7){
                    Toast.makeText(MainActivity.this,"Congratulation!! You Completed The Challenge :)",Toast.LENGTH_LONG).show();




                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        bottomNavigation=findViewById(R.id.bottom_navigation);
        bottomNavigation.add(new MeowBottomNavigation.Model(1, R.drawable.step));
        bottomNavigation.add(new MeowBottomNavigation.Model(2, R.drawable.friends));
        bottomNavigation.add(new MeowBottomNavigation.Model(3, R.drawable.rewards));
        bottomNavigation.add(new MeowBottomNavigation.Model(4, R.drawable.contest));
        bottomNavigation.add(new MeowBottomNavigation.Model(5, R.drawable.profile));
        s=findViewById(R.id.steps);
        f=findViewById(R.id.friends);
        r=findViewById(R.id.rewards);
        c=findViewById(R.id.contest);
        p=findViewById(R.id.profile);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new StepFragment()).addToBackStack(null).commit();

        bottomNavigation.setOnClickMenuListener(new MeowBottomNavigation.ClickListener() {
            @Override
            public void onClickItem(MeowBottomNavigation.Model item) {
                //Toast.makeText(getApplicationContext(),"Clicked item"+item.getId(),Toast.LENGTH_SHORT).show();
                if(item.getId()==1)
                {
                    s.setVisibility(View.INVISIBLE);
                    f.setVisibility(View.VISIBLE);
                    r.setVisibility(View.VISIBLE);
                    c.setVisibility(View.VISIBLE);
                    p.setVisibility(View.VISIBLE);
                }else if(item.getId()==2)
                {
                    s.setVisibility(View.VISIBLE);
                    f.setVisibility(View.INVISIBLE);
                    r.setVisibility(View.VISIBLE);
                    c.setVisibility(View.VISIBLE);
                    p.setVisibility(View.VISIBLE);
                }else if(item.getId()==3)
                {
                    s.setVisibility(View.VISIBLE);
                    f.setVisibility(View.VISIBLE);
                    r.setVisibility(View.INVISIBLE);
                    c.setVisibility(View.VISIBLE);
                    p.setVisibility(View.VISIBLE);
                }else if(item.getId()==4)
                {
                    s.setVisibility(View.VISIBLE);
                    f.setVisibility(View.VISIBLE);
                    r.setVisibility(View.VISIBLE);
                    c.setVisibility(View.INVISIBLE);
                    p.setVisibility(View.VISIBLE);
                }else
                    {
                    s.setVisibility(View.VISIBLE);
                    f.setVisibility(View.VISIBLE);
                    r.setVisibility(View.VISIBLE);
                    c.setVisibility(View.VISIBLE);
                    p.setVisibility(View.INVISIBLE);
                }
            }
        });
        bottomNavigation.setOnReselectListener(new MeowBottomNavigation.ReselectListener() {
            @Override
            public void onReselectItem(MeowBottomNavigation.Model item) {

            }
        });
        final Fragment[] select_fragment = {null};
        bottomNavigation.setOnShowListener(new MeowBottomNavigation.ShowListener() {
            @Override
            public void onShowItem(MeowBottomNavigation.Model item) {

                switch (item.getId()){
                    case 1:
                        checkInternetConnection(getWindow().getDecorView().getRootView());
                        select_fragment[0]=new StepFragment();
                        break;
                    case 2:
                        checkInternetConnection(getWindow().getDecorView().getRootView());
                        select_fragment[0] =new FriendsFragment();
                        break;
                    case 3:
                        checkInternetConnection(getWindow().getDecorView().getRootView());
                        select_fragment[0] =new RewardFragment();
                        break;
                    case 4:
                        checkInternetConnection(getWindow().getDecorView().getRootView());
                        select_fragment[0] =new ContestFragment();
                        break;
                    case 5:
                        checkInternetConnection(getWindow().getDecorView().getRootView());
                        select_fragment[0] =new ProfileFragment();
                        break;

                }

                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, select_fragment[0]).commit();
            }
        });









    }


    public String getCurrDate()  {
        Date d = new Date();
        CharSequence s  = DateFormat.format("yyyy-MM-dd", d.getTime());
        return s.toString();
    }

    private void bannerAd() {
        MobileAds.initialize(MainActivity.this);

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
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        finishAffinity();
    }
    private void checkInternetConnection(View view) {
        ConnectivityManager connectivityManager = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo == null) {
            Snackbar snackbar = Snackbar.make(view, "No Internet Connection", Snackbar.LENGTH_LONG);
            snackbar.setBackgroundTint(ContextCompat.getColor(this, R.color.dark_orange));
            snackbar.setTextColor(ContextCompat.getColor(this, R.color.black));

            snackbar.show();

        }

    }
}