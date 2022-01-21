package com.fishpott.fishpott5.Activities;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import com.fishpott.fishpott5.Fragments.Signup.SignupStartFragment;
import com.fishpott.fishpott5.Inc.Config;
import com.fishpott.fishpott5.R;

public class SignUpActivity extends AppCompatActivity {

    private Thread initThread = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);


        initThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Config.openFragment(getSupportFragmentManager(), R.id.activity_signup_fragment_holder, SignupStartFragment.newInstance() , "SignupStartFragment", 1);
                initThread.interrupt();
            }
        });
        initThread.start();
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() == 1) {
            finish();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e("memoryManage", "onResume STARTED SIGNUP-ACTIVITY");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.e("memoryManage", "onStop STARTED SIGNUP-ACTIVITY");
        if(initThread != null){
            initThread.interrupt();
            initThread = null;
        }
        Config.freeMemory();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        Log.e("memoryManage", "finish STARTED SIGNUP-ACTIVITY");
        Config.freeMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e("memoryManage", "onDestroy STARTED SIGNUP-ACTIVITY");
        if(initThread != null){
            initThread.interrupt();
            initThread = null;
        }
        Config.unbindDrawables(findViewById(R.id.root_signup_actvitiy));
        Config.freeMemory();
    }

}
