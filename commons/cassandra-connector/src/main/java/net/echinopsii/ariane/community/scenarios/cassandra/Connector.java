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

import java.util.HashMap;
import java.util.Properties;

public class Connector {
    public final static String PROPS_FIELD_CASS_CONTACT_POINTS = "cassandra.contact_points";
    public final static String PROPS_FIELD_CASS_KEYSPACE       = "cassandra.keyspace";
    public final static String PROPS_FIELD_CASS_REP_STRAT      = "cassandra.keyspace.replication_strategy";
    public final static String PROPS_FIELD_CASS_REP_FACTOR     = "cassandra.keyspace.replication_factors";

    private String[] contactPoints;
    private Cluster cluster;

    private String  keySpace ;
    private String  keySpaceRepStrat = "SimpleStrategy";
    private HashMap<String, String> keySpaceRepFactor = new HashMap<String, String>();
    private Session session;

    public Connector(Properties conf) {
        this.contactPoints = ((String) conf.get(PROPS_FIELD_CASS_CONTACT_POINTS)).split(",");
        this.keySpace = (String) conf.get(PROPS_FIELD_CASS_KEYSPACE);
        if (conf.containsKey(PROPS_FIELD_CASS_REP_STRAT))
            this.keySpaceRepStrat = (String) conf.get(PROPS_FIELD_CASS_REP_STRAT);
        if (conf.containsKey(PROPS_FIELD_CASS_REP_FACTOR)) {
            for (String dc_rep : ((String)conf.get(PROPS_FIELD_CASS_REP_FACTOR)).split(",")) {
                String dc_name = dc_rep.split(":")[0];
                String rep_factor = dc_rep.split(":")[1];
                this.keySpaceRepFactor.put(dc_name, rep_factor);
            }
        }
    }

    public Session start() throws Exception {
        if (contactPoints!=null) {
            Cluster.Builder builder = Cluster.builder();
            for (String contactPoint : contactPoints)
                builder.addContactPoint(contactPoint);
            this.cluster = builder.build();
            this.session = cluster.connect();
            if (keySpace!=null && !keySpace.equals("")) {
                String keyspaceCreateStatement;
                if (this.keySpaceRepStrat.equals("SimpleStrategy")) {
                    String replicationFactor = (keySpaceRepFactor.size()==1) ? (String) keySpaceRepFactor.values().toArray()[0] : "1";
                    keyspaceCreateStatement = "CREATE KEYSPACE IF NOT EXISTS " + keySpace + " WITH REPLICATION = { " +
                            "'class' : '" + this.keySpaceRepStrat + "', " +
                            "'replication_factor' : " + replicationFactor + " }";
                } else {
                    String dcReplicationFactors = "";
                    for (String dc : this.keySpaceRepFactor.keySet()) {
                        String dcReplicationFactor = this.keySpaceRepFactor.get(dc);
                        dcReplicationFactors += ", '" + dc + "' : " + dcReplicationFactor;
                    }
                    keyspaceCreateStatement = "CREATE KEYSPACE IF NOT EXISTS " + keySpace + " WITH REPLICATION = { " +
                            "'class' : '" + this.keySpaceRepStrat + "'" + dcReplicationFactors + " }";
                }
                this.session.execute(keyspaceCreateStatement);
                this.session.execute("USE " + this.keySpace);
            }
        } else
            throw new Exception("Cassandra contact points badly defined !");
        return session;
    }

    public void stop() {
        if (this.session!=null) this.session.close();
        if (this.cluster!=null) this.cluster.close();
    }

    public Session getSession() {
        return session;
    }
}