package com.yc.intelligentlightingassistant.util;

import android.support.annotation.StringRes;
import android.widget.Toast;

import com.yc.intelligentlightingassistant.App;


/**
 * 用于显示Toast的工具类，简化操作
 */

public class ToastUtil {

    /**
     * 传入String资源id，显示
     * @param stringId 需要显示的文字的资源
     */
    public static void showToast(@StringRes int stringId) {
        Toast.makeText(App.getContext(), App.getContext().getResources().getString(stringId), Toast.LENGTH_SHORT).show();
    }

}
