package com.template.project.base;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;

public class BaseFragment extends Fragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    /** Get Application Context from the Activity of this Fragment instance. */
    public BaseApp getApp() {
        return ((BaseActivity) getActivity()).getApp();
    }

}
