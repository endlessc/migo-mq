package com.migo.mq.cluster;

import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * cluster配置活动的broker
 * Author  知秋
 * Created by Auser on 2017/2/24.
 */
public class Cluster {
    private static final Queue<Group> MASTER_BROKER_GROUP = new LinkedBlockingDeque<>();
    private static final Queue<String> MASTER_BROKER_IP = new LinkedBlockingDeque<>();
    private static final ConcurrentHashMap<String/*queueName*/, List<Group>/*List*/> SLAVE_BROKER = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String/*host*/, Set<String>/*queue*/> HOSTS_QUEUES = new ConcurrentHashMap<>();
    private static Group current;

    public static void addGroup(Group group){
        MASTER_BROKER_GROUP.add(group);
        MASTER_BROKER_IP.add(group.getMaster().getHost());
    }

    public static Queue<Group> getMasters(){
        return MASTER_BROKER_GROUP;
    }

    public static ConcurrentHashMap<String, List<Group>> getQueueGroups(){
        return SLAVE_BROKER;
    }

    public static Queue<String> getMasterIps(){
        return MASTER_BROKER_IP;
    }

    public static void addGroups(List<Group> groups){

        if(groups != null){
            MASTER_BROKER_GROUP.addAll(groups);
            for(Group gp:groups){
                if(null != gp.getMaster()){
                    MASTER_BROKER_IP.add(gp.getMaster().getHost());
                }
            }
        }
    }

    public static void addHostQueueName(String host, String queueName){
       /* Set<String> queues = HOSTS_QUEUES.get(host);
        if(queues == null){
            queues = new HashSet<String>();
            HOSTS_QUEUES.put(host, queues);
        }*/
        //看不明白请看上面注释代码
        Set<String> queues = HOSTS_QUEUES.computeIfAbsent(host, k -> new HashSet<String>());
        queues.add(queueName);
    }

    public static void clear(){
        MASTER_BROKER_GROUP.clear();
        MASTER_BROKER_IP.clear();
        SLAVE_BROKER.clear();
        HOSTS_QUEUES.clear();
    }

    public static Group peek(){
        return MASTER_BROKER_GROUP.peek();
    }


    public static Set<String> getQueuesByServerHost(String host){
        return HOSTS_QUEUES.get(host);
    }

    /**
     *
     * @param queueName name list
     * @return ip:queue name array
     */
    public static Map<Broker, List<String>> getCustomerServerByQueues(String[] queueName){
        Map<Broker, List<String>> hostsMap = new HashMap<>();
        if(null != queueName){
            for(String name:queueName){

                if(SLAVE_BROKER.containsKey(name)){
                    List<Group> groups = SLAVE_BROKER.get(name);
                    Broker found = null;
                    for(Group gp:groups){
                        if(gp.getMaster() != null && StringUtils.isNotBlank(gp.getMaster().getShost())){
                            found = gp.getMaster();
                            break;
                        }
                    }
                    //如果遍历的内容中每个group所得到非空broker的shost都为空，所以需要做进一步判断
                    if(found == null && groups.size() > 0){
                        found = groups.get(0).getMaster();
                    }
                    if(null != found){
                        if(hostsMap.get(found) == null){
                            List<String> list = new ArrayList<>();
                            list.add(name);
                            //map<broker，装有相对应queueName的list>
                            hostsMap.put(found, list);
                        }else{
                            //一个broker下可以对应处理多个queue
                            hostsMap.get(found).add(name);
                        }
                    }
                }
            }
        }
        return hostsMap;
    }

    public static Group getCurrent() {
        return current;
    }

    public static void setCurrent(Group current) {
        Cluster.current = current;
    }

    public static void putSlave(String queueName, String masterHost, String slaveHost){
        boolean notFind = true;
        //遍历MASTER_BROKER_GROUP，有匹配上masterHost时，SLAVE_BROKER进行相应获取，没有就同步放进去
        for(Group group : MASTER_BROKER_GROUP){
            if(group.getMaster().getHost().equals(masterHost)){
                List<Group> groups = SLAVE_BROKER.get(queueName);
                if(groups == null){
                    groups = new ArrayList<>();
                    groups.add(group.clone());
                    SLAVE_BROKER.put(queueName, groups);
                }else{
                    SLAVE_BROKER.get(queueName).add(group.clone());
                }
                notFind = false;
                break;
            }
        }
        //假如没有group可处理，那就搞个新group来处理就ok
        if(notFind){
            Group gp = new Group();
            Group temp = MASTER_BROKER_GROUP.peek();
            Broker slave = null;
            boolean slaveFlag = false;
            if(StringUtils.isNotBlank(slaveHost)){
                //如果传近来slaveHost在MASTER_BROKER_IP包含，那就建个新的broker实例，并加入到上面新建的group中，
                // 然后在SLAVE_BROKER上做要处理队列和相应处理group的map映射
                if(MASTER_BROKER_IP.contains(slaveHost)){
                    slave = new Broker(slaveHost, temp.getMaster().getPort());
                    slaveFlag = true;
                }
            }
            if(slaveFlag){
                gp.setSlaveOf(slave);
                List<Group> groups = SLAVE_BROKER.get(queueName);
                if(groups == null){
                    groups = new ArrayList<>();
                    groups.add(gp);
                    SLAVE_BROKER.put(queueName, groups);
                }else{
                    SLAVE_BROKER.get(queueName).add(gp);
                }
            }
        }
    }

    @Override
    public String toString() {
        return "Cluster(" + MASTER_BROKER_GROUP.toArray() + ")";
    }
}
