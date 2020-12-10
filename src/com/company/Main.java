package com.company;

import java.io.IOException;
import java.net.*;

public class Main {

    public DatagramPacket getPacket(){
        String[] serverNames = {
                "mmo1.ntp.se",
                "mmo2.ntp.se",
                "gbg1.ntp.se",
                "gbg2.ntp.se",
                "sth1.ntp.se",
                "sth2.ntp.se",
                "svl1.ntp.se",
                "svl2.ntp.se"};

        for (String serverName : serverNames) {
            try {
                InetAddress address = InetAddress.getByName(serverName);
                SNTPMessage  message = new SNTPMessage();
                byte [] buf = message.toByteArray();
                return new DatagramPacket(buf, buf.length, address, 123);
            } catch (UnknownHostException ignored) {}
        }
        return null;
    }

    public SNTPMessage getMessage(DatagramSocket socket, DatagramPacket packet) throws IOException {
        socket.send(packet);
        socket.receive(packet);
        return new SNTPMessage(packet.getData());
    }

    public double getSystemTimeFromYear1900(){
        double secondsBetweenYear1900and1970 = 2208988800.0;
        return (System.currentTimeMillis() / 1000.0) + secondsBetweenYear1900and1970;
    }

    public static void main(String[] args) {
        Main main = new Main();

        try (DatagramSocket socket = new DatagramSocket()) {
            DatagramPacket packet = main.getPacket();
            SNTPMessage response = main.getMessage(socket, packet);
            double systemTimeInSeconds = main.getSystemTimeFromYear1900();
            System.out.println(response.toString());

            System.out.println("Roundtrip delay: " + response.getRoundtripDelay(systemTimeInSeconds));
            System.out.println("System clock offset: " + response.getSystemClockOffset(systemTimeInSeconds));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}