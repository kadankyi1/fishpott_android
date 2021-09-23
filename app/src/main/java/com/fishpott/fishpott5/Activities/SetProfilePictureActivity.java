package com.fishpott.fishpott5.Activities;

import android.Manifest;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.StringRequestListener;
import com.fishpott.fishpott5.Fragments.CameraFragment;
import com.fishpott.fishpott5.Inc.Config;
import com.fishpott.fishpott5.Inc.Connectivity;
import com.fishpott.fishpott5.Miscellaneous.LocaleHelper;
import com.fishpott.fishpott5.R;
import com.fishpott.fishpott5.Util.MyLifecycleHandler;
import com.fishpott.fishpott5.Views.CircleImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

public class SetProfilePictureActivity extends AppCompatActivity implements View.OnClickListener, CameraFragment.OnFragmentInteractionListener{

    private ImageView mSmallCameraImageView, mBackImageView;
    private CircleImageView mDefaultImageHolderImageView;
    private RelativeLayout mSmallCameraHolderRelativeLayout, mDefaultImageHolderRelative;
    private TextView mInfoTextTextView;
    private Button mUploadButton;
    private ProgressBar mUploadingProgress;
    private Dialog.OnCancelListener cancelListenerActive1;
    private int KEY_PERMISSION_REQUEST_FILEREAD_FILEWRITE_CAMERA = 0001;
    private String[] permissions = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
    };
    private FrameLayout mCameraHolder;
    private Boolean CameraIsNotSupported = false;
    private String activityFinished = "no", redirectPath = "", networkResponse = "";
    private Bitmap uploadImageBitmap = null;
    private Thread imageUploadThread = null, imageUploadThread2 = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_profile_picture);

        mBackImageView = findViewById(R.id.activity_setprofilepicture_back_imageview);
        mDefaultImageHolderImageView = findViewById(R.id.activity_setprofilepicture_profilepicture_imageview);
        mSmallCameraImageView = findViewById(R.id.activity_setprofilepicture_smallcamera_imageview);
        mDefaultImageHolderRelative = findViewById(R.id.activity_setprofilepicture_relativelayout_1);
        mSmallCameraHolderRelativeLayout = findViewById(R.id.activity_setprofilepicture_relativelayout_2);
        mInfoTextTextView = findViewById(R.id.activity_setprofilepicture_info_textview);
        mUploadButton = findViewById(R.id.list_item_suggestedlinkupsactivity_upload_button);
        mUploadingProgress = findViewById(R.id.activity_setprofilepicture_loader);
        mCameraHolder = findViewById(R.id.camera_container);

        if(getIntent().getExtras() !=null) {
            activityFinished =(String) getIntent().getExtras().get(Config.KEY_ACTIVITY_FINISHED);
            redirectPath = (String) getIntent().getExtras().get("redirect_path");
            if(redirectPath == null){redirectPath = "";}
            if(activityFinished == null){activityFinished = "no";}
        }

        mUploadButton.setOnClickListener(this);
        mBackImageView.setOnClickListener(this);
        mDefaultImageHolderImageView.setOnClickListener(this);

        cancelListenerActive1 = new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                Config.getPermission(SetProfilePictureActivity.this, permissions, KEY_PERMISSION_REQUEST_FILEREAD_FILEWRITE_CAMERA);
            }
        };
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.list_item_suggestedlinkupsactivity_upload_button){
            if(uploadImageBitmap != null){
                imageUploadThread2 = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if(Connectivity.isConnected(SetProfilePictureActivity.this)){
                            uploadPottPicture(LocaleHelper.getLanguage(SetProfilePictureActivity.this));
                        } else {
                            Config.showToastType1(SetProfilePictureActivity.this, getResources().getString(R.string.login_activity_check_your_internet_connection_and_try_again));
                        }
                    }
                });
                imageUploadThread2.start();
            } else {
                Config.showToastType1(SetProfilePictureActivity.this, getString(R.string.setprofilepicture_activity_please_choose_a_picture_for_your_pott));
            }
        } else if(view.getId() == R.id.activity_setprofilepicture_back_imageview){
            onBackPressed();
        } else if (view.getId() ==  R.id.activity_setprofilepicture_profilepicture_imageview){
            mDefaultImageHolderImageView.setImageResource(R.drawable.setprofilepicture_activity_imageholder_default_image);
            if(CameraIsNotSupported){
                Config.showToastType1(SetProfilePictureActivity.this, getString(R.string.setprofilepicture_activity_your_device_camera_has_malfunctioned));
            } else {
                if(Config.permissionsHaveBeenGranted(getApplicationContext(), permissions)){
                    if(Config.checkCameraHardware(getApplicationContext())){
                        Config.openFragment(getSupportFragmentManager(),R.id.camera_container, CameraFragment.newInstance(), "CameraFragment", 1);
                        mCameraHolder.setVisibility(View.VISIBLE);
                    } else {
                        Config.showToastType1(SetProfilePictureActivity.this, getString(R.string.setprofilepicture_activity_your_device_camera_has_malfunctioned));
                    }
                } else {
                    cancelListenerActive1 = Config.showDialogType1(SetProfilePictureActivity.this, "1", getString(R.string.setprofilepicture_activity_fishpott_needs_your_permission_access_your_camera_and_storage_device_so_that_you_can_take_and_save_pictures_we_are_gonna_ask_you_for_these_permissions_should_we), getString(R.string.setprofilepicture_activity_without_granting_these_permissions_you_cannot_use_fishpott), cancelListenerActive1, true, getString(R.string.ask), getString(R.string.setprofilepicture_activity_not_now));
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mBackImageView = findViewById(R.id.activity_setprofilepicture_back_imageview);
        mDefaultImageHolderImageView = findViewById(R.id.activity_setprofilepicture_profilepicture_imageview);
        mSmallCameraImageView = findViewById(R.id.activity_setprofilepicture_smallcamera_imageview);
        mDefaultImageHolderRelative = findViewById(R.id.activity_setprofilepicture_relativelayout_1);
        mSmallCameraHolderRelativeLayout = findViewById(R.id.activity_setprofilepicture_relativelayout_2);
        mInfoTextTextView = findViewById(R.id.activity_setprofilepicture_info_textview);
        mUploadButton = findViewById(R.id.list_item_suggestedlinkupsactivity_upload_button);
        mUploadingProgress = findViewById(R.id.activity_setprofilepicture_loader);
        mCameraHolder = findViewById(R.id.camera_container);

        if(!networkResponse.trim().equalsIgnoreCase("")){
            Config.showDialogType1(SetProfilePictureActivity.this, getString(R.string.login_activity_error), networkResponse, "", null, false, "", "");
            mUploadingProgress.setVisibility(View.INVISIBLE);
            mDefaultImageHolderImageView.setVisibility(View.VISIBLE);
            mSmallCameraImageView.setVisibility(View.VISIBLE);
            mDefaultImageHolderRelative.setVisibility(View.VISIBLE);
            mSmallCameraHolderRelativeLayout.setVisibility(View.VISIBLE);
            mInfoTextTextView.setVisibility(View.VISIBLE);
            mCameraHolder.setVisibility(View.VISIBLE);
            mUploadButton.setVisibility(View.VISIBLE);
            networkResponse = "";
        }

    }

    @Override
    protected void onStop() {
        super.onStop();

        Log.e("memoryManage", "onStop STARTED SETPROFILEPICTURE-ACTIVITY");
        mSmallCameraImageView = null;
        mBackImageView = null;
        mDefaultImageHolderImageView = null;
        mSmallCameraHolderRelativeLayout = null;
        mDefaultImageHolderRelative = null;
        mInfoTextTextView = null;
        mUploadButton = null;
        mUploadingProgress = null;
        mCameraHolder = null;
        uploadImageBitmap = null;

        if(imageUploadThread != null){
            imageUploadThread.interrupt();
            imageUploadThread = null;
        }

        if(imageUploadThread2 != null){
            imageUploadThread2.interrupt();
            imageUploadThread2 = null;
        }

        Config.freeMemory();
    }

    @Override
    public void finish() {
        super.finish();
        if(activityFinished.trim().equalsIgnoreCase("no")){
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        }
        Log.e("memoryManage", "finish STARTED SETPROFILEPICTURE-ACTIVITY");
        Config.freeMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Log.e("memoryManage", "onDestroy STARTED SETPROFILEPICTURE-ACTIVITY");
        mSmallCameraImageView = null;
        mBackImageView = null;
        mDefaultImageHolderImageView = null;
        mSmallCameraHolderRelativeLayout = null;
        mDefaultImageHolderRelative = null;
        mInfoTextTextView = null;
        mUploadButton = null;
        mUploadingProgress = null;
        cancelListenerActive1 = null;
        mCameraHolder = null;
        CameraIsNotSupported = null;
        activityFinished = null;
        redirectPath = null;
        uploadImageBitmap = null;
        networkResponse = null;

        if(imageUploadThread != null){
            imageUploadThread.interrupt();
            imageUploadThread = null;
        }

        if(imageUploadThread2 != null){
            imageUploadThread2.interrupt();
            imageUploadThread2 = null;
        }

        Config.unbindDrawables(findViewById(R.id.root_setprofilepicture_activity));
        Config.freeMemory();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            if(requestCode == KEY_PERMISSION_REQUEST_FILEREAD_FILEWRITE_CAMERA){
                if(Config.permissionsHaveBeenGranted(getApplicationContext(), permissions)){
                    if(Config.checkCameraHardware(getApplicationContext())){
                        mCameraHolder.setVisibility(View.VISIBLE);
                        Config.openFragment(getSupportFragmentManager(),R.id.camera_container, CameraFragment.newInstance(), "CameraFragment", 1);
                    } else {
                        Config.showToastType1(SetProfilePictureActivity.this, getString(R.string.setprofilepicture_activity_your_device_camera_has_malfunctioned));
                    }
                } else {
                    Config.showDialogType1(SetProfilePictureActivity.this, "1", getString(R.string.setprofilepicture_activity_without_granting_these_permissions_you_cannot_use_fishpott), "", null, true, getString(R.string.setprofilepicture_activity_okay), "");
                }

            }

    }

    @Override
    public void onBackPressed() {
        mCameraHolder.setVisibility(View.INVISIBLE);
        super.onBackPressed();
    }

    @Override
    public void onFragmentInteraction(final Uri imageUri) {
        Bitmap compressedBitmap = Config.decodeSampledBitmapFromFileOrResource(1, imageUri.getPath(), null, null, 0, 100, 100);
        uploadImageBitmap = compressedBitmap;
        mDefaultImageHolderImageView.setImageBitmap(compressedBitmap);
    }


    public void uploadPottPicture(final String language){
                File uploadPottPictureFile = Config.getFileFromBitmap(uploadImageBitmap, "temp.png", SetProfilePictureActivity.this);
                if(!Config.fileExists(uploadPottPictureFile.getAbsolutePath())){
                    if (MyLifecycleHandler.isApplicationInForeground()) {
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                Config.showToastType1(SetProfilePictureActivity.this, getResources().getString(R.string.setprofilepicture_activity_an_error_occured_try_choosing_photo_again));
                                mUploadingProgress.setVisibility(View.INVISIBLE);
                                mDefaultImageHolderImageView.setVisibility(View.VISIBLE);
                                mSmallCameraImageView.setVisibility(View.VISIBLE);
                                mDefaultImageHolderRelative.setVisibility(View.VISIBLE);
                                mSmallCameraHolderRelativeLayout.setVisibility(View.VISIBLE);
                                mInfoTextTextView.setVisibility(View.VISIBLE);
                                mCameraHolder.setVisibility(View.VISIBLE);
                                mUploadButton.setVisibility(View.VISIBLE);
                            }
                        });

                    } else {
                        networkResponse = getResources().getString(R.string.setprofilepicture_activity_an_error_occured_try_choosing_photo_again);
                    }
                    return;
                }

                if (MyLifecycleHandler.isApplicationInForeground()) {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        mDefaultImageHolderImageView.setVisibility(View.INVISIBLE);
                        mSmallCameraImageView.setVisibility(View.INVISIBLE);
                        mDefaultImageHolderRelative.setVisibility(View.INVISIBLE);
                        mSmallCameraHolderRelativeLayout.setVisibility(View.INVISIBLE);
                        mInfoTextTextView.setVisibility(View.INVISIBLE);
                        mCameraHolder.setVisibility(View.INVISIBLE);
                        mUploadButton.setVisibility(View.INVISIBLE);
                        mUploadingProgress.setVisibility(View.VISIBLE);
                    }
                });

                AndroidNetworking.upload(Config.LINK_UPLOAD_POTT_PICTURE)
                        .addMultipartParameter("user_phone_number", Config.getSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_PHONE))
                        .addMultipartParameter("investor_id", Config.getSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_PASSWORD_ACCESS_TOKEN))
                        .addMultipartFile("pott_picture", uploadPottPictureFile)
                        .addMultipartParameter("user_language", language)
                        .addMultipartParameter("app_type", "ANDROID")
                        .addMultipartParameter("app_version_code", String.valueOf(Config.getAppVersionCode(getApplicationContext())))
                        .setTag("setprofilepicture_activity_pottpictureupload")
                        .setPriority(Priority.HIGH)
                        .build().getAsString(new StringRequestListener() {
                    @Override
                    public void onResponse(String response) {
                            try {
                                Log.e("PSignup", response);
                                JSONObject o = new JSONObject(response);
                                String myStatus = o.getString("status");
                                final String myStatusMessage = o.getString("message");

                                if (myStatus.equalsIgnoreCase("yes")) {
                                    Config.setSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_PROFILE_PICTURE, o.getString("pott_pic_path"));

                                    if (o.getBoolean("phone_verification_is_on")) {
                                        Config.openActivity(SetProfilePictureActivity.this, ConfirmPhoneNumberActivity.class, 1, 2, 1, Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_PHONE, Config.getSharedPreferenceString(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_PHONE));
                                        return;
                                    }

                                    if(o.getBoolean("government_verification_is_on")){
                                        Config.setSharedPreferenceBoolean(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_ACCOUNT_GOVERNMENT_ID_VERIFICATION_NEEDED_STATUS, true);
                                        Config.openActivity(SetProfilePictureActivity.this, GovernmentIDVerificationActivity.class, 1, 2, 0, "", "");
                                        return;
                                    }

                                    if (Config.checkUpdateAndForwardToUpdateActivity(SetProfilePictureActivity.this, o.getInt("user_android_app_max_vc"), o.getBoolean("user_android_app_force_update"))) {
                                        Config.setSharedPreferenceBoolean(getApplicationContext(), Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_BY_FORCE, o.getBoolean("user_android_app_force_update"));
                                        Config.setSharedPreferenceInt(getApplicationContext(), Config.SHARED_PREF_KEY_UPDATE_ACTIVITY_UPDATE_VERSION_CODE, o.getInt("user_android_app_max_vc"));
                                        return;
                                    }

                                    if(redirectPath.trim().equalsIgnoreCase("1")){
                                        finish();
                                    } else {
                                        Config.openActivity(SetProfilePictureActivity.this, SuggestedLinkUpsActivity.class, 1, 2, 0, "", "");
                                    }
                                } else if(myStatus.equalsIgnoreCase("0")){
                                    Config.setSharedPreferenceInt(getApplicationContext(), Config.SHARED_PREF_KEY_USER_CREDENTIALS_USER_ACCOUNT_SUSPENDED_STATUS, 1);
                                    Config.openActivity(SetProfilePictureActivity.this, FlaggedAccountActivity.class, 1, 2, 0, "", "");
                                    return;
                                } else {

                                    if (MyLifecycleHandler.isApplicationInForeground()) {
                                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                                            @Override
                                            public void run() {
                                                Config.showDialogType1(SetProfilePictureActivity.this, getString(R.string.login_activity_login_failed), myStatusMessage, "", null, true, "", "");
                                                mUploadingProgress.setVisibility(View.INVISIBLE);
                                                mDefaultImageHolderImageView.setVisibility(View.VISIBLE);
                                                mSmallCameraImageView.setVisibility(View.VISIBLE);
                                                mDefaultImageHolderRelative.setVisibility(View.VISIBLE);
                                                mSmallCameraHolderRelativeLayout.setVisibility(View.VISIBLE);
                                                mInfoTextTextView.setVisibility(View.VISIBLE);
                                                mCameraHolder.setVisibility(View.VISIBLE);
                                                mUploadButton.setVisibility(View.VISIBLE);

                                            }
                                        });
                                    }  else {
                                        networkResponse = getResources().getString(R.string.login_activity_login_failed);
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                if (MyLifecycleHandler.isApplicationInForeground()) {
                                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                                        @Override
                                        public void run() {
                                            Config.showDialogType1(SetProfilePictureActivity.this, getString(R.string.login_activity_error), getString(R.string.login_activity_an_unexpected_error_occured), "", null, false, "", "");
                                            mUploadingProgress.setVisibility(View.INVISIBLE);
                                            mDefaultImageHolderImageView.setVisibility(View.VISIBLE);
                                            mSmallCameraImageView.setVisibility(View.VISIBLE);
                                            mDefaultImageHolderRelative.setVisibility(View.VISIBLE);
                                            mSmallCameraHolderRelativeLayout.setVisibility(View.VISIBLE);
                                            mInfoTextTextView.setVisibility(View.VISIBLE);
                                            mCameraHolder.setVisibility(View.VISIBLE);
                                            mUploadButton.setVisibility(View.VISIBLE);
                                        }
                                    });
                                } else {
                                    networkResponse = getResources().getString(R.string.login_activity_an_unexpected_error_occured);
                                }
                            }
                    }

                    @Override
                    public void onError(ANError anError) {
                        if (MyLifecycleHandler.isApplicationInForeground()) {
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    Config.showToastType1(SetProfilePictureActivity.this, getResources().getString(R.string.login_activity_check_your_internet_connection_and_try_again));
                                    mUploadingProgress.setVisibility(View.INVISIBLE);
                                    mDefaultImageHolderImageView.setVisibility(View.VISIBLE);
                                    mSmallCameraImageView.setVisibility(View.VISIBLE);
                                    mDefaultImageHolderRelative.setVisibility(View.VISIBLE);
                                    mSmallCameraHolderRelativeLayout.setVisibility(View.VISIBLE);
                                    mInfoTextTextView.setVisibility(View.VISIBLE);
                                    mCameraHolder.setVisibility(View.VISIBLE);
                                    mUploadButton.setVisibility(View.VISIBLE);
                                }
                            });

                        } else {
                            networkResponse = getResources().getString(R.string.login_activity_check_your_internet_connection_and_try_again);
                        }
                    }
                });
            } else {
                    networkResponse = getResources().getString(R.string.setprofilepicture_activity_an_error_occured_try_choosing_photo_again);
                }

    }

}
