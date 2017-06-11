package com.ghurabah.jemaah;

/**
 * Created by musa on 10/6/17.
 */

import android.os.Build;

public class AndroidSdkCheckerUtils {
    public static boolean isMarshmallow() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }
}