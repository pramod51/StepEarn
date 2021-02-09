package step.earn.stepearn;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import step.earn.stepearn.Adopters.AdaptorClass;
import step.earn.stepearn.Adopters.AdopterItems;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.LoadAdError;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;


public class RewardDetails extends AppCompatActivity {
    private AdaptorClass adaptorClass;
    private RecyclerView recyclerView;
    private ImageView rewardImage;
    private TextView titleText;
    private Button claim;
    private String uId= FirebaseAuth.getInstance().getCurrentUser().getUid();
    private DatabaseReference userRef=FirebaseDatabase.getInstance().getReference().child("Users").child(uId);
    private DatabaseReference mRef=FirebaseDatabase.getInstance().getReference().child("RewardItems");
    private InterstitialAd mInterstitialAd;
    private boolean want=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reward_details);
        claim=findViewById(R.id.claim);
        recyclerView=findViewById(R.id.recycler_view_reward_details);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        rewardImage=findViewById(R.id.reward_image);
        titleText=findViewById(R.id.tittle);
        AdmobIntertitial();











        final String path=getIntent().getStringExtra("xyz");
        mRef.child(path).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child("imageUrl").exists())
                Picasso.get().load(snapshot.child("imageUrl").getValue(String.class)).into(rewardImage);
                if (snapshot.child("tittle").exists()){
                    titleText.setText(snapshot.child("tittle").getValue(String.class));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });



        FirebaseRecyclerOptions<AdopterItems> options =
                new FirebaseRecyclerOptions.Builder<AdopterItems>()
                        .setQuery(mRef.child(path).child("Details"), AdopterItems.class)
                        .build();
        adaptorClass=new AdaptorClass(options);
        recyclerView.setAdapter(adaptorClass);
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child("ClaimedRewards").exists()){
                    for (DataSnapshot ds:snapshot.child("ClaimedRewards").getChildren()){
                        if (ds.child("isJoinedGrp").getValue(String.class).equals("Yes")) {
                            claim.setText("Claim Reward");
                        }
                        else if (path.equals(ds.child("RewardPos").getValue(String.class))){
                            claim.setText("Join Whatsapp Group");
                            break;
                        }

                    }


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        FirebaseDatabase.getInstance().getReference().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull final DataSnapshot snapshot) {
                //if (claim.getText().equals("Claim Reward"))
                {
                    claim.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            DataSnapshot rewardSnapshot=snapshot.child("RewardItems").child(path);
                            final DataSnapshot userSnapshot=snapshot.child("Users").child(uId);
                            String type=rewardSnapshot.child("Type").getValue(String.class);
                            final String requiredCoin=rewardSnapshot.child("requiredCoin").getValue(String.class);
                            final String challengeCompleted=snapshot.child("Users").child(uId).child("Profile").child("challengeCompleted").getValue(String.class);

                            if (claim.getText().equals("Join Whatsapp Group")) {
                                //String whatsappGrp=snapshot.child("BasicRecords").child("WhatsappGrp").getValue(String.class);
                                String url = snapshot.child("BasicRecords").child("WhatsappGrp").getValue(String.class);

                                Intent intentWhatsapp = new Intent("android.intent.action.MAIN");
                                intentWhatsapp.setAction(Intent.ACTION_VIEW);
                                //String url = "https://api.whatsapp.com/send?phone=" + "90xxxxxxxx";
                                intentWhatsapp.setData(Uri.parse(url));
                                intentWhatsapp.setPackage("com.whatsapp");
                                userRef.child("ClaimedRewards").child(String.valueOf(userSnapshot.child("ClaimedRewards").getChildrenCount())).child("isJoinedGrp").setValue("Yes");
                                startActivity(intentWhatsapp);

                            }
                            else if (challengeCompleted.equals("Yes")||Integer.parseInt(requiredCoin)<=snapshot.child("Users").child(uId).child("TotalCoin").getValue(Integer.class)){

                                //Alert Dialog put here


                                AlertDialog alertDialog = new AlertDialog.Builder(RewardDetails.this)
//set icon
                                        //.setIcon(android.R.drawable.ic_dialog_alert)
//set title
                                        .setTitle("Are you sure to Claim")
//set message
                                        .setMessage("Your Reward cost will we deducted")
//set positive butto
                                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                //set what would happen when positive button is clicked



                                                int deduction=Integer.parseInt(requiredCoin);
                                                HashMap<String, Object> map=new HashMap<>();
                                                map.put("RewardPos",path);
                                                map.put("isJoinedGrp","No");
                                                userRef.child("ClaimedRewards").child(String.valueOf(userSnapshot.child("ClaimedRewards").getChildrenCount()+1)).setValue(map);

                                                if (challengeCompleted.equals("Yes")) {
                                                    userRef.child("RewardHistory").removeValue();
                                                    userRef.child("InvestedCoin").push().setValue(5*snapshot.child("Useres").child(uId).child("RewardHistory").getChildrenCount());
                                                } else {
                                                    userRef.child("InvestedCoin").push().setValue(requiredCoin);
                                                    int endPos=snapshot.child("BasicRecords").child("RewardPlane").getValue(Integer.class),
                                                            TotalCoin=snapshot.child("Users").child(uId).child("TotalCoin").getValue(Integer.class),count=TotalCoin-Integer.parseInt(requiredCoin)
                                                            ,required=TotalCoin-userSnapshot.child("RewardCoin").getValue(Integer.class),rc=Integer.parseInt(requiredCoin)-required;
                                                    if (Integer.parseInt(requiredCoin)>required){
                                                        for (int j=endPos;j>=1;j--){
                                                            if (userSnapshot.child("RewardHistory").child(""+j).child("Day").getValue(Integer.class)>=7){
                                                                if (rc>=5){
                                                                    rc-=5;
                                                                    userRef.child("RewardHistory").child(""+j).child("Day").setValue(0);
                                                                }
                                                                else
                                                                    break;
                                                            }
                                                        }
                                                    }

                                                }
                                                claim.setText("Reward Claimed");
                                                userRef.child("Profile").child("challengeCompleted").setValue("No");


                                                userRef.child("TotalCoin").setValue(snapshot.child("Users").child(uId).child("TotalCoin").getValue(Integer.class)-deduction);




                                            }
                                        })
//set negative button
                                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                want=false;
                                                //set what should happen when negative button is clicked
                                                //Toast.makeText(getApplicationContext(),"Nothing Happened",Toast.LENGTH_LONG).show();
                                            }
                                        })
                                        .show();







                            }
                            else {
                                claim.setText("Claim Reward");
                                Toast.makeText(RewardDetails.this,"Please Read the Details Above :)",Toast.LENGTH_LONG).show();
                            }





                        }
                    });
                }


            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });




    }

    private void AdmobIntertitial() {

        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-4725607852978925/9321517717");
        mInterstitialAd.loadAd(new AdRequest.Builder().build());
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                // Code to be executed when an ad finishes loading.
                mInterstitialAd.show();
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



    @Override
    protected void onStop() {
        super.onStop();
        adaptorClass.startListening();
    }

    @Override
    protected void onStart() {
        super.onStart();
        adaptorClass.startListening();
    }



}

