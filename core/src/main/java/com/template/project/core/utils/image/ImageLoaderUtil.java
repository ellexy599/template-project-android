package com.template.project.core.utils.image;

import android.content.Context;
import android.net.Uri;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.io.File;

/**
 * Utility class for setting image to ImageView with the use of Picasso library.
 */
public class ImageLoaderUtil {

    /**
     * Set image uri to ImageView.
     * @param ctx The activity context.
     * @param uri The URI of image file or resource.
     * @param iv The ImageView view where to load the image from uri.
     * @param errDrawableResId An error drawable to be used if the request image could not be loaded.
     */
    public static void setImage(Context ctx, Uri uri, ImageView iv, int errDrawableResId) {
        Picasso.with(ctx)
                .load(uri)
                .error(errDrawableResId)
                .skipMemoryCache()
                .into(iv);
    }

    /**
     * Set the image file to ImageView.
     * @param ctx The activity context.
     * @param imgFile The image File to load in ImageView.
     * @param iv The ImageView view where to load the image from uri.
     * @param errDrawableResId An error drawable to be used if the request image could not be loaded.
     */
    public static void setImage(Context ctx, File imgFile, ImageView iv, int errDrawableResId) {
        Picasso.with(ctx)
                .load(imgFile)
                .error(errDrawableResId)
                .skipMemoryCache()
                .into(iv);
    }

    /**
     * Set the image file to ImageView.
     * @param ctx The activity context.
     * @param remoteUrl The http url of file to display in ImageView.
     * @param iv The ImageView view where to load the image from uri.
     * @param errDrawableResId An error drawable to be used if the request image could not be loaded.
     */
    public static void setImage(Context ctx, String remoteUrl, ImageView iv, int errDrawableResId) {
        Picasso.with(ctx)
                .load(remoteUrl)
                .error(errDrawableResId)
                .skipMemoryCache()
                .into(iv);
    }

    /**
     * Set the drawable resource to ImageView.
     * @param ctx The activity context.
     * @param drawableResId The resource drawable id to display in ImageView.
     * @param iv The ImageView view where to load the image from uri.
     * @param errDrawableResId An error drawable to be used if the request image could not be loaded.
     */
    public static void setImage(Context ctx, int drawableResId, ImageView iv, int errDrawableResId) {
        Picasso.with(ctx)
                .load(drawableResId)
                .error(errDrawableResId)
                .skipMemoryCache()
                .into(iv);
    }

}
