package temich.win.cammery.ui.gallery;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;

import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;

import temich.win.cammery.R;

public class GalleryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setContentView(R.layout.activity_gallery);

        // use 1/8th of the available memory for this memory cache
        final int cacheSize = (int) (Runtime.getRuntime().maxMemory() / 8L);

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext())
                .threadPriority(Thread.NORM_PRIORITY - 2)
                .denyCacheImageMultipleSizesInMemory()
                .memoryCache(new LruMemoryCache(cacheSize))
                .tasksProcessingOrder(QueueProcessingType.FIFO)
                .memoryCacheExtraOptions(400, 400)
                .build();
        // initialize ImageLoader with configuration
        ImageLoader.getInstance().init(config);

        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager.findFragmentByTag(GalleryFragment.VIEW_TAG) != null) {
            // no need to add the same fragment
            return;
        }

        Fragment galleryFragment = new GalleryFragment();

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.fragment_gallery_container, galleryFragment, GalleryFragment.VIEW_TAG);
        fragmentTransaction.commit();
    }

}
