package com.fishpott.fishpott5.Fragments;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Size;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.fishpott.fishpott5.Interfaces.SetProfilePictureInterface;
import com.fishpott.fishpott5.Inc.Config;
import com.fishpott.fishpott5.Miscellaneous.Home;
import com.fishpott.fishpott5.R;
import com.fishpott.fishpott5.customphoto.cropoverlay.CropOverlayView;
import com.fishpott.fishpott5.customphoto.cropoverlay.edge.Edge;
import com.fishpott.fishpott5.customphoto.cropoverlay.utils.ImageViewUtil;
import com.fishpott.fishpott5.customphoto.cropoverlay.utils.Utils;
import com.fishpott.fishpott5.customphoto.customcropper.CropperView;
import com.fishpott.fishpott5.customphoto.customcropper.CropperViewAttacher;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;

public class CameraFragment extends Fragment implements SetProfilePictureInterface {

    private String TAG = "CameraFragment";
    private OnFragmentInteractionListener mListener;
    private ImageView mTakePictureIconImageView, mOpenGalleryImageView, mTurnFlashOnImageView, mTurnFlashOffImageView,
            mSwitchCameraImageView, mRetakePictureImageView;
    private TextureView mCameraPreviewHolderTextureView;
    private ConstraintLayout mCameraPreviewHolderConstraintLayout, mCropperHolderConstraintLayout;
    private Button mCropButton;
    private String imageFilePath = "";
    private boolean mFlashSupported;
    private boolean mFlashIsOn =  false;
    private CaptureRequest thisCaptureRequest;
    private CaptureRequest.Builder thisCaptureRequestBuilder;
    private CameraCaptureSession thisCameraCaptureSession;
    private HandlerThread backgroundThread;
    private Handler backgroundHandler;
    private String cameraId, mBackCameraId, mFrontCameraId;
    private Size previewSize;
    private CameraDevice thisCameraDevice;
    private CameraDevice.StateCallback stateCallback;
    private TextureView.SurfaceTextureListener surfaceTextureListener;
    private CameraManager cameraManager;
    private int cameraFacing = 1;
    private String[] permissions = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
    };
    public static final String TEMP_PHOTO_FILE_NAME = "temp_photo.jpg";
    private static final int REQUEST_CODE_PICK_GALLERY = 0x1;
    private final int IMAGE_MAX_SIZE = 1024;
    protected ImageView imgImage;
    private float minScale = 1f;
    private RelativeLayout relativeImage, mMainCroppingRelativeLayout;
    private CropperView cropperView;
    private CropOverlayView cropOverlayView;
    private File mFileTemp;
    private String mImagePath = null;
    private Uri mSaveUri = null;
    private Uri mImageUri = null;
    private ContentResolver mContentResolver;
    private Thread toggleCameraThread = null, switchFlashThread = null;
    private View view;

    // Required empty public constructor
    public CameraFragment() {}

    public static CameraFragment newInstance() {
        CameraFragment fragment = new CameraFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_camera, container, false);

        //BINDING VIEWS
        mCameraPreviewHolderTextureView = view.findViewById(R.id.fragment_camera_camera_preview_textview);
        mTakePictureIconImageView = view.findViewById(R.id.camera_fragment_picture_taker_icon);
        mOpenGalleryImageView = view.findViewById(R.id.camera_fragment_image_gallery_icon);
        mTurnFlashOnImageView = view.findViewById(R.id.camera_fragment_flashon_icon);
        mTurnFlashOffImageView = view.findViewById(R.id.camera_fragment_flashoff_icon);
        mSwitchCameraImageView = view.findViewById(R.id.camera_fragment_frontback_camera_rotate_icon);
        mRetakePictureImageView = view.findViewById(R.id.camera_fragment_delete_icon);
        mCameraPreviewHolderConstraintLayout = view.findViewById(R.id.fragment_camera_camera_preview_constraintLayout);
        mCropperHolderConstraintLayout = view.findViewById(R.id.fragment_camera_circlecrop_constraintLayout);
        mCropButton = view.findViewById(R.id.fragment_camera_crop_button);
        relativeImage = view.findViewById(R.id.fragment_camera_cropmargin_relativelayout);
        cropperView = view.findViewById(R.id.fragment_camera_cropperView);
        mMainCroppingRelativeLayout = view.findViewById(R.id.fragment_camera_overlayingcircle_relativelayout);
        cropOverlayView = view.findViewById(R.id.fragment_camera_cropoverlayview);
        imgImage = view.findViewById(R.id.fragment_camera_imageholder_imageview);

        mContentResolver = getActivity().getContentResolver();

        mOpenGalleryImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickImage();
            }
        });

        // MAKING SURE THE USER HAS GRANTED CAMERA AND FILE PERMISSION

                if(!Config.permissionsHaveBeenGranted(getActivity().getApplicationContext(), permissions)){

                    mTurnFlashOffImageView.setVisibility(View.INVISIBLE);
                    mTurnFlashOnImageView.setVisibility(View.VISIBLE);
                    Config.showToastType1(getActivity(), getString(R.string.fragment_camera_permission_to_access_camera_and_file_has_not_been_granted));
                    getActivity().onBackPressed();


                    // CLOSE CAMERA START
                    thisCameraCaptureSession = Config.closeCameraCaptureSession(thisCameraCaptureSession);
                    thisCameraDevice = Config.closeCameraDevice(thisCameraDevice);
                    // CLOSE CAMERA END

                    // CLOSE BACKGROUND THREAD START
                    backgroundThread = Config.closeBackgroundThread(backgroundHandler, backgroundThread);
                    backgroundHandler = Config.nullifyBackgroundThreadHandler(backgroundHandler);
                    //CLOSE BACKGROUND THREAD END
                }

                // DECLARING THE CAMERA MANAGER AND SETTING THE CAMERA CURRENT ORIENTATION AS FACING BACK
                cameraManager = (CameraManager) getActivity().getSystemService(Context.CAMERA_SERVICE);
                cameraFacing = CameraCharacteristics.LENS_FACING_BACK;

        // WHEN THE CANCEL ICON IS CLICKED, WE REOPEN THE CAMERA
        mRetakePictureImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCropperHolderConstraintLayout.setVisibility(View.INVISIBLE);
                openBackgroundThread();
                if (mCameraPreviewHolderTextureView.isAvailable()) {
                    setUpCamera();
                    openCamera();
                } else {
                    mCameraPreviewHolderTextureView.setSurfaceTextureListener(surfaceTextureListener);
                }
                mCameraPreviewHolderConstraintLayout.setVisibility(View.VISIBLE);
            }
        });

        // WHEN THE CROP BUTTON IS CLICKED
        mCropButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveAndUploadImage();
            }
        });

        // SETTING A LISTENER FOR THE CROPPER VIEW
        cropperView.addListener(new CropperViewAttacher.IGetImageBounds() {
            @Override
            public Rect getImageBounds() {
                return new Rect((int) Edge.LEFT.getCoordinate(), (int) Edge.TOP.getCoordinate(), (int) Edge.RIGHT.getCoordinate(), (int) Edge.BOTTOM.getCoordinate());
            }
        });

        // TAKING THE PICTURE WHEN THE USER CLICKS THE PICTURE TAKER ICON
        mTakePictureIconImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onTakePhoto();
            }
        });

        // WHEN THE FRONT-BACK CAMERA TOGGLE ICON IS CLICKED
        mSwitchCameraImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleCameraDisplayOrientation();
            }
        });

        // WHEN THE FLASH ICONS ARE CLICKED
        mTurnFlashOnImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchFlash();
            }
        });
        mTurnFlashOffImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchFlash();
            }
        });


        // THIS TEXTUREVIEW SURFACE LISTENER LISTENS FOR WHEN THE TEXTURE VIEW PREVIEWING THE CAMERA IS AVAILABLE
        surfaceTextureListener = new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
                setUpCamera();
                openCamera();
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int width, int height) {

            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

            }
        };

        // THE CALLBACK FOR THE STATE OF THE CAMERA
        stateCallback = new CameraDevice.StateCallback() {
            @Override
            public void onOpened(CameraDevice cameraDevice) {
                thisCameraDevice = cameraDevice;
                createPreviewSession();
            }

            @Override
            public void onDisconnected(CameraDevice cameraDevice) {
                cameraDevice.close();
                thisCameraDevice = null;
            }

            @Override
            public void onError(CameraDevice cameraDevice, int error) {
                cameraDevice.close();
                thisCameraDevice = null;
            }
        };
        return view;
    }

    // WHEN THE FRAGMENT RESUMES
    @Override
    public void onResume() {
        super.onResume();
        openBackgroundThread();
        if (mCameraPreviewHolderTextureView.isAvailable()) {
            setUpCamera();
            openCamera();
        } else {
            mCameraPreviewHolderTextureView.setSurfaceTextureListener(surfaceTextureListener);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        // CLOSE CAMERA START
        thisCameraCaptureSession = Config.closeCameraCaptureSession(thisCameraCaptureSession);
        thisCameraDevice = Config.closeCameraDevice(thisCameraDevice);
        // CLOSE CAMERA END

        // CLOSE BACKGROUND THREAD START
        backgroundThread = Config.closeBackgroundThread(backgroundHandler, backgroundThread);
        backgroundHandler = Config.nullifyBackgroundThreadHandler(backgroundHandler);
        //CLOSE BACKGROUND THREAD END
    }

    // WHEN THE FRAGMENT STOPS
    @Override
    public void onStop() {
        super.onStop();
        thisCameraCaptureSession = Config.closeCameraCaptureSession(thisCameraCaptureSession);
        thisCameraDevice = Config.closeCameraDevice(thisCameraDevice);
        if(backgroundThread != null){
            backgroundThread.interrupt();
            backgroundThread = null;
        }
        backgroundHandler = Config.nullifyBackgroundThreadHandler(backgroundHandler);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //Home.getRefWatcher(getActivity()).watch(this);
        if(toggleCameraThread != null){
            toggleCameraThread.interrupt();
            toggleCameraThread = null;
        }
        if(switchFlashThread != null){
            switchFlashThread.interrupt();
            switchFlashThread = null;
        }
        Config.unbindDrawables(view.findViewById(R.id.fragment_of_signupactivity_signupstart_mainConstraintLayout));
        Config.freeMemory();
    }



    // CREATING A SESSION FOR THE CAMERA PREVIEW
    private void createPreviewSession() {
        try {
            SurfaceTexture surfaceTexture = mCameraPreviewHolderTextureView.getSurfaceTexture();
            surfaceTexture.setDefaultBufferSize(previewSize.getWidth(), previewSize.getHeight());
            Surface previewSurface = new Surface(surfaceTexture);
            thisCaptureRequestBuilder = thisCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            thisCaptureRequestBuilder.addTarget(previewSurface);

            thisCameraDevice.createCaptureSession(Collections.singletonList(previewSurface),
                    new CameraCaptureSession.StateCallback() {

                        @Override
                        public void onConfigured(CameraCaptureSession cameraCaptureSession) {
                            if (thisCameraDevice == null) {
                                return;
                            }

                            try {
                                thisCaptureRequest = thisCaptureRequestBuilder.build();
                                thisCameraCaptureSession = cameraCaptureSession;
                                thisCameraCaptureSession.setRepeatingRequest(thisCaptureRequest, null, backgroundHandler);
                            } catch (CameraAccessException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onConfigureFailed(CameraCaptureSession cameraCaptureSession) {

                        }
                    }, backgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    // SETTING UP THE CAMERA
    private void setUpCamera() {
        try {
            for (String cameraId : cameraManager.getCameraIdList()) {
                CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraId);
                Boolean available = cameraCharacteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
                if (cameraCharacteristics.get(CameraCharacteristics.LENS_FACING) == cameraFacing) {
                    StreamConfigurationMap streamConfigurationMap = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                    //previewSize = streamConfigurationMap.getOutputSizes(SurfaceTexture.class)[0];
                    DisplayMetrics dm = new DisplayMetrics(); // comment out
                    getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);  // comment out
                    previewSize = chooseOptimalSize(streamConfigurationMap.getOutputSizes(SurfaceTexture.class), dm.widthPixels, dm.heightPixels);
                    this.cameraId = cameraId;
                    mBackCameraId = cameraId;
                    mFlashSupported = available == null ? false : available;

                } else {
                    mFrontCameraId = cameraId;
                }
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    // TOGGLING THE CAMERA ORIENTATION
    private void toggleCameraDisplayOrientation(){

        toggleCameraThread = new Thread(new Runnable() {
            public void run() {

                if(cameraId.equals(mBackCameraId)){
                    cameraId = mFrontCameraId;
                    // CLOSING THE CAMERA START
                    thisCameraCaptureSession = Config.closeCameraCaptureSession(thisCameraCaptureSession);
                    thisCameraDevice = Config.closeCameraDevice(thisCameraDevice);
                    // CLOSING THE CAMERA END
                    reopenCamera();
                } else if(cameraId.equals(mFrontCameraId)){
                    cameraId = mBackCameraId;
                    // CLOSING THE CAMERA START
                    thisCameraCaptureSession = Config.closeCameraCaptureSession(thisCameraCaptureSession);
                    thisCameraDevice = Config.closeCameraDevice(thisCameraDevice);
                    // CLOSING THE CAMERA END
                    reopenCamera();
                } else{
                    Config.showToastType1(getActivity(), getString(R.string.login_activity_an_unexpected_error_occured));
                }
            }
        });
        toggleCameraThread.start();

    }

    // TURNING THE FLASH ON
    public void switchFlash() {

        switchFlashThread = new Thread(new Runnable() {
            public void run() {

                try {
                    if (cameraId.equals(mBackCameraId)) {
                        if (mFlashSupported) {
                            if (mFlashIsOn) {
                                thisCaptureRequestBuilder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_OFF);
                                thisCameraCaptureSession.setRepeatingRequest(thisCaptureRequestBuilder.build(), null, null);
                                mFlashIsOn = false;
                                new Handler(Looper.getMainLooper()).post(new Runnable() {
                                    @Override
                                    public void run() {
                                        mTurnFlashOffImageView.setVisibility(View.INVISIBLE);
                                        mTurnFlashOnImageView.setVisibility(View.VISIBLE);
                                    }
                                });
                            } else {
                                thisCaptureRequestBuilder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_TORCH);
                                thisCameraCaptureSession.setRepeatingRequest(thisCaptureRequestBuilder.build(), null, null);
                                mFlashIsOn = true;
                                new Handler(Looper.getMainLooper()).post(new Runnable() {
                                    @Override
                                    public void run() {
                                        mTurnFlashOnImageView.setVisibility(View.INVISIBLE);
                                        mTurnFlashOffImageView.setVisibility(View.VISIBLE);
                                    }
                                });
                            }
                        }
                    }
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }
            }
        });

        switchFlashThread.start();
    }


    // OPENING THE CAMERA
    private void openCamera() {
        if(getActivity().getApplicationContext() != null){
            try {
                if (ActivityCompat.checkSelfPermission(getActivity().getApplicationContext(), android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    cameraManager.openCamera(cameraId, stateCallback, backgroundHandler);
                }
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        } else {
            Config.showToastType1(getActivity(), getString(R.string.login_activity_an_unexpected_error_occured));
        }

    }

    public void reopenCamera() {
        if (mCameraPreviewHolderTextureView.isAvailable()) {
            openCamera();
        } else {
            mCameraPreviewHolderTextureView.setSurfaceTextureListener(surfaceTextureListener);
        }

    }

    // OPENING THE BACKGROUND THREAD
    private void openBackgroundThread() {
        backgroundThread = new HandlerThread("camera_background_thread");
        backgroundThread.start();
        backgroundHandler = new Handler(backgroundThread.getLooper());
    }

    // USED TO SEND INFORMATION BACK TO THE CALLING ACTIVITY
    public void sendBackInfo(Uri image) {
        if (mListener != null) {
            mListener.onFragmentInteraction(image);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnFragmentInteractionListener");
        }
    }

    // TAKING THE PICTURE
    public void onTakePhoto() {
                lock();

                FileOutputStream outputPhoto = null;
                try {

                    File capturedImageFile = createTempFile();
                    mFileTemp = capturedImageFile;
                    imageFilePath = capturedImageFile.getAbsolutePath();
                    Log.e("TAKE-PHOTO", "imageFilePath : " + imageFilePath);
                    if(Config.fileExists(imageFilePath)){
                        Log.e("TAKE-PHOTO", "1-fileExists : TRUE");
                    }else {
                        Log.e("TAKE-PHOTO", "1-fileExists : FALSE");
                    }
                    outputPhoto = new FileOutputStream(capturedImageFile);
                    mCameraPreviewHolderTextureView.getBitmap().compress(Bitmap.CompressFormat.JPEG, 100, outputPhoto);

                } catch (Exception e) {
                    e.printStackTrace();

                    if(Config.fileExists(imageFilePath)){
                        Log.e("TAKE-PHOTO", "2-fileExists : TRUE");
                    }else {
                        Log.e("TAKE-PHOTO", "2-fileExists : FALSE");
                    }
                } finally {

                    unlock();
                    try {
                        if (outputPhoto != null) {
                            outputPhoto.close();
                            showCropping();
                            // CLOSE CAMERA START
                            thisCameraCaptureSession = Config.closeCameraCaptureSession(thisCameraCaptureSession);
                            thisCameraDevice = Config.closeCameraDevice(thisCameraDevice);
                            // CLOSE CAMERA END

                            // CLOSE BACKGROUND THREAD START
                            backgroundThread = Config.closeBackgroundThread(backgroundHandler, backgroundThread);
                            backgroundHandler = Config.nullifyBackgroundThreadHandler(backgroundHandler);
                            //CLOSE BACKGROUND THREAD END
                            mCameraPreviewHolderConstraintLayout.setVisibility(View.INVISIBLE);
                            mCropperHolderConstraintLayout.setVisibility(View.VISIBLE);

                            mSaveUri = Uri.fromFile(mFileTemp);
                            mImageUri = Uri.fromFile(mFileTemp);

                            if(mFileTemp.exists() && mSaveUri != null && mImageUri != null){
                                init();
                            } else {
                                Config.showToastType1(getActivity(), getString(R.string.login_activity_an_unexpected_error_occured));
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
    }

    //TO PROVIDE THE USER WITH A NICE PICTURE TAKING EXPERIENCE, WE LOCK AND UNLOCK THE CAMERA SCREEN DURING A PICTURE TAKING PERIOD
    private void lock() {
        try {
            thisCameraCaptureSession.capture(thisCaptureRequestBuilder.build(), null, backgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void unlock() {
        try {
            thisCameraCaptureSession.setRepeatingRequest(thisCaptureRequestBuilder.build(), null, backgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    //OPTIMIZING TEXTURE VIEW SIZE ON DEVICES
    private Size chooseOptimalSize(Size[] outputSizes, int width, int height) {
        double preferredRatio = height / (double) width;
        Size currentOptimalSize = outputSizes[0];
        double currentOptimalRatio = currentOptimalSize.getWidth() / (double) currentOptimalSize.getHeight();
        for (Size currentSize : outputSizes) {
            double currentRatio = currentSize.getWidth() / (double) currentSize.getHeight();
            if (Math.abs(preferredRatio - currentRatio) <
                    Math.abs(preferredRatio - currentOptimalRatio)) {
                currentOptimalSize = currentSize;
                currentOptimalRatio = currentRatio;
            }
        }
        return currentOptimalSize;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri imageUri);
    }



    // COPYING A STREAM OF DATA
    private static void copyStream(InputStream input, OutputStream output) throws IOException {
        byte[] buffer = new byte[512];
        int bytesRead;
        while ((bytesRead = input.read(buffer)) != -1) {
            output.write(buffer, 0, bytesRead);
        }
    }


    public File createTempFile() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            mFileTemp = new File(Environment.getExternalStorageDirectory(), TEMP_PHOTO_FILE_NAME);
        } else {
            mFileTemp = new File(getActivity().getFilesDir(), TEMP_PHOTO_FILE_NAME);
        }

        return mFileTemp;
    }




    @Override
    public void makeLayoutSquare() {
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, width);
        relativeImage.setLayoutParams(params);
    }


    @Override
    public void hideCropping() {
        imgImage.setVisibility(View.VISIBLE);
        cropperView.setVisibility(View.GONE);
        cropOverlayView.setVisibility(View.GONE);
        mMainCroppingRelativeLayout.setVisibility(View.GONE);
    }

    @Override
    public void showCropping() {
        imgImage.setVisibility(View.GONE);
        cropperView.setVisibility(View.VISIBLE);
        cropOverlayView.setVisibility(View.GONE);
        mMainCroppingRelativeLayout.setVisibility(View.VISIBLE);
    }


    private void init() {
        showCropping();
        Bitmap b = getBitmap(mImageUri);
        Drawable bitmap = new BitmapDrawable(getResources(), b);
        int h = bitmap.getIntrinsicHeight();
        int w = bitmap.getIntrinsicWidth();
        final float cropWindowWidth = Edge.getWidth();
        final float cropWindowHeight = Edge.getHeight();
        if (h <= w) {
            minScale = (cropWindowHeight + 1f) / h;
        } else if (w < h) {
            minScale = (cropWindowWidth + 1f) / w;
        }

        cropperView.setMaximumScale(minScale * 9);
        cropperView.setMediumScale(minScale * 6);
        cropperView.setMinimumScale(minScale);
        cropperView.setImageDrawable(bitmap);
        cropperView.setScale(minScale);


    }

    private Bitmap getBitmap(Uri uri) {
        InputStream in = null;
        Bitmap returnedBitmap = null;
        try {
            in = mContentResolver.openInputStream(uri);
            //Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(in, null, o);
            in.close();
            int scale = 1;
            if (o.outHeight > IMAGE_MAX_SIZE || o.outWidth > IMAGE_MAX_SIZE) {
                scale = (int) Math.pow(2, (int) Math.round(Math.log(IMAGE_MAX_SIZE / (double) Math.max(o.outHeight, o.outWidth)) / Math.log(0.5)));
            }

            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            in = mContentResolver.openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(in, null, o2);
            in.close();
            returnedBitmap = fixOrientationBugOfProcessedBitmap(bitmap);
            return returnedBitmap;
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
        }
        return null;
    }

    public static int getCameraPhotoOrientation(@NonNull Context context, Uri imageUri) {
        int rotate = 0;
        try {
            context.getContentResolver().notifyChange(imageUri, null);
            ExifInterface exif = new ExifInterface(
                    imageUri.getPath());
            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotate = 270;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotate = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotate = 90;
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rotate;
    }


    private Bitmap fixOrientationBugOfProcessedBitmap(Bitmap bitmap) {
        try {
            if (getCameraPhotoOrientation(getActivity().getApplicationContext(), Uri.parse(mFileTemp.getPath())) == 0) {
                return bitmap;
            } else {
                Matrix matrix = new Matrix();
                matrix.postRotate(getCameraPhotoOrientation(getActivity().getApplicationContext(), Uri.parse(mFileTemp.getPath())));
                // recreate the new Bitmap and set it back
                return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    private void saveAndUploadImage() {
        boolean saved = saveOutput();
        imgImage.setImageBitmap(getBitmap(mImageUri));

        if (saved) {
            hideCropping();
            sendBackInfo(mSaveUri);
            getActivity().onBackPressed();
            mCropperHolderConstraintLayout.setVisibility(View.INVISIBLE);
            mCameraPreviewHolderConstraintLayout.setVisibility(View.VISIBLE);
        } else {
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_PICK_GALLERY && resultCode == getActivity().RESULT_OK){
            mCameraPreviewHolderConstraintLayout.setVisibility(View.INVISIBLE);
            createTempFile();
            showCropping();
            try {
                InputStream inputStream = getActivity().getContentResolver().openInputStream(data.getData());
                FileOutputStream fileOutputStream = new FileOutputStream(mFileTemp);
                copyStream(inputStream, fileOutputStream);
                fileOutputStream.close();
                inputStream.close();
                mImagePath = mFileTemp.getPath();
                mSaveUri = Utils.getImageUri(mImagePath);
                mImageUri = Utils.getImageUri(mImagePath);
                init();
                mCropperHolderConstraintLayout.setVisibility(View.VISIBLE);
            } catch (Exception e) {
                Config.showToastType1(getActivity(), getString(R.string.login_activity_an_unexpected_error_occured));
                openBackgroundThread();
                mCropperHolderConstraintLayout.setVisibility(View.INVISIBLE);
                mCameraPreviewHolderConstraintLayout.setVisibility(View.VISIBLE);
                if (mCameraPreviewHolderTextureView.isAvailable()) {
                    setUpCamera();
                    openCamera();
                } else {
                    mCameraPreviewHolderTextureView.setSurfaceTextureListener(surfaceTextureListener);
                }
            }

        }
    }

    public void pickImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT).setType("image/*");
        try {
            startActivityForResult(intent, REQUEST_CODE_PICK_GALLERY);
            Log.e(TAG, "Camera Closed");
        } catch (ActivityNotFoundException e) {
        }
    }


    private boolean saveOutput() {
        Bitmap croppedImage = getCroppedImage();
        if (mSaveUri != null) {
            OutputStream outputStream = null;
            try {
                outputStream = mContentResolver.openOutputStream(mSaveUri);
                if (outputStream != null) {
                    croppedImage.compress(Bitmap.CompressFormat.JPEG, 80, outputStream);
                }
            } catch (IOException ex) {
                ex.printStackTrace();
                return false;
            } finally {
                if (outputStream != null) {
                    try {
                        outputStream.close();
                    } catch (Throwable t) {
                    }
                }
            }
        } else {
            return false;
        }
        croppedImage.recycle();
        return true;
    }

    private Bitmap getCurrentDisplayedImage() {
        Bitmap result = Bitmap.createBitmap(cropperView.getWidth(), cropperView.getHeight(), Bitmap.Config.RGB_565);
        Canvas c = new Canvas(result);
        cropperView.draw(c);
        return result;
    }

    public Bitmap getCroppedImage() {
        Bitmap mCurrentDisplayedBitmap = getCurrentDisplayedImage();
        Rect displayedImageRect = ImageViewUtil.getBitmapRectCenterInside(mCurrentDisplayedBitmap, cropperView);

        // Get the scale_bigout_and_small_in factor between the actual Bitmap dimensions and the
        // displayed dimensions for width.
        float actualImageWidth = mCurrentDisplayedBitmap.getWidth();
        float displayedImageWidth = displayedImageRect.width();
        float scaleFactorWidth = actualImageWidth / displayedImageWidth;

        // Get the scale_bigout_and_small_in factor between the actual Bitmap dimensions and the
        // displayed dimensions for height.
        float actualImageHeight = mCurrentDisplayedBitmap.getHeight();
        float displayedImageHeight = displayedImageRect.height();
        float scaleFactorHeight = actualImageHeight / displayedImageHeight;

        // Get crop window position relative to the displayed image.
        float cropWindowX = Edge.LEFT.getCoordinate() - displayedImageRect.left;
        float cropWindowY = Edge.TOP.getCoordinate() - displayedImageRect.top;
        float cropWindowWidth = Edge.getWidth();
        float cropWindowHeight = Edge.getHeight();

        // Scale the crop window position to the actual size of the Bitmap.
        float actualCropX = cropWindowX * scaleFactorWidth;
        float actualCropY = cropWindowY * scaleFactorHeight;
        float actualCropWidth = cropWindowWidth * scaleFactorWidth;
        float actualCropHeight = cropWindowHeight * scaleFactorHeight;

        // Crop the subset from the original Bitmap.
        return Bitmap.createBitmap(mCurrentDisplayedBitmap, (int) actualCropX, (int) actualCropY, (int) actualCropWidth, (int) actualCropHeight);
    }

}
