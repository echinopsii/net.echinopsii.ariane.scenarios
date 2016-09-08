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

package net.echinopsii.ariane.community.scenarios.tradeworkflow.boapp;

import net.echinopsii.ariane.community.messaging.api.AppMsgWorker;
import net.echinopsii.ariane.community.messaging.api.MomClient;
import net.echinopsii.ariane.community.messaging.common.MomClientFactory;
import net.echinopsii.ariane.community.messaging.api.MomMsgTranslator;
import net.echinopsii.ariane.community.scenarios.cassandra.Connector;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class BackOffice {

    class BackOfficeWorker implements AppMsgWorker {
        public Map<String, Object> apply(Map<String, Object> message) {
            System.out.println("Back office work on  : {" + message.get(MomMsgTranslator.MSG_APPLICATION_ID) + "," + message.get("NAME") + "," +
                    message.get("PRICE") + "," + message.get("ORDER") + "," + message.get("QUANTITY") + " }...");
            try {
                new Thread().sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("Back Office return DONE");
            Map<String, Object> reply = new HashMap<String, Object>();
            reply.put(MomMsgTranslator.MSG_BODY, "DONE");
            return reply;
        }
    }

    private static final String PROPS_FIELD_BOQUEUE = "back_office.queue";
    private static String backOfficeQueue = "BOQUEUE";

    private MomClient momClient = null;
    private Connector cassandraConnector = null;

    public void start(Properties properties) {
        if (properties != null && properties.get(MomClient.MOM_CLI) != null && properties.get(MomClient.MOM_CLI) instanceof String) {
            try {
                momClient = MomClientFactory.make((String) properties.get(MomClient.MOM_CLI));
            } catch (Exception e) {
                System.err.println("Error while loading MoM momClient : " + e.getMessage());
                System.err.println("Provided MoM momClient : " + properties.get(MomClient.MOM_CLI));
                return;
            }

            try {
                momClient.init(properties);
            } catch (Exception e) {
                System.err.println("Error while initializing MoM Client : " + e.getMessage());
                System.err.println("Provided MoM host : " + properties.get(MomClient.MOM_HOST));
                System.err.println("Provided MoM port : " + properties.get(MomClient.MOM_PORT));
                momClient = null;
                return;
            }

            if (properties.getProperty(PROPS_FIELD_BOQUEUE)!=null)
                backOfficeQueue = properties.getProperty(PROPS_FIELD_BOQUEUE);

            momClient.getServiceFactory().requestService(backOfficeQueue, new BackOfficeWorker());
            System.out.println("Back office waiting requests on " + backOfficeQueue + "...");
        }

        if (properties != null && properties.get(Connector.PROPS_FIELD_CASS_CONTACT_POINTS) != null && properties.get(Connector.PROPS_FIELD_CASS_KEYSPACE) !=null) {
            cassandraConnector = new Connector(properties);
            try {
                cassandraConnector.start();
            } catch (Exception e) {
                System.err.println("Error while initializing Cassandra connector : " + e.getMessage());
                System.err.println("Provided Cassandra contact points: " + properties.get(Connector.PROPS_FIELD_CASS_CONTACT_POINTS));
                System.err.println("Provided Cassandra keyspace : " + properties.get(Connector.PROPS_FIELD_CASS_KEYSPACE));
                if (properties.containsKey(Connector.PROPS_FIELD_CASS_REP_STRAT))
                    System.err.println("Provided Cassandra replication strategy: " + properties.get(Connector.PROPS_FIELD_CASS_REP_STRAT));
                if (properties.containsKey(Connector.PROPS_FIELD_CASS_REP_FACTOR))
                    System.err.println("Provided Cassandra replication factors : " + properties.get(Connector.PROPS_FIELD_CASS_REP_FACTOR));
                cassandraConnector = null;
                return;
            }
        }
    }

    public void stop() throws Exception {
        System.out.println("Stop Back Office ...");
        if (cassandraConnector != null)
            cassandraConnector.stop();
        if (momClient !=null)
            momClient.close();
    }

    public static void main(String[] argv) throws IOException {
        final BackOffice backoffice = new BackOffice();
        Properties properties = new Properties();
        InputStream conf = backoffice.getClass().getResourceAsStream("/backoffice.properties");
        if (conf==null) {
            System.out.println("Configuration file backoffice.properties not found in the classpath");
            System.exit(1);
        }
        properties.load(conf);

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run(){
                try {
                    backoffice.stop();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        backoffice.start(properties);
    }
}