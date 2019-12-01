package net.dm73.plainpress.util;

import android.os.Build;
import android.view.Window;
import android.view.WindowManager;


public abstract class ActivityConfig {

    public static void setStatusBarTranslucent(Window window) {

        window.setBackgroundDrawable(null);

        if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)) {
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
    }
}
