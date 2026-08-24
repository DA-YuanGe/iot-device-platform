package com.iot.service.alarm;

import com.iot.service.event.DeviceEvent;
import com.iot.service.event.DeviceEventListener;
import com.iot.service.event.DeviceEventType;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 设备告警服务。
 *
 * 负责根据设备事件创建和恢复告警。
 */
@Service
public class DeviceAlarmService
        implements DeviceEventListener {

    private final Map<String, DeviceAlarm> activeAlarms =
            new ConcurrentHashMap<>();

    /**
     * 接收设备事件。
     *
     * OFFLINE：创建离线告警。
     * ONLINE / RECONNECTED：恢复离线告警。
     */
    @Override
    public void onEvent(DeviceEvent event) {

        if (event == null
                || event.getDeviceId() == null
                || event.getType() == null) {
            return;
        }

        if (event.getType() == DeviceEventType.OFFLINE) {

            createOfflineAlarm(
                    event.getDeviceId()
            );

            return;
        }

        if (event.getType() == DeviceEventType.ONLINE
                || event.getType()
                == DeviceEventType.RECONNECTED) {

            recoverOfflineAlarm(
                    event.getDeviceId()
            );
        }
    }

    /**
     * 创建设备离线告警。
     *
     * 同一设备存在未恢复的离线告警时，不重复创建。
     */
    public DeviceAlarm createOfflineAlarm(String deviceId) {

        if (deviceId == null || deviceId.isBlank()) {
            return null;
        }

        DeviceAlarm existing =
                activeAlarms.get(deviceId);

        if (existing != null
                && "ACTIVE".equals(existing.getStatus())) {
            return existing;
        }

        DeviceAlarm alarm =
                new DeviceAlarm(
                        UUID.randomUUID().toString(),
                        deviceId,
                        "DEVICE_OFFLINE",
                        "HIGH"
                );

        activeAlarms.put(deviceId, alarm);

        System.out.println(
                "[ALARM] created, device=" +
                deviceId +
                ", type=" +
                alarm.getType() +
                ", level=" +
                alarm.getLevel()
        );

        return alarm;
    }

    /**
     * 恢复设备离线告警。
     */
    public DeviceAlarm recoverOfflineAlarm(String deviceId) {

        if (deviceId == null || deviceId.isBlank()) {
            return null;
        }

        DeviceAlarm alarm =
                activeAlarms.get(deviceId);

        if (alarm == null
                || !"ACTIVE".equals(alarm.getStatus())) {
            return null;
        }

        alarm.recover();

        System.out.println(
                "[ALARM] recovered, device=" +
                deviceId +
                ", alarmId=" +
                alarm.getAlarmId()
        );

        return alarm;
    }

    /**
     * 查询设备当前未恢复告警。
     */
    public DeviceAlarm getActiveAlarm(String deviceId) {

        DeviceAlarm alarm =
                activeAlarms.get(deviceId);

        if (alarm == null
                || !"ACTIVE".equals(alarm.getStatus())) {
            return null;
        }

        return alarm;
    }

    /**
     * 查询所有告警。
     */
    public Map<String, DeviceAlarm> getAlarms() {
        return Map.copyOf(activeAlarms);
    }

    /**
     * 查询所有当前未恢复告警。
     */
    public Map<String, DeviceAlarm> getActiveAlarms() {

        Map<String, DeviceAlarm> result =
                new ConcurrentHashMap<>();

        activeAlarms.forEach(
                (deviceId, alarm) -> {

                    if ("ACTIVE".equals(
                            alarm.getStatus())) {

                        result.put(
                                deviceId,
                                alarm
                        );
                    }
                }
        );

        return Map.copyOf(result);
    }
}
