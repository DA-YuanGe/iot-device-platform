package com.iot.service.status;

import com.iot.service.event.DeviceEvent;
import com.iot.service.event.DeviceEventListener;
import com.iot.service.event.DeviceEventType;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 设备状态服务。
 *
 * 负责维护设备当前运行状态，
 * 同时将实时状态和事件历史同步到 Redis。
 */
public class DeviceStatusService
        implements DeviceEventListener {

    private static final String REDIS_STATUS_KEY_PREFIX =
            "iot:device:status:";

    private static final String REDIS_EVENT_KEY_PREFIX =
            "iot:device:events:";

    private static final long MAX_EVENT_HISTORY = 100;

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
                || event.getDeviceId() == null
                || event.getType() == null) {
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
        saveEventToRedis(event);

        System.out.println(
                "[STATUS] device=" +
                event.getDeviceId() +
                ", state=" +
                status.getState()
        );
    }

    private void saveToRedis(DeviceStatus status) {

        String key =
                REDIS_STATUS_KEY_PREFIX +
                status.getDeviceId();

        Map<String, Object> data =
                new HashMap<>();

        data.put("deviceId", status.getDeviceId());
        data.put("state", status.getState().name());
        data.put("connectedAt",
                toString(status.getConnectedAt()));
        data.put("lastHeartbeatTime",
                toString(status.getLastHeartbeatTime()));
        data.put("lastEventTime",
                toString(status.getLastEventTime()));

        HashOperations<String, Object, Object> hash =
                redisTemplate.opsForHash();

        hash.putAll(key, data);
    }

    private void saveEventToRedis(DeviceEvent event) {

        String key =
                REDIS_EVENT_KEY_PREFIX +
                event.getDeviceId();

        String record =
                event.getType().name() +
                "|" +
                event.getEventTime();

        redisTemplate.opsForList().rightPush(
                key,
                record
        );

        Long size =
                redisTemplate.opsForList().size(key);

        if (size != null
                && size > MAX_EVENT_HISTORY) {

            redisTemplate.opsForList().trim(
                    key,
                    size - MAX_EVENT_HISTORY,
                    -1
            );
        }
    }

    private String toString(LocalDateTime time) {

        return time == null
                ? null
                : time.toString();
    }

    /**
     * 查询设备当前状态。
     */
    public DeviceStatus getStatus(String deviceId) {
        return statuses.get(deviceId);
    }

    /**
     * 查询设备事件历史。
     */
    public List<Object> getEventHistory(String deviceId) {

        if (deviceId == null || deviceId.isBlank()) {
            return List.of();
        }

        String key =
                REDIS_EVENT_KEY_PREFIX +
                deviceId;

        List<Object> events =
                redisTemplate.opsForList()
                        .range(key, 0, -1);

        return events == null
                ? List.of()
                : events;
    }

    /**
     * 判断设备是否在线。
     */
    public boolean isOnline(String deviceId) {

        DeviceStatus status =
                statuses.get(deviceId);

        return status != null
                && (status.getState()
                == DeviceEventType.ONLINE
                || status.getState()
                == DeviceEventType.RECONNECTED
                || status.getState()
                == DeviceEventType.HEARTBEAT);
    }

    /**
     * 获取当前所有设备状态。
     */
    public Map<String, DeviceStatus> getAllStatuses() {
        return Map.copyOf(statuses);
    }
}
