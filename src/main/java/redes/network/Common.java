package redes.network;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;

public class Common {
    public static final int BUFFER_SIZE = 1024;
    public static final byte[] END_SIGNAL = new byte[BUFFER_SIZE];
    public static final int multicastGroupPort = 4000;
    public static InetAddress multicastGroup;

    static {
        Arrays.fill(END_SIGNAL, (byte) 4);
        try {
            multicastGroup = InetAddress.getByName("228.1.1.1");
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }
    public static DatagramPacket END_SIGNAL_packet() {
        return new DatagramPacket(END_SIGNAL,
                END_SIGNAL.length, multicastGroup, multicastGroupPort);
    }
    public static String[][] splitMessage(String input) {
        var keyChain = new ArrayList<String[]>();
        var split = input.split("\n");
        for (String keyVal : split) {
            keyChain.add(keyVal.split(":"));
        }

        String[][] ret = new String[0][2];
        ret = keyChain.toArray(ret);
        return ret;
    }
    public static String getPropertyFromMessage(String [][] table, String key) {
        //System.out.println("Searching for: " + key + " inside message");
        for(String [] pair: table) {
            if(pair[0].equals(key)) {
                return pair[1];
            }
        }
        return "";
    }
}
