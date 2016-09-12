package temich.win.cammery.ui.viewer;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.common.base.Preconditions;

import temich.win.cammery.R;

public class EditNameDialog extends DialogFragment implements TextView.OnEditorActionListener {

    interface OnEditNameResultListener {
        void onEditNameResult(String inputText);
        void onCancelled();
    }

    static final String VIEW_TAG = EditNameDialog.class.getSimpleName();

    static final String ARGS_KEY_PHOTO_NAME = "EditNameDialog.KEY_PHOTO_NAME";

    private String mCurrentPhotoName;

    private EditText mEditPhotoName;

    public EditNameDialog() {
        // Empty constructor required for DialogFragment
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mCurrentPhotoName = getArguments().getString(ARGS_KEY_PHOTO_NAME);
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.fragment_edit_name_dialog, container);

        getDialog().requestWindowFeature(DialogFragment.STYLE_NO_TITLE);

        mEditPhotoName = (EditText) view.findViewById(R.id.et_edit_name);
        mEditPhotoName.setText(mCurrentPhotoName);
        // Show soft keyboard automatically
        mEditPhotoName.requestFocus();
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        mEditPhotoName.setOnEditorActionListener(this);

        Button btnCancel = (Button) view.findViewById(R.id.btn_edit_name_cancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OnEditNameResultListener fragment = (OnEditNameResultListener) getTargetFragment();
                fragment.onCancelled();
                EditNameDialog.this.dismiss();
            }
        });

        Button btnOk = (Button) view.findViewById(R.id.btn_edit_name_ok);
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCurrentPhotoName = mEditPhotoName.getText().toString().trim();
                String validationResult = validateNewPhotoName(mCurrentPhotoName);

                if (validationResult.equals(getString(R.string.success_valid_name))) {
                    // Return input text to fragment
                    OnEditNameResultListener fragment = (OnEditNameResultListener) getTargetFragment();
                    fragment.onEditNameResult(mEditPhotoName.getText().toString());
                    EditNameDialog.this.dismiss();
                } else {
                    mEditPhotoName.setError(validationResult);
                }
            }
        });

        return view;
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (EditorInfo.IME_ACTION_DONE == actionId) {
            mCurrentPhotoName = v.getText().toString().trim();
            String validationResult = validateNewPhotoName(mCurrentPhotoName);

            if (validationResult.equals(getString(R.string.success_valid_name))) {
                // Return new name to fragment
                OnEditNameResultListener fragment = (OnEditNameResultListener) getTargetFragment();
                fragment.onEditNameResult(mEditPhotoName.getText().toString());
                this.dismiss();
                return true;
            } else {
                mEditPhotoName.setError(validationResult);
            }
        }
        return false;
    }

    private String validateNewPhotoName(String photoName) {
        Preconditions.checkNotNull(photoName);

        if (photoName.isEmpty()) {
            return getString(R.string.error_empty_name);
        }

        return getString(R.string.success_valid_name);
    }
}
