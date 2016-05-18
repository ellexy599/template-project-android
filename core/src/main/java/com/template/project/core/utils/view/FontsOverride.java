package com.template.project.core.utils.view;

import android.content.Context;
import android.graphics.Typeface;

import java.lang.reflect.Field;

/**
 * Override overall font used by the app. This is based from
 * http://stackoverflow.com/questions/2711858/is-it-possible-to-set-font-for-entire-application/16883281#16883281
 * Font file should be under assets/fonts directory.
 *
 * To use this, replaceFont must be called in Application onCreate
 *
 * FontsOverride.setDefaultFont(this, "DEFAULT", "MyFontAsset.ttf");
 * FontsOverride.setDefaultFont(this, "MONOSPACE", "MyFontAsset2.ttf");
 * FontsOverride.setDefaultFont(this, "SERIF", "MyFontAsset3.ttf");
 * FontsOverride.setDefaultFont(this, "SANS_SERIF", "MyFontAsset4.ttf");
 *
 */
public final class FontsOverride {

    public static void setDefaultFont(Context context,
                                      String staticTypefaceFieldName, String fontAssetName) {
        final Typeface regular = Typeface.createFromAsset(context.getAssets(),
                fontAssetName);
        replaceFont(staticTypefaceFieldName, regular);
    }

    protected static void replaceFont(String staticTypefaceFieldName,
                                      final Typeface newTypeface) {
        try {
            final Field staticField = Typeface.class
                    .getDeclaredField(staticTypefaceFieldName);
            staticField.setAccessible(true);
            staticField.set(null, newTypeface);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
