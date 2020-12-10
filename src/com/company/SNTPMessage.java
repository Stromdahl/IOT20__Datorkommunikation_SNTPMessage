package com.company;

public class SNTPMessage {
    private byte leapIndicator = 0;
    private byte versionNumber = 4;
    private final byte mode;

    private short stratum = 0;
    private short pollInterval = 0;
    private byte precision = 0;

    private double rootDelay = 0;
    private double rootDispersion = 0;

    private final byte[] referenceIdentifier = {0, 0, 0, 0};

    private double referenceTimestamp = 0;
    private double originateTimestamp = 0;
    private double receiveTimestamp = 0;
    private final double transmitTimestamp;


    public SNTPMessage(byte[] buf) {
        byte b = buf[0];

        leapIndicator = (byte) ((b >> 6) & 0x3);
        versionNumber = (byte) ((b >> 3) & 0x7);
        mode = (byte) (b & 0x7);
        stratum = unsignedByteToShort(buf[1]);
        pollInterval = unsignedByteToShort(buf[2]);
        precision = buf[3];
        rootDelay = (buf[4] * 256.0)
                + unsignedByteToShort(buf[5])
                + (unsignedByteToShort(buf[6]) / (0xff + 1.0))
                + (unsignedByteToShort(buf[7]) / (0xffff + 1.0));

        rootDispersion = (buf[8] * 256.0)
                + unsignedByteToShort(buf[9])
                + (unsignedByteToShort(buf[10]) / (0xff + 1.0))
                + (unsignedByteToShort(buf[11]) / (0xffff + 1.0));

        referenceIdentifier[0] = buf[12];
        referenceIdentifier[1] = buf[13];
        referenceIdentifier[2] = buf[14];
        referenceIdentifier[3] = buf[15];

        referenceTimestamp = byteArrayToDouble(buf, 16);
        originateTimestamp = byteArrayToDouble(buf, 24);
        receiveTimestamp = byteArrayToDouble(buf, 32);
        transmitTimestamp = byteArrayToDouble(buf, 40);

    }

    public SNTPMessage() {
        mode = 3;
        transmitTimestamp = (System.currentTimeMillis() / 1000.0) + 2208988800.0;
    }

    private double byteArrayToDouble(byte[] buf, int index) {
        double result = 0.0;
        for (int i = 0; i < 8; i++) {
            result += unsignedByteToShort(buf[index + i]) * Math.pow(2, (3 - i) * 8);
        }
        return result;
    }

    private short unsignedByteToShort(byte b) {
        if ((b & 0x80) == 0x80) {
            return (short) (128 + (b & 0x7f));
        }
        return b;
    }

    public byte[] toByteArray() {
        byte[] array = new byte[48];
        array[0] = (byte) (leapIndicator << 6 | versionNumber << 3 | mode);
        array[1] = (byte) stratum;
        array[2] = (byte) pollInterval;
        array[3] = precision;

        int data = (int) (rootDelay * (0xff + 1));
        array[4] = (byte) ((data >> 24) & 0xff);
        array[5] = (byte) ((data >> 16) & 0xff);
        array[6] = (byte) ((data >> 8) & 0xff);
        array[7] = (byte) (data & 0xff);

        int rd = (int) (rootDispersion * (0xff + 1));
        array[8] = (byte) ((rd >> 24) & 0xff);
        array[9] = (byte) ((rd >> 16) & 0xff);
        array[10] = (byte) ((rd >> 8) & 0xff);
        array[11] = (byte) (rd & 0xff);

        array[12] = referenceIdentifier[0];
        array[13] = referenceIdentifier[1];
        array[14] = referenceIdentifier[2];
        array[15] = referenceIdentifier[3];

        doubleToByteArray(array, 16, referenceTimestamp);
        doubleToByteArray(array, 24, originateTimestamp);
        doubleToByteArray(array, 32, receiveTimestamp);
        doubleToByteArray(array, 40, transmitTimestamp);

        return array;
    }

    private void doubleToByteArray(byte[] array, int index, double data) {
        for (int i = 0; i < 8; i++) {
            array[index + i] = (byte) (data / Math.pow(2, (3 - i) * 8));
            data -= unsignedByteToShort(array[index + i]) * Math.pow(2, (3 - i) * 8);
        }
    }

    public String toString() {
        return "Leap Indicator: " + leapIndicator +
                "\nVersion Number: " + versionNumber +
                "\nMode: " + mode +
                "\nStratum: " + stratum +
                "\nPoll Interval: " + pollInterval +
                "\nPrecision: " + precision +
                "\nRoot Delay: " + rootDelay +
                "\nRoot Dispersion: " + rootDispersion +
                "\nReference Identifier: " + referenceIdentifierToString() +
                "\nReference Timestamp: " + referenceTimestamp +
                "\nOriginate Timestamp: " + originateTimestamp +
                "\nReceive Timestamp: " + rootDispersion +
                "\nTransmit Timestamp: " + rootDispersion;
    }

    public String referenceIdentifierToString() {

        if(stratum==0 || stratum==1)
        {
            return new String(referenceIdentifier);
        }

        else if(versionNumber==3)
        {
            return unsignedByteToShort(referenceIdentifier[0]) + "." +
                    unsignedByteToShort(referenceIdentifier[1]) + "." +
                    unsignedByteToShort(referenceIdentifier[2]) + "." +
                    unsignedByteToShort(referenceIdentifier[3]);
        }

        else if(versionNumber==4)
        {
            return "" + ((unsignedByteToShort(referenceIdentifier[0]) / 256.0) +
                    (unsignedByteToShort(referenceIdentifier[1]) / 65536.0) +
                    (unsignedByteToShort(referenceIdentifier[2]) / 16777216.0) +
                    (unsignedByteToShort(referenceIdentifier[3]) / 4294967296.0));
        }

        return "";
    }

    public double getRoundtripDelay(double systemTime){
        return (systemTime - this.originateTimestamp) - (this.transmitTimestamp - this.receiveTimestamp);
    }

    public double getSystemClockOffset(double systemTime){
        return ((this.receiveTimestamp - this.originateTimestamp) + (this.transmitTimestamp - systemTime)) / 2;
    }
}
