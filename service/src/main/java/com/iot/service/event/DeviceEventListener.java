package com.iot.service.event;

/**
 * 设备事件监听器。
 */
@FunctionalInterface
public interface DeviceEventListener {

    /**
     * 处理设备事件。
     */
    void onEvent(DeviceEvent event);
}
