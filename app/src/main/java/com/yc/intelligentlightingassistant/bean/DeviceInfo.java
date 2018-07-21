package com.yc.intelligentlightingassistant.bean;


/**
 *设备信息类
 */

public class DeviceInfo{
    //id
    private int id;
    //ip
    private String ip;
    //频率
    private double frequency;
    //占空比
    private double duty;
    //电压
    private double voltage;
    // 通道
    private int aisle;
    // 名字
    private String name;

    /**
     * 构造方法
     */
    //无参数的构造方法
    public DeviceInfo() {

    }

    //带参数的构造方法
    public DeviceInfo(String ip, double frequency, double duty, double voltage, int aisle) {
        this.ip = ip;
        this.frequency = frequency;
        this.duty = duty;
        this.voltage = voltage;
        this.aisle = aisle;
    }

    /**
     * getters和setters方法
     */
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public double getFrequency() {
        return frequency;
    }

    public void setFrequency(double frequency) {
        this.frequency = frequency;
    }

    public double getDuty() {
        return duty;
    }

    public void setDuty(double duty) {
        this.duty = duty;
    }

    public double getVoltage() {
        return voltage;
    }

    public void setVoltage(double voltage) {
        this.voltage = voltage;
    }

    public int getAisle() {
        return aisle;
    }

    public void setAisle(int aisle) {
        this.aisle = aisle;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * 重写toString()方法
     */
    @Override
    public String toString() {
        return "DeviceInfo{" +
                "id=" + id +
                ", ip='" + ip + '\'' +
                ", frequency=" + frequency +
                ", duty=" + duty +
                ", voltage=" + voltage +
                ", aisle=" + aisle +
                ", name='" + name + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DeviceInfo that = (DeviceInfo) o;

        if (id != that.id) return false;
        if (frequency != that.frequency) return false;
        if (duty != that.duty) return false;
        if (Double.compare(that.voltage, voltage) != 0) return false;
        if (aisle != that.aisle) return false;
        if (!ip.equals(that.ip)) return false;
        if (name != null && that.name != null) {
            return name.equals(that.name);
        } else if (name == null && that.name == null){
            return true;
        }
        return false;

    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = id;
        result = 31 * result + ip.hashCode();
        temp = Double.doubleToLongBits(frequency);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(duty);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(voltage);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + aisle;
        result = 31 * result + name.hashCode();
        return result;
    }
}
