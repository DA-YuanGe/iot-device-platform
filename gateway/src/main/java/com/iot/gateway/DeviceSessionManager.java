package com.iot.gateway;

import com.iot.gateway.session.DeviceSession;
import io.netty.channel.Channel;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 设备连接会话管理器。
 *
 * 负责维护：
 * deviceId -> DeviceSession
 */
public class DeviceSessionManager {

    private static final DeviceSessionManager INSTANCE =
            new DeviceSessionManager();

    private final Map<String, DeviceSession> sessions =
            new ConcurrentHashMap<>();

    private DeviceSessionManager() {
    }

    public static DeviceSessionManager getInstance() {
        return INSTANCE;
    }

    /**
     * 注册设备连接。
     */
    public void register(
            String deviceId,
            Channel channel) {

        if (deviceId == null || channel == null) {
            return;
        }

        DeviceSession session =
                new DeviceSession(deviceId, channel);

        sessions.put(deviceId, session);

        System.out.println(
                "[SESSION] device registered: " +
                deviceId +
                ", remote=" +
                session.getRemoteAddress()
        );
    }

    /**
     * 移除设备连接。
     */
    public void remove(String deviceId) {

        if (deviceId == null) {
            return;
        }

        DeviceSession session = sessions.remove(deviceId);

        if (session != null) {

            session.offline();

            System.out.println(
                    "[SESSION] device removed: " +
                    deviceId
            );
        }
    }

    /**
     * 获取设备会话。
     */
    public DeviceSession getSession(String deviceId) {
        return sessions.get(deviceId);
    }

    /**
     * 获取设备连接。
     */
    public Channel getChannel(String deviceId) {

        DeviceSession session = sessions.get(deviceId);

        return session != null
                ? session.getChannel()
                : null;
    }

    /**
     * 判断设备是否在线。
     */
    public boolean isOnline(String deviceId) {

        DeviceSession session = sessions.get(deviceId);

        return session != null
                && session.isOnline()
                && session.getChannel().isActive();
    }

    /**
     * 更新设备心跳。
     */
    public void heartbeat(String deviceId) {

        DeviceSession session = sessions.get(deviceId);

        if (session != null) {
            session.heartbeat();
        }
    }

    /**
     * 当前在线设备数量。
     */
    public int onlineCount() {
        return sessions.size();
    }
}
