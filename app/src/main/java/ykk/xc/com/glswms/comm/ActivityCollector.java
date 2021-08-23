package ykk.xc.com.glswms.comm;

import android.app.Activity;

import java.util.ArrayList;
import java.util.List;

/**
 * 此类用作收集和销毁activity的公共类
 */
public class ActivityCollector {
    public static List<Activity> activities = new ArrayList<Activity>();

    public static void addActivity(Activity activity) {
        activities.add(activity);
    }

    public static void removeActivity(Activity activity) {
        activities.remove(activity);
    }

    public static void finishAll() {
        for (Activity activity : activities) {
            if (!activity.isFinishing()) {
                activity.finish();
            }
        }
    }

}
