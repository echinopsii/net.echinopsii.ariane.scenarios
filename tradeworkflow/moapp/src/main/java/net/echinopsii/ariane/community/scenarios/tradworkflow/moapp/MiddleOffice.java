/**
 * [DEFINE YOUR PROJECT NAME/MODULE HERE]
 * [DEFINE YOUR PROJECT DESCRIPTION HERE] 
 * Copyright (C) 01/09/14 echinopsii
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.echinopsii.ariane.community.scenarios.tradworkflow.moapp;

import net.echinopsii.ariane.community.messaging.api.*;
import net.echinopsii.ariane.community.messaging.common.MomClientFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeoutException;

public class MiddleOffice {

    private final static Logger log = LoggerFactory.getLogger(MiddleOffice.class);

    class RiskReplyWorker implements AppMsgWorker {
        public Map<String, Object> apply(Map<String, Object> message) {
            return message;
        }
    }

    class BOReplyWorker implements AppMsgWorker {
        public Map<String, Object> apply(Map<String, Object> message) {
            return message;
        }
    }

    class MiddleOfficeWorker implements AppMsgWorker {
        private MomClient client;
        private String risk_queue;
        private String bo_queue ;
        private MomRequestExecutor riskRexec = null;
        private MomRequestExecutor boRexec = null;

        public MiddleOfficeWorker(MomClient cli, String rq, String bq) {
            client     = cli;
            risk_queue = rq;
            bo_queue   = bq;
            riskRexec = client.createRequestExecutor();
            boRexec = client.createRequestExecutor();
        }

        public Map<String, Object> apply(final Map<String, Object> message) {
            log.debug("Forward front request to risk service : {" + message.get(MomMsgTranslator.MSG_APPLICATION_ID) + "," + message.get("NAME") + "," +
                      message.get("PRICE") + "," + message.get("ORDER") + "," + message.get("QUANTITY") + " }...");
            Map<String, Object> reply = null;
            try {
                reply = riskRexec.RPC(message, risk_queue, client.getClientID()+"Q01", new RiskReplyWorker());
                log.debug("Forward front request to risk service : DONE");
            } catch (TimeoutException e) {
                e.printStackTrace();
            } catch (MomException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            new Thread(new Runnable() {
                //@Override
                public void run() {
                    log.debug("Forward front request to back office : {" + message.get(MomMsgTranslator.MSG_APPLICATION_ID) + "," + message.get("NAME") + "," +
                              message.get("PRICE") + "," + message.get("ORDER") + "," + message.get("QUANTITY") + " }...");
                    try {
                        boRexec.RPC(message, bo_queue, client.getClientID()+"Q02", new BOReplyWorker());
                        log.debug("Forward front request to back office : DONE");
                    } catch (TimeoutException e) {
                        e.printStackTrace();
                    } catch (MomException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();

            return reply;
        }
    }


    private static final String PROPS_FIELD_MOQUEUE     = "middle_office.queue";
    private static final String PROPS_FIELD_MO_RSKQUEUE = "middle_office.risk_queue";
    private static final String PROPS_FIELD_MO_BOQUEUE  = "middle_office.bo_queue";

    private static String moQueue = "MOQUEUE";

    private MomClient client = null;

    public void start(Properties properties) {
        if (properties != null && properties.get(MomClient.MOM_CLI) != null && properties.get(MomClient.MOM_CLI) instanceof String) {
            if (properties.getProperty(PROPS_FIELD_MO_BOQUEUE)==null) {
                log.error("Error while initializing Middle Office service : back office queue isn't defined...");
                return;
            }

            if (properties.getProperty(PROPS_FIELD_MO_RSKQUEUE)==null) {
                log.error("Error while initializing Middle Office service : risk queue isn't defined...");
                return;
            }

            try {
                client = MomClientFactory.make((String) properties.get(MomClient.MOM_CLI));
            } catch (Exception e) {
                log.error("Error while loading MoM client : " + e.getMessage());
                log.error("Provided MoM client : " + properties.get(MomClient.MOM_CLI));
                return;
            }

            try {
                client.init(properties);
            } catch (Exception e) {
                log.error("Error while initializing MoM client : " + e.getMessage());
                log.error("Provided MoM host : " + properties.get(MomClient.MOM_HOST));
                log.error("Provided MoM port : " + properties.get(MomClient.MOM_PORT));
                client = null;
                return;
            }

            if (properties.getProperty(PROPS_FIELD_MOQUEUE)!=null)
                moQueue = properties.getProperty(PROPS_FIELD_MOQUEUE);

            client.getServiceFactory().requestService(moQueue, new MiddleOfficeWorker(client,
                                                      properties.getProperty(PROPS_FIELD_MO_RSKQUEUE),
                                                      properties.getProperty(PROPS_FIELD_MO_BOQUEUE)));
            log.debug("Middle office waiting requests on " + moQueue + "...");
        }
    }

    public void stop() throws Exception {
        System.out.println("Stop middle office ...");
        if (client!=null)
            client.close();
    }

    public static void main(String[] argv) throws IOException {
        final MiddleOffice middleoffice = new MiddleOffice();
        Properties properties = new Properties();
        InputStream conf = middleoffice.getClass().getResourceAsStream("/middleoffice.properties");
        if (conf==null) {
            log.error("Configuration file middleoffice.properties not found in the classpath");
            System.exit(1);
        }
        properties.load(conf);

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run(){
                try {
                    middleoffice.stop();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        middleoffice.start(properties);
    }
}