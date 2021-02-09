package step.earn.stepearn;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.material.snackbar.Snackbar;
import com.rilixtech.widget.countrycodepicker.Country;
import com.rilixtech.widget.countrycodepicker.CountryCodePicker;

public class MobileAthuntication extends AppCompatActivity {
    CountryCodePicker ccp;
    private EditText phoneNo,refCode;
    private Button continue_no;
    final static String PHONE="phone";
    final static String CUNTERYCODE="cc";
    final static String REF_CODE="RF";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mobile_athuntication);
        ccp = (CountryCodePicker) findViewById(R.id.ccp);
        refCode=findViewById(R.id.referral);

        ccp.setOnCountryChangeListener(new CountryCodePicker.OnCountryChangeListener() {
            @Override
            public void onCountrySelected(Country selectedCountry) {
                //Toast.makeText(MobileAthuntication.this, "Updated " + selectedCountry.getName(), Toast.LENGTH_SHORT).show();
            }
        });
        //Log.v("tag",ccp.getSelectedCountryCodeWithPlus());
        phoneNo=findViewById(R.id.mobile_no);
        continue_no=findViewById(R.id.continue_button);
        continue_no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (phoneNo.getText().toString().isEmpty()){
                    phoneNo.setError("Please enter mobile number");
                }
                else {
                    if (checkInternetConnection(v))
                        return;
                    Intent intent=new Intent(MobileAthuntication.this, MobileOtpAuthentication.class);
                    intent.putExtra(PHONE,phoneNo.getText().toString());
                    intent.putExtra(CUNTERYCODE,ccp.getSelectedCountryCodeWithPlus());
                    intent.putExtra(REF_CODE,refCode.getText().toString());
                    startActivity(intent);
                }
            }
        });

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        finishAffinity();
    }
    private Boolean checkInternetConnection(View view) {
        ConnectivityManager connectivityManager=(ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo=connectivityManager.getActiveNetworkInfo();
        if(networkInfo==null){
            Snackbar snackbar = Snackbar.make(view, "No Internet Connection", Snackbar.LENGTH_LONG);
            snackbar.setBackgroundTint(ContextCompat.getColor(this,R.color.dark_orange));
            snackbar.setTextColor(ContextCompat.getColor(this,R.color.black));
            snackbar.show();
            return true;
        }
        return false;
    }
}