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
 *
 * 同一个 deviceId 同一时间只允许存在一个有效会话。
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
     *
     * 如果设备已经存在旧连接，
     * 则关闭旧连接并使用新连接建立会话。
     */
    public void register(
            String deviceId,
            Channel channel) {

        if (deviceId == null || channel == null) {
            return;
        }

        DeviceSession newSession =
                new DeviceSession(deviceId, channel);

        DeviceSession oldSession =
                sessions.put(deviceId, newSession);

        if (oldSession != null) {

            Channel oldChannel =
                    oldSession.getChannel();

            if (oldChannel != channel &&
                    oldChannel.isActive()) {

                System.out.println(
                        "[SESSION] duplicate device connection: " +
                        deviceId +
                        ", closing old channel=" +
                        oldChannel.id()
                );

                oldSession.offline();

                oldChannel.close();
            }
        }

        System.out.println(
                "[SESSION] device registered: " +
                deviceId +
                ", remote=" +
                newSession.getRemoteAddress()
        );
    }

    /**
     * 移除设备连接。
     *
     * 只有当前会话对应的连接才能真正移除设备。
     */
    public void remove(
            String deviceId,
            Channel channel) {

        if (deviceId == null || channel == null) {
            return;
        }

        sessions.computeIfPresent(
                deviceId,
                (key, session) -> {

                    if (session.getChannel() == channel) {

                        session.offline();

                        System.out.println(
                                "[SESSION] device removed: " +
                                deviceId
                        );

                        return null;
                    }

                    return session;
                }
        );
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

        DeviceSession session =
                sessions.get(deviceId);

        return session != null
                ? session.getChannel()
                : null;
    }

    /**
     * 判断设备是否在线。
     */
    public boolean isOnline(String deviceId) {

        DeviceSession session =
                sessions.get(deviceId);

        return session != null
                && session.isOnline()
                && session.getChannel().isActive();
    }

    /**
     * 更新设备心跳。
     */
    public void heartbeat(String deviceId) {

        DeviceSession session =
                sessions.get(deviceId);

        if (session != null) {
            session.heartbeat();
        }
    }

    /**
     * 当前在线设备数量。
     */
    /**
     * 获取当前所有设备会话。
     */
    public Map<String, DeviceSession> getSessions() {
        return sessions;
    }


    public int onlineCount() {
        return sessions.size();
    }
}
