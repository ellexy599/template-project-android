package com.template.project.base;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

public class BaseActivity extends ActionBarActivity {

    private BaseApp mApp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mApp = (BaseApp) getApplicationContext();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /** Get the Application context */
    public BaseApp getApp() {
        return mApp;
    }

}
