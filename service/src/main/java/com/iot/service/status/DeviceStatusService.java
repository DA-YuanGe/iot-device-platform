package com.iot.service.status;

import com.iot.service.event.DeviceEvent;
import com.iot.service.event.DeviceEventListener;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 设备状态服务。
 *
 * 负责维护设备当前运行状态，
 * 同时将实时状态同步到 Redis。
 */
@Service
public class DeviceStatusService
        implements DeviceEventListener {

    private static final String REDIS_KEY_PREFIX =
            "iot:device:status:";

    private final Map<String, DeviceStatus> statuses =
            new ConcurrentHashMap<>();

    private final RedisTemplate<String, Object> redisTemplate;

    public DeviceStatusService(
            RedisTemplate<String, Object> redisTemplate) {

        this.redisTemplate = redisTemplate;
    }

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

        saveToRedis(status);

        System.out.println(
                "[STATUS] device=" +
                event.getDeviceId() +
                ", state=" +
                status.getState()
        );
    }

    /**
     * 将设备实时状态写入 Redis。
     */
    private void saveToRedis(DeviceStatus status) {

        String key =
                REDIS_KEY_PREFIX +
                status.getDeviceId();

        Map<String, Object> data =
                new HashMap<>();

        data.put(
                "deviceId",
                status.getDeviceId()
        );

        data.put(
                "state",
                status.getState().name()
        );

        data.put(
                "connectedAt",
                toString(status.getConnectedAt())
        );

        data.put(
                "lastHeartbeatTime",
                toString(status.getLastHeartbeatTime())
        );

        data.put(
                "lastEventTime",
                toString(status.getLastEventTime())
        );

        HashOperations<String, Object, Object> hash =
                redisTemplate.opsForHash();

        hash.putAll(key, data);
    }

    private String toString(LocalDateTime time) {

        return time == null
                ? null
                : time.toString();
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
