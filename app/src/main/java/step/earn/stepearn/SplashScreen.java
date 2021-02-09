package step.earn.stepearn;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;



public class SplashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        new Handler().postDelayed(new Runnable(){
            @Override
            public void run() {
                /* Create an Intent that will start the Menu-Activity. */
                FirebaseUser user= FirebaseAuth.getInstance().getCurrentUser();
                if (user!=null) {
                    Intent mainIntent = new Intent(SplashScreen.this,MainActivity.class);
                    startActivity(mainIntent);

                }
                else {
                    startActivity(new Intent(SplashScreen.this,MobileAthuntication.class));
                }
                SplashScreen.this.finish();

            }
        }, 2000);
    }

}