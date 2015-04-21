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

import net.echinopsii.ariane.community.core.messaging.api.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

public class MiddleOffice {

    class RiskReplyWorker implements AppMsgWorker {
        @Override
        public Map<String, Object> apply(Map<String, Object> message) {
            return message;
        }
    }

    class BOReplyWorker implements AppMsgWorker {
        @Override
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

        @Override
        public Map<String, Object> apply(final Map<String, Object> message) {
            System.out.println("Forward front request to risk service : {" + message.get(MomMsgTranslator.MSG_APPLICATION_ID) + "," + message.get("NAME") + "," +
                               message.get("PRICE") + "," + message.get("ORDER") + "," + message.get("QUANTITY") + " }...");
            Map<String, Object> reply = riskRexec.RPC(message, risk_queue, client.getClientID()+"Q01", new RiskReplyWorker());
            System.out.println("Forward front request to risk service : DONE");

            new Thread(new Runnable() {
                @Override
                public void run() {
                    System.out.println("Forward front request to back office : {" + message.get(MomMsgTranslator.MSG_APPLICATION_ID) + "," + message.get("NAME") + "," +
                                        message.get("PRICE") + "," + message.get("ORDER") + "," + message.get("QUANTITY") + " }...");
                    boRexec.RPC(message, bo_queue, client.getClientID()+"Q02", new BOReplyWorker());
                    System.out.println("Forward front request to back office : DONE");
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
                System.err.println("Error while initializing Middle Office service : back office queue isn't defined...");
                return;
            }

            if (properties.getProperty(PROPS_FIELD_MO_RSKQUEUE)==null) {
                System.err.println("Error while initializing Middle Office service : risk queue isn't defined...");
                return;
            }

            try {
                client = MomClientFactory.make((String) properties.get(MomClient.MOM_CLI));
            } catch (Exception e) {
                System.err.println("Error while loading MoM client : " + e.getMessage());
                System.err.println("Provided MoM client : " + properties.get(MomClient.MOM_CLI));
                return;
            }

            try {
                client.init(properties);
            } catch (Exception e) {
                System.err.println("Error while initializing MoM client : " + e.getMessage());
                System.err.println("Provided MoM host : " + properties.get(MomClient.MOM_HOST));
                System.err.println("Provided MoM port : " + properties.get(MomClient.MOM_PORT));
                client = null;
                return;
            }

            if (properties.getProperty(PROPS_FIELD_MOQUEUE)!=null)
                moQueue = properties.getProperty(PROPS_FIELD_MOQUEUE);

            client.getServiceFactory().requestService(moQueue, new MiddleOfficeWorker(client,
                                                      properties.getProperty(PROPS_FIELD_MO_RSKQUEUE),
                                                      properties.getProperty(PROPS_FIELD_MO_BOQUEUE)));
            System.out.println("Middle office waiting requests on " + moQueue + "...");
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
            System.out.println("Configuration file middleoffice.properties not found in the classpath");
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