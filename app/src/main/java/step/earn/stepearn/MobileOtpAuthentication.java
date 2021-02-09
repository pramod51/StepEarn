package step.earn.stepearn;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import in.aabhasjindal.otptextview.OTPListener;
import in.aabhasjindal.otptextview.OtpTextView;

public class MobileOtpAuthentication extends AppCompatActivity {
    private TextView next,resend;
    final static String PHONE="phone";
    final static String CUNTERYCODE="cc";
    final static String REF_CODE="RF";
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;
    private OtpTextView otpTextView;
    private Button continueButton;

    String otp="";
    String id;
    String phoneN0=null,referralCode=null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mobile_otp_authentication);
        progressBar = findViewById(R.id.progressBar1);
        continueButton=findViewById(R.id.next);
        progressBar.setVisibility(View.GONE);
        resend=findViewById(R.id.resend);
        mAuth = FirebaseAuth.getInstance();
        otpTextView=findViewById(R.id.otp_view);
        SendVerificationCode();
        otpTextView.setOtpListener(new OTPListener() {
            @Override
            public void onInteractionListener() {

            }

            @Override
            public void onOTPComplete(String otpId) {
                otp+=otpId;
            }
        });
        resend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SendVerificationCode();
            }
        });
        mAuth=FirebaseAuth.getInstance();
        referralCode=getIntent().getStringExtra(REF_CODE);
        phoneN0=getIntent().getStringExtra(CUNTERYCODE)+getIntent().getStringExtra(PHONE);
        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (otp.isEmpty()){
                    Toast.makeText(MobileOtpAuthentication.this,"Please Enter Your OTP",Toast.LENGTH_LONG).show();
                }
                else if (otp.replace(" ","").length()!=6){
                    Toast.makeText(MobileOtpAuthentication.this,"Please Enter 6 Digit OTP",Toast.LENGTH_LONG).show();
                }
                else {

                    startActivity(new Intent(MobileOtpAuthentication.this,MainActivity.class));
                }
            }
        });


        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneN0,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onCodeSent(@NonNull String id, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                        MobileOtpAuthentication.this.id=id;
                        //super.onCodeSent(s, forceResendingToken);
                    }

                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                        signInWithPhoneAuthCredential(phoneAuthCredential);
                    }

                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) {

                        Toast.makeText(MobileOtpAuthentication.this," Verification Failed "+e.toString(),Toast.LENGTH_LONG).show();
                    }
                });



    }
    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(MobileOtpAuthentication.this,"OTP Verified", Toast.LENGTH_LONG).show();
                            if (task.getResult().getAdditionalUserInfo().isNewUser()) {
                                //do create new user
                                HashMap<String, String> map=new HashMap<>();
                                map.put("challengeCompleted","No");
                                map.put("Phone",phoneN0);

                                SetReferralCode(FirebaseAuth.getInstance().getCurrentUser().getUid(),referralCode);
                                FirebaseDatabase.getInstance().getReference().child("Users")
                                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("Profile").setValue(map)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Intent inte = new Intent(MobileOtpAuthentication.this,MainActivity.class);
                                                inte.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                startActivity(inte);
                                            }
                                        });


                            } else {
                                //user is exists, just do login
                                startActivity(new Intent(MobileOtpAuthentication.this,MainActivity.class));
                            }


                            progressBar.setVisibility(View.GONE);
                        }
                        else
                        {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(MobileOtpAuthentication.this,"Verification Failed",Toast.LENGTH_LONG).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.v("tag",e.toString());
            }
        });



    }

    public void SetReferralCode(final String uId, final String referralCode){
        FirebaseDatabase.getInstance().getReference().child("Users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                DataSnapshot dataSnapshot=null;
                String referralCd=null;
                while (!snapshot.child(uId).child("ReferralCode").exists()){
                    boolean isSet=true;
                    referralCd=createRandomCode();
                    for (DataSnapshot ds:snapshot.getChildren()){
                        if (ds.child("ReferralCode").exists()&&ds.child("ReferralCode").getValue(String.class).equals(referralCd)) {
                            isSet=false;
                            break;
                        }
                        if (ds.child("ReferralCode").exists()&&!referralCode.isEmpty()&&ds.child("ReferralCode").getValue(String.class).equals(referralCode)){
                            dataSnapshot=ds;
                        }
                    }
                    if (isSet){
                        FirebaseDatabase.getInstance().getReference().child("Users")
                                .child(uId).child("ReferralCode").setValue(referralCd);
                        break;
                    }

                }

                if (dataSnapshot!=null&&!dataSnapshot.child("YourReferral").child(referralCd).exists()){
                    FirebaseDatabase.getInstance().getReference().child("Users")
                            .child(dataSnapshot.getKey()).child("YourReferral").child(referralCd).setValue(5);
                }

            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public String createRandomCode(){
        int codeLength=5;
        char[] chars = "abcdefghijklmnopqrstuvwxyz1234567890ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
        StringBuilder sb = new StringBuilder();
        Random random = new SecureRandom();
        for (int i = 0; i < codeLength; i++) {
            char c = chars[random.nextInt(chars.length)];
            sb.append(c);
        }
        String output = sb.toString();
        System.out.println(output);
        return output ;
    }
    private void SendVerificationCode() {

        new CountDownTimer(60000, 1000) {

            @Override
            public void onTick(long l) {
                resend.setText(""+l/1000);
                resend.setEnabled(false);

            }

            @Override
            public void onFinish() {
                resend.setText("Resend");
                resend.setEnabled(true);

            }
        }.start();
    }

}
