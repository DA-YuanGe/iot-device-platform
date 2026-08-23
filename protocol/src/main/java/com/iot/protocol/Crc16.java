package com.iot.protocol;

/**
 * CRC16-IBM 校验工具。
 *
 * 多项式：0xA001
 * 初始值：0xFFFF
 */
public final class Crc16 {

    private Crc16() {
    }

    public static int calculate(byte[] data) {
        int crc = 0xFFFF;

        for (byte value : data) {
            crc ^= (value & 0xFF);

            for (int i = 0; i < 8; i++) {
                if ((crc & 0x0001) != 0) {
                    crc = (crc >>> 1) ^ 0xA001;
                } else {
                    crc >>>= 1;
                }
            }
        }

        return crc & 0xFFFF;
    }
}
