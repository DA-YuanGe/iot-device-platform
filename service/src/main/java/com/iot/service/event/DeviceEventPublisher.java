package com.iot.service.event;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 设备事件发布器。
 *
 * Gateway 产生设备事件后，
 * 通过该组件向业务层发布。
 */
public class DeviceEventPublisher {

    private final List<DeviceEventListener> listeners =
            new CopyOnWriteArrayList<>();

    /**
     * 注册监听器。
     */
    public void addListener(DeviceEventListener listener) {

        if (listener != null) {
            listeners.add(listener);
        }
    }

    /**
     * 发布设备事件。
     */
    public void publish(DeviceEvent event) {

        if (event == null) {
            return;
        }

        for (DeviceEventListener listener : listeners) {
            listener.onEvent(event);
        }
    }
}
