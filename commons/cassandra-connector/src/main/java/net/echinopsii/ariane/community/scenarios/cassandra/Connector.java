/**
 * Cassandra connector facility module
 * Cassandra connector facility class
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

package net.echinopsii.ariane.community.scenarios.cassandra;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;

import java.util.Properties;

public class Connector {
    public final static String PROPS_FIELD_CASS_CONTACT_POINTS = "cassandra.contact_points";
    public final static String PROPS_FIELD_CASS_KEYSPACE       = "cassandra.keyspace";

    private String[] contactPoints;
    private Cluster cluster;

    private String  keySpace ;
    private Session session;

    public Connector(Properties conf) {
        this.contactPoints = ((String) conf.get(PROPS_FIELD_CASS_CONTACT_POINTS)).split(",");
        this.keySpace = (String) conf.get(PROPS_FIELD_CASS_KEYSPACE);
    }

    public Session start() throws Exception {
        if (contactPoints!=null) {
            Cluster.Builder builder = Cluster.builder();
            for (String contactPoint : contactPoints)
                builder.addContactPoint(contactPoint);
            this.cluster = builder.build();
            if (keySpace!=null && !keySpace.equals(""))
                this.session = cluster.connect(keySpace);
            else
                this.session = cluster.connect();
        } else
            throw new Exception("Cassandra contact points badly defined !");
        return session;
    }

    public void stop() {
        if (this.cluster!=null)
            this.cluster.close();
    }

    public Session getSession() {
        return session;
    }
}