package org.xcore.plugin.utils.models;

import fr.xpdustry.javelin.JavelinEvent;
import org.bson.codecs.pojo.annotations.BsonIgnore;

import static org.xcore.plugin.utils.Database.bansCollection;

public class BanData implements JavelinEvent {
    public long bid;

    public String uuid;
    public String ip;

    public String name = "<unknown>";
    public String adminName;
    public String reason;
    public String server;
    public long unbanDate;

    @BsonIgnore
    public boolean full = true;
    @BsonIgnore
    public boolean unban = false;

    @SuppressWarnings("unused")
    public BanData() {
    }

    public BanData(String uuid, String ip, String name, String adminName, String server) {
        this.uuid = uuid;
        this.ip = ip;
        this.name = name;
        this.adminName = adminName;
        this.server = server;
        this.full = false;
    }

    public BanData(String uuid, String ip, String name, String adminName, String reason, String server, long unbanDate) {
        this.uuid = uuid;
        this.ip = ip;
        this.name = name;
        this.adminName = adminName;
        this.reason = reason;
        this.server = server;
        this.unbanDate = unbanDate;
    }

    public void generateBid() {
        this.bid = bansCollection.countDocuments() + 1;
    }
}