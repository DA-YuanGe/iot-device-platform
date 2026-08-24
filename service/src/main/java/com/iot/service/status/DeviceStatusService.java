package com.iot.service.status;

import com.iot.service.event.DeviceEvent;
import com.iot.service.event.DeviceEventListener;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 设备状态服务。
 *
 * 负责维护设备当前运行状态，
 * 为后续数据库、Redis和监控接口提供统一状态入口。
 */
public class DeviceStatusService
        implements DeviceEventListener {

    private final Map<String, DeviceStatus> statuses =
            new ConcurrentHashMap<>();

    @Override
    public void onEvent(DeviceEvent event) {

        if (event == null
                || event.getDeviceId() == null) {
            return;
        }

        DeviceStatus status =
                statuses.computeIfAbsent(
                        event.getDeviceId(),
                        DeviceStatus::new
                );

        status.update(
                event.getType(),
                event.getEventTime()
        );

        System.out.println(
                "[STATUS] device=" +
                event.getDeviceId() +
                ", state=" +
                status.getState()
        );
    }

    /**
     * 查询设备状态。
     */
    public DeviceStatus getStatus(String deviceId) {
        return statuses.get(deviceId);
    }

    /**
     * 判断设备是否在线。
     */
    public boolean isOnline(String deviceId) {

        DeviceStatus status =
                statuses.get(deviceId);

        return status != null
                && (status.getState()
                == com.iot.service.event.DeviceEventType.ONLINE
                || status.getState()
                == com.iot.service.event.DeviceEventType.RECONNECTED
                || status.getState()
                == com.iot.service.event.DeviceEventType.HEARTBEAT);
    }

    /**
     * 获取当前所有设备状态。
     */
    public Map<String, DeviceStatus> getAllStatuses() {
        return Map.copyOf(statuses);
    }
}
