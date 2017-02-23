package com.migo.mq.cluster;

import lombok.Data;

import java.io.Serializable;

/**
 * Author  知秋
 * Created by Auser on 2017/2/23.
 */
@Data
public class Broker implements Serializable{

    private static final long serialVersionUID = 1L;

    /**
     * broker hostname
     */
    private final String host;

    /**
     * broker port
     */
    private final int port;

    private String shost;

    /**
     *
     * @param host      broker hostname
     * @param port      broker port
     */
    public Broker(String host, int port) {
        this.host = host;
        this.port = port;
    }

    @Override
    public String toString() {
        return String.format("%s:%s:%s",  host, port, shost);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Broker broker = (Broker) o;

        if (port != broker.port) return false;
        if (host != null ? !host.equals(broker.host) : broker.host != null) return false;
        if (shost != null ? !shost.equals(broker.shost) : broker.shost != null) return false;
        return true;
    }

    @Override
    public int hashCode() {
        int result = 0;
        result = 31 * result + (host != null ? host.hashCode() : 0);
        result = 31 * result + port;
        result = 31 * result + (shost != null ? shost.hashCode() : 0);
        return result;
    }

}
