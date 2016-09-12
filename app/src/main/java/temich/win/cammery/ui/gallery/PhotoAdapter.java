package temich.win.cammery.ui.gallery;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import temich.win.cammery.R;
import temich.win.cammery.ui.viewer.PhotoViewerActivity;
import temich.win.cammery.utils.IOHelper;

class PhotoAdapter extends BaseAdapter implements AdapterView.OnItemClickListener {

    private static final String LOG_TAG = PhotoAdapter.class.getSimpleName();

    private LayoutInflater mLayoutInflater;
    private List<File> mPhotoFiles;
    private WeakReference<Context> mContextRef;

    private DisplayImageOptions mDisplayImageOptions;
    private List<String> mPhotoUris = new ArrayList<>();

    PhotoAdapter(Context context) {
        mContextRef = new WeakReference<>(context);
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        File[] photoFiles = IOHelper.getPhotoFiles();
        if (photoFiles == null) {
            Log.e(LOG_TAG, "Failure while obtaining existing photos");
            return;
        }
        mPhotoFiles = Arrays.asList(photoFiles);
        Collections.sort(mPhotoFiles, new FileLastModifiedComparator());

        for (File photoFile : mPhotoFiles) {
            mPhotoUris.add(Uri.fromFile(photoFile).toString());
        }

        mDisplayImageOptions = new DisplayImageOptions.Builder()
                .showImageOnLoading(android.R.color.transparent)
                .showImageForEmptyUri(android.R.color.transparent)
                .showImageOnFail(android.R.color.transparent)
                .cacheInMemory(true)
                .cacheOnDisk(false)
                .considerExifParams(true)
                .bitmapConfig(Bitmap.Config.ALPHA_8)
                .build();
    }

    private Context getContext() {
        return mContextRef.get();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intentLaunchImageViewerActivity = new Intent(getContext(), PhotoViewerActivity.class);
        intentLaunchImageViewerActivity.putExtra(PhotoViewerActivity.EXTRA_KEY_VIEW_MODE, PhotoViewerActivity.MODE_VIEW);
        intentLaunchImageViewerActivity.putExtra(PhotoViewerActivity.EXTRA_KEY_TITLE, mPhotoFiles.get(position).getName());
        intentLaunchImageViewerActivity.putExtra(PhotoViewerActivity.EXTRA_KEY_FILEPATH, mPhotoFiles.get(position).getAbsolutePath());

        getContext().startActivity(intentLaunchImageViewerActivity);
    }

    @Override
    public int getCount() {
        return mPhotoFiles.size();
    }

    @Override
    public Object getItem(int position) {
        return mPhotoFiles.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            holder = new ViewHolder();
            convertView = mLayoutInflater.inflate(R.layout.item_photo_thumbnail, null);
            holder.photo = (SquareImageView) convertView.findViewById(R.id.photo_thumbnail);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        ImageLoader.getInstance().displayImage(mPhotoUris.get(position), holder.photo, mDisplayImageOptions);

        return convertView;
    }

    private static class ViewHolder {
        SquareImageView photo;
    }

    private class FileLastModifiedComparator implements Comparator<File> {

        @Override
        public int compare(File lhs, File rhs) {
            if (!lhs.exists() || !rhs.exists()) {
                return 0;
            }

            if (lhs.lastModified() < rhs.lastModified()) {
                return 1;
            } else if (lhs.lastModified() > rhs.lastModified()) {
                return -1;
            } else {
                return 0;
            }
        }

    }
}
