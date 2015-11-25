package com.template.project.core.utils.conn;

import com.template.project.core.utils.LogMe;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutionException;

/**
 * NetConnUtil is a helper class with methods checking mobile data or internet connectivity
 */
public class NetConnUtil {

    private final String TAG = NetConnUtil.class.getSimpleName();

    private Context mContext;
    private NetConnListener mNetConnListener;

    private static NetConnUtil sNetConn;

    public static NetConnUtil getInstance() {
        if (sNetConn == null)
            sNetConn = new NetConnUtil();
        return sNetConn;
    }

    /**
     * Check if network connection can connect to server. This will execute Asynctask.
     */
    public void checkConnectionToServer(Context mContext, String urlServerLink,
                                     NetConnListener netConnListener)
            throws InterruptedException, ExecutionException {
        this.mContext = mContext;
        this.mNetConnListener = netConnListener;
        new CheckInternetAsync().execute(new String[] {urlServerLink});
    }

    /**
     * Check network connectivity.
     * @return true if device is connected, false otherwise.
     */
    public boolean hasNetworkConnectivity(Context ctx) {
        ConnectivityManager cm = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnected()) {
            return true;
        }
        return false;
    }

    private class CheckInternetAsync extends AsyncTask<String, Void, Boolean> {
        private Boolean hasConn = false;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mNetConnListener.onPreExec();
        }

        @Override
        protected Boolean doInBackground(String... urlServerLink) {
            if(hasNetworkConnectivity(mContext)) {
                try {
                    LogMe.d(TAG, "url to reach: " + urlServerLink[0]);
                    URL url = new URL(urlServerLink[0]);
                    HttpURLConnection urlc = (HttpURLConnection) url.openConnection();
                    urlc.setConnectTimeout(10000);//set timeout to 15 seconds
                    urlc.connect();
                    int serverResponse = urlc.getResponseCode();

                    if (serverResponse == 200)
                        hasConn = true;
                    else
                        LogMe.e(TAG, "Server response code: " + serverResponse);
                } catch (Exception e) {
                    LogMe.e(TAG, e.toString());
                }

            } else
                LogMe.e(TAG, "No established connection");

            return hasConn;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            String message = (hasConn == true) ? TAG + " url reached" : " url could not be reached";
            mNetConnListener.onPostExec(hasConn, message);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }
    }

    /**
     * Callback if the host has been reached or
     * not with corresponding message of the request
     */
    public interface NetConnListener {
        void onPreExec();
        void onPostExec(boolean isHostReached, String message);
    }
}
