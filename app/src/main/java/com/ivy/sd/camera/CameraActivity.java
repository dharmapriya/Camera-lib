package com.ivy.sd.camera;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Camera;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Display;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.ivy.android.ivycamera.MainActivity;
import com.ivy.android.ivycamera.R;


import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class CameraActivity extends Activity implements CameraCallback,
        OnClickListener {
    private static final String photoFolderName = "IvyDist";
    private static final int RESULT_CANCEL = 0;
    private static final int RESULT_SAVE = 1;

    private FrameLayout cameraHolderFrame = null;
    private CameraSurface cameraSurface = null;
    private String mImagePath = null;
    private int mImageQuality = 50;
    private byte[] mImageData = null;
    private Bitmap mCapturedImage = null;
    private String savedpath="";
    private boolean isClicked=false;


    private ImageView captureBTN, cancelBTN, saveBTN, discardBTN;

    private OrientationEventListener orientationEventListener;
    public Bitmap cancelBTNbitmap, discardBTNbitmap, saveBTNbitmap;
    public int camera_picture_width, camera_picture_height;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.camera);
        camera_picture_width = 640;
        camera_picture_height = 480;

        /* listen for orientation changes
         and rotate the visible camera option images based on the orientation

         orientation - from 0 to 359
         */
        mImagePath = getExternalFilesDir(Environment.DIRECTORY_PICTURES) + "/"
                + photoFolderName;
        File folder = new File(mImagePath);
        if (!folder.exists()) {
            folder.mkdir();
        }
        orientationEventListener = new OrientationEventListener(this,
                SensorManager.SENSOR_DELAY_NORMAL) {

            @Override
            public void onOrientationChanged(int orientation) {
                if (orientation >= 269 && orientation < 315) {
                    orientation = 90;
                    rotateCameraIcons(orientation);
                } else if (orientation >= 179 && orientation < 225) {
                    orientation = 180;
                    rotateCameraIcons(orientation);
                } else if (orientation >= 89 && orientation < 135) {
                    orientation = 270;
                    rotateCameraIcons(orientation);
                } else if (orientation < 48 && !(orientation < 0)) {
                    orientation = 0;
                    rotateCameraIcons(orientation);
                }


            }

        };
        if (orientationEventListener.canDetectOrientation()) {
            orientationEventListener.enable();
        }

        Display display = getWindowManager().getDefaultDisplay();
         Log.e("image path", mImagePath);

        cameraHolderFrame = (FrameLayout) findViewById(R.id.cameraHolderFrame);
        cameraSurface = new CameraSurface(this, getResources().getConfiguration().orientation, cameraHolderFrame
                , (WindowManager) getSystemService(Context.WINDOW_SERVICE), display.getHeight(), camera_picture_width, camera_picture_height);

        captureBTN = (ImageView) findViewById(R.id.takepictureBTN);
        captureBTN.setOnClickListener(this);
        cancelBTN = (ImageView) findViewById(R.id.cancelBTN);
        cancelBTN.setOnClickListener(this);

        saveBTN = (ImageView) findViewById(R.id.saveBTN);
        saveBTN.setOnClickListener(this);
        discardBTN = (ImageView) findViewById(R.id.discardBTN);
        discardBTN.setOnClickListener(this);

        cancelBTNbitmap = ((BitmapDrawable) cancelBTN.getDrawable()).getBitmap();
        discardBTNbitmap = ((BitmapDrawable) discardBTN.getDrawable()).getBitmap();
        saveBTNbitmap = ((BitmapDrawable) saveBTN.getDrawable()).getBitmap();
    }

    //rotate camera option images to particular orientation
    public void rotateCameraIcons(int orientation) {
        if (Build.VERSION.SDK_INT < 11) {
            Bitmap cancelBTNbitmapnew = rotateImage(cancelBTNbitmap, orientation);
            Bitmap discardBTNbitmapnew = rotateImage(discardBTNbitmap, orientation);
            Bitmap saveBTNbitmapnew = rotateImage(saveBTNbitmap, orientation);
            cancelBTN.setImageBitmap(cancelBTNbitmapnew);
            discardBTN.setImageBitmap(discardBTNbitmapnew);
            saveBTN.setImageBitmap(saveBTNbitmapnew);
            cancelBTNbitmapnew.recycle();
            discardBTNbitmapnew.recycle();
            saveBTNbitmapnew.recycle();
        } else {
            captureBTN.setRotation(orientation);
            cancelBTN.setRotation(orientation);
            discardBTN.setRotation(orientation);
            saveBTN.setRotation(orientation);
        }
    }


    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
      super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    protected void onResume() {

        super.onResume();
        try {
            if (!backCameraExists()) {
                finish();
                Toast.makeText(CameraActivity.this, "There is no back camera on your device", Toast.LENGTH_SHORT).show();
            } else if (cameraSurface.isCameraUsebyApp()) {

                Toast.makeText(getApplicationContext(), "Camera busy , please try again",
                        Toast.LENGTH_SHORT).show();
                finish();
            } else {


                cameraHolderFrame.addView(cameraSurface, new LayoutParams(
                        LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
                cameraSurface.setCallback(this);
            }

        } catch (Exception e) {

        }
    }

    @Override
    protected void onStop() {

        super.onStop();
        cameraSurface.stopTakePicture();
    }

    boolean isClick = true;

    @Override
    public void onClick(View v) {

        if (isClick) {

            int i = v.getId();
            if (i == R.id.cancelBTN) {
                if(!isClicked) {
                    isClicked=true;
                    cameraSurface.stopTakePicture();
                    setResult(RESULT_CANCEL);
                    finish();

                }
            } else if (i == R.id.takepictureBTN) {
                if(!isClicked) {
                    isClicked = true;

                    cameraSurface.startTakePicture();

                }


            } else if (i == R.id.saveBTN) {
                if(!isClicked) {
                    isClicked = true;

                    try {
                       new SaveCapturedImageTask().execute();
                    } catch (Exception e) {

                    }
                }


            } else if (i == R.id.discardBTN) {
                //isClick = false;
                if(!isClicked) {
                    isClicked = true;
                    cameraSurface.startPreview();
                    mImageData = null;
                    mCapturedImage = null;
                    ButtonStatus(captureBTN, cancelBTN, saveBTN, discardBTN);
                }
                //isClick = true;

            } else {
            }

        }
    }

    /*
      Change camera option images after particular actions like capture, save
     */
    private void ButtonStatus(ImageView enableimgview1, ImageView enableimgview2
            , ImageView disableimgview1, ImageView disableimgview2) {

        enableimgview1.setVisibility(View.VISIBLE);
        enableimgview2.setVisibility(View.VISIBLE);
        disableimgview1.setVisibility(View.GONE);
        disableimgview2.setVisibility(View.GONE);
        isClicked=false;

    }

    @Override
    public String onGetVideoFilename() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void onJpegPictureTaken(byte[] data, Camera camera) {
        mImageData = data;
        byteToBitmap();
        ButtonStatus(saveBTN, discardBTN, captureBTN, cancelBTN);
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {

    }

    @Override
    public void onRawPictureTaken(byte[] data, Camera camera) {

    }

    @Override
    public void onShutter() {

    }

    private void byteToBitmap() {
        try {

            mCapturedImage = BitmapFactory.decodeByteArray(mImageData, 0, mImageData.length);
        } catch (Exception e) {

        }

    }

    private void storeCapturedImage() {
      //  Log.e("mCapturedImage ",mCapturedImage.getWidth()+"");
        if (mCapturedImage != null) {
            FileOutputStream fileOutputStream = null;
            try {
                savedpath=mImagePath+ "/" +now()+".jpg";
                Log.e("savedpath ",savedpath);
                fileOutputStream = new FileOutputStream(savedpath);

                BufferedOutputStream bos = new BufferedOutputStream(
                        fileOutputStream);

                //rotates the captured image to 90 degree if the screen orientation is portrait
                if (getResources().getConfiguration().orientation == 1) {
                    Matrix matrix = new Matrix();
                    matrix.postRotate(90);
                    mCapturedImage = Bitmap.createBitmap(mCapturedImage, 0, 0, mCapturedImage.getWidth(), mCapturedImage.getHeight(), matrix, true);
                }


                mCapturedImage
                        .compress(CompressFormat.JPEG, mImageQuality, bos);
                mCapturedImage.recycle();//need to recycle the bitmap to avoid OUTOFMEMORYERROR
                mCapturedImage = null;
                System.gc();
                bos.flush();
                bos.close();

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    //if api is less than 11 rotate the bitmap based on the orientation changes
    public Bitmap rotateImage(Bitmap img, int degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);

        Bitmap rotatedimg = Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, true);

        return rotatedimg;
    }

    private boolean checkCreateDir() {
        try {
            String folders[] = mImagePath.split(getResources().getString(
                    R.string.forwadslash));
            String path = "";
            for (String folderName : folders) {
                if (!folderName
                        .endsWith(getResources().getString(R.string.jpg))
                        && !folderName.endsWith(getResources().getString(
                        R.string.png))
                        && !folderName.endsWith(getResources().getString(
                        R.string.gif)))
                    path += folderName
                            + getResources().getString(R.string.forwadslash);
                File SDPath = new File(path);
                if (!SDPath.exists()) {
                    if (!SDPath.mkdir()) {
                        return false;
                    }
                }
            }
        } catch (Exception e) {
            // TODO: handle exception
        }
        return true;
    }

    //check for the back camera
    private boolean backCameraExists() {
        boolean isFrontExists = getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT);
        if (isFrontExists && (Camera.getNumberOfCameras() < 2)) {
            return false;
        }
        return true;
    }
    public void showAlert(String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(CameraActivity.this);
        builder.setMessage(msg);
        builder.setCancelable(false);
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                Intent i=new Intent(CameraActivity.this, MainActivity.class);
                i.putExtra("displayimg",savedpath);
                startActivity(i);
                finish();
            }
        });
        builder.show();

    }

    class SaveCapturedImageTask extends AsyncTask<String, Integer, Boolean> {

        @Override
        protected Boolean doInBackground(String... arg0) {
            try {

                Log.e("checkcreatedir" , " " + checkCreateDir()
                        + " " + mImagePath);
                if (mImagePath != null && checkCreateDir())
                    storeCapturedImage();

                cameraSurface.stopTakePicture();

                return Boolean.TRUE;
            } catch (Exception e) {
                e.printStackTrace();
                return Boolean.FALSE;
            }

        }

        protected void onPreExecute() {
           }

        protected void onProgressUpdate(Integer... progress) {

        }

        protected void onPostExecute(Boolean result) {
          setResult(RESULT_SAVE);
             showAlert(
                    getResources().getString(R.string.saved_successfully));


        }

    }

    public static String now() {
        String format="yyDHHmmss";
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.ENGLISH);
        return sdf.format(cal.getTime());
    }
}