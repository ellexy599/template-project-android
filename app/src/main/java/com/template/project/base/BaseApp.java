package com.template.project.base;

import android.app.Application;
import com.template.project.core.AppConfiguration;

/**
 * Base Application context of the app
 */
public class BaseApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        AppConfiguration.ENABLE_LOG = true;
    }

}
