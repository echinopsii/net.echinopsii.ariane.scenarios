/**
 * [DEFINE YOUR PROJECT NAME/MODULE HERE]
 * [DEFINE YOUR PROJECT DESCRIPTION HERE] 
 * Copyright (C) 8/24/14 echinopsii
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

import akka.actor.ActorRef;
import akka.actor.ActorRefFactory;
import akka.actor.ActorSystem;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import net.echinopsii.ariane.community.scenarios.momcli.MomClient;
import net.echinopsii.ariane.community.scenarios.momcli.MomRequestExecutor;
import net.echinopsii.ariane.community.scenarios.momcli.MomService;
import net.echinopsii.ariane.community.scenarios.momcli.MomServiceFactory;

import java.io.IOException;
import java.util.Properties;

public class Client implements MomClient {

    private MomServiceFactory serviceFactory ;
    private MomRequestExecutor requestFactory ;

    private ActorSystem system     = ActorSystem.create("MySystem");
    private Connection  connection = null;

    @Override
    public void init(Properties properties) throws IOException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost((String)properties.get(MOM_HOST));
        factory.setPort((Integer) properties.get(MOM_PORT));
        connection = factory.newConnection();

        serviceFactory = new ServiceFactory(this);
        requestFactory = new RequestExecutor(this);
    }

    @Override
    public void close() throws IOException {
        if (serviceFactory!=null)
            for (MomService<ActorRef> service : ((ServiceFactory)serviceFactory).getServices())
                service.stop();
        if (connection.isOpen())
            connection.close();
    }

    @Override
    public boolean isConnected() {
        return connection.isOpen();
    }

    @Override
    public Connection getConnection() {
        return connection;
    }

    @Override
    public MomRequestExecutor getRequestFactory() {
        return requestFactory;
    }

    @Override
    public MomServiceFactory getServiceFactory() {
        return serviceFactory;
    }

    public ActorSystem getActorSystem() {
        return system;
    }
}