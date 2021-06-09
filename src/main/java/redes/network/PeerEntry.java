package redes.network;
public class PeerEntry {
    private String ipAddr;
    private int rmiPort;
    private int multicastPort;

    public PeerEntry(String ipAddr, int rmiPort, int multicastPort) {
        this.ipAddr = ipAddr;
        this.multicastPort = multicastPort;
        this.rmiPort = rmiPort;
    }

    public String getIpAddr() {
        return ipAddr;
    }

    public void setIpAddr(String ipAddr) {
        this.ipAddr = ipAddr;
    }

    public int getRmiPort() {
        return rmiPort;
    }

    public void setRmiPort(int rmiPort) {
        this.rmiPort = rmiPort;
    }

    public int getMulticastPort() {
        return multicastPort;
    }

    public void setMulticastPort(int multicastPort) {
        this.multicastPort = multicastPort;
    }
    public String getKey() {
        return this.ipAddr+":"+this.rmiPort;
    }
}
