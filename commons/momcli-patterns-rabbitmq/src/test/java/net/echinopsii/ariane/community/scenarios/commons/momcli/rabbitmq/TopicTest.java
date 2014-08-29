/**
 * [DEFINE YOUR PROJECT NAME/MODULE HERE]
 * [DEFINE YOUR PROJECT DESCRIPTION HERE] 
 * Copyright (C) 29/08/14 echinopsii
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

package net.echinopsii.ariane.community.scenarios.commons.momcli.rabbitmq;

import net.echinopsii.ariane.community.scenarios.momcli.AppMsgFeeder;
import net.echinopsii.ariane.community.scenarios.momcli.AppMsgWorker;
import net.echinopsii.ariane.community.scenarios.momcli.MomClient;
import net.echinopsii.ariane.community.scenarios.momcli.MomClientFactory;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class TopicTest {
    private static MomClient client = null;

    @BeforeClass
    public static void testSetup() throws IllegalAccessException, ClassNotFoundException, InstantiationException {
        Properties props = new Properties();
        props.put(MomClient.MOM_HOST, "localhost");
        props.put(MomClient.MOM_PORT, 5672);

        client = MomClientFactory.make("net.echinopsii.ariane.community.scenarios.commons.momcli.rabbitmq.Client");

        try {
            client.init(props);
        } catch (Exception e) {
            System.err.println("No local rabbit to test");
            client = null;
        }
    }

    @AfterClass
    public static void testCleanup() throws Exception {
        if (client!=null)
            client.close();
    }

    class TestFeeder implements AppMsgFeeder {

        private int interval = 100;

        @Override
        public Map<String, Object> apply() {
            Map<String, Object> ret = new HashMap<String, Object>()
            return null;
        }

        @Override
        public int getInterval() {
            return interval;
        }
    }

    class TestSubscriber implements AppMsgWorker {

        private int msgNumber = 0;

        @Override
        public Map<String, Object> apply(Map<String, Object> message) {
            return message;
        }

        public int getMsgNumber() {
            return msgNumber;
        }
    }


}