package fantasy.rqg.sdk.util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class STM32CRC {

    int crc = 0xFFFFFFFF;
    final int CrcTable[] = { // Nibble lookup table for 0x04C11DB7 polynomial
            0x00000000, 0x04C11DB7, 0x09823B6E, 0x0D4326D9, 0x130476DC, 0x17C56B6B, 0x1A864DB2, 0x1E475005, 0x2608EDB8,
            0x22C9F00F, 0x2F8AD6D6, 0x2B4BCB61, 0x350C9B64, 0x31CD86D3, 0x3C8EA00A, 0x384FBDBD};

    public void reset() {
        crc = 0xFFFFFFFF;
    }

    public void update(ByteBuffer bb) {
        if (bb.capacity() % 4 != 0) {
            return;
        }
        bb.position(0);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        while (bb.hasRemaining()) {
            crc = Crc32Fast(crc, bb.getInt()); // 4-bytes at a time
        }

    }

    public void update(byte[] data) {
        byte[] newData = new byte[data.length + 3 - (data.length + 3) % 4];
        for (int i = 0; i < newData.length; i++) {

            if (i < data.length) {
                newData[i] = data[i];
            } else {
                newData[i] = (byte) 0xFF;
            }
        }

        update(ByteBuffer.wrap(newData));
    }

    public int getValue() {
        return crc;
    }


    int Crc32Fast(int Crc, int Data) {

        Crc = Crc ^ Data; // Apply all 32-bits

        Crc = (Crc << 4) ^ CrcTable[(Crc >> 28) & 0x0F]; // Assumes 32-bit reg,
        // masking index to
        // 4-bits
        Crc = (Crc << 4) ^ CrcTable[(Crc >> 28) & 0x0F]; // 0x04C11DB7
        // Polynomial used
        // in STM32
        Crc = (Crc << 4) ^ CrcTable[(Crc >> 28) & 0x0F];
        Crc = (Crc << 4) ^ CrcTable[(Crc >> 28) & 0x0F];
        Crc = (Crc << 4) ^ CrcTable[(Crc >> 28) & 0x0F];
        Crc = (Crc << 4) ^ CrcTable[(Crc >> 28) & 0x0F];
        Crc = (Crc << 4) ^ CrcTable[(Crc >> 28) & 0x0F];
        Crc = (Crc << 4) ^ CrcTable[(Crc >> 28) & 0x0F];

        return (Crc);
    }

    public static void main(String[] args) {
        STM32CRC crc = new STM32CRC();
        byte[] data = new byte[]{0x41, 0x42, 0x43, 0x44};

        ByteBuffer bb = ByteBuffer.wrap(data);

        crc.reset();
        crc.update(bb);

        System.out.printf("CRC ERR %08X \n", crc.getValue());
    }

}