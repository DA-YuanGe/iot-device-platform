package com.iot.service.alarm;

import com.iot.service.event.DeviceEvent;
import com.iot.service.event.DeviceEventListener;
import com.iot.service.event.DeviceEventType;

/**
 * 设备告警事件监听器。
 *
 * 根据设备运行事件自动创建和恢复告警。
 */
public class DeviceAlarmEventListener
        implements DeviceEventListener {

    private final DeviceAlarmService alarmService;

    public DeviceAlarmEventListener(
            DeviceAlarmService alarmService) {

        this.alarmService = alarmService;
    }

    @Override
    public void onEvent(DeviceEvent event) {

        if (event == null
                || event.getDeviceId() == null
                || event.getType() == null) {
            return;
        }

        String deviceId = event.getDeviceId();

        if (event.getType() == DeviceEventType.OFFLINE) {

            alarmService.createOfflineAlarm(deviceId);

            return;
        }

        if (event.getType() == DeviceEventType.ONLINE
                || event.getType() == DeviceEventType.RECONNECTED) {

            alarmService.recoverOfflineAlarm(deviceId);
        }
    }
}
