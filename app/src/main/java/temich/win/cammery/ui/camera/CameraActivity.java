package temich.win.cammery.ui.camera;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import com.google.common.base.Preconditions;

import java.io.IOException;
import java.util.List;

import temich.win.cammery.R;
import temich.win.cammery.ui.gallery.GalleryActivity;
import temich.win.cammery.ui.viewer.PhotoViewerActivity;
import temich.win.cammery.utils.TakenPhotoHolder;
import temich.win.cammery.utils.ImageHelper;

public class CameraActivity extends AppCompatActivity implements SurfaceHolder.Callback {

    private static final String LOG_TAG = CameraActivity.class.getSimpleName();

    private static final int REQUEST_CAMERA_PERMISSION_CODE = 2525;
    private static final int REQUEST_STORAGE_PERMISSION_CODE = 2626;

    private Camera mCamera;

    private SurfaceView mCameraPreview;
    private FrameLayout mCameraPreviewContainer;

    private ImageHelper.PhotoOrientation mCurrentOrientation;

    private SensorManager mSensorManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Camera-based window
        getWindow().setFormat(PixelFormat.TRANSLUCENT);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setContentView(R.layout.activity_camera);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
    }

    @Override
    protected void onResume() {
        super.onResume();

        mSensorManager.registerListener(mOrientationListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(mOrientationListener, mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_NORMAL);

        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.CAMERA},
                    REQUEST_CAMERA_PERMISSION_CODE);
            return;
        }

        permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_STORAGE_PERMISSION_CODE);
            return;
        }

        // views initialization is allowed after all required permissions are granted
        initCameraAndViews();
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String permissions[],
            @NonNull int[] grantResults
    ) {
        switch (requestCode) {
            // exit app in case ANY of the permission nor granted
            case REQUEST_CAMERA_PERMISSION_CODE:
            case REQUEST_STORAGE_PERMISSION_CODE:
                if (grantResults.length == 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    CameraActivity.this.finish();
                }
                break;
            default:
                break;
        }
    }

    private void initCameraAndViews() {
        if (mCamera == null) {
            mCamera = getCameraInstance();
            if (mCamera == null) {
                finish();
            }
        }

        mCameraPreview = new SurfaceView(CameraActivity.this);
        mCameraPreview.getHolder().addCallback(this);
        mCameraPreview.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        // SurfaceView is added dynamically because of permission dialogs (Black screen)
        mCameraPreviewContainer = (FrameLayout) findViewById(R.id.camera_preview_container);
        mCameraPreviewContainer.addView(mCameraPreview);

        ImageButton btnTakePhoto = (ImageButton) findViewById(R.id.btn_take_photo);
        btnTakePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // empty ShutterCallback is needed to provide system sound when shutter button is clicked
                mCamera.takePicture(new Camera.ShutterCallback() {
                    @Override
                    public void onShutter() {
                    }
                }, null, mPictureCallback);
            }
        });
        // SurfaceView was added dynamically and overlapped this view
        btnTakePhoto.bringToFront();

        ImageButton btnGotoGallery = (ImageButton) findViewById(R.id.btn_goto_gallery);
        btnGotoGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryActivityIntent = new Intent(getApplicationContext(), GalleryActivity.class);
                galleryActivityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(galleryActivityIntent);
            }
        });
        // SurfaceView was added dynamically and overlapped this view
        btnGotoGallery.bringToFront();
    }

    private Camera getCameraInstance() {
        Camera camera = null;
        try {
            // attempt to get a Camera instance
            camera = Camera.open();
        } catch (Exception ex) {
            // Camera is not available (in use or does not exist)
        }
        // returns null if camera is unavailable
        return camera;
    }

    @Override
    protected void onPause() {
        if (mCameraPreviewContainer != null) {
            mCameraPreviewContainer.removeView(mCameraPreview);
        }

        mSensorManager.unregisterListener(mOrientationListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER));
        mSensorManager.unregisterListener(mOrientationListener, mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD));

        super.onPause();
    }

    @Override
    protected void onDestroy() {
        releaseCamera();

        super.onDestroy();
    }

    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
    }

    private Camera.PictureCallback mPictureCallback = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            Preconditions.checkNotNull(CameraActivity.this);
            Preconditions.checkNotNull(data);

            // in-memory storage for taken photo
            TakenPhotoHolder.getInstance().setPhoto(data, mCurrentOrientation, System.currentTimeMillis());

            Intent intentLaunchViewerActivity = new Intent(CameraActivity.this, PhotoViewerActivity.class);
            intentLaunchViewerActivity.putExtra(PhotoViewerActivity.EXTRA_KEY_VIEW_MODE, PhotoViewerActivity.MODE_PREVIEW);
            intentLaunchViewerActivity.putExtra(PhotoViewerActivity.EXTRA_KEY_TITLE, getString(R.string.taken_photo_title));

            startActivity(intentLaunchViewerActivity);
        }

    };

    private SensorEventListener mOrientationListener = new SensorEventListener() {
        private float[] mAccelerometerValues;
        private float[] mGeomagneticValues;

        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                mAccelerometerValues = event.values;
            }
            if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                mGeomagneticValues = event.values;
            }

            if ((mAccelerometerValues != null) && (mGeomagneticValues != null)) {
                float[] r = new float[9];
                float[] outR = new float[9];
                float[] orientation = new float[4];

                boolean success = SensorManager.getRotationMatrix(r, null, mAccelerometerValues, mGeomagneticValues);
                if (success) {
                    SensorManager.remapCoordinateSystem(r, SensorManager.AXIS_X, SensorManager.AXIS_Z, outR);
                    SensorManager.getOrientation(outR, orientation);
                    float yaw = (float) (Math.toDegrees(orientation[0]));
                    float pitch = (float) Math.toDegrees(orientation[1]);
                    float roll = (float) Math.toDegrees(orientation[2]);

                    if (Math.abs(ImageHelper.LANDSCAPE_ANGLE - Math.abs(roll)) < ImageHelper.DELTA_ANGLE) {
                        if (roll < 0) {
                            mCurrentOrientation = ImageHelper.PhotoOrientation.LANDSCAPE_LEFT;
                        } else {
                            mCurrentOrientation = ImageHelper.PhotoOrientation.LANDSCAPE_RIGHT;
                        }
                    } else {
                        mCurrentOrientation = ImageHelper.PhotoOrientation.PORTRAIT;
                    }
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };

    // region SurfaceHolder.Callback

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            mCamera.setPreviewDisplay(holder);
            // we need a portrait view, not landscape
            mCamera.setDisplayOrientation(90);

            // auto-focus - for simplicity
            Camera.Parameters params = mCamera.getParameters();
            params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            mCamera.setParameters(params);

            mCamera.startPreview();
        } catch (IOException ex) {
            Log.d(LOG_TAG, "Failure while setting camera preview: " + ex.getMessage());
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        // stop preview before making changes
        try {
            mCamera.stopPreview();
        } catch (Exception ex) {
            // ignore: tried to stop a non-existent preview
        }

        Camera.Parameters params = mCamera.getParameters();

        // get the device's supported sizes and pick the first, which is the largest
        List<Camera.Size> previewSizes = params.getSupportedPreviewSizes();
        Camera.Size selectedPreviewSize = previewSizes.get(0);
        params.setPreviewSize(selectedPreviewSize.width, selectedPreviewSize.height);

        List<Camera.Size> pictureSizes = params.getSupportedPictureSizes();
        Camera.Size selectedPictureSize = pictureSizes.get(0);
        params.setPictureSize(selectedPictureSize.width, selectedPictureSize.height);

        mCamera.setParameters(params);

        // start preview with new settings
        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
        } catch (Exception ex) {
            Log.e(LOG_TAG, "Failure while starting camera preview", ex);
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mCamera.setPreviewCallback(null);
        mCamera.stopPreview();
    }

    // endregion

}
