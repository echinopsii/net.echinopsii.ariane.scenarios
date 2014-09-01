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

import net.echinopsii.ariane.community.scenarios.momcli.AppMsgWorker;
import net.echinopsii.ariane.community.scenarios.momcli.MomClient;
import net.echinopsii.ariane.community.scenarios.momcli.MomClientFactory;
import net.echinopsii.ariane.community.scenarios.momcli.MomMsgTranslator;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class MiddleOffice {

    class MiddleOfficeWorker implements AppMsgWorker {
        @Override
        public Map<String, Object> apply(Map<String, Object> message) {
            try {
                new Thread().sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Map<String, Object> reply = new HashMap<String, Object>();
            reply.put(MomMsgTranslator.MSG_BODY, "OK");
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

            client.getServiceFactory().requestService(moQueue, new MiddleOfficeWorker());
        }
    }

    public void stop() throws Exception {
        if (client!=null)
            client.close();
    }

    public static void main(String[] argv) throws IOException {
        final MiddleOffice middleoffice = new MiddleOffice();
        Properties properties = new Properties();
        properties.load(middleoffice.getClass().getResourceAsStream("middleoffice.properties"));

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