package temich.win.cammery.ui.viewer;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import temich.win.cammery.R;
import temich.win.cammery.utils.TakenPhotoHolder;
import temich.win.cammery.utils.IOHelper;
import temich.win.cammery.utils.ImageHelper;

public class PhotoViewerFragment extends Fragment
        implements EditNameDialog.OnEditNameResultListener, IOHelper.OnFileOperationListener {

    static final String VIEW_TAG = PhotoViewerFragment.class.getSimpleName();

    static final String ARGS_KEY_MODE = "PhotoViewerFragment.KEY_MODE";
    static final String ARGS_KEY_FILEPATH = "PhotoViewerFragment.KEY_FILEPATH";

    private int mCurrentMode;

    private Bitmap mPhotoBitmap;
    private long mPhotoTimestamp;
    private String mPhotoFilePath;

    private TouchImageView mImageViewFullScreen;
    private View mViewProgress;
    private ImageButton mBtnCancel;
    private ImageButton mBtnOk;
    private ImageButton mBtnDelete;

    private ExecutorService mPhotoLoaderExecutor;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);

        mPhotoLoaderExecutor = Executors.newSingleThreadExecutor();

        mCurrentMode = getArguments().getInt(ARGS_KEY_MODE, PhotoViewerActivity.MODE_VIEW);
        mPhotoFilePath = getArguments().getString(ARGS_KEY_FILEPATH);
        if (mPhotoFilePath == null) {
            loadImageFromPreviewHolder();
        } else {
            loadImageFromFile(mPhotoFilePath);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView =  inflater.inflate(R.layout.fragment_photo_viewer, container, false);

        initViews(rootView);

        return rootView;
    }

    private void initViews(View rootView) {
        mImageViewFullScreen = (TouchImageView) rootView.findViewById(R.id.view_full_screen_image);

        mBtnCancel = (ImageButton) rootView.findViewById(R.id.btn_photo_viewer_cancel);
        mBtnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });

        mBtnOk = (ImageButton) rootView.findViewById(R.id.btn_photo_viewer_ok);
        mBtnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle fragmentArgs = new Bundle();
                String defaultPhotoName = constructDefaultPhotoName(mPhotoTimestamp);
                fragmentArgs.putString(EditNameDialog.ARGS_KEY_PHOTO_NAME, defaultPhotoName);

                DialogFragment dialog = new EditNameDialog();
                dialog.setArguments(fragmentArgs);
                dialog.setTargetFragment(PhotoViewerFragment.this, 0);
                dialog.show(getFragmentManager(), EditNameDialog.VIEW_TAG);
            }
        });

        mBtnDelete = (ImageButton) rootView.findViewById(R.id.btn_photo_viewer_delete);
        mBtnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IOHelper.deleteFileAsync(mPhotoFilePath, PhotoViewerFragment.this);
            }
        });

        mViewProgress = rootView.findViewById(R.id.view_full_screen_progress);
        if (mPhotoBitmap != null) {
            showPhoto();
        } else {
            showProgress();
        }
    }

    private void showProgress() {
        mViewProgress.setVisibility(View.VISIBLE);
    }

    private void hideProgress() {
        mViewProgress.setVisibility(View.GONE);
    }

    private void showActions() {
        switch (mCurrentMode) {
            case PhotoViewerActivity.MODE_VIEW:
                mBtnDelete.setVisibility(View.VISIBLE);
                break;
            case PhotoViewerActivity.MODE_PREVIEW:
                mBtnOk.setVisibility(View.VISIBLE);
                mBtnCancel.setVisibility(View.VISIBLE);
                break;
            default:
                throw new IllegalStateException("Unknown mode = " + mCurrentMode);
        }
    }

    private void showPhoto() {
        mImageViewFullScreen.setImageBitmap(mPhotoBitmap);
        mImageViewFullScreen.setZoom(1.0f);
        hideProgress();
        showActions();
    }

    private String constructDefaultPhotoName(long timestamp) {
        String formattedTimestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date(timestamp));
        return "IMG_" + formattedTimestamp;
    }

    @Override
    public void onDestroy() {
        mPhotoBitmap = null;

        mPhotoLoaderExecutor.shutdown();

        super.onDestroy();
    }

    private void loadImageFromPreviewHolder() {
        mPhotoLoaderExecutor.execute(new Runnable() {
            @Override
            public void run() {
                TakenPhotoHolder.TakenPhoto takenPhoto = TakenPhotoHolder.getInstance().getPhoto(true);
                if (takenPhoto == null) {
                    getActivity().finish();
                    return;
                }

                Bitmap processedPhotoBitmap = ImageHelper.processPhoto(takenPhoto.rawPhoto, takenPhoto.orientation);
                if (processedPhotoBitmap == null) {
                    BitmapDrawable drawable = (BitmapDrawable) getActivity().getResources().getDrawable(android.R.color.black);
                    mPhotoBitmap = drawable.getBitmap();
                } else {
                    mPhotoBitmap = processedPhotoBitmap;
                    mPhotoTimestamp = takenPhoto.timestamp;
                }

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showPhoto();
                    }
                });

            }
        });
    }

    private void loadImageFromFile(final String filePath) {
        mPhotoLoaderExecutor.execute(new Runnable() {
            @Override
            public void run() {
                byte[] rawPhoto = IOHelper.readFile(filePath);
                if (rawPhoto == null) {
                    getActivity().finish();
                    return;
                }

                Bitmap processedPhotoBitmap = ImageHelper.processPhoto(rawPhoto);
                if (processedPhotoBitmap == null) {
                    BitmapDrawable drawable = (BitmapDrawable) getActivity().getResources().getDrawable(android.R.color.black);
                    mPhotoBitmap = drawable.getBitmap();
                } else {
                    mPhotoBitmap = processedPhotoBitmap;
                }

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showPhoto();
                    }
                });
            }
        });
    }

    // region OnEditNameResultListener

    @Override
    public void onEditNameResult(String photoName) {
        showProgress();
        IOHelper.writeToFileAsync(photoName, mPhotoBitmap, PhotoViewerFragment.this);
    }

    @Override
    public void onCancelled() {

    }

    // endregion

    // region OnFileOperationListener

    @Override
    public void onSuccess() {
        getActivity().finish();
    }

    @Override
    public void onFailure() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                hideProgress();
            }
        });
        // TODO: notify user about IO failure
    }

    // endregion

}
