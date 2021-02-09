package step.earn.stepearn;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.dynamiclinks.DynamicLink;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.ShortDynamicLink;

public class FriendsFragment extends Fragment {
    private Button inviteNow;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view=inflater.inflate(R.layout.activity_friends_fragment, container, false);
        inviteNow=view.findViewById(R.id.invite_a_friend);
        //FirebaseDatabase.getInstance().getReference().child("BasicRecords").child("")
        inviteNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getContext(),"Please wait :)",Toast.LENGTH_LONG).show();

                FirebaseDatabase.getInstance().getReference().addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        DataSnapshot snapshot=dataSnapshot.child("BasicRecords");
                        final String inviteMessage=snapshot.child("InviteMessage").getValue(String.class);
                        final String inviteUrl=snapshot.child("InviteUrl").getValue(String.class);
                        final String inviteCode=dataSnapshot.child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                .child("ReferralCode").getValue(String.class);

                        DynamicLink dynamicLink = FirebaseDynamicLinks.getInstance().createDynamicLink()
                                .setLink(Uri.parse(inviteUrl))
                                .setDomainUriPrefix("stepearn100.page.link")
                                // Open links with this app on Android
                                .setAndroidParameters(new DynamicLink.AndroidParameters.Builder().build())
                                // Open links with com.example.ios on iOS
                                //.setIosParameters(new DynamicLink.IosParameters.Builder("com.example.ios").build())
                                .buildDynamicLink();

                        Uri dynamicLinkUri = dynamicLink.getUri();
                        Log.v("tag", dynamicLinkUri.toString());
                        Task<ShortDynamicLink> shortLinkTask = FirebaseDynamicLinks.getInstance().createDynamicLink()
                                .setLongLink(Uri.parse("https://"+dynamicLinkUri.toString()))
                                .buildShortDynamicLink()
                                .addOnCompleteListener(getActivity(), new OnCompleteListener<ShortDynamicLink>() {
                                    @Override
                                    public void onComplete(@NonNull Task<ShortDynamicLink> task) {
                                        if (task.isSuccessful()) {
                                            // Short link created
                                            Uri shortLink = task.getResult().getShortLink();
                                            Uri flowchartLink = task.getResult().getPreviewLink();
                                            Log.v("tag",shortLink.toString());
                                            Intent shareIntent = new Intent();
                                            String msg = inviteMessage+"Invite Code : "+inviteCode+ "\n"+ shortLink.toString();
                                            shareIntent.setAction(Intent.ACTION_SEND);
                                            shareIntent.putExtra(Intent.EXTRA_TEXT, msg);
                                            shareIntent.setType("text/plain");
                                            startActivity(shareIntent);
                                        }
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        //Log.v("tag",e.toString());
                                        Toast.makeText(getContext(),"Invite Link Not Genrated Try Again :)",Toast.LENGTH_LONG).show();
                                    }
                                });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


            }
        });
        return view;
    }


}