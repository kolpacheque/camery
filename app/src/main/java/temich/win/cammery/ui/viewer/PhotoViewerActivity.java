package temich.win.cammery.ui.viewer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;

import com.google.common.base.Preconditions;

import temich.win.cammery.R;

public class PhotoViewerActivity extends AppCompatActivity {

    public static final String EXTRA_KEY_VIEW_MODE = "temich.win.cammery.extra.KEY_VIEW_MODE";
    public static final String EXTRA_KEY_TITLE = "temich.win.cammery.extra.KEY_TITLE";
    public static final String EXTRA_KEY_FILEPATH = "temich.win.cammery.extra.KEY_FILEPATH";

    public static final int MODE_VIEW = 1;
    public static final int MODE_PREVIEW = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_viewer);

        Intent launchIntent = getIntent();
        Preconditions.checkNotNull(launchIntent);

        String title = launchIntent.getStringExtra(EXTRA_KEY_TITLE);
        changeTitle(title);

        int mode = launchIntent.getIntExtra(EXTRA_KEY_VIEW_MODE, MODE_VIEW);

        String filePath = launchIntent.getStringExtra(EXTRA_KEY_FILEPATH);

        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager.findFragmentByTag(PhotoViewerFragment.VIEW_TAG) != null) {
            return;
        }

        Bundle fragmentArgs = new Bundle();
        fragmentArgs.putInt(PhotoViewerFragment.ARGS_KEY_MODE, mode);
        fragmentArgs.putString(PhotoViewerFragment.ARGS_KEY_FILEPATH, filePath);

        Fragment photoViewerFragment = new PhotoViewerFragment();
        photoViewerFragment.setArguments(fragmentArgs);

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.fragment_viewer_container, photoViewerFragment, PhotoViewerFragment.VIEW_TAG);
        fragmentTransaction.commit();
    }

    private void changeTitle(String title) {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar == null) {
            return;
        }
        actionBar.setTitle(title);
    }

}
