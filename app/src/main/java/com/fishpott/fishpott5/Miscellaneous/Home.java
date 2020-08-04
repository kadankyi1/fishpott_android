package com.fishpott.fishpott5.Miscellaneous;

import android.app.Application;
import android.content.Context;
import android.os.StrictMode;

import com.fishpott.fishpott5.Util.MyLifecycleHandler;
//import com.squareup.leakcanary.LeakCanary;
//import com.squareup.leakcanary.RefWatcher;

/**
 * Created by zatana on 10/27/18.
 */


public class Home extends Application {

    //private RefWatcher refWatcher;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base, "en"));
    }

    /*
    public static RefWatcher getRefWatcher(Context context){
        Home application = (Home)  context.getApplicationContext();
        return application.refWatcher;
    }
     */

    @Override
    public void onCreate() {
        super.onCreate();
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .permitAll()
                //.detectDiskReads()
                //.detectDiskWrites()
                //.detectNetwork()
                //.penaltyDialog()
                //.penaltyLog()
                //.penaltyDeath()
                .build());

        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                .detectLeakedSqlLiteObjects()
                .detectLeakedClosableObjects()
                .penaltyLog()
                //.penaltyDeath()
                .build());
    /*
        if(LeakCanary.isInAnalyzerProcess(this)){
            return;
        }
        refWatcher = LeakCanary.install(this);
    */
        registerActivityLifecycleCallbacks(new MyLifecycleHandler());
    }
}
