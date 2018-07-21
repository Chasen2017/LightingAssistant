package com.yc.intelligentlightingassistant.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.yc.intelligentlightingassistant.bean.DeviceInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * 设备信息数据库常用操作实现类
 */

public class DeviceInfoImpl implements IDeviceInfoDao {

    private DataBaseOpenHelper mHelper;

    public DeviceInfoImpl(Context context) {
        mHelper = DataBaseOpenHelper.getInstance(context);
    }

    // 插入一路信息
    @Override
    public void insertSingleInfo(DeviceInfo info) {
        SQLiteDatabase db = mHelper.getWritableDatabase();
        if (info != null && !isExists(info.getIp())) {
            String sql = "insert into device_info(ip, frequency, duty, voltage, aisle, name) values (？, ?, ?, ?, ?, ?)";
            db.execSQL(sql,
                    new Object[]{info.getIp(), info.getFrequency(), info.getDuty(),
                    info.getVoltage(), info.getAisle(), info.getName()});
        }
    }

    // 插入16路信息
    @Override
    public void insertInfo(String ip) {
        SQLiteDatabase db = mHelper.getWritableDatabase();
        if (isExists(ip))
            return;
        for (int i = 1; i <= 16; i++) {
            String sql = "insert into device_info(ip, frequency, duty, voltage, aisle, name) values (?, ?, ?, ?, ?, ?)";
            db.execSQL(sql, new Object[]{ip, 0, 0,
                    0, i, null});
        }
    }

    // 删除设备信息
    @Override
    public void deleteInfo(String ip) {
        SQLiteDatabase db = mHelper.getWritableDatabase();
        if (ip != null && isExists(ip)) {
            String sql = "delete from device_info where ip = ?";
            db.execSQL(sql, new String[]{ip});
        }
    }

    @Override
    public void deleteAll() {
        SQLiteDatabase db = mHelper.getWritableDatabase();
        db.execSQL("delete from device_info where id = 0");

    }

    // 更新设备名称
    @Override
    public void updateDeviceName(String ip, String name) {
        SQLiteDatabase db = mHelper.getWritableDatabase();
        if (name != null) {
            db.execSQL("update device_info set name = ? where ip = ? ",
                    new Object[]{name, ip});
        }
    }

    // 更新1路信息
    @Override
    public void updateAisleInfo(DeviceInfo info) {
        SQLiteDatabase db = mHelper.getWritableDatabase();
        if (info != null) {
            db.execSQL("update device_info set frequency = ?, duty = ?, voltage = ? where " +
                    "ip = ? and aisle = ?", new Object[]{info.getFrequency(), info.getDuty(),
                    info.getVoltage(), info.getIp(), info.getAisle()});
        }
    }

    // 更新设备16路信息
    @Override
    public void updateDeviceInfo(DeviceInfo info) {
        SQLiteDatabase db = mHelper.getWritableDatabase();
        if (info != null) {
            db.execSQL("update device_info set frequency = ?, duty = ? , voltage = ? where " +
                    "ip = ? ", new Object[]{info.getFrequency(), info.getDuty(), info.getVoltage(),
                    info.getIp()});
        }
    }

    @Override
    public void updateAllInfo(DeviceInfo info) {
        SQLiteDatabase db = mHelper.getWritableDatabase();
        db.execSQL("update device_info set frequency = ?, duty = ? , voltage =?",
                new Object[]{info.getFrequency(), info.getDuty(), info.getVoltage()});
    }

    @Override
    public DeviceInfo querySingleAisleInfo(String ip) {
        SQLiteDatabase db = mHelper.getReadableDatabase();
        DeviceInfo info = null;
        Cursor cursor = null;
        if (ip != null) {
            cursor = db.rawQuery("select * from device_info where ip = ? and aisle = ?", new String[]{ip, 1 + ""});
            while (cursor.moveToNext()) {
                info = new DeviceInfo();
                info.setIp(ip);
                info.setFrequency(cursor.getInt(cursor.getColumnIndex("frequency")));
                info.setDuty(cursor.getInt(cursor.getColumnIndex("duty")));
                info.setAisle(cursor.getInt(cursor.getColumnIndex("aisle")));
                info.setVoltage(cursor.getDouble(cursor.getColumnIndex("voltage")));
                info.setName(cursor.getString(cursor.getColumnIndex("name")));
            }
        }
        if (cursor != null) {
            cursor.close();
        }
        return info;
    }

    // 查询一个设备的16路信息
    @Override
    public List<DeviceInfo> querySingleInfo(String ip) {
        SQLiteDatabase db = mHelper.getReadableDatabase();
        ArrayList<DeviceInfo> deviceInfos = new ArrayList<DeviceInfo>();
        Cursor cursor = null;
        if (ip != null) {
            cursor = db.rawQuery("select * from device_info where ip = ?", new String[]{ip});
            while (cursor.moveToNext()) {
                DeviceInfo info = new DeviceInfo();
                info.setIp(ip);
                info.setFrequency(cursor.getDouble(cursor.getColumnIndex("frequency")));
                info.setDuty(cursor.getDouble(cursor.getColumnIndex("duty")));
                info.setVoltage(cursor.getDouble(cursor.getColumnIndex("voltage")));
                info.setAisle(cursor.getInt(cursor.getColumnIndex("aisle")));
                info.setName(cursor.getString(cursor.getColumnIndex("name")));
                deviceInfos.add(info);
            }
        }

        if (cursor != null) {
            cursor.close();
        }
        return deviceInfos;
    }

    // 设备是否已经存在
    @Override
    public boolean isExists(String ip) {
        SQLiteDatabase db = mHelper.getReadableDatabase();
        Cursor cursor = null;
        if (ip != null) {

            cursor = db.rawQuery("select * from device_info where ip = ? ",
                    new String[]{ip});
            boolean isExist = cursor.moveToNext();
            return isExist;
        }
        return false;
    }
}
