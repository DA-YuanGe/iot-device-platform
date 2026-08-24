package com.iot.service.event;

/**
 * 设备事件日志监听器。
 *
 * 当前用于验证 Gateway -> Service 事件链路。
 */
public class LoggingDeviceEventListener
        implements DeviceEventListener {

    @Override
    public void onEvent(DeviceEvent event) {

        System.out.println(
                "[EVENT] device=" +
                event.getDeviceId() +
                ", type=" +
                event.getType() +
                ", time=" +
                event.getEventTime()
        );
    }
}
