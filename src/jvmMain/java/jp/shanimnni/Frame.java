package jp.shanimnni;

import com.igormaznitsa.jbbp.io.JBBPOut;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public class Frame {
    static byte[] ACK = {0x00, 0x00, (byte) 0xff, 0x00, (byte) 0xff, 0x00};
    static byte[] ERR = {0x00, 0x00, (byte) 0xff, (byte) 0xff, (byte) 0xff};

    static String TYPE_ACK = "ACK";
    static String TYPE_ERR = "ERR";
    static String TYPE_DATA = "DATA";


    byte[] data;
    String type;
    byte[] frame;

    Frame(byte[] data) {

        if (Arrays.equals(Arrays.copyOfRange(data, 0, 3), new byte[]{(byte) 0x00, (byte) 0x00, (byte) 0xff})) {

            this.frame = data;
            if (Arrays.equals(Arrays.copyOfRange(data, 0, 6), jp.shanimnni.Frame.ACK)) {
                this.type = jp.shanimnni.Frame.TYPE_ACK;
//            } else if (Arrays.equals( Arrays.copyOfRange(data, 0, 5), Frame.ERR)) {
//                this.type = "err";
            } else if (Arrays.equals(Arrays.copyOfRange(this.frame, 3, 5), new byte[]{(byte) 0xff, (byte) 0xff})) {
                this.type = jp.shanimnni.Frame.TYPE_DATA;
            }

            assert this.type != null;
            if (this.type.equals(Frame.TYPE_DATA)) {
                ByteBuffer buf = ByteBuffer.wrap(Arrays.copyOfRange(this.frame, 5, 7));
                buf.order(ByteOrder.LITTLE_ENDIAN);
                int length = buf.getShort();
                this.data = Arrays.copyOfRange(this.frame, 8, 8 + length);
            }
        } else {

            JBBPOut bytes = JBBPOut.BeginBin();
            try {
                bytes.Byte(0, 0, -1, -1, -1);
                bytes.Byte(ByteBuffer.allocate(Short.SIZE / Byte.SIZE).order(ByteOrder.LITTLE_ENDIAN).putShort((short) data.length).array());
                bytes.Byte(getCheckSum(ByteBuffer.allocate(Short.SIZE / Byte.SIZE).order(ByteOrder.LITTLE_ENDIAN).putShort((short) data.length).array()));
                bytes.Byte(data);


                byte[] cur = bytes.End().toByteArray();

                bytes = JBBPOut.BeginBin().Byte(cur);

                bytes.Byte(getCheckSum(Arrays.copyOfRange(cur, 8, cur.length)));
                bytes.Byte(0);

                this.frame = bytes.End().toByteArray();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    byte getCheckSum(byte[] data) {
        int sum = sumOfBytes(data);

        return (byte) ((0x100 - sum) % 0x100);
    }

    int sumOfBytes(byte[] bytes) {
        int sum = 0;
        for (byte aByte : bytes) {
            sum += 0x0FF & aByte;
        }
        return sum;
    }

    String getType() {
        return this.type;
    }

    byte[] getString() {
        return this.frame;
    }


}

