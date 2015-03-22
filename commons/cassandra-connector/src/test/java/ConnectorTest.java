/**
 * Cassandra connector facility module
 * Cassandra connector facility class test
 * Copyright (C) 21/03/15 echinopsii
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
import net.echinopsii.ariane.community.scenarios.cassandra.Connector;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Properties;

public class ConnectorTest {

    private static Properties cassandra1 = new Properties();
    private static Properties cassandra2 = new Properties();
    private static Properties cassandra3 = new Properties();

    private static Connector connector1 ;
    private static Connector connector2 ;
    private static Connector connector3 ;

    @BeforeClass
    public static void testSetup() throws IllegalAccessException, ClassNotFoundException, InstantiationException, IOException {
        cassandra1.load(ConnectorTest.class.getResourceAsStream("/cassandra1.properties"));
        cassandra2.load(ConnectorTest.class.getResourceAsStream("/cassandra2.properties"));
        cassandra3.load(ConnectorTest.class.getResourceAsStream("/cassandra3.properties"));

        connector1 = new Connector(cassandra1);
        connector2 = new Connector(cassandra2);
        connector3 = new Connector(cassandra3);
    }

    @AfterClass
    public static void testCleanup() throws Exception {
        connector1.stop();
        connector2.stop();
        connector3.stop();
    }

    @Test
    public void testConnectionCassandra1() throws Exception {
        connector1.start();
        assertNotNull(connector1.getSession());
    }

    @Test
    public void testConnectionCassandra2() throws Exception {
        connector2.start();
        assertNotNull(connector2.getSession());
    }

    @Test
    public void testConnectionCassandra3() throws Exception {
        connector3.start();
        assertNotNull(connector3.getSession());
    }
}