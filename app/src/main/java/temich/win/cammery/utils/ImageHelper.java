package temich.win.cammery.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.util.Log;

import com.google.common.base.Preconditions;

import javax.microedition.khronos.opengles.GL10;

public class ImageHelper {

    private static final String LOG_TAG = ImageHelper.class.getSimpleName();

    public enum PhotoOrientation {
        PORTRAIT,
        LANDSCAPE_LEFT,
        LANDSCAPE_RIGHT
    }

    private static final int DEFAULT_ROTATION_ANGLE = 90;

    public static final double PORTRAIT_ANGLE = 0.0;
    public static final double REVERSE_PORTRAIT_ANGLE = 180.0;
    public static final double LANDSCAPE_ANGLE = 90.0;
    public static final double DELTA_ANGLE = 45.0;

    public static Bitmap processPhoto(byte[] rawPhoto) {
        Preconditions.checkNotNull(rawPhoto);
        try {
            BitmapFactory.Options boundOptions = new BitmapFactory.Options();
            boundOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeByteArray(rawPhoto, 0, rawPhoto.length, boundOptions);

            int photoHeight = boundOptions.outHeight;
            int photoWidth = boundOptions.outWidth;
            Log.d(LOG_TAG, "processPhoto()::Height = " + photoHeight + " Width = " + photoWidth);

            BitmapFactory.Options configOptions = new BitmapFactory.Options();
            configOptions.inPreferredConfig = Bitmap.Config.RGB_565;
            Bitmap bitmap = BitmapFactory.decodeByteArray(rawPhoto, 0, rawPhoto.length, configOptions);

            return scaleIfNeeded(bitmap);
        } catch (OutOfMemoryError ex) {
            Log.e(LOG_TAG, "Failure while processing photo", ex);
        }
        return null;
    }

    public static Bitmap processPhoto(byte[] rawPhoto, PhotoOrientation photoOrientation) {
        // TODO: use EXIF
        // TODO: remove code duplicates
        Preconditions.checkNotNull(rawPhoto);
        try {
            int rotationAngle = DEFAULT_ROTATION_ANGLE;

            BitmapFactory.Options boundOptions = new BitmapFactory.Options();
            boundOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeByteArray(rawPhoto, 0, rawPhoto.length, boundOptions);

            int photoHeight = boundOptions.outHeight;
            int photoWidth = boundOptions.outWidth;
            Log.d(LOG_TAG, "processPhoto()::Height = " + photoHeight + " Width = " + photoWidth);

            BitmapFactory.Options configOptions = new BitmapFactory.Options();
            configOptions.inPreferredConfig = Bitmap.Config.RGB_565;
            Bitmap bitmap = BitmapFactory.decodeByteArray(rawPhoto, 0, rawPhoto.length, configOptions);

            switch (photoOrientation) {
                case PORTRAIT:
                    rotationAngle = 90;
                    break;
                case LANDSCAPE_LEFT:
                    rotationAngle = 0;
                    break;
                case LANDSCAPE_RIGHT:
                    rotationAngle = 180;
                    break;
                default:
                    break;
            }

            if (rotationAngle != 0) {
                Matrix matrix = new Matrix();
                matrix.setRotate(rotationAngle, (float) bitmap.getWidth() / 2, (float) bitmap.getHeight() / 2);
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, boundOptions.outWidth, boundOptions.outHeight, matrix, true);
            }
            return scaleIfNeeded(bitmap);
        } catch (OutOfMemoryError ex) {
            Log.e(LOG_TAG, "Failure while processing photo", ex);
        }
        return null;
    }

    private static Bitmap scaleIfNeeded(Bitmap original) {
        int width;
        int height;
        if (original.getHeight() > GL10.GL_MAX_TEXTURE_SIZE) {
            float scale = (float) GL10.GL_MAX_TEXTURE_SIZE / original.getHeight();
            height = (int) (original.getHeight() * scale);
            width = (height * original.getWidth()) / original.getHeight();
            if (original.getWidth() == width
                    && original.getHeight() == height) {
                return original;
            } else {
                Bitmap scaled = Bitmap.createScaledBitmap(original, width, height, false);
                original.recycle();
                return scaled;
            }
        }

        if (original.getWidth() > GL10.GL_MAX_TEXTURE_SIZE) {
            float scale = (float) GL10.GL_MAX_TEXTURE_SIZE / original.getWidth();
            width = (int) (original.getWidth() * scale);
            height = (original.getHeight() * width) / original.getWidth();
            if (original.getWidth() == width
                    && original.getHeight() == height) {
                return original;
            } else {
                Bitmap scaled = Bitmap.createScaledBitmap(original, width, height, false);
                original.recycle();
                return scaled;
            }
        }

        return original;
    }

}
