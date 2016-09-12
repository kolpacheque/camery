package temich.win.cammery.utils;

import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;

import com.google.common.base.Preconditions;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.Executors;

public class IOHelper {

    public interface OnFileOperationListener {
        void onSuccess();
        void onFailure();
    }

    private static final String LOG_TAG = IOHelper.class.getSimpleName();

    private static final String DEFAULT_STORAGE_DIR_NAME = "Cammery";

    private static final int DEFAULR_BUFFER_SIZE = 256 * 1024;

    public static void deleteFileAsync(
            final String fileName,
            final OnFileOperationListener listener
    ) {
        Preconditions.checkNotNull(fileName);

        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                File photoFile = new File(fileName);
                if (photoFile.delete()) {
                    listener.onSuccess();
                } else {
                    listener.onFailure();
                }
            }
        });
    }

    public static void writeToFileAsync(
            final String fileName,
            final Bitmap bitmap,
            final OnFileOperationListener listener
    ) {
        Preconditions.checkNotNull(fileName);
        Preconditions.checkNotNull(bitmap);

        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                File photoFile = createPhotoFile(fileName);
                if (photoFile == null) {
                    Log.e(LOG_TAG, "Failure while creating photo file");
                    return;
                }

                byte[] rawPhoto = prepareRawPhotoFromBitmap(bitmap);

                boolean writeSuccess = writeRawPhotoToFile(rawPhoto, photoFile);
                if (writeSuccess) {
                    listener.onSuccess();
                } else {
                    listener.onFailure();
                }
            }
        });

    }

    private static File createPhotoFile(String photoName){
        Preconditions.checkNotNull(photoName);

        File defaultStorageDir = getDefaultStorageDir();
        if (defaultStorageDir == null) {
            Log.e(LOG_TAG, "Storage directory is null");
            return null;
        }

        // Create a photo file
        return new File(defaultStorageDir.getPath() + File.separator + photoName + ".jpg");
    }

    private static File getDefaultStorageDir() {
        // To be safe, you should check that the SDCard is mounted
        String externalStorageState = Environment.getExternalStorageState();
        if (!externalStorageState.equals(Environment.MEDIA_MOUNTED)) {
            Log.e(LOG_TAG, "externalStorageState = " + externalStorageState);
            return null;
        }

        File defaultStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), DEFAULT_STORAGE_DIR_NAME);
        // Create the storage directory if it does not exist
        if (!defaultStorageDir.exists()) {
            if (!defaultStorageDir.mkdirs()) {
                Log.e(LOG_TAG, "Failure while creating storage directory: " + defaultStorageDir.getPath());
                return null;
            }
        }

        return defaultStorageDir;
    }

    private static byte[] prepareRawPhotoFromBitmap(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        return stream.toByteArray();
    }

    private static boolean writeRawPhotoToFile(byte[] rawPhoto, File photoFile) {
        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(photoFile);
            outputStream.write(rawPhoto);
            return true;
        } catch (IOException ex) {
            Log.e(LOG_TAG, "Failure while writing photo to file", ex);
            return false;
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException ex) {
                    Log.e(LOG_TAG, "Failure while closing output stream", ex);
                }
            }
        }
    }

    public static File[] getPhotoFiles() {
        File defaultStorageDir = getDefaultStorageDir();
        if (defaultStorageDir == null) {
            Log.e(LOG_TAG, "Storage directory is null");
            return null;
        }

        return defaultStorageDir.listFiles();
    }

    public static byte[] readFile(String filePath) {
        Preconditions.checkNotNull(filePath);

        FileInputStream inputStream;
        try {
            inputStream = new FileInputStream(filePath);
        } catch (FileNotFoundException ex) {
            Log.e(LOG_TAG, "Failure while accessing " + filePath, ex);
            return null;
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int read;
        byte[] buffer = new byte[DEFAULR_BUFFER_SIZE];
        try {
            while ((read = inputStream.read(buffer)) != -1) {
                baos.write(buffer, 0, read);
            }
            return baos.toByteArray();
        } catch (IOException ex) {
            Log.e(LOG_TAG, "Failure while reading " + filePath, ex);
            return null;
        } finally {
            try {
                inputStream.close();
            } catch (IOException ex) {
                Log.e(LOG_TAG, "Failure while closing input stream", ex);
            }

            baos.reset();
            try {
                baos.close();
            } catch (IOException ex) {
                Log.e(LOG_TAG, "Failure while closing output stream", ex);
            }
        }
    }

}
