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

package net.echinopsii.ariane.community.scenarios.tradeworkflow.feederapp;

import net.echinopsii.ariane.community.core.messaging.api.AppMsgFeeder;
import net.echinopsii.ariane.community.core.messaging.api.MomClient;
import net.echinopsii.ariane.community.core.messaging.api.MomClientFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class Feeder {

    class StockFeeder implements AppMsgFeeder {
        private String stockName;
        private int    interval=10000;

        public StockFeeder(String sname) {
            stockName = sname;
        }

        @Override
        public Map<String, Object> apply() {
            Map<String, Object> ret = new HashMap<String, Object>();
            ret.put("NAME", stockName);
            long price = (long)(Math.random() * 10 + Math.random() * 100 + Math.random() * 1000);
            ret.put("PRICE", price);
            System.out.println("name : " + stockName + "; price : " + price);
            return ret;
        }

        @Override
        public int getInterval() {
            return interval;
        }
    }

    private static final String PROPS_FIELD_BASETOPICE = "feeder.base_topic";
    private static final String PROPS_FIELS_STOCKSLIST = "feeder.stocks_list";
    private static String baseTopic = "PRICE";

    private MomClient client = null;

    public void start(Properties properties) {
        if (properties!=null && properties.get(MomClient.MOM_CLI)!=null && properties.get(MomClient.MOM_CLI) instanceof String) {
            try {
                client = MomClientFactory.make((String)properties.get(MomClient.MOM_CLI));
            } catch (Exception e) {
                System.err.println("Error while loading MoM client : " + e.getMessage());
                System.err.println("Provided MoM client : " + properties.get(MomClient.MOM_CLI));
                return ;
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

            if (properties.getProperty(PROPS_FIELD_BASETOPICE)!=null)
                baseTopic = properties.getProperty(PROPS_FIELD_BASETOPICE);

            if (properties.getProperty(PROPS_FIELS_STOCKSLIST)!=null) {
                String[] stockNamesList = ((String) properties.getProperty(PROPS_FIELS_STOCKSLIST)).split(",");
                for (String stockName : stockNamesList) {
                    System.out.println("Load stock " + stockName + " feeder on baseTopic " + baseTopic + "...");
                    StockFeeder stockFeeder = new StockFeeder(stockName);
                    client.getServiceFactory().feederService(baseTopic, stockName, stockFeeder.getInterval(), stockFeeder);
                }
            } else {
                System.err.println("No stocks provided !");
                return;
            }
        } else {
            System.err.println("MoM client implementation must be provided");
        }
    }

    public void stop() throws Exception {
        System.out.println("Stop feeder ...");
        if (client!=null)
            client.close();
    }

    public static void main(String[] argv) throws IOException {
        final Feeder feeder = new Feeder();
        Properties properties = new Properties();
        InputStream conf = feeder.getClass().getResourceAsStream("/feeder.properties");
        if (conf==null) {
            System.out.println("Configuration file feeder.properties not found in the classpath");
            System.exit(1);
        }
        properties.load(conf);

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run(){
                try {
                    feeder.stop();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        feeder.start(properties);
    }

}