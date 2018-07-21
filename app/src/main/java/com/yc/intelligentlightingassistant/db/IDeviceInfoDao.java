package com.yc.intelligentlightingassistant.db;

import com.yc.intelligentlightingassistant.bean.DeviceInfo;

import java.util.List;

/**
 * 对设备信息数据库操作的接口
 */

public interface IDeviceInfoDao {

    /**
     * 插入设备的1路信息
     *
     * @param info 设备信息
     */
    void insertSingleInfo(DeviceInfo info);

    /**
     * 插入设备的16路信息
     *
     * @param ip 设备ip
     */
    void insertInfo(String ip);

    /**
     * 删除设备信息
     *
     * @param ip 设备Ip
     */
    void deleteInfo(String ip);

    /**
     * 删除全部的设备信息
     */
    void deleteAll();

    /**
     * 更新设备名称
     *
     * @param ip   设备IP
     * @param name 设备名称
     */
    void updateDeviceName(String ip, String name);

    /**
     * 更新1路信息
     *
     * @param info 设备信息
     */
    void updateAisleInfo(DeviceInfo info);

    /**
     * 更新一个设备16路信息
     *
     * @param info 设备信息
     */
    void updateDeviceInfo(DeviceInfo info);

    /**
     * 更新全部设备的信息
     *
     * @param info 设备信息
     */
    void updateAllInfo(DeviceInfo info);

    DeviceInfo querySingleAisleInfo(String ip);

    /**
     * 查询单个设备的16路信息
     *
     * @param ip 设备IP
     * @return 16路设备信息
     */
    List<DeviceInfo> querySingleInfo(String ip);

    /**
     * 设备是否存在
     *
     * @param ip 设备IP
     * @return 存在返回TRUE
     */
    boolean isExists(String ip);
}
