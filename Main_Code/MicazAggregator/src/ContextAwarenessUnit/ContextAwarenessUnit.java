/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ContextAwarenessUnit;

import ContextAwarenessUnit.contextClasses.Aggregator;
import ContextAwarenessUnit.contextClasses.FullTopic;
import ContextAwarenessUnit.contextClasses.MigrationInfo;
import ContextAwarenessUnit.contextClasses.TopicInformationPiece;
import ControlUnit.Control;
import SensorsCommunicationUnit.MicazMote;
import ServiceProvisionUnit.RequestObject;
import ServiceProvisionUnit.ServiceProvisionUnit;
import SharedMemory.SharedMemory;
import com.google.gson.Gson;
import java.net.InetAddress;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author billaros
 */
public class ContextAwarenessUnit {

    private HashMap<String, Object> context = new HashMap();
    private final SharedMemory memory = SharedMemory.<String, SharedMemory>get("SMU");
    private boolean amISubbed = false;

    public void handleNewInformation(String jsonString) {
        System.out.println("********************************************************");
        System.out.println(jsonString);
        System.out.println("********************************************************");
        Gson gson = new Gson();
        TopicInformationPiece information = gson.fromJson(jsonString, TopicInformationPiece.class);

        if (information.getInformationType().equals("MigrationInfo")) {
            MigrationInfo migInfo = gson.fromJson(information.getObject(), MigrationInfo.class);
            if (!context.containsKey("MigrationInfo")) {
                context.put("MigrationInfo", new HashMap<String, MigrationInfo>());
            }
            ((HashMap<String, MigrationInfo>) context.get("MigrationInfo")).put(migInfo.getIp() + ":" + migInfo.getPort(), migInfo);
        }

    }

    private void handleNewTopicInformation(String jsonString) {
        Gson gson = new Gson();
        Map<String, Map> topics = gson.fromJson(jsonString, Map.class);
        if (!context.containsKey("topics")) {
            context.put("topics", new HashMap<String, FullTopic>());
        }

        topics.values().forEach((topic) -> {
            FullTopic parsedTopic = new FullTopic(((Double) topic.get("id")).intValue(), (String) topic.get("description"), (String) topic.get("machineDescription"), (ArrayList<Aggregator>) topic.get("participants"), (String) topic.get("name"));
            ArrayList<Aggregator> aggs = new ArrayList<>();
            for (Object agg : parsedTopic.getParticipants()) {
                Aggregator parsedAggregator = new Aggregator((String) ((Map) agg).get("IP"), ((Double) ((Map) agg).get("Port")).intValue());
                aggs.add(parsedAggregator);
            }
            parsedTopic.setParticipants(aggs);

            ((HashMap<String, FullTopic>) context.get("topics")).put(parsedTopic.getId() + "", parsedTopic);
        });

        //totally legit way
        if (((HashMap<String, FullTopic>) context.get("topics")).values().size() < 1) {
            createTheProperTopic();
        }

        if (!amISubbed) {
            subToTheProperTopic();
            amISubbed = true;
        }
    }

    private void createTheProperTopic() {
        RequestObject ro = memory.<String, ServiceProvisionUnit>get("SPU").httpContact(
                new RequestObject(
                        "http://" + memory.<String, String>get("registryUnitIP"),
                        memory.<String, Integer>get("registryPort").intValue(),
                        URLEncoder.encode("body={\n"
                                + "\"description\": \"micaz_migration_policy_discussion_group\",\n"
                                + "\"machineDescription\": \"micaz_migration/load/driver/visible_sensors/ip/port\",\n"
                                + "\"name\":\"topic_1\"\n"
                                + "}"),
                        "/topics/create",
                        memory.<String, InetAddress>get("addr"),
                        "Post"));
    }

    private void subToTheProperTopic() {
        RequestObject ro = memory.<String, ServiceProvisionUnit>get("SPU").httpContact(
                new RequestObject(
                        "http://" + memory.<String, String>get("registryUnitIP"),
                        memory.<String, Integer>get("registryPort").intValue(),
                        "",
                        "/topics/subscribe/1/" + SharedMemory.<String, Control>get("MCU").getUid(),
                        memory.<String, InetAddress>get("addr"),
                        "GET")
        );
    }

    private void sendNewInformation(int topicId) {
        ArrayList<String> drivers = new ArrayList<String>();
        drivers.add("Micaz");
        ArrayList<MicazMote> visibleSensors = new ArrayList<MicazMote>();
        MicazMote mote1 = new MicazMote(1, 2, System.currentTimeMillis() - 100);
        MicazMote mote2 = new MicazMote(2, 2, System.currentTimeMillis() - 100);
        MicazMote mote3 = new MicazMote(3, 2, System.currentTimeMillis() - 100);
        MicazMote mote4 = new MicazMote(4, 2, System.currentTimeMillis() - 100);
        visibleSensors.add(mote1);
        visibleSensors.add(mote2);
        visibleSensors.add(mote3);
        visibleSensors.add(mote4);
        MigrationInfo migInfo = null;
        try {
            migInfo = new MigrationInfo(
                    Float.valueOf((SharedMemory.<String, Control>get("MCU").getProcessCpuLoad() * 100) + ""),
                    drivers,
                    visibleSensors,
                    SharedMemory.<String, Control>get("MCU").ip,
                    SharedMemory.<String, Integer>get("myPort") + "");
            if (((HashMap<String, FullTopic>) context.get("topics")).get(topicId + "") == null || ((HashMap<String, FullTopic>) context.get("topics")).get(topicId + "").getParticipants() == null) {
                return;
            }
            for (Aggregator agg : ((HashMap<String, FullTopic>) context.get("topics")).get(topicId + "").getParticipants()) {
                if ((!agg.getIP().equals(memory.<String, String>get("ip"))) || agg.getPort() != memory.<String, Integer>get("myPort")) {
                    sendUpdateTo(agg, topicId, migInfo);
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(ContextAwarenessUnit.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void sendUpdateTo(Aggregator agg, int topicId, MigrationInfo topic) {
        Gson gson = new Gson();
        TopicInformationPiece topPiece = new TopicInformationPiece("MigrationInfo", gson.toJson(topic));

        System.out.println("**************Sending some shit!!******************");
        memory.<String, ServiceProvisionUnit>get("SPU").httpContact(new RequestObject(
                "http://" + agg.getIP(),
                agg.getPort(),
                URLEncoder.encode(gson.toJson(topPiece)),
                "/topics/update/" + topicId,
                memory.<String, InetAddress>get("addr"),
                "POST"));
        System.out.println("**************Sent some shit!!******************");
    }

    private Thread getTopicListUpdateDaemon() {
        Thread t = new Thread(() -> {
            while (true) {
                try {
                    RequestObject ro = memory.<String, ServiceProvisionUnit>get("SPU").httpContact(
                            new RequestObject(
                                    "http://" + memory.<String, String>get("registryUnitIP"),
                                    memory.<String, Integer>get("registryPort").intValue(),
                                    "",
                                    "/topics",
                                    memory.<String, InetAddress>get("addr"),
                                    "GET")
                    );
                    String response = ro.getResponse();
                    handleNewTopicInformation(response);

                } catch (Exception ex) {
                    Logger.getLogger(ContextAwarenessUnit.class.getName()).log(Level.SEVERE, null, ex);
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    //ignored
                }
            }
        });
        return t;
    }

    private Thread sendTopicUpdateThread() {
        Thread t = new Thread(() -> {
            while (true) {
                if (!context.containsKey("topics") || ((HashMap<String, FullTopic>) context.get("topics")).values().size() < 1) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        //ignored
                    }
                    continue;
                }

                try {
                    sendNewInformation(1);
                } catch (Exception ex) {
                    Logger.getLogger(ContextAwarenessUnit.class.getName()).log(Level.SEVERE, null, ex);
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    //ignored
                }
            }
        });
        return t;
    }

    public ContextAwarenessUnit() {
        Thread topicsThread = this.getTopicListUpdateDaemon();
        topicsThread.start();

        Thread updates = this.sendTopicUpdateThread();
        updates.start();
    }
}
