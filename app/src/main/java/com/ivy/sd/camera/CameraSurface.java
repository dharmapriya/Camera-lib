package com.ivy.sd.camera;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.hardware.Camera.Size;
import android.util.AttributeSet;
import android.util.FloatMath;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;



import java.io.IOException;
import java.util.List;


public class CameraSurface extends SurfaceView implements
		SurfaceHolder.Callback {
	private Camera camera = null;
	private SurfaceHolder holder = null;
	private CameraCallback callback = null;
	private Camera.Parameters params;

	private int screenorientation;
	private int measuredwidth,measuredheight;
	//public Activity activity;

	private int currentZoomLevel=1;

	public float mDist=0.0f;

public int cameraheight=100,displayheight;
private FrameLayout cameraHolderFrame;
public WindowManager winManager;
	int camera_picture_width,camera_picture_height;
	private long maxMemory=0;

public Context context;
	public CameraSurface(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		initialize(context);
	}

	public CameraSurface(Context context,int screenorientation,FrameLayout cameraHolderFrame,
						 WindowManager winManager,int displayheight,int camera_picture_width,int camera_picture_height) {
		super(context);
		this.context=context;
		this.screenorientation=screenorientation;
		this.cameraHolderFrame=cameraHolderFrame;
		cameraheight=cameraHolderFrame.getHeight();
		this.winManager=winManager;

		this.displayheight=displayheight;
		this.camera_picture_width=camera_picture_width;
		this.camera_picture_height=camera_picture_height;
		initialize(context);
	}

	public CameraSurface(Context context, AttributeSet attrs) {
		super(context, attrs);

		initialize(context);
	}

	public void setCallback(CameraCallback callback) {
		this.callback = callback;
	}

	public void startPreview() {
		camera.startPreview();
	}

	public void startTakePicture() {
		camera.autoFocus(new AutoFocusCallback() {
			@Override
			public void onAutoFocus(boolean success, Camera camera) {
				takePicture();
			}
		});
	}

	public void stopTakePicture() {
		if (camera != null) {
			camera.setPreviewCallback(null);
		}
	}

	public void takePicture() {
		try {
			camera.takePicture(new ShutterCallback() {
				@Override
				public void onShutter() {
					if (null != callback)
						callback.onShutter();
				}
			},null /*new PictureCallback() {
				@Override
				public void onPictureTaken(byte[] data, Camera camera) {
					if (null != callback)
						callback.onRawPictureTaken(data, camera);
				}
			}*/, new PictureCallback() {
				@Override
				public void onPictureTaken(byte[] data, Camera camera) {
					if (null != callback)
						callback.onJpegPictureTaken(data, camera);
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
			// TODO: handle exception
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {

		// Now that the size is known, set up the camera parameters and begin
		// the preview.
		/*Camera.Parameters parameters = camera.getParameters();

		List<Size> sizes = parameters.getSupportedPreviewSizes();
		Size optimalSize = getOptimalPreviewSize(sizes, width, height);
		parameters.setPreviewSize(optimalSize.width, optimalSize.height);

		camera.setParameters(parameters);
		camera.startPreview();

		*/
		if (holder.getSurface() == null) {
			return;
		}

		try {
			camera.stopPreview();
		} catch (Exception e) {
		}

		try {

			camera.setPreviewDisplay(holder);
			params=camera.getParameters();
			Size optimalSize = getOptimalPreviewSize(camera.getParameters().getSupportedPreviewSizes(), width, height);
			//Size optimalSize=getBestPreviewSize(params);//getOptimalPreviewSize(camera.getParameters().getSupportedPreviewSizes(), width, height);
			/*if(screenorientation==1&&size.width>displayheight)
			{
				params.setPreviewSize(cameraHolderFrame.getWidth(), size.height);
			}
			else
			{
				params.setPreviewSize(size.width, size.height);
			}*/
			params.setPreviewSize(optimalSize.width, optimalSize.height);
			camera.setParameters(params);

			//change frame size based on the preview size
//			RelativeLayout.LayoutParams relativeparams=new RelativeLayout.LayoutParams(optimalSize.width,optimalSize.height);
//			relativeparams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
//			cameraHolderFrame.setLayoutParams(relativeparams);

		//	requestLayout();
			camera.startPreview();

		} catch (Exception e) {
		}

	}


	private Size getBestPreviewSize(Camera.Parameters parameters) {

    	Size bestSize = parameters.getSupportedPreviewSizes().get(0);
		for (Size size : parameters.getSupportedPreviewSizes()) {
			Log.i("preview sizes ",size.width+", "+size.height);
			if((size.width * size.height) >
					(bestSize.width * bestSize.height)){
				bestSize = size;
			}

		}
		Log.i("preview sizes best ",bestSize.width+", "+bestSize.height);
		return bestSize;
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		camera = Camera.open();
	try {

		camera.setPreviewDisplay(holder);

		if(screenorientation==1||screenorientation==0)//portrait-1 or undefined-0
		{
			setCameraDisplayOrientation(camera);
		}
			camera.setPreviewCallback(new Camera.PreviewCallback() {
				@Override
				public void onPreviewFrame(byte[] data, Camera camera) {
					if (null != callback)
						callback.onPreviewFrame(data, camera);
				}
			});
			setSizeForPhoto();
		} catch (IOException e) {
			e.printStackTrace();
		}
}


	public void setCameraDisplayOrientation(Camera mCamera)
	{
		if (mCamera == null)
		{

			return;
		}

		Camera.CameraInfo info = new Camera.CameraInfo();
		Camera.getCameraInfo(0, info);//get info of back camera
		int rotation = winManager.getDefaultDisplay().getRotation();

		int degrees = 0;

		switch (rotation)
		{
			case Surface.ROTATION_0: degrees = 0;break;
			case Surface.ROTATION_90: degrees = 90;break;
			case Surface.ROTATION_180: degrees = 180; break;
			case Surface.ROTATION_270: degrees = 270; break;
		}


		  // back-facing
		int	result = (info.orientation - degrees + 360) % 360;

		mCamera.setDisplayOrientation(result);
	}

	//set the nearest size for picture from the config
	private void setSizeForPhoto() {
		try {

			Camera.Parameters params = camera.getParameters();
			List<Size> l = params.getSupportedPictureSizes();
			Size startvalue=l.get(0);
			int index=0;
			int distance= Math.abs((startvalue.width*startvalue.height)-(camera_picture_width*camera_picture_height));
				for (int i = 0; i < l.size(); i++) {

					if(maxMemory<82331648)//if allocated memory of an app is less than 10MB use default width and height
					{
						if (l.get(i).height == 480) {

							index=i;

						}
					}
				else {
						int newdistance = Math.abs((l.get(i).width * l.get(i).height) - (camera_picture_width * camera_picture_height));
						if (newdistance < distance) {
							index = i;
							distance = newdistance;

						}
					}


			}
			params.setPictureSize(l.get(index).width, l.get(index).height);
			camera.setParameters(params);

		} catch (Exception e) {
			Log.e("Camera Size error.", e+"");
			e.printStackTrace();
		}
	}

	private Size getOptimalPreviewSize(List<Size> sizes, int w, int h) {
		final double ASPECT_TOLERANCE = 0.2;
		double targetRatio=(double)w / h;

		if (sizes == null) return null;

		Camera.Size optimalSize = null;
		double minDiff = Double.MAX_VALUE;

		int targetHeight = h;

		for (Camera.Size size : sizes) {
			double ratio = (double) size.width / size.height;
			if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
			if (Math.abs(size.height - targetHeight) < minDiff) {
				optimalSize = size;
				minDiff = Math.abs(size.height - targetHeight);
			}
		}

		if (optimalSize == null) {
			minDiff = Double.MAX_VALUE;
			for (Camera.Size size : sizes) {
				if (Math.abs(size.height - targetHeight) < minDiff) {
					optimalSize = size;
					minDiff = Math.abs(size.height - targetHeight);
				}
			}
		}
		Log.e("supported preview size ", optimalSize.width + ", " + optimalSize.height);
		return optimalSize;
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		if(camera!=null) {
			camera.stopPreview();
			camera.release();
			camera = null;
		}
	}


	//used for zoom controls
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// Get the pointer ID
		params = camera.getParameters();
		int action = event.getAction();

		// handle multi-touch events
		if (event.getPointerCount() > 1) {

			if (action == MotionEvent.ACTION_POINTER_DOWN) {
				mDist = getFingerSpacing(event);
			}

			else if (action == MotionEvent.ACTION_MOVE && params.isZoomSupported()) {
				camera.cancelAutoFocus();
				handleZoom(event, params);
			}
		}
		// handle single touch events
		else {

			if (action == MotionEvent.ACTION_UP) {

				handleFocus(event, params);
			}
		}
		return true;
	}

	private void handleZoom(MotionEvent event, Camera.Parameters params) {

		//get max zoom level of camera
		int maxZoom = params.getMaxZoom();
		//limit maximum zoom level to prevent the blurred preview
		if(maxZoom>20)
		{
			maxZoom-=10;
		}
		float newDist = getFingerSpacing(event);
		//zoom in
		if (newDist > mDist) {

			if((int)newDist%4==0) {
				if (currentZoomLevel < maxZoom) {
					currentZoomLevel++;
					params.setZoom(currentZoomLevel);
				}
			}
		}
		//zoom out
		else if (newDist < mDist) {

			if (currentZoomLevel > 0)
				if((int)newDist%4==0) {
					currentZoomLevel--;
					params.setZoom(currentZoomLevel);
				}
		}
		mDist = newDist;
		camera.setParameters(params);
	}


	public void handleFocus(MotionEvent event, Camera.Parameters params) {
		int pointerId = event.getPointerId(0);
		int pointerIndex = event.findPointerIndex(pointerId);
		// Get the pointer's current position
		float x = event.getX(pointerIndex);
		float y = event.getY(pointerIndex);

		List<String> supportedFocusModes = params.getSupportedFocusModes();
		if (supportedFocusModes != null && supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
			camera.autoFocus(new AutoFocusCallback() {
				@Override
				public void onAutoFocus(boolean b, Camera camera) {
					// currently set to auto-focus on single touch
				}
			});
		}
	}

	/** Determine the space between the first two fingers */
	private float getFingerSpacing(MotionEvent event) {

		float x = event.getX(0) - event.getX(1);
		float y = event.getY(0) - event.getY(1);
		return FloatMath.sqrt(x * x + y * y);

	}



	private void initialize(Context context) {
		holder = getHolder();
		holder.addCallback(this);
		holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		Runtime rt = Runtime.getRuntime();
		maxMemory = rt.maxMemory();

	}
	public boolean isCameraUsebyApp() {
	    Camera camera = null;
	    try {
	        camera = Camera.open();
	    } catch (RuntimeException e) {
	        return true;
	    } finally {
	        if (camera != null) camera.release();
	    }
	    return false;
	}


}