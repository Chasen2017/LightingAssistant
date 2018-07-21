package com.yc.intelligentlightingassistant;

import android.app.Activity;
import android.content.Context;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;

import com.yc.intelligentlightingassistant.bean.DeviceInfo;
import com.yc.intelligentlightingassistant.db.DeviceInfoImpl;
import com.yc.intelligentlightingassistant.db.IDeviceInfoDao;
import com.yc.intelligentlightingassistant.util.FormatUtil;

import org.greenrobot.eventbus.EventBus;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.logging.Handler;

/**
 * 与socket有关的操作
 *
 * 发送完数据后，接收客户端发送来的数据
 */

public class SocketThread extends Thread {
    //发送设备某一路的信息
    public static final int SEND_SINGLE = 1;
    //发送设备16路的信息
    public static final int SEND_DEVICE = 2;

    private Socket socket;

    private DeviceInfo info;

    private int type;

    private Context context;

    private IDeviceInfoDao mDao;

    public SocketThread(Context context, int type, Socket socket, DeviceInfo info) {
        this.socket = socket;
        this.info = info;
        this.type = type;
        this.context = context;
        mDao = new DeviceInfoImpl(context);
    }

    @Override
    public void run() {
        try {
            //发送
            if (socket  == null) {
                return ;
            }
            String str = sendInfo(type, info);
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            bw.write(str+"\n");
            bw.flush();
            BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String s;

            while ( (s = br.readLine()) !=  null) {
                try {
                    handleStrToInfo(s);
                } catch (Exception e){
                    e.printStackTrace();
                    continue;
                }
                Log.d("TAG", "接收到的数据  "+s);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * 格式化发送的数据
     */
    private String sendInfo(int type, DeviceInfo info) {
        return FormatUtil.formatSendInfo(info);
    }

    /**
     * 接收数据
     */
    private void receiveInfo(int type, DeviceInfo info, String s) {
        //注：设置全部设备信息时，逐个socket发送，此时type设置为SEND_DEVICE即可
        switch (type) {
            case SocketThread.SEND_SINGLE:
                mDao.updateAisleInfo(info);
                break;
            case SocketThread.SEND_DEVICE:
                double[] voltage =  new double[16];
                for (int i = 1; i <= 16; i++) {
                    info.setVoltage(voltage[i]);
                    info.setAisle(i);
                    mDao.updateAisleInfo(info);
                }
                break;
            default:
                break;
        }
    }


    /**
     * 将接收到的数据转化为设备信息+心跳包处理
     */
    private void handleStrToInfo(String s) {
        String[] strings = s.replace("{", "").replace("}", "").split(",");
        double frequency = Double.parseDouble(strings[0]);
        double duty = Double.parseDouble(strings[1]);
        double voltage = Double.parseDouble(strings[2]);
        int aisle = Integer.parseInt(strings[3]);
        Log.e("receive info:" , "frequency:" +frequency + "duty:" + duty+"voltage:" + voltage+"aisle:" + aisle);
        final DeviceInfo info =  new DeviceInfo(socket.getInetAddress().toString().replace("/", ""), frequency, duty,
                voltage, aisle);
        // 更新16路
        if (aisle == 0) {
            mDao.updateDeviceInfo(info);

        }
        // 更新1路
        else {
            mDao.updateAisleInfo(info);
        }
        // 通知显示
        ((Activity)context).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                EventBus.getDefault().post(info);
            }
        });

    }

}
