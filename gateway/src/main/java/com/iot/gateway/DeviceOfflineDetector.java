package com.iot.gateway;

import com.iot.gateway.session.DeviceSession;
import com.iot.service.event.DeviceEvent;
import com.iot.service.event.DeviceEventPublisher;
import com.iot.service.event.DeviceEventType;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * 设备离线检测器。
 *
 * 定期检查设备最近一次心跳时间，
 * 超过规定时间未收到心跳则认为设备离线。
 */
public class DeviceOfflineDetector {

    /**
     * 心跳超时时间：15秒。
     */
    private static final long OFFLINE_TIMEOUT_SECONDS = 15;

    /**
     * 检测周期：5秒。
     */
    private static final long CHECK_INTERVAL_SECONDS = 5;

    private final DeviceSessionManager sessionManager =
            DeviceSessionManager.getInstance();

    private final DeviceEventPublisher eventPublisher;

    private volatile boolean running;

    private Thread detectorThread;

    public DeviceOfflineDetector(
            DeviceEventPublisher eventPublisher) {

        this.eventPublisher = eventPublisher;
    }

    /**
     * 启动离线检测任务。
     */
    public synchronized void start() {

        if (running) {
            return;
        }

        running = true;

        detectorThread = new Thread(
                this::detectLoop,
                "device-offline-detector"
        );

        detectorThread.setDaemon(true);

        detectorThread.start();

        System.out.println(
                "[OFFLINE-DETECTOR] started"
        );
    }

    /**
     * 停止离线检测任务。
     */
    public synchronized void stop() {

        running = false;

        if (detectorThread != null) {
            detectorThread.interrupt();
        }

        System.out.println(
                "[OFFLINE-DETECTOR] stopped"
        );
    }

    /**
     * 离线检测循环。
     */
    private void detectLoop() {

        while (running) {

            try {

                detectOfflineDevices();

                Thread.sleep(
                        CHECK_INTERVAL_SECONDS * 1000
                );

            } catch (InterruptedException e) {

                Thread.currentThread().interrupt();

                break;
            }
        }
    }

    /**
     * 检查所有设备是否超时。
     */
    private void detectOfflineDevices() {

        LocalDateTime now =
                LocalDateTime.now();

        for (DeviceSession session :
                sessionManager.getSessions().values()) {

            if (!session.isOnline()) {
                continue;
            }

            long seconds =
                    Duration.between(
                            session.getLastHeartbeatTime(),
                            now
                    ).getSeconds();

            if (seconds >= OFFLINE_TIMEOUT_SECONDS) {

                session.offline();

                System.out.println(
                        "[OFFLINE] device=" +
                        session.getDeviceId() +
                        ", lastHeartbeat=" +
                        session.getLastHeartbeatTime() +
                        ", timeout=" +
                        seconds +
                        "s"
                );

                eventPublisher.publish(
                        new DeviceEvent(
                                session.getDeviceId(),
                                DeviceEventType.OFFLINE
                        )
                );
            }
        }
    }
}
