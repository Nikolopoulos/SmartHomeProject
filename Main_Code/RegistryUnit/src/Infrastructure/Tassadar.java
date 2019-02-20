/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Infrastructure;

import Logging.MyLogger;
import Util.Hasher;
import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.*;

/**
 *
 * @author billaros
 */
public class Tassadar {

    //EN TARO TASSADAR
    private static final String OUTPUT_P1 = "<html><head><title>Response</title></head><body>";
    private static final String OUTPUT_P2 = "</body></html>";
    private static final String OUTPUT_HEADERS_P1 = "HTTP/1.1 ";
    private static final String OUTPUT_HEADERS_P2 = "\n"
            + "Content-Type: text/html\n";
    private static final String OUTPUT_END_OF_HEADERS = "\n\n";
    Request request;
    OutputStream out;
    Universe uni;
    ArrayList<Service> Services;
    Socket sock;

    public Tassadar(Request requestArg, OutputStream outArg, Universe uniArg, Socket server) {
        this.request = requestArg;
        this.out = outArg;
        this.uni = uniArg;
        sock = server;
    }

    public synchronized void execute() {
        if (request == null || out == null) {
            throw new NullPointerException();
        }
        Communication comm = new Communication();
        comm.setRequest(request);
        ArrayList<Service> services = new ArrayList<Service>();
        if (request.getURI().equalsIgnoreCase("/register")) {
            comm.setRequestType(Util.Statics.REGISTER_REQUEST);
            System.out.println("request.client: " + request.client);
            String IP = request.Parameters.get("ip");
            int Port = Integer.parseInt(request.getParameters().get("port"));
            boolean result = true;
            for (Aggregator agr : uni.aggregators.values()) {
                if (agr.getIP().equals(IP) && agr.getPort() == Port) {
                    result = false;
                    break;
                }
            }
            System.out.println("test1");
            if (!result) {
                comm.setResponseType(Util.Statics.NOT_PERMITTED_ERROR);
                try {
                    errorRespond(comm, "Already Registered");
                } catch (IOException ex) {
                    Logger.getLogger(Tassadar.class.getName()).log(Level.SEVERE, null, ex);
                }
                System.out.println("test2");
            } else {
                Aggregator agr = new Aggregator(IP, Port);
                //byte[] buf = Hasher.hash((System.currentTimeMillis() + "").toCharArray(), Hasher.getNextSalt());
                String pass = Hasher.generateRandomPassword(10);
                agr.setUid(pass);

                String jsonServices = request.getParameters().get("services");
                JSONObject obj;
                System.out.println("test3");
                try {
                    obj = new JSONObject(jsonServices);
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("Offending line is " + jsonServices);
                    comm.setResponseType(Util.Statics.BAD_REQUEST_ERROR);
                    System.out.println("test4");
                    try {
                        errorRespond(comm, "Services not correctly stated");
                    } catch (IOException ex) {
                        Logger.getLogger(Tassadar.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    return;
                }
                System.out.println("test4.2");
                try {
                    services = new ArrayList<Service>();
                    JSONArray arr = obj.getJSONArray("services");
                    for (int i = 0; i < arr.length(); i++) {
                        String serviceUri = arr.getJSONObject(i).getString("uri");
                        String serviceDescription = arr.getJSONObject(i).getString("description");
                        Service parsed = new Service(agr.getUid() + (serviceUri.startsWith("/") ? ("") : ("/")) + serviceUri, "http://" + agr.IP + ":" + agr.getPort() + (serviceUri.startsWith("/") ? ("") : ("/")) + serviceUri, serviceDescription, agr);
                        services.add(parsed);
                        System.out.println("service:" + parsed.Description);
                        //hackathon code begin
                        if (agr.Port == 8086) {
                            serviceUri = "/sensor/160";
                            serviceDescription = "Returns all sensor data for sensor with id 160";
                            parsed = new Service(agr.getUid() + (serviceUri.startsWith("/") ? ("") : ("/")) + serviceUri, "http://" + agr.IP + ":" + agr.getPort() + (serviceUri.startsWith("/") ? ("") : ("/")) + serviceUri, serviceDescription, agr);
                            services.add(parsed);

                            serviceUri = "/sensor/160/temp";
                            serviceDescription = "Returns temperature data for sensor with id 160";
                            parsed = new Service(agr.getUid() + (serviceUri.startsWith("/") ? ("") : ("/")) + serviceUri, "http://" + agr.IP + ":" + agr.getPort() + (serviceUri.startsWith("/") ? ("") : ("/")) + serviceUri, serviceDescription, agr);
                            services.add(parsed);

                            serviceUri = "/sensor/160/humi";
                            serviceDescription = "Returns humidity data for sensor with id 160";
                            parsed = new Service(agr.getUid() + (serviceUri.startsWith("/") ? ("") : ("/")) + serviceUri, "http://" + agr.IP + ":" + agr.getPort() + (serviceUri.startsWith("/") ? ("") : ("/")) + serviceUri, serviceDescription, agr);
                            services.add(parsed);

                            serviceUri = "/sensor/160/pres";
                            serviceDescription = "Returns atmospheric pressure data for sensor with id 160";
                            parsed = new Service(agr.getUid() + (serviceUri.startsWith("/") ? ("") : ("/")) + serviceUri, "http://" + agr.IP + ":" + agr.getPort() + (serviceUri.startsWith("/") ? ("") : ("/")) + serviceUri, serviceDescription, agr);
                            services.add(parsed);

                        }

                    }

                    agr.services = services;

                } catch (Exception e) {
                    comm.setResponseType(Util.Statics.BAD_REQUEST_ERROR);
                    System.out.println("test5");
                    try {
                        errorRespond(comm, "Services not correctly formated");
                    } catch (IOException ex) {
                        Logger.getLogger(Tassadar.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    return;
                }
                try {
                    System.out.println("test6");
                    for (Service itter : services) {
                        uni.services.put(itter.URI, itter);
                    }
                    uni.aggregators.put(agr.uid, agr);
                    this.registerRespond(comm, agr);
                } catch (Exception e) {
                    try {
                        System.out.println("test7");
                        comm.setResponseType(Util.Statics.INTERNAL_SERVER_ERROR);
                        errorRespond(comm, "Services not correctly formated");
                        return;
                    } catch (IOException ex) {
                        Logger.getLogger(Tassadar.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

            }

        } else if (request.getURI().equalsIgnoreCase("/delete")) {
            JSONObject obj = null;
            String uid = "";
            try {
                uid = request.getParameters().get("uid");
                String jsonServices = request.getParameters().get("services");
                obj = new JSONObject(jsonServices);
            } catch (Exception e) {
                try {
                    comm.setResponseType(Util.Statics.BAD_REQUEST_ERROR);
                    errorRespond(comm, "Malformed Parameters");
                    return;
                } catch (IOException ex) {
                    Logger.getLogger(Tassadar.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            try {
                JSONArray arr = obj.getJSONArray("services");
                ArrayList<String> StringServices = new ArrayList<String>();
                for (int i = 0; i < arr.length(); i++) {
                    String serviceUri = arr.getJSONObject(i).getString("uri");
                    String transformedURI = uid + (serviceUri.startsWith("/") ? ("") : ("/")) + serviceUri;
                    StringServices.add(transformedURI);
                }
                Aggregator agg = null;
                for (Aggregator agr : uni.aggregators.values()) {
                    if (agr.getUid().equals(request.getParameters().get("uid"))) {
                        agg = agr;
                        break;
                    }
                }
                if (agg == null) {
                    throw new NullPointerException();
                }
                ArrayList<Service> toRemove = new ArrayList<Service>();
                for (String serv : StringServices) {
                    for (Service srv : agg.services) {
                        if (srv.URI.equals(serv)) {
                            toRemove.add(srv);

                        }
                    }

                    if (uni.services.containsKey(serv)) {
                        uni.services.remove(serv);
                    }
                }

                for (Service serv : toRemove) {
                    agg.services.remove(serv);

                }

            } catch (NullPointerException e) {
                e.printStackTrace();
                comm.setResponseType(Util.Statics.NOT_PERMITTED_ERROR);
                try {
                    errorRespond(comm, "You don't have permission to delete some or all of the services. Check UID");
                } catch (IOException ex) {
                    Logger.getLogger(Tassadar.class.getName()).log(Level.SEVERE, null, ex);
                }
                return;
            } catch (Exception e) {
                e.printStackTrace();
                comm.setResponseType(Util.Statics.BAD_REQUEST_ERROR);
                try {
                    errorRespond(comm, "Services not correctly formated");
                } catch (IOException ex) {
                    Logger.getLogger(Tassadar.class.getName()).log(Level.SEVERE, null, ex);
                }
                return;
            }
            try {
                comm.setResponseType(Util.Statics.OK_RESPONSE);
                this.genericRespond(comm);
            } catch (Exception e) {
                comm.setResponseType(Util.Statics.INTERNAL_SERVER_ERROR);
                try {
                    errorRespond(comm, "Internal Server Error");
                } catch (IOException ex) {
                    Logger.getLogger(Tassadar.class.getName()).log(Level.SEVERE, null, ex);
                }
                return;
            }
        } else if (request.getURI().equalsIgnoreCase("/update")) {
            JSONObject obj;
            String uid = "";
            try {
                uid = request.getParameters().get("uid");
                String jsonServices = request.getParameters().get("services");
                obj = new JSONObject(jsonServices);
            } catch (Exception e) {
                comm.setResponseType(Util.Statics.BAD_REQUEST_ERROR);
                try {
                    errorRespond(comm, "Malformed Parameters or wrong UID");
                } catch (IOException ex) {
                    Logger.getLogger(Tassadar.class.getName()).log(Level.SEVERE, null, ex);
                }
                return;
            }
            try {
                JSONArray arr = obj.getJSONArray("services");
                Aggregator agg = null;
                int aggArrId = -1;
                System.out.println("test1 of update");

                for (int aggCount = 0; aggCount < uni.aggregators.size(); aggCount++) {
                    System.out.println("test2 of update");
                    if (uni.aggregators.get(aggCount).getUid().equals(request.getParameters().get("uid"))) {
                        aggArrId = aggCount;
                        break;
                    } else {
                        System.out.println("uid needed is " + uni.aggregators.get(aggCount).getUid() + " and i got " + request.getParameters().get("uid"));
                    }
                }
                System.out.println("test3 of update");
                if (aggArrId == -1) {
                    System.out.println("uid i got " + request.getParameters().get("uid"));
                    throw new NullPointerException();
                }
                System.out.println("test4 of update");
                for (int i = 0; i < arr.length(); i++) {
                    String serviceUri = arr.getJSONObject(i).getString("uri");
                    String serviceDescription = arr.getJSONObject(i).getString("description");
                    Service parsed = new Service(uid + (serviceUri.startsWith("/") ? ("") : ("/")) + serviceUri, "http://" + uni.aggregators.get(aggArrId).IP + ":" + uni.aggregators.get(aggArrId).getPort() + (serviceUri.startsWith("/") ? ("") : ("/")) + serviceUri, serviceDescription, uni.aggregators.get(aggArrId));
                    services.add(parsed);
                }
                System.out.println("test5 of update");
                try {
                    for (Service srv : services) {
                        for (Service asrv : uni.aggregators.get(aggArrId).services) {
                            if (asrv.URI.equals(srv.URI)) {
                                uni.aggregators.get(aggArrId).services.remove(asrv);
                            }
                        }
                        uni.aggregators.get(aggArrId).services.add(srv);
                        if (uni.services.containsKey(srv.URI)) {
                            uni.services.remove(srv.getURI());
                            uni.services.put(srv.getURI(), srv);
                        }
                    }
                } catch (Exception es) {
                    es.printStackTrace();
                }
                System.out.println("test6 of update");
            } catch (NullPointerException e) {
                comm.setResponseType(Util.Statics.NOT_PERMITTED_ERROR);
                try {
                    errorRespond(comm, "You don't have permission to update some or all of the services. Check UID");
                } catch (IOException ex) {
                    Logger.getLogger(Tassadar.class.getName()).log(Level.SEVERE, null, ex);
                }
                return;
            } catch (Exception e) {
                comm.setResponseType(Util.Statics.BAD_REQUEST_ERROR);
                try {
                    errorRespond(comm, "Services not correctly formated");
                } catch (IOException ex) {
                    Logger.getLogger(Tassadar.class.getName()).log(Level.SEVERE, null, ex);
                }
                return;
            }

            try {
                comm.setResponseType(Util.Statics.OK_RESPONSE);
                this.genericRespond(comm);
            } catch (Exception e) {
                try {
                    comm.setResponseType(Util.Statics.INTERNAL_SERVER_ERROR);
                    errorRespond(comm, "Internal Server Error");
                    return;
                } catch (IOException ex) {
                    Logger.getLogger(Tassadar.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } else if (request.getURI().equalsIgnoreCase("/getServices")) {
            System.out.println("test 1 of get services");
            try {
                String servicesJSON = "[";
                for (Aggregator agg : uni.aggregators.values()) {
                    for (Service srv : agg.services) {
                        servicesJSON += "{";
                        servicesJSON += "\"url\" : \"" + srv.canonURI + "\" , \"sid\" : \"" + srv.getId() + "\"";
                        servicesJSON += "},";
                    }
                }
                System.out.println("test 2 of get services");
                if (servicesJSON.length() > 5) {
                    servicesJSON = servicesJSON.substring(0, servicesJSON.length() - 1);
                }
                servicesJSON += "]";
                comm.setResponseType(Util.Statics.OK_RESPONSE);
                System.out.println("test 3 of get services");
                servicesGetRespond(comm, servicesJSON);
            } catch (Exception e) {
                comm.setResponseType(Util.Statics.INTERNAL_SERVER_ERROR);
                try {
                    errorRespond(comm, "Internal Server Error");
                } catch (IOException ex) {
                    Logger.getLogger(Tassadar.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        } else if (request.getURI().equalsIgnoreCase("/getAggregators")) {
            try {
                String aggregatorsJSON = "[";
                for (Aggregator agg : uni.aggregators.values()) {

                    aggregatorsJSON += "{";
                    aggregatorsJSON += "\"url\" = \"" + agg.getIP() + ":" + agg.Port + "\"";
                    aggregatorsJSON += "},";

                }
                if (aggregatorsJSON.length() > 5) {
                    aggregatorsJSON = aggregatorsJSON.substring(0, aggregatorsJSON.length() - 1);
                }
                aggregatorsJSON += "]";
                comm.setResponseType(Util.Statics.OK_RESPONSE);
                aggregatorsGetRespond(comm, aggregatorsJSON);
            } catch (Exception e) {
                comm.setResponseType(Util.Statics.INTERNAL_SERVER_ERROR);
                try {
                    errorRespond(comm, "Internal Server Error");
                } catch (IOException ex) {
                    Logger.getLogger(Tassadar.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } else if (request.getURI().equalsIgnoreCase("/describe")) {
            try {
                String ID = request.getParameters().get("sid");
                String describeJSON = "";
                boolean found = false;
                for (Aggregator agg : uni.aggregators.values()) {
                    for (Service srv : agg.services) {
                        if (srv.getId().equals(ID)) {
                            describeJSON = srv.Description;
                            found = true;
                            break;
                        }
                    }
                    if (found) {
                        break;
                    }
                }

                if (describeJSON.equals("")) {
                    comm.setResponseType(Util.Statics.GONE_ERROR);
                    errorRespond(comm, "Service id not found");
                }
                comm.setResponseType(Util.Statics.OK_RESPONSE);
                describeRespond(comm, describeJSON);
            } catch (Exception e) {
                try {
                    comm.setResponseType(Util.Statics.INTERNAL_SERVER_ERROR);
                    errorRespond(comm, "Internal Server Error");
                } catch (IOException ex) {
                    Logger.getLogger(Tassadar.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } else if (request.getURI().equalsIgnoreCase("/whoIsAGoodTeapot")) {
            try {
                comm.setResponseType(Util.Statics.IM_A_TEAPOT_ERROR);
                errorRespond(comm, "I'M A GOOD TEAPOT :D");
            } catch (IOException ex) {
                Logger.getLogger(Tassadar.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else if (request.getURI().equalsIgnoreCase("/log")) {
            try {
                comm.setResponseType(Util.Statics.OK_RESPONSE);
                logRespond(comm);
            } catch (IOException ex) {
                Logger.getLogger(Tassadar.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else if (request.getURI().startsWith("/topics")) {
            System.out.println("parsed uri ok");
            //not going to fix the above madness, but I will try to amend future work on it
            manageTopicRequest(request, comm);
        } else {
            try {
                comm.setResponseType(Util.Statics.SERVICE_UNAVAILABLE_ERROR);
                errorRespond(comm, "Service not found or service unavailable");
            } catch (IOException ex) {
                Logger.getLogger(Tassadar.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        /* 
         registryIP:8282/register -> uses post headers to register a list of services available at an aggregator. 
         The registry unit responds with a unique ID that the aggregator should use in following API calls as a 
         parameter.

         registryIP:8282/delete -> uses post headers to delete a specific service earlier registered at the 
         registry unit. Registry unit responds with OK or NOT_OK followed by an error code
        
         registryIP:8282/update -> uses post headers to update a specific service earlier registered at the registry 
         unit. Registry unit responds with OK or NOT_OK followed by an error code

         registryIP:8282/getServices -> returns a list of all services currently registered at the registry unit.
    
         registryIP:8282/getAggregators -> returns a list of all available aggregators.
        
         registryIP:8282/describe/serviceID ->  returns a description provided from the aggregator at register 
         time for service with id serviceID. It should be noted that an aggregator does not need to 
         know of the services' IDs as they are used only by the registry unit to denote  different services.
        
         registryIP:8282/describe/aggregatorID/path/service -> Same as before, but with different notation for ease 
         of use
         return;
         */

    }

    private void manageTopicRequest(Request request, Communication comm) {
        String[] requestPath = request.getURI().split("/");
        if (requestPath.length == 2) {
            try {
                Gson gson = new Gson();
                String topicsList = gson.toJson(uni.topics);
                normalPersonsResponseThatIsNotLikeATotalPieceOfShit(comm, topicsList, Util.Statics.OK_RESPONSE);
                return;
            } catch (IOException ex) {
                Logger.getLogger(Tassadar.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        String action = requestPath[2];
        System.out.println("requestPath[1] is " + requestPath[1]);
        System.out.println("requestPath[2] is " + requestPath[2]);

        if (action.equals("create")) {
            try {
                try {
                    BufferedReader br = request.getReader();
                    String body = "";
                    String line = "";
                    while (br.ready()) {
                        line = br.readLine();
                        System.out.println("l is " + line);
                        body += line;
                    }
                    System.out.println("Body is ");
                    System.out.println(body);
                    //br.close();
                    try {
                        Gson gson = new Gson();
                        BasicTopic basicTopic = gson.fromJson(body, BasicTopic.class);
                        if (basicTopic.description == null || basicTopic.machineDescription == null || basicTopic.name == null) {
                            throw new Exception("Invalid Json File");
                        }
                        System.out.println("[" + (new Date()) + "] [Registry0x00] Received request to create topic with machine description " + basicTopic.machineDescription);

                        int id = uni.topics.size() + 1;
                        uni.topics.put(id, new FullTopic(basicTopic, id));
                        normalPersonsResponseThatIsNotLikeATotalPieceOfShit(comm, "", Util.Statics.NO_CONTENT);
                        System.out.println("[" + (new Date()) + "] [Registry0x00] Created topic with machine description " + basicTopic.machineDescription);
                    } catch (Exception e) {
                        System.out.println("test");
                        errorRespond(comm, "Sent body is not a valid JSON Topic. Details: " + e.getLocalizedMessage(), Util.Statics.BAD_REQUEST_ERROR);
                        return;
                    }
                } catch (IOException ex) {
                    Logger.getLogger(Tassadar.class.getName()).log(Level.SEVERE, null, ex);
                }

                errorRespond(comm, "something went terribly wrong. sorry bro", Util.Statics.BAD_REQUEST_ERROR);
            } catch (IOException ex) {
                Logger.getLogger(Tassadar.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else if (action.equals("subscribe")) {
            try {
                Integer topicId = requestPath.length > 3 ? Integer.parseInt(requestPath[3]) : null;
                String aggregatorUid = requestPath.length > 4 ? requestPath[4] : null;

                if (topicId == null || aggregatorUid == null) {
                    try {
                        errorRespond(comm, "you dun goofed, missing parameters", Util.Statics.BAD_REQUEST_ERROR);
                    } catch (IOException ex) {
                        Logger.getLogger(Tassadar.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

                Aggregator agg = uni.aggregators.get(aggregatorUid);

                if (agg == null) {
                    try {
                        errorRespond(comm, "why are you like that? please specify a valid aggregator id", Util.Statics.UNAUTHORIZED_ERROR);
                    } catch (IOException ex) {
                        Logger.getLogger(Tassadar.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

                if (!uni.topics.containsKey(topicId)) {
                    try {
                        errorRespond(comm, "why are you like that? please specify a valid topic id", Util.Statics.NOT_FOUND_ERROR);
                    } catch (IOException ex) {
                        Logger.getLogger(Tassadar.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                System.out.println("[" + (new Date()) + "] [Registry0x00] Received request to subscribe aggregator " + agg.uid + " to topic " + topicId);

                uni.topics.get(topicId).participants.add(agg);

                normalPersonsResponseThatIsNotLikeATotalPieceOfShit(comm, "", Util.Statics.NO_CONTENT);
                System.out.println("[" + (new Date()) + "] [Registry0x00] Subscribed aggregator " + agg.uid + " to topic " + topicId);
            } catch (IOException ex) {
                Logger.getLogger(Tassadar.class.getName()).log(Level.SEVERE, null, ex);
            }

        } else if (action.equals("unsubscribe")) {
            Integer topicId = requestPath.length > 3 ? Integer.parseInt(requestPath[3]) : null;
            String aggregatorUid = requestPath.length > 4 ? requestPath[4] : null;

            if (topicId == null || aggregatorUid == null) {
                try {
                    errorRespond(comm, "you dun goofed, missing parameters", Util.Statics.BAD_REQUEST_ERROR);
                } catch (IOException ex) {
                    Logger.getLogger(Tassadar.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            Aggregator agg = uni.aggregators.get(aggregatorUid);

            if (agg == null) {
                try {
                    errorRespond(comm, "why are you like that? please specify a valid aggregator id", Util.Statics.UNAUTHORIZED_ERROR);
                } catch (IOException ex) {
                    Logger.getLogger(Tassadar.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            if (!uni.topics.containsKey(topicId)) {
                try {
                    errorRespond(comm, "why are you like that? please specify a valid topic id", Util.Statics.NOT_FOUND_ERROR);
                } catch (IOException ex) {
                    Logger.getLogger(Tassadar.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            System.out.println("[" + (new Date()) + "] [Registry0x00] Received request to unsubscribe aggregator " + agg.uid + " to topic " + topicId);

            uni.topics.get(topicId).participants.remove(agg);

            try {
                normalPersonsResponseThatIsNotLikeATotalPieceOfShit(comm, "", Util.Statics.NO_CONTENT);
            } catch (IOException ex) {
                Logger.getLogger(Tassadar.class.getName()).log(Level.SEVERE, null, ex);
            }
            System.out.println("[" + (new Date()) + "] [Registry0x00] Unsubscribed aggregator " + agg.uid + " to topic " + topicId);

        } else if (action.equals("participants")) {
            try {
                Integer topicId = requestPath.length > 3 ? Integer.parseInt(requestPath[3]) : null;

                if (topicId == null) {
                    try {
                        errorRespond(comm, "you dun goofed, missing parameters", Util.Statics.BAD_REQUEST_ERROR);
                    } catch (IOException ex) {
                        Logger.getLogger(Tassadar.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

                if (!uni.topics.containsKey(topicId)) {
                    try {
                        errorRespond(comm, "why are you like that? please specify a valid topic id", Util.Statics.NOT_FOUND_ERROR);
                    } catch (IOException ex) {
                        Logger.getLogger(Tassadar.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                System.out.println("[" + (new Date()) + "] [Registry0x00] Received request to list participants for topic with id " + topicId);

                Gson gson = new Gson();
                String participantsList = gson.toJson(uni.topics.get(topicId).participants);
                normalPersonsResponseThatIsNotLikeATotalPieceOfShit(comm, participantsList, Util.Statics.OK_RESPONSE);
                System.out.println("[" + (new Date()) + "] [Registry0x00] Listed participants for topic with id " + topicId);
            } catch (IOException ex) {
                Logger.getLogger(Tassadar.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
    }

    private void logRespond(Communication comm) throws IOException {
        String response = MyLogger.readLog();
        comm.setAnswer(response);
        uni.comms.add(comm);
        out.write(comm.getAnswer().getBytes());
        out.flush();
        comm.getRequest().socket.close();
        //out.close();
    }

    private void normalPersonsResponseThatIsNotLikeATotalPieceOfShit(Communication comm, String preFormattedMessage, int status) throws IOException {
        String response = preFormattedMessage;
        comm.setResponseType(status);
        comm.setAnswer(response + "\n\n\n\n\n\n");
        uni.comms.add(comm);
        out.write(comm.getAnswer().getBytes());
        out.flush();
        comm.getRequest().socket.close();
        //out.close();
    }

    private void errorRespond(Communication comm, String message, int status) throws IOException {
        comm.setResponseType(status);

        String response = "{\"result\" : \"fail\", \"reason\" : \"" + comm.getResponseType() + "\", \"message\" : \"" + message + "\"}";
        String headers = "HTTP/1.1 200 OK\n"
                + "Date: Wed, 20 Feb 2019 13:57:55 GMT\n"
                + "Content-Type: text; charset=UTF-8\n"
                + "Content-Length: " + response.length();
        comm.setAnswer(headers + "\n\n" + response);
        System.out.println("Socket is closed:" + comm.getRequest().socket.isConnected());

        out.write(comm.getAnswer().getBytes());
        System.out.println("Socket is closed:" + comm.getRequest().socket.isClosed());
        out.flush();
        System.out.println("Socket is closed:" + comm.getRequest().socket.isClosed());
//        
//        out.close();
        comm.getRequest().socket.shutdownOutput();
        System.out.println("Socket is closed:" + comm.getRequest().socket.isClosed());
        comm.getRequest().socket.shutdownInput();
        System.out.println("Socket is closed:" + comm.getRequest().socket.isClosed());
        comm.getRequest().socket.close();
        System.out.println("Socket is closed:" + comm.getRequest().socket.isClosed());
//        uni.comms.add(comm);

    }

    private void errorRespond(Communication comm, String message) throws IOException {
        String response = "{\"result\" : \"fail\", \"reason\" : \"" + comm.getResponseType() + "\", \"message\" : \"" + message + "\"}";
        comm.setAnswer(response);
        uni.comms.add(comm);
        out.write(comm.getAnswer().getBytes());
        out.flush();
        comm.getRequest().socket.close();
        //out.close();
    }

    private void registerRespondDump(Communication comm, Aggregator ag) throws IOException {
        String response = "{\"result\" = \"success\", \"reason\" = \"" + comm.getResponseType() + "\", \"uid\" = \"" + ag.getUid() + "\"}";
        comm.setAnswer(response);
        uni.comms.add(comm);
        out.write(comm.getAnswer().getBytes());
        out.flush();
        comm.getRequest().socket.close();
        //out.close();

    }

    private void genericRespond(Communication comm) throws IOException {
        String response = "{\"result\" : \"success\", \"reason\" : \"" + comm.getResponseType() + "\"}";
        comm.setAnswer(response);
        uni.comms.add(comm);
        out.write(comm.getAnswer().getBytes());
        out.flush();
        comm.getRequest().socket.close();
        //out.close();
    }

    private void servicesGetRespond(Communication comm, String services) {
        try {
            System.out.println("test8");
            String response = "{\"result\" : \"success\", \"reason\" : \"" + comm.getResponseType() + "\", \"services\" : " + services + "}";
            uni.comms.add(comm);
            out.write(response.getBytes());
            comm.setAnswer(formatResponse(" 200 OK ", response));
            System.out.write(comm.getAnswer().getBytes());
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("test9");
        }
    }

    private void aggregatorsGetRespond(Communication comm, String aggregators) {
        try {
            String response = "{\"result\" : \"success\", \"reason\" : \"" + comm.getResponseType() + "\", \"aggregators\" : \"" + aggregators + "\"}";
            comm.setAnswer(response);
            uni.comms.add(comm);
            out.write(comm.getAnswer().getBytes());
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void describeRespond(Communication comm, String description) throws IOException {
        String response = "{\"result\" : \"success\", \"reason\" : \"" + comm.getResponseType() + "\",\"sid\" : \"" + comm.getRequest().getParameters().get("sid") + "\", \"description\" = \"" + description + "\"}";
        comm.setAnswer(response);
        uni.comms.add(comm);
        out.write(comm.getAnswer().getBytes());
        out.flush();
        out.close();
    }

    private void registerRespond(Communication comm, Aggregator ag) {
        try {
            System.out.println("test8");
            String jsonR = "{\"result\" : \"success\", \"reason\" : \"" + comm.getResponseType() + "\", \"uid\" : \"" + ag.getUid() + "\"}\n";
            System.out.println("out.checkError() ");
            System.out.println("Let's test the socket: \n"
                    + "closed? " + sock.isClosed() + "\n"
                    + "bound? " + sock.isBound() + "\n"
                    + "connected? " + sock.isConnected() + "\n"
                    + "inputshutdown? " + sock.isInputShutdown() + "\n"
                    + "outputshutdown? " + sock.isOutputShutdown() + "\n"
                    + "bound? " + sock.isBound() + "\n************");
            sock.getOutputStream().flush();
            System.out.println("Let's test the socket: \n"
                    + "closed? " + sock.isClosed() + "\n"
                    + "bound? " + sock.isBound() + "\n"
                    + "connected? " + sock.isConnected() + "\n"
                    + "inputshutdown? " + sock.isInputShutdown() + "\n"
                    + "outputshutdown? " + sock.isOutputShutdown() + "\n"
                    + "bound? " + sock.isBound() + "\n************");
            sock.getOutputStream().write(jsonR.getBytes("UTF-8"));//.print(jsonR);
            System.out.println("Let's test the socket: \n"
                    + "closed? " + sock.isClosed() + "\n"
                    + "bound? " + sock.isBound() + "\n"
                    + "connected? " + sock.isConnected() + "\n"
                    + "inputshutdown? " + sock.isInputShutdown() + "\n"
                    + "outputshutdown? " + sock.isOutputShutdown() + "\n" + "\n************");
            out.write("\0".getBytes());
            String response = jsonR;
            uni.comms.add(comm);
            comm.setAnswer(formatResponse(" 200 OK ", response));
            System.out.println("flushed");
            System.out.println("appended");

            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("test9");
        }
    }

    private String formatResponse(String status, String response) {
        return OUTPUT_HEADERS_P1 + status + OUTPUT_HEADERS_P2 + OUTPUT_END_OF_HEADERS + OUTPUT_P1 + response + OUTPUT_P2;
    }
}
