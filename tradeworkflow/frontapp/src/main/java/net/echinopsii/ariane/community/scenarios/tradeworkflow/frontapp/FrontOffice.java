/**
 * [DEFINE YOUR PROJECT NAME/MODULE HERE]
 * [DEFINE YOUR PROJECT DESCRIPTION HERE] 
 * Copyright (C) 02/09/14 echinopsii
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

package net.echinopsii.ariane.community.scenarios.tradeworkflow.frontapp;

import net.echinopsii.ariane.community.messaging.api.*;
import net.echinopsii.ariane.community.messaging.common.MomClientFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class FrontOffice {

    private final static Logger log = LoggerFactory.getLogger(FrontOffice.class);

    class FrontOfficeWorker implements AppMsgWorker {

        class AcquiredStock {
            private String stockName;
            private Date   date;
            private long   acquiredPrice;
            private long   quantity;

            public AcquiredStock(String name, long price, long quant) {
                date          = new Date();
                stockName     = name;
                acquiredPrice = price;
                quantity      = quant;
            }

            public String getStockName() {
                return stockName;
            }

            public long getAcquiredPrice() {
                return acquiredPrice;
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) {
                    return true;
                }
                if (o == null || getClass() != o.getClass()) {
                    return false;
                }

                AcquiredStock that = (AcquiredStock) o;

                if (acquiredPrice != that.acquiredPrice) {
                    return false;
                }
                if (!date.equals(that.date)) {
                    return false;
                }
                if (!stockName.equals(that.stockName)) {
                    return false;
                }

                return true;
            }

            @Override
            public int hashCode() {
                int result = stockName.hashCode();
                result = 31 * result + date.hashCode();
                result = 31 * result + (int) (acquiredPrice ^ (acquiredPrice >>> 32));
                return result;
            }
        }

        private MomClient client;
        //private String              baseTopic;
        private String              moQueue;
        private int                 mindiff=10;
        private int                 stockblksize=10;
        private List<AcquiredStock> acquiredStocks = new ArrayList<AcquiredStock>();
        private MomRequestExecutor moRexec = null;

        private long position;

        public FrontOfficeWorker(MomClient cli, /*String topic,*/ String queue, int mindiff_, int stockblksize_) {
            client    = cli;
            //baseTopic = topic;
            moQueue   = queue;
            position  = 100000;
            mindiff   = mindiff_;
            stockblksize = stockblksize_;
            moRexec = client.createRequestExecutor();
        }

        //@Override
        public Map<String, Object> apply(Map<String, Object> message) {
            log.debug(message.toString());
            String name  = message.get("NAME").toString();
            long   price = (Long) message.get("PRICE");

            log.debug("Received {"+ message.get(MomMsgTranslator.MSG_APPLICATION_ID) + "," +name+","+price+"}");

            AcquiredStock allReaddyAcquired = null;
            for (AcquiredStock acquiredStock : acquiredStocks) {
                if (acquiredStock.getStockName().equals(name)) {
                    allReaddyAcquired = acquiredStock;
                    break;
                }
            }

            if (allReaddyAcquired==null) {
                if (price * stockblksize < position) {
                    message.put("ORDER", "BUY");
                    message.put("QUANTITY", stockblksize);
                    moRexec.RPC(message, moQueue, client.getClientID()+"Q", null);
                    log.debug(stockblksize + " stocks {"+name+","+price+"} acquired...");
                    acquiredStocks.add(new AcquiredStock(name, price, stockblksize));
                    position -= price*stockblksize;
                    log.debug("New position : " + position);
                }
            } else {
                if (allReaddyAcquired.getAcquiredPrice()<(price-mindiff)) {
                    message.put("ORDER","SELL");
                    message.put("QUANTITY", stockblksize);
                    moRexec.RPC(message, moQueue, client.getClientID()+"Q", null);
                    log.debug(stockblksize+" stocks {"+name+","+price+"} sold...");
                    acquiredStocks.remove(allReaddyAcquired);
                    position += price*stockblksize;
                    log.debug("New position : " + position);
                }
            }

            return null;
        }
    }


    private static final String PROPS_FIELD_FO_FEEDER_BASE_TOPIC = "front_office.feeder_base_topic";
    private static final String PROPS_FIELD_FO_MO_QUEUE          = "front_office.mo_queue";
    private static final String PROPS_FIELD_FO_STOCKS_BLOCK_SIZE = "front_office.stocks_block_size";
    private static final String PROPS_FIELD_FO_STOCK_SOLD_MINDIF = "front_office.stocks_sold_mindif";
    private MomClient client = null;

    public void start(Properties properties) {
        if (properties != null && properties.get(MomClient.MOM_CLI) != null && properties.get(MomClient.MOM_CLI) instanceof String) {
            int block_size=10;
            int min_diff=10;
            if (properties.getProperty(PROPS_FIELD_FO_FEEDER_BASE_TOPIC)==null) {
                log.error("Error while initializing Front Office service : feeder base topic isn't defined...");
                return;
            }

            if (properties.getProperty(PROPS_FIELD_FO_MO_QUEUE)==null) {
                log.error("Error while initializing Front Office service : mo queue isn't defined...");
                return;
            }

            if (properties.getProperty(PROPS_FIELD_FO_STOCKS_BLOCK_SIZE)!=null)
                block_size=new Integer(properties.getProperty(PROPS_FIELD_FO_STOCKS_BLOCK_SIZE));

            if (properties.getProperty(PROPS_FIELD_FO_STOCK_SOLD_MINDIF)!=null)
                min_diff=new Integer(properties.getProperty(PROPS_FIELD_FO_STOCK_SOLD_MINDIF));

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

            client.getServiceFactory().subscriberService(properties.getProperty(PROPS_FIELD_FO_FEEDER_BASE_TOPIC),null,
                                                         new FrontOfficeWorker(client,
                                                                               /*properties.getProperty(PROPS_FIELD_FO_FEEDER_BASE_TOPIC),*/
                                                                               properties.getProperty(PROPS_FIELD_FO_MO_QUEUE), min_diff, block_size));
            log.debug("Subscribed to topic " + properties.getProperty(PROPS_FIELD_FO_FEEDER_BASE_TOPIC));
        }
    }

    public void stop() throws Exception {
        log.info("Stop front office...");
        if (client!=null)
            client.close();
    }

    public static void main(String[] argv) throws IOException {
        final FrontOffice frontoffice = new FrontOffice();
        Properties properties = new Properties();
        InputStream conf = frontoffice.getClass().getResourceAsStream("/frontoffice.properties");
        if (conf==null) {
            log.error("Configuration file frontoffice.properties not found in the classpath");
            System.exit(1);
        }
        properties.load(conf);

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run(){
                try {
                    frontoffice.stop();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        frontoffice.start(properties);
    }
}