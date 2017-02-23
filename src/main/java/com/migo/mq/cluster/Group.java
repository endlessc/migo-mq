package com.migo.mq.cluster;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;

/**
 * Author  知秋
 * Created by Auser on 2017/2/23.
 */
@Data
public class Group implements Serializable{
    private static final long serialVersionUID = 1L;

    public static final String QUEUE_INDEX_PREFIX = "mi-";
    private String name;

    private Broker master;

    private Broker slaveOf;

    public Group(){
    }

    /**
     * 提供两种实现，一种带主从，一种不带
     * @param name
     * @param hostname
     * @param port
     */
    public Group(String name, String hostname, int port){
        this(name, hostname, port, null);
    }

    public Group(String name, String hostname, int port, String replicaHost){
        String groupName = "ser";
        if(StringUtils.isNotBlank(name)){
            groupName = name;
        }
        this.setName(groupName + "-");
        this.master = new Broker(hostname, port);
        if(StringUtils.isNotBlank(replicaHost)){
            this.slaveOf = new Broker(replicaHost, port);
        }
    }

    /**
     *
     * @return
     */
    public Group clone(){
        Group newGroup = null;
        if(this.slaveOf == null){
            newGroup = new Group(name, master.getHost(), master.getPort());
        }else{
            newGroup = new Group(name, master.getHost(), master.getPort(), this.slaveOf.getHost());
        }
        newGroup.getMaster().setShost(this.master.getShost());
        return newGroup;
    }

    public String getZkIndexMasterSlave(){
        StringBuilder sb = new StringBuilder(QUEUE_INDEX_PREFIX);
        if(master != null && StringUtils.isNotBlank(master.getHost())){
            sb.append(master.getHost()).append(":");
        }

        if(slaveOf != null && StringUtils.isNotBlank(slaveOf.getHost())){
            sb.append(slaveOf.getHost()).append(":");
        }

        sb.deleteCharAt(sb.lastIndexOf(":"));
        return sb.toString();
    }

    public String toString(){
        return String.format("name:%s,master:[%s],slaveOf:[%s]", name, master==null?"":master.toString(), slaveOf==null?"":slaveOf.toString());
    }
}
