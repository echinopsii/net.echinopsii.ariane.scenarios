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

import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import net.echinopsii.ariane.community.messaging.api.AppMsgWorker;
import net.echinopsii.ariane.community.messaging.api.MomClient;
import net.echinopsii.ariane.community.messaging.common.MomClientFactory;
import net.echinopsii.ariane.community.messaging.api.MomMsgTranslator;
import net.echinopsii.ariane.community.scenarios.cassandra.Connector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static com.datastax.driver.core.querybuilder.QueryBuilder.timestamp;
import static com.datastax.driver.core.querybuilder.QueryBuilder.ttl;

public class BackOffice {

    private final static Logger log = LoggerFactory.getLogger(BackOffice.class);

    class BackOfficeWorker implements AppMsgWorker {
        private Connector cassandraConnector;

        public BackOfficeWorker(Connector cassandraConnector) {
            this.cassandraConnector = cassandraConnector;
        }

        public Map<String, Object> apply(Map<String, Object> message) {
            log.debug("Back office work on  : {" + message.get(MomMsgTranslator.MSG_APPLICATION_ID) + "," + message.get("NAME") + "," +
                    message.get("PRICE") + "," + message.get("ORDER") + "," + message.get("QUANTITY") + " }...");
            Statement insertStatement = QueryBuilder.insertInto("back_office_orders_history").
                    value("order_time", timestamp(System.currentTimeMillis())).
                    value("order_operation", message.get("ORDER")).
                    value("stock_name", message.get("NAME")).
                    value("stock_price", message.get("PRICE")).
                    value("quantity", message.get("QUANTITY")).
                    using(ttl(7776000)).setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
            //String insertStatement = "INSERT INTO back_office_orders_history(order_time, order_operation, stock_name, stock_price, quantity) " +
            //        "VALUES(" + System.currentTimeMillis()  + ", " + message.get("ORDER") + ", " + message.get("NAME") + ",  " +
            //        message.get("PRICE") + ", " + message.get("QUANTITY") + ") USING TTL 7776000";
            try {
                if (this.cassandraConnector==null)
                    new Thread().sleep(1000);
                else {
                    this.cassandraConnector.getSession().execute(insertStatement);
                }
                log.debug("Back Office return DONE");
                Map<String, Object> reply = new HashMap<String, Object>();
                reply.put(MomMsgTranslator.MSG_BODY, "DONE");
                return reply;
            } catch (Exception e) {
                if (this.cassandraConnector!=null)
                    log.error("Statement raise exception : " + insertStatement.toString());
                e.printStackTrace();
                log.debug("Back Office return ERROR !!!");
                Map<String, Object> reply = new HashMap<String, Object>();
                reply.put(MomMsgTranslator.MSG_BODY, "ERROR !!!");
                return reply;
            }
        }
    }

    private static final String PROPS_FIELD_BOQUEUE = "back_office.queue";
    private static String backOfficeQueue = "BOQUEUE";

    private MomClient momClient = null;
    private Connector cassandraConnector = null;

    public void start(Properties properties) {
        if (properties != null && properties.get(Connector.PROPS_FIELD_CASS_CONTACT_POINTS) != null && properties.get(Connector.PROPS_FIELD_CASS_KEYSPACE) !=null) {
            cassandraConnector = new Connector(properties);
            try {
                cassandraConnector.start();
                String tableCreationStatement = "CREATE TABLE IF NOT EXISTS back_office_orders_history " +
                        "(order_time timestamp, order_operation text, stock_name text, " +
                        "stock_price float, quantity int, PRIMARY KEY (stock_name, order_time) ) " +
                        "WITH compaction = { 'class' : 'DateTieredCompactionStrategy' } " +
                        "WITH CLUSTERING ORDER BY (order_time DESC);";
                cassandraConnector.getSession().execute(tableCreationStatement);
            } catch (Exception e) {
                log.error("Error while initializing Cassandra connector : " + e.getMessage());
                log.error("Provided Cassandra contact points: " + properties.get(Connector.PROPS_FIELD_CASS_CONTACT_POINTS));
                log.error("Provided Cassandra keyspace : " + properties.get(Connector.PROPS_FIELD_CASS_KEYSPACE));
                if (properties.containsKey(Connector.PROPS_FIELD_CASS_REP_STRAT))
                    log.error("Provided Cassandra replication strategy: " + properties.get(Connector.PROPS_FIELD_CASS_REP_STRAT));
                if (properties.containsKey(Connector.PROPS_FIELD_CASS_REP_FACTOR))
                    log.error("Provided Cassandra replication factors : " + properties.get(Connector.PROPS_FIELD_CASS_REP_FACTOR));
                cassandraConnector = null;
            }

        }

        if (properties != null && properties.get(MomClient.MOM_CLI) != null && properties.get(MomClient.MOM_CLI) instanceof String) {
            try {
                momClient = MomClientFactory.make((String) properties.get(MomClient.MOM_CLI));
            } catch (Exception e) {
                log.error("Error while loading MoM momClient : " + e.getMessage());
                log.error("Provided MoM momClient : " + properties.get(MomClient.MOM_CLI));
                return;
            }

            try {
                momClient.init(properties);
            } catch (Exception e) {
                log.error("Error while initializing MoM Client : " + e.getMessage());
                log.error("Provided MoM host : " + properties.get(MomClient.MOM_HOST));
                log.error("Provided MoM port : " + properties.get(MomClient.MOM_PORT));
                momClient = null;
                return;
            }

            if (properties.getProperty(PROPS_FIELD_BOQUEUE)!=null)
                backOfficeQueue = properties.getProperty(PROPS_FIELD_BOQUEUE);

            momClient.getServiceFactory().requestService(backOfficeQueue, new BackOfficeWorker(this.cassandraConnector));
            log.info("Back office waiting requests on " + backOfficeQueue + "...");
        }
    }

    public void stop() throws Exception {
        log.info("Stop Back Office ...");
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
            log.debug("Configuration file backoffice.properties not found in the classpath");
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