package com.yc.intelligentlightingassistant.util;


import com.yc.intelligentlightingassistant.bean.DeviceInfo;

import java.text.DecimalFormat;

/**
 * 格式化显示的数据格式
 * <p>
 * Created by Chasen on 2017/9/10.
 */

public class FormatUtil {

    /**
     * 格式化频率显示
     *
     * @param frequency
     * @return
     */
    public static String formatFrequency(double frequency) {
        DecimalFormat df = new DecimalFormat("0.00");
        String fre = null;
        if (frequency > 1000) {
            fre = "频率:" + df.format(frequency / 1000.0) + " kHz";
        } else {
            fre = "频率:" + frequency + " Hz";
        }
        return fre;
    }

    /**
     * 格式化占空比显示
     */
    public static String formatDuty(double duty) {
        String s = "占空比：" + duty + "%";
        return s;
    }

    /**
     * 格式化电流显示
     */
    public static String formatVoltage(double voltage) {
        DecimalFormat df = new DecimalFormat("0.00");
        String s = "电压：" + df.format(voltage) + "V";
        return s;
    }

    /**
     * 格式化线路显示
     */
    public static String formatChannel(int channel) {
        return channel + "路";
    }

    /**
     * 格式化发送的数据
     */
    public static String formatSendInfo(DeviceInfo info) {
        DecimalFormat df1 = new DecimalFormat("00000"); //频率
        DecimalFormat df2 = new DecimalFormat("000"); //占空比
        DecimalFormat df3 = new DecimalFormat("00"); //路
        String str = "{" +
                "frequency:" + df1.format(info.getFrequency()) + "," +
                "duty:" + df2.format(info.getDuty()) + "," +
                "aisle:" + df3.format(info.getAisle()) + "}";
        return str;
    }


}
