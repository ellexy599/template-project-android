package com.template.project.core.utils.file;

import android.content.Context;
import android.os.Environment;

import com.template.project.core.utils.LogMe;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

public class FileUtil {

    private static final String TAG = FileUtil.class.getSimpleName();

    /**
     * Get the application cache directory
     * @param context The context of the Application
     * @return The applications' cache directory in external if sdcard mounted; else interal
     * application cache directory
     */
    public static File getAppCacheDirectory(Context context) {
        File appCacheDir = context.getCacheDir();// use internal storage
        File externCacheDir = getExternalCacheDir(context);
        if(appCacheDir == null && isExternalMounted() && externCacheDir != null) {
            appCacheDir = externCacheDir;// use external storage if no internal storage
        }
        return appCacheDir;
    }

    /**
     * Create file in the applications' cache directory
     * @param context The context of the Application
     * @return The File from specific folder in the Application cache directory
     */
    public static File getFileInFolder(Context context, String filename) {
        File cacheDir = getAppCacheDirectory(context);
        File individualFile = new File(cacheDir, filename);
        if (!individualFile.exists()) {
            try {
                individualFile.createNewFile();
            } catch (Exception e) {
                LogMe.e(TAG, "ERROR getFileByFolder " + e.toString());
            }
        }
        return individualFile;
    }

    /**
     * Create folder in folder in app cache directory
     * @param context The context of the Application
     * @param folderName The folder name you want to create in app cache directory
     */
    public static File getFolderInAppCacheDir(Context context, String folderName) {
        File cacheDir = getAppCacheDirectory(context);
        File folderDir = new File(cacheDir, folderName);
        if(folderDir.mkdirs() || folderDir.isDirectory()) {
            return folderDir;
        } else {
            folderDir.mkdirs();
        }
        return folderDir;
    }

    /**
     * Check if external storage is mounted.
     * return True if external storage is mounted else False.
     */
    public static boolean isExternalMounted() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    /**
     * Create external cache directory of the Application. Note that external storage directory must be present.
     * @param context The context of the Application
     * @return The applications' external cache directory
     */
    public static File getExternalCacheDir(Context context) {
        File dataDir = new File(new File(Environment.getExternalStorageDirectory(), "Android"), "data");
        File appExternCacheDir = new File(
                new File(dataDir, context.getPackageName()),
                "cache");
        if (!appExternCacheDir.exists()) {
            if (!appExternCacheDir.mkdirs()) {
                LogMe.e("TAG", "Unable to create external cache directory");
                return null;
            }
            try {
                new File(appExternCacheDir, ".nomedia").createNewFile();
            } catch (IOException e) {
                LogMe.e("TAG", "Can't create \".nomedia\" file in application external cache directory");
                return null;
            }
        }
        return appExternCacheDir;
    }

    public static void copy(File src, File dst) {
        try {
            src.setReadable(true, true);
            dst.setReadable(true, true);
            InputStream in = new FileInputStream(src);
            OutputStream out = new FileOutputStream(dst);

            // Transfer bytes from in to out
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
        } catch (Exception e) {
            LogMe.e(TAG, "ERROR copy(): " + e.toString());
        }
    }

    public static void getFileFromPath(String filePath, File outputFile) {
        outputFile.setReadable(true, true);
        InputStream inputStream = null;
        OutputStream outputStream = null;

        try {
            // read this file into InputStream
            inputStream = new FileInputStream(new File(filePath));

            // write the inputStream to a FileOutputStream
            outputStream = new FileOutputStream(outputFile);

            int read = 0;
            byte[] bytes = new byte[1024];
            while ((read = inputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, read);
            }

            //close the stream
            if (inputStream != null) {
                inputStream.close();
            }
            if (outputStream != null) {
                outputStream.close();
            }
        } catch (IOException e) {
            LogMe.e(TAG, "ERROR " + e.toString());
        }
    }

    public static void getFileFromInputStream(File outputFile, InputStream inputStream) {
        OutputStream outputStream = null;
        try {
            // write the inputStream to a FileOutputStream
            outputStream = new FileOutputStream(outputFile);

            int read;
            byte[] bytes = new byte[1024];
            while ((read = inputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, read);
            }

            //close the stream
            if (inputStream != null) {
                inputStream.close();
                inputStream.reset();
            }
            if (outputStream != null) {
                outputStream.close();
                outputStream.flush();
            }
        } catch (Exception e) {
            LogMe.e(TAG, "ERROR " + e.toString());
        }
    }

    public static String readStream(InputStream iStream) {
        StringBuilder builder = new StringBuilder();
        try {
            InputStreamReader iStreamReader = new InputStreamReader(iStream);
            BufferedReader bReader = new BufferedReader(iStreamReader);
            String line = null;
            while ((line = bReader.readLine()) != null) {  //Read till end
                builder.append(line);
            }
            bReader.close();
            iStreamReader.close();
            iStream.close();
            iStream = null;
            iStreamReader = null;
            bReader = null;
            return builder.toString();
        } catch (Exception e) {
            LogMe.e(TAG, "ERROR readStream" + e.toString());
        }

        return builder.toString();
    }

    /**
     * Write String values to File.
     * @param ctx Activity context.
     * @param destFile File where to write the String values.
     */
    public static void writeToFile(Context ctx, String data, File destFile) {
        try {
            OutputStream outputStream = new FileOutputStream(destFile);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        } catch (Exception e) {
            LogMe.e(TAG, "writeToFile ERROR " + e.toString());
        }
    }

    /**
     * Read String values from File.
     * @param ctx Activity context.
     * @param fileToRead File to read or get String from.
     */
    public static String readFromFile(Context ctx, File fileToRead) {
        String ret = "";
        try {
            fileToRead.setReadable(true, true);
            InputStream inputStream = new FileInputStream(fileToRead);
            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();
                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append(receiveString);
                }
                inputStream.close();
                ret = stringBuilder.toString();
            }
        } catch (Exception e) {
            LogMe.e(TAG, "readFromFile ERROR " + e.toString());
        }
        return ret;
    }

}
