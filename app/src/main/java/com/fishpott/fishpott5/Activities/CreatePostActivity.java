package com.fishpott.fishpott5.Activities;

import android.Manifest;
import android.app.Dialog;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.fishpott.fishpott5.Fragments.LoadSharesForPostingFragment;
import com.fishpott.fishpott5.Fragments.NewsFeedFragment;
import com.fishpott.fishpott5.Inc.Config;
import com.fishpott.fishpott5.ListDataGenerators.Vertical_NewsType_ListDataGenerator;
import com.fishpott.fishpott5.Miscellaneous.LocaleHelper;
import com.fishpott.fishpott5.Models.Vertical_NewsType_Model;
import com.fishpott.fishpott5.R;
import com.fishpott.fishpott5.Util.MyLifecycleHandler;
import com.fishpott.fishpott5.Util.RealPathUtil2;

public class CreatePostActivity extends AppCompatActivity implements View.OnClickListener, LoadSharesForPostingFragment.OnFragmentInteractionListener {

    private Dialog.OnCancelListener cancelListenerActive1;
    private ImageView mSelectNewsImageImageView, mSelectNewsVideoImageView, mRemoveImagesIconImageView,
            mRemoveVideosIconImageView, mRemoveSharesIconImageView, mBackIconImageView;
    private EditText mNewsTextEditText;
    private ConstraintLayout mImagesCountConstraintLayout, mVideoCountConstraintLayout;
    private Button mAddSharesButton;
    private Boolean selectingImage = true;
    private static final int REQUEST_CODE_PICK_GALLERY = 0x1;
    private static final int REQUEST_CODE_PICK_VIDEO = 0x2;
    public static Uri[] allselectedImagesUri = {null, null, null, null, null, null, null, null, null, null};
    public static String[] allselectedImagesRealPaths = {"", "", "", "", "", "", "", "", "", ""};
    private String allImagesSelected = "", selectedVideoPath = "";
    private String addedSharesId = "", addedSharesPricePerShare = "", addedSharesQuantity = "", addedSharesMyPass = "";
    private int allImages = 0;
    private TextView mImagesAddedInfoTextView, mPostTextView, mVideoAddedInfoTextView, mSharesAddedInfoTextView;
    public static final int KEY_PERMISSION_REQUEST_FILEREAD_FILEWRITE_CAMERA = 0001;
    private String[] permissions = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private String repostingNewsId = "";
    private Thread imageFetchThread = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_post);

        mBackIconImageView = findViewById(R.id.top_bar_back_imageview);
        mSelectNewsImageImageView = findViewById(R.id.gallery_icon_imageview);
        mSelectNewsVideoImageView = findViewById(R.id.video_icon_imageview);
        mSharesAddedInfoTextView = findViewById(R.id.numberofsharesadded_textview);
        mImagesCountConstraintLayout = findViewById(R.id.numberofimages_holder_constraintlayout);
        mImagesAddedInfoTextView = findViewById(R.id.numberofimagesadded_textview);
        mVideoCountConstraintLayout = findViewById(R.id.numberofvideos_holder_constraintlayout);
        mVideoAddedInfoTextView = findViewById(R.id.numberofvideosadded_textview);
        mNewsTextEditText = findViewById(R.id.post_text_edit_text);
        mPostTextView = findViewById(R.id.top_bar_title_textview);
        mRemoveSharesIconImageView = findViewById(R.id.removenewsaddedshares_icon_imageview);
        mRemoveImagesIconImageView = findViewById(R.id.removenewsimage_icon_imageview);
        mRemoveVideosIconImageView = findViewById(R.id.removenewsvideo_icon_imageview);
        mAddSharesButton = findViewById(R.id.add_shares_button);

        if(Config.getSharedPreferenceInt(getApplicationContext(), Config.SHARED_PREF_KEY_USER_MEDIA_POSTING_ALLOWED) == 0){
            mSelectNewsImageImageView.setVisibility(View.VISIBLE);
            mSelectNewsVideoImageView.setVisibility(View.VISIBLE);
            mImagesCountConstraintLayout.setVisibility(View.VISIBLE);
            mVideoCountConstraintLayout.setVisibility(View.VISIBLE);
        } else {
            mSelectNewsImageImageView.setVisibility(View.GONE);
            mSelectNewsVideoImageView.setVisibility(View.GONE);
            mImagesCountConstraintLayout.setVisibility(View.GONE);
            mVideoCountConstraintLayout.setVisibility(View.GONE);
        }

        if(getIntent().getExtras() != null) {
            repostingNewsId =(String) getIntent().getExtras().get("newsid");
            mSelectNewsImageImageView.setVisibility(View.GONE);
            mSelectNewsVideoImageView.setVisibility(View.GONE);
            mImagesCountConstraintLayout.setVisibility(View.GONE);
            mVideoCountConstraintLayout.setVisibility(View.GONE);
            mPostTextView.setText(getResources().getString(R.string.repost));
        }

        mBackIconImageView.setOnClickListener(this);
        mRemoveSharesIconImageView.setOnClickListener(this);
        mRemoveImagesIconImageView.setOnClickListener(this);
        mRemoveVideosIconImageView.setOnClickListener(this);
        mSelectNewsVideoImageView.setOnClickListener(this);
        mSelectNewsImageImageView.setOnClickListener(this);
        mAddSharesButton.setOnClickListener(this);
        mAddSharesButton.setOnClickListener(this);
        mPostTextView.setOnClickListener(this);

        // WHEN THE USER CLICKS THAT HE WILL ALLOW THE PERMISSIONS, WE ASK FOR THEM
        cancelListenerActive1 = new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                Config.getPermission(CreatePostActivity.this, permissions, KEY_PERMISSION_REQUEST_FILEREAD_FILEWRITE_CAMERA);
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Config.freeMemory();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        Config.freeMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Config.unbindDrawables(findViewById(R.id.root_createpost_activity));
        Config.freeMemory();
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.gallery_icon_imageview){
            selectingImage = true;
            if(Config.permissionsHaveBeenGranted(getApplicationContext(), permissions)){
                imageFetchThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Config.pickMultipleImages(CreatePostActivity.this, REQUEST_CODE_PICK_GALLERY);
                    }
                });
                imageFetchThread.start();
            } else {
                cancelListenerActive1 = Config.showDialogType1(CreatePostActivity.this, "1", getString(R.string.setprofilepicture_activity_fishpott_needs_your_permission_access_your_camera_and_storage_device_so_that_you_can_take_and_save_pictures_we_are_gonna_ask_you_for_these_permissions_should_we), "", cancelListenerActive1, true, getString(R.string.setprofilepicture_activity_i_will_allow), getString(R.string.setprofilepicture_activity_not_now));
            }
        } else if (v.getId() == R.id.video_icon_imageview){
            selectingImage = false;
            if(Config.permissionsHaveBeenGranted(getApplicationContext(), permissions)){
                Intent intent = new Intent();
                intent.setType("video/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent,"Select Video"), REQUEST_CODE_PICK_VIDEO);
            } else {
                cancelListenerActive1 = Config.showDialogType1(CreatePostActivity.this, "1", getString(R.string.setprofilepicture_activity_fishpott_needs_your_permission_access_your_camera_and_storage_device_so_that_you_can_take_and_save_pictures_we_are_gonna_ask_you_for_these_permissions_should_we), "", cancelListenerActive1, true, getString(R.string.setprofilepicture_activity_i_will_allow), getString(R.string.setprofilepicture_activity_not_now));
            }
        } else if (v.getId() == R.id.top_bar_back_imageview){
            onBackPressed();
        } else if (v.getId() == R.id.removenewsaddedshares_icon_imageview){
            addedSharesId = ""; addedSharesQuantity  = ""; addedSharesPricePerShare  = "";
            mSharesAddedInfoTextView.setText(getString(R.string.no_shares_added));
        } else if (v.getId() == R.id.removenewsimage_icon_imageview){
            for (int i = 0; i < allselectedImagesUri.length; i++){
                allselectedImagesUri[i] = null;
                allselectedImagesRealPaths[i] = "";
            }
            allImages = 0; allImagesSelected = ""; mImagesAddedInfoTextView.setText(getString(R.string.no_images_added));
        } else if (v.getId() == R.id.removenewsvideo_icon_imageview){
            selectedVideoPath = ""; mVideoAddedInfoTextView.setText(getString(R.string.no_video_added));
        } else if(v.getId() == R.id.add_shares_button){
            Config.openFragment(getSupportFragmentManager(), R.id.shares_holder_framelayout, LoadSharesForPostingFragment.newInstance("CreatePostActivity", "Activity"), "LoadSharesForPostingFragment", 3);
        } else if (v.getId() == R.id.top_bar_title_textview){
            postNews();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.e("ImagePick", "================= onActivityResult =================");
            if (requestCode == REQUEST_CODE_PICK_GALLERY ) {
                Log.e("ImagePick", "HERE 1");
                if (resultCode == RESULT_OK && data != null) {
                    Log.e("ImagePick", "HERE 2");
                    if (data.getData() != null) {
                        Log.e("ImagePick", "HERE 3");

                        Uri contentURI = data.getData();
                        allselectedImagesUri[0] = contentURI;
                        Log.e("ImagePick", "ONE IMAGE uri.getPath " + contentURI.getPath());
                        //allselectedImagesRealPaths[0] = Config.getRealPathFromUri(getApplicationContext(), 0, contentURI);
                        allselectedImagesRealPaths[0] = RealPathUtil2.getPath(getApplicationContext(), contentURI);
                        Log.e("ImagePick", "ONE IMAGE allselectedImagesRealPaths " + allselectedImagesRealPaths[0]);
                        allImages = 1;
                        allImagesSelected = "1";
                        mImagesAddedInfoTextView.setText(getString(R.string._1_image_added_to_post));

                    } else {
                        allImages = 0;
                        if (data.getClipData() != null) {
                            ClipData mClipData = data.getClipData();
                            for (int i = 0; i < mClipData.getItemCount(); i++) {
                                ClipData.Item item = mClipData.getItemAt(i);
                                Uri uri = item.getUri();
                                allselectedImagesUri[i] = uri;
                                allselectedImagesRealPaths[i] = RealPathUtil2.getPath(getApplicationContext(), uri);
                                //allselectedImagesRealPaths[i] = Config.getRealPathFromUri(getApplicationContext(), 0, uri);
                                Log.e("ImagePick", String.valueOf(i) + " uri.getPath " + uri.getPath());
                                allImages++;
                                if(i == 9){
                                    break;
                                }
                            }
                            if (allImages == 1) {
                                mImagesAddedInfoTextView.setText(getString(R.string._1_image_added_to_post));
                            } else {
                                mImagesAddedInfoTextView.setText(String.valueOf(allImages) + " " + getString(R.string.images_added));
                            }

                            allImagesSelected = "1";
                        }
                    }
                } else {
                    mImagesAddedInfoTextView.setText(getString(R.string.no_images_added));
                }


            } else if (requestCode ==  REQUEST_CODE_PICK_VIDEO) {
                if (resultCode == RESULT_OK) {
                    Uri selectedImageUri = data.getData();
                    selectedVideoPath = RealPathUtil2.getPath(getApplicationContext(), selectedImageUri);
                    //selectedVideoPath = Config.getRealPathFromUri(getApplicationContext(), 1, selectedImageUri);
                    Log.e("ImagePick", "Video Gallery Path " + selectedVideoPath);

                    mVideoAddedInfoTextView.setText(R.string.video_added);
                } else {
                    mVideoAddedInfoTextView.setText(getString(R.string.no_video_added));
                }
            }

        super.onActivityResult(requestCode, resultCode, data);
    }

    //AFTER A PERMISSION IS GRANTED  OR NOT, WHAT WE DO
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(MyLifecycleHandler.isApplicationVisible()){
            if(requestCode == KEY_PERMISSION_REQUEST_FILEREAD_FILEWRITE_CAMERA){
                //CHECKING IF A PERMISSION IS GRANTED
                if(Config.permissionsHaveBeenGranted(getApplicationContext(), permissions)){
                    // CHECKING IF THE USERS CAMERA WORKS
                    if(selectingImage){
                        Config.pickMultipleImages(CreatePostActivity.this, REQUEST_CODE_PICK_GALLERY);
                    } else {
                        Intent intent = new Intent();
                        intent.setType("video/*");
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        startActivityForResult(Intent.createChooser(intent,"Select Video"), REQUEST_CODE_PICK_VIDEO);
                    }
                } else {
                    Config.showDialogType1(CreatePostActivity.this, "1", getString(R.string.setprofilepicture_activity_without_granting_these_permissions_you_cannot_use_fishpott), "", null, true, getString(R.string.setprofilepicture_activity_okay), "");
                }

            }
        }

    }

    public void postNews(){
        if(!mNewsTextEditText.getText().toString().trim().equalsIgnoreCase("")
           || (!addedSharesId.trim().equalsIgnoreCase("") && !addedSharesPricePerShare.trim().equalsIgnoreCase("") && !addedSharesQuantity.trim().equalsIgnoreCase(""))
           ||  !allImagesSelected.trim().equalsIgnoreCase("") ||  !selectedVideoPath.trim().equalsIgnoreCase("") ) {
            if (Vertical_NewsType_ListDataGenerator.getAllData().get(1).getNewsType() == Config.NEWS_TYPE_25_POSTINGFAILEDRETRYPOST_VERTICAL_KEY) {
                Vertical_NewsType_ListDataGenerator.getAllData().remove(1);
                NewsFeedFragment.mNewsRecyclerView.getAdapter().notifyItemRemoved(1);
            }
            Vertical_NewsType_Model verticalNewsType_model = new Vertical_NewsType_Model();
            verticalNewsType_model.setNewsType(Config.NEWS_TYPE_25_POSTINGFAILEDRETRYPOST_VERTICAL_KEY);
            verticalNewsType_model.setNewsHasBeenPosted(1);
            verticalNewsType_model.setNewsId(Config.getCurrentDateTime2());
            verticalNewsType_model.setNewsText(mNewsTextEditText.getText().toString());
            verticalNewsType_model.setNewsTime(Config.getCurrentDateTime2());
            verticalNewsType_model.setNewsAddedItemId(addedSharesId);
            verticalNewsType_model.setNewsAddedItemIcon(addedSharesMyPass);
            verticalNewsType_model.setNewsAddedItemPrice(addedSharesPricePerShare);
            verticalNewsType_model.setNewsAddedItemQuantity(addedSharesQuantity);
            verticalNewsType_model.setNewsImagesLinksSeparatedBySpaces(allImagesSelected);
            verticalNewsType_model.setNewsImagesCount(String.valueOf(allImages));
            verticalNewsType_model.setNewsVideosLinksSeparatedBySpaces(selectedVideoPath);
            verticalNewsType_model.setRepostedItemPrice(repostingNewsId);

            if(!repostingNewsId.trim().equalsIgnoreCase("")){
                verticalNewsType_model.setRepostedNewsId(repostingNewsId);
                verticalNewsType_model.setRepostedItemPrice(repostingNewsId);
                verticalNewsType_model.setRepostedText(mNewsTextEditText.getText().toString());
                verticalNewsType_model.setRepostedItemId(addedSharesId);
                verticalNewsType_model.setRepostedItemPrice(addedSharesPricePerShare);
                Log.e("CreatePost", "HERE 1");

            } else {
                verticalNewsType_model.setRepostedNewsId("");
                verticalNewsType_model.setRepostedItemPrice("");
                verticalNewsType_model.setRepostedText("");
                verticalNewsType_model.setRepostedItemId("");
                verticalNewsType_model.setRepostedItemPrice("");
                Log.e("CreatePost", "HERE 2");
            }
            //--------------------------------------------------------//
            //verticalNewsType_model.setRepostedIcon(cursor.getString(verticalNewsTypeDatabaseAdapter.COL_NEWS_REPOST_ICON));
            //

            //ADDING STORY OBJECT TO LIST
            Vertical_NewsType_ListDataGenerator.getAllData().add(1, verticalNewsType_model);


            Intent goBackIntent = new Intent();
            goBackIntent.putExtra("result", 1);
            setResult(RESULT_OK, goBackIntent);
            Log.e("CreatePost", "HERE 3");
            finish();
        } else {
            Config.showToastType1(CreatePostActivity.this, getString(R.string.add_shares_an_image_or_a_video_t_least_say_something));
        }
    }

    @Override
    public void onFragmentInteraction(String[] sharesInfo) {
        addedSharesId = sharesInfo[0]; addedSharesQuantity  = sharesInfo[2];
        addedSharesPricePerShare  = sharesInfo[3];  addedSharesMyPass  = sharesInfo[4];
        mSharesAddedInfoTextView.setText(addedSharesQuantity + " " + getString(R.string.shares_added_to_post));
    }
}
