package com.template.project.core.utils.image;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;

import com.template.project.core.utils.LogMe;
import com.template.project.core.utils.file.FileUtil;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;

/**
 * Utility class for setting the image file to imageview, displaying image picker dialog,
 * and handling activity result of choosing image.
 */
public class ImagePickerUtil {

    private static final String TAG = ImagePickerUtil.class.getSimpleName();

    public static final int REQUEST_TAKE_PHOTO = 1001, REQUEST_PICK_GALLERY = 1002;
    public static int REQUEST_CODE;

    private static File sFileStorage;

     /**
     * Intent picker for choosing picture for users' avatar.
     * Shows dialog of showing image picker "Take Photo", "Gallery".
     */
    public static void chooseImage(final Activity activity, File fileStorage) {
        sFileStorage = fileStorage;
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Choose Picture");
        builder.setItems(new CharSequence[]{"Take Photo", "Gallery"}, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.dismiss();
                switch (which) {
                    case 0:
                        ImagePickerUtil.REQUEST_CODE = ImagePickerUtil.REQUEST_TAKE_PHOTO;
                        Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        activity.startActivityForResult(takePicture, ImagePickerUtil.REQUEST_TAKE_PHOTO);
                        break;
                    case 1:
                        ImagePickerUtil.REQUEST_CODE = ImagePickerUtil.REQUEST_PICK_GALLERY;
                        Intent intent = new Intent();
                        intent.setType("image/*");
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        activity.startActivityForResult(Intent.createChooser(
                                intent, "Select Picture"), ImagePickerUtil.REQUEST_PICK_GALLERY);
                        break;
                }
            }
        });
        builder.create().show();
    }

    /**
     * Handle activity result of avatar choosing. This should be place on
     * the Activity onActivityResult.
     * @return true if image picking result is ok, false otherwise.
     */
    public static boolean handleOnActivityResultImagePicker(Activity activity, int requestCode,
                                                   int resultCode, Intent data) {
        boolean isImgFileToReturn = false;
        try {
            LogMe.d(TAG, "handleAvatarChooseIntent resultCode: " + resultCode
                        + "  requestCode: " + requestCode
                        + " REQUEST_CODE: " + ImagePickerUtil.REQUEST_CODE);
            if (resultCode == Activity.RESULT_OK) {
                if (ImagePickerUtil.REQUEST_CODE == ImagePickerUtil.REQUEST_PICK_GALLERY) {
                    LogMe.d(TAG, "ImagePickerUtil.REQUEST_CODE == ImagePickerUtil.REQUEST_PICK_GALLERY");
                    File fileFromGetPath = new File(ImagePickerUtil.getPath(activity,data.getData()));
                    FileUtil.copy(fileFromGetPath, sFileStorage);
                    isImgFileToReturn = true;
                } else {
                    LogMe.d(TAG, "ImagePickerUtil.REQUEST_CODE == ImagePickerUtil.REQUEST_TAKE_PHOTO");
                    Bitmap capturedImg = (Bitmap) data.getExtras().get("data");
                    capturedImg.setDensity(240);
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    capturedImg.compress(Bitmap.CompressFormat.JPEG, 100, bos);
                    byte[] bmpData = bos.toByteArray();
                    FileOutputStream fos = new FileOutputStream(sFileStorage);
                    fos.write(bmpData);
                    fos.flush();
                    fos.close();
                    isImgFileToReturn = true;
                }
            } else {
                isImgFileToReturn = false;
                LogMe.d(TAG, "resultCode: " + resultCode + " isImgFileToReturn: " + isImgFileToReturn);
            }
        } catch (Exception e) {
            LogMe.e(TAG, "handleAvatarChooseIntent ERROR " + e.toString());
        }

        return isImgFileToReturn;
    }

    public static void startImagePickGalleryIntent(Activity activity){
        Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        activity.startActivityForResult(i, REQUEST_PICK_GALLERY);
    }

    public static void startImagePickCameraIntent(Activity activity){
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        activity.startActivityForResult(intent, REQUEST_TAKE_PHOTO);
    }

    public static boolean handleOnActivityResultThrownIntent(Activity activity, int requestCode,
                                                          int resultCode, Intent data) {
        boolean isImgFileToReturn = false;
        if (requestCode == REQUEST_PICK_GALLERY
                && resultCode == Activity.RESULT_OK
                && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA };

            Cursor cursor = activity.getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();
            FileUtil.copy(new File(picturePath), sFileStorage);
            isImgFileToReturn = true;
        } else if (requestCode == REQUEST_TAKE_PHOTO
                && resultCode == Activity.RESULT_OK
                && null != data) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            // CALL THIS METHOD TO GET THE URI FROM THE BITMAP
            Uri tempUri = getImageUri(activity, photo);

            // CALL THIS METHOD TO GET THE ACTUAL PATH
            File finalFile = new File(getRealPathFromURI(activity, tempUri));
            FileUtil.copy(finalFile, sFileStorage);
            isImgFileToReturn = true;
        }
        return isImgFileToReturn;
    }

    private static Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    private static String getRealPathFromURI(Activity activity, Uri uri) {
        Cursor cursor = activity.getContentResolver().query(uri, null, null, null, null);
        cursor.moveToFirst();
        int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
        return cursor.getString(idx);
    }

    /**
     * Get a file path from a Uri. This will get the the path for Storage Access
     * Framework Documents, as well as the _data field for the MediaStore and
     * other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri The Uri to query.
     * @author paulburke
     */
    @SuppressLint("NewApi")
    public static String getPath(final Context context, final Uri uri) {
        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[] {
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {

            // Return the remote address
            if (isGooglePhotosUri(uri))
                return uri.getLastPathSegment();

            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri The Uri to query.
     * @param selection (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }
}
