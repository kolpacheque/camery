package temich.win.cammery.ui.gallery;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageButton;

import temich.win.cammery.R;
import temich.win.cammery.ui.camera.CameraActivity;

public class GalleryFragment extends Fragment {

    static final String VIEW_TAG = GalleryFragment.class.getSimpleName();

    private GridView mGridView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_gallery, container, false);

        mGridView = (GridView) rootView.findViewById(R.id.grid_container);

        ImageButton btnGotoGallery = (ImageButton) rootView.findViewById(R.id.btn_goto_camera);
        btnGotoGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent cameraActivityIntent = new Intent(getActivity(), CameraActivity.class);
                cameraActivityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                startActivity(cameraActivityIntent);
            }
        });

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mGridView != null) {
            PhotoAdapter photoAdapter = new PhotoAdapter(getActivity());
            mGridView.setAdapter(photoAdapter);
            mGridView.setOnItemClickListener(photoAdapter);
        }
    }

}
