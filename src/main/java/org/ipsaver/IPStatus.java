package org.ipsaver;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.util.Date;

public class IPStatus {
    private Inet4Address ipv4;
    private Inet6Address ipv6;
    private Date lastCheckTime;
    private Date lastUpdateTime;

    public IPStatus(Inet4Address ipv4, Inet6Address ipv6, Date lastCheckTime, Date lastUpdateTime) {
        this.ipv4 = ipv4;
        this.ipv6 = ipv6;
        this.lastCheckTime = lastCheckTime;
        this.lastUpdateTime = lastUpdateTime;
    }

    public Inet4Address getIpv4() {
        return ipv4;
    }

    public void setIpv4(Inet4Address ipv4) {
        this.ipv4 = ipv4;
    }

    public Inet6Address getIpv6() {
        return ipv6;
    }

    public void setIpv6(Inet6Address ipv6) {
        this.ipv6 = ipv6;
    }

    public Date getLastCheckTime() {
        return lastCheckTime;
    }

    public void setLastCheckTime(Date lastCheckTime) {
        this.lastCheckTime = lastCheckTime;
    }

    public Date getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(Date lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    public String toJson() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(this);
    }

    public static IPStatus fromJson(String json) {
        Gson gson = new Gson();
        return gson.fromJson(json, IPStatus.class);
    }
}
