package temich.win.cammery.utils;

import com.google.common.base.Preconditions;

import java.util.Arrays;

public class TakenPhotoHolder {

    private static TakenPhotoHolder INSTANCE;

    public static TakenPhotoHolder getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new TakenPhotoHolder();
        }
        return INSTANCE;
    }

    private TakenPhoto mTakenPhoto;

    private TakenPhotoHolder() {

    }

    public void setPhoto(byte[] rawPhoto, ImageHelper.PhotoOrientation orientation, long timestamp) {
        Preconditions.checkNotNull(rawPhoto);
        Preconditions.checkArgument(timestamp >= 0);

        byte[] rawPhotoCopy = new byte[rawPhoto.length];
        System.arraycopy(rawPhoto, 0, rawPhotoCopy, 0, rawPhotoCopy.length);

        mTakenPhoto = new TakenPhoto(rawPhotoCopy, orientation, timestamp);
    }

    public TakenPhoto getPhoto(boolean resetTakenPhoto) {
        if (mTakenPhoto == null) {
            return null;
        }

        byte[] rawPhotoCopy = new byte[mTakenPhoto.rawPhoto.length];
        System.arraycopy(mTakenPhoto.rawPhoto, 0, rawPhotoCopy, 0, rawPhotoCopy.length);

        TakenPhoto takenPhotoCopy = new TakenPhoto(rawPhotoCopy, mTakenPhoto.orientation, mTakenPhoto.timestamp);

        if (resetTakenPhoto) {
            Arrays.fill(mTakenPhoto.rawPhoto, (byte) 0);
            mTakenPhoto = null;
        }

        return takenPhotoCopy;
    }

    // TODO: extending is cumbersome, use builder
    public static class TakenPhoto {
        public final byte[] rawPhoto;
        public final ImageHelper.PhotoOrientation orientation;
        public final long timestamp;

        private TakenPhoto(byte[] rawPhoto, ImageHelper.PhotoOrientation orientation, long timestamp) {
            this.rawPhoto = rawPhoto;
            this.orientation = orientation;
            this.timestamp = timestamp;
        }
    }

}
