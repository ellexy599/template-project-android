package com.template.project.core.utils.permission;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import com.template.project.core.R;
import com.template.project.core.utils.view.ToastUtil;

public class CheckPermissionUtil {

    public static final int REQ_PERMISSION_CAMERA = 35;

    public interface OnPermissionResult {
        void onPermissionGranted();

        void onPermissionDenied();
    }

    public static boolean checkCameraPermission(Activity ctx) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(ctx,
                    Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {

                if (ActivityCompat.shouldShowRequestPermissionRationale(ctx,
                        Manifest.permission.CAMERA)) {
                    ToastUtil.showShortToast(ctx,
                            ctx.getResources().getString(R.string.permission_request_camera));
                    requestForResultCameraPersmission(ctx);
                } else {
                    requestForResultCameraPersmission(ctx);
                }
            } else {
                return false;
            }
            return true;
        }

        return false;
    }

    /** Call this onRequestPermissionsResult */
    public static void checkPermissionResult(int requestCode, String permissions[],
                                             int[] grantResults, OnPermissionResult callback) {
        switch (requestCode) {
            case REQ_PERMISSION_CAMERA: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    callback.onPermissionGranted();
                } else {
                    callback.onPermissionDenied();
                }
                return;
            }
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    private static void requestForResultCameraPersmission(Activity activity) {
        ActivityCompat.requestPermissions(activity,
                new String[]{Manifest.permission.CAMERA},
                REQ_PERMISSION_CAMERA);
    }

}
