package com.iot.service.event;

/**
 * 设备事件类型。
 */
public enum DeviceEventType {

    /**
     * 设备上线。
     */
    ONLINE,

    /**
     * 设备心跳。
     */
    HEARTBEAT,

    /**
     * 设备离线。
     */
    OFFLINE,

    /**
     * 设备重新上线。
     */
    RECONNECTED
}
