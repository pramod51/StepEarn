package step.earn.stepearn;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListPopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static android.app.Activity.RESULT_OK;
public class ProfileFragment extends Fragment {

    ImageView backgroundImage,editProfileImage,profileImageView,backgroundImageView;
    private Button logOut;

    String uId=FirebaseAuth.getInstance().getCurrentUser().getUid();
    final StorageReference backgroundReference= FirebaseStorage.getInstance().getReference()
            .child("Images").child(uId).child("background.jpeg");
    final StorageReference profileReference= FirebaseStorage.getInstance().getReference()
            .child("Images").child(uId).child("profile.jpeg");
    private DatabaseReference mRef= FirebaseDatabase.getInstance().getReference().child("Users");
    private boolean backgroundImageLoaded=false;
    private ProgressBar progressBar;
    private List<String> list;
    ListPopupWindow popupWindow;
    private TextView totalSteps,totalCoins;
    private EditText name,bio;
    private ProgressBar progressbar;
    private LinearLayout linearLayout;
    private AdView mAdView;
    private TextView mailUs,connectInstagram,connectTeligram,privacyPolicy;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view=inflater.inflate(R.layout.activity_profile_fragment, container, false);

        initView(view);
        bannerAd();
        popupWindow=new ListPopupWindow(getActivity());
        list=new ArrayList<>();
        list.add("Edit");
        list.add("Remove");
        final PackageManager pm=getActivity().getPackageManager();
        backgroundImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                backgroundImageLoaded=true;
                listPopUp(view,true);

            }
        });
        editProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listPopUp(view,false);
                backgroundImageLoaded=false;

            }
        });
        logOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseUser user= FirebaseAuth.getInstance().getCurrentUser();
                if (user!=null){
                    FirebaseAuth.getInstance().signOut();
                    startActivity(new Intent(getActivity(),MobileAthuntication.class));
                }
            }
        });
        mRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child(uId).child("Profile").child("backgroundUrl").exists()){
                    Picasso.get().load(snapshot.child(uId).child("Profile").child("backgroundUrl").getValue(String.class)).into(backgroundImageView);
                }
                if (snapshot.child(uId).child("Profile").child("profileUrl").exists()){
                    progressBar.setVisibility(View.VISIBLE);
                    Picasso.get().load(snapshot.child(uId).child("Profile").child("profileUrl").getValue(String.class)).into(profileImageView);
                    progressBar.setVisibility(View.GONE);
                }
                if (snapshot.child(uId).child("Profile").exists()&&snapshot.child(uId).child("Profile").child("Name").exists()){
                    name.setText(snapshot.child(uId).child("Profile").child("Name").getValue(String.class));
                }
                if (snapshot.child(uId).child("Profile").exists()&&snapshot.child(uId).child("Profile").child("Bio").exists()){
                    bio.setText(snapshot.child(uId).child("Profile").child("Bio").getValue(String.class));
                }
                DataSnapshot dataSnapshot1=snapshot.child(uId).child("Steps");
                long sum=0,totalStp=0;
                if (dataSnapshot1.child(getCurrDate()).child("Today").exists()){

                    for (DataSnapshot ds:dataSnapshot1.getChildren()){
                        if (ds.child("Coins").exists()){
                            sum+=ds.child("Coins").getValue(Integer.class);
                        }
                        if (ds.child("Today").exists()){
                            totalStp+=ds.child("Today").getValue(Integer.class);
                        }
                    }
                }
                if (dataSnapshot1.exists()){
                    for (DataSnapshot dataSnapshot:dataSnapshot1.getChildren()){
                        if (dataSnapshot.child("AdWatchCount").exists()&&dataSnapshot.child("AdWatchCount").getChildrenCount()<=5)
                            sum+=dataSnapshot.child("AdWatchCount").getChildrenCount();
                        else if (dataSnapshot.child("AdWatchCount").exists()){
                            sum+=5;
                        }
                    }
                }
                int investedCoins=0;
                for (DataSnapshot ds:snapshot.child(uId).child("InvestedCoin").getChildren()){
                    if (ds.exists()){
                        investedCoins+=Integer.parseInt(ds.getValue(String.class));
                    }
                }
                if (snapshot.child(uId).child("YourReferral").exists())
                    sum+=snapshot.child(uId).child("YourReferral").getChildrenCount()*5;
                totalCoins.setText(String.valueOf(sum-investedCoins));
                totalSteps.setText(String.valueOf(totalStp));
                progressbar.setVisibility(View.GONE);
                linearLayout.setVisibility(View.VISIBLE);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        mailUs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/html");
                intent.putExtra(Intent.EXTRA_EMAIL, "pramodchaurasia990@gmail.com.com");
                intent.putExtra(Intent.EXTRA_SUBJECT, "Hello Step Earn");
                intent.putExtra(Intent.EXTRA_TEXT, "How can i help you");

                startActivity(Intent.createChooser(intent, "Send Email"));
            }
        });
        connectTeligram.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = "https://t.me/StepEarn";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            }
        });
        connectInstagram.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = "https://www.instagram.com/chaurasiya__pramod/";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            }
        });
        privacyPolicy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = "https://docs.google.com/document/d/1UJ6KPUzECGaVX_1nE3tX8wvA20z7fi_9NGXDtVRWLJc/edit?usp=sharing";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            }
        });









        return view;
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri imageUri = result.getUri();
                Log.v("Tag","uploaded tas1");
                InputStream inputStream;
                try {
                    inputStream = getActivity().getContentResolver().openInputStream(imageUri);
                    Bitmap photo = BitmapFactory.decodeStream(inputStream);
                    int nh = (int) ( photo.getHeight() * (512.0 / photo.getWidth()) );
                    Bitmap scaled = Bitmap.createScaledBitmap(photo, 512, nh, true);


                    if (backgroundImageLoaded)
                    uploadBackground(scaled);
                    else {
                        uploadProfile(scaled);
                    }

                    //backgroundImage.setImageBitmap(photo);

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Toast.makeText(getActivity(), "Cropping failed: " + result.getError(), Toast.LENGTH_LONG).show();
            }
        }else {
            Toast.makeText(getActivity(), ""+requestCode, Toast.LENGTH_SHORT).show();
        }

    }



    private void uploadBackground(Bitmap bitmap) {


        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        UploadTask uploadTask = backgroundReference.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
                Log.v("Tag",exception.toString());
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                // ...

                taskSnapshot.getMetadata().getReference().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Picasso.get().load(uri.toString()).into(backgroundImageView);
                        mRef.child(uId).child("Profile").child("backgroundUrl").setValue(uri.toString());
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getContext(),"Failed to upload! Try Again :)",Toast.LENGTH_LONG).show();
                    }
                });
                Log.v("Tag","uploaded task");
            }
        });
    }
    private void uploadProfile(Bitmap bitmap) {

        progressBar.setVisibility(View.VISIBLE);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        UploadTask uploadTask = profileReference.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
                Log.v("Tag",exception.toString());
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                // ...

                taskSnapshot.getMetadata().getReference().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Picasso.get().load(uri.toString()).into(profileImageView);
                        mRef.child(uId).child("Profile").child("profileUrl").setValue(uri.toString());
                        progressBar.setVisibility(View.GONE);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getContext(),"Failed to upload! Try Again :)",Toast.LENGTH_LONG).show();
                    }
                });


            }
        });

    }

    private void initView(View view) {
        editProfileImage=view.findViewById(R.id.edit_profile);
        backgroundImage=view.findViewById(R.id.background_image_edit);
        logOut=view.findViewById(R.id.log_out);
        profileImageView=view.findViewById(R.id.profile);
        backgroundImageView=view.findViewById(R.id.background_image);
        progressBar=view.findViewById(R.id.circular_progress_bar);
        progressBar.setVisibility(View.GONE);
        name=view.findViewById(R.id.name);
        bio=view.findViewById(R.id.bio);
        totalSteps=view.findViewById(R.id.total_steps);
        totalCoins=view.findViewById(R.id.total_coin_earned);
        progressbar=view.findViewById(R.id.progressbar);
        progressbar.setVisibility(View.VISIBLE);
        linearLayout=view.findViewById(R.id.ll);
        linearLayout.setVisibility(View.INVISIBLE);
        mAdView = view.findViewById(R.id.adView);
        mailUs = view.findViewById(R.id.mail_us);
        connectInstagram = view.findViewById(R.id.connect_instagram);
        connectTeligram = view.findViewById(R.id.connect_teligram);
        privacyPolicy = view.findViewById(R.id.privacy_policy);


    }




    public void listPopUp(View view, boolean background) {
        popupWindow.setAdapter(new ArrayAdapter<>(getContext(),
                android.R.layout.simple_list_item_1, list));
        popupWindow.setWidth(200);
        popupWindow.setAnchorView(view);
        popupWindow.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Toast.makeText(getContext(), list.get(position), Toast.LENGTH_SHORT).show();
                if (position==0&&backgroundImageLoaded){
                    Intent intent = CropImage.activity()
                            .setGuidelines(CropImageView.Guidelines.ON)
                            //.setAspectRatio(35,50)
                            .getIntent(getContext());
                    startActivityForResult(intent, CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE);
                }
                else if (position==0&&!backgroundImageLoaded){
                    Intent intent = CropImage.activity()
                            .setGuidelines(CropImageView.Guidelines.ON)
                            .setAspectRatio(80,80)
                            .getIntent(getContext());
                    startActivityForResult(intent, CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE);
                }
                else if (position==1&&backgroundImageLoaded){
                    mRef.child(uId).child("Profile").child("backgroundUrl").removeValue();
                    backgroundReference.delete();
                }else {
                    mRef.child(uId).child("Profile").child("profileUrl").removeValue();
                    profileReference.delete();
                }

                popupWindow.dismiss();
            }
        });
        popupWindow.show();
    }
    public String getCurrDate()  {
        Date d = new Date();
        CharSequence s  = DateFormat.format("yyyy-MM-dd", d.getTime());
        return s.toString();
    }

    @Override
    public void onStart() {
        super.onStart();
        getActivity().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mRef.child(uId).child("Profile").child("Name").setValue(name.getText().toString());
        mRef.child(uId).child("Profile").child("Bio").setValue(bio.getText().toString());
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


}