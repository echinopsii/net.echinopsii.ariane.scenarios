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
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.QueueingConsumer;
import net.echinopsii.ariane.community.scenarios.momcli.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ServiceFactory implements MomServiceFactory<Service, AppMsgWorker, AppFeeder, String, String> {

    private Client        momClient ;
    private List<Service> serviceList  = new ArrayList<Service>();

    public ServiceFactory(Client client) {
        momClient = client;
    }

    /**
     * Create and start a new request service
     *
     * @param source the source where request are coming from
     * @param requestCB the application request worker
     *
     * @return the request service
     *
     */
    @Override
    public Service requestService(final String source, final AppMsgWorker requestCB) {
        final Connection  connection   = momClient.getConnection();
        Service     ret          = null;
        ActorRef    requestActor = null;
        MomConsumer consumer     = null;

        if (connection != null && connection.isOpen()) {
            requestActor = momClient.getActorFactory().actorOf(MsgWorkerActor.props(requestCB), source + "_msgWorker");
            final ActorRef runnableReqActor   = requestActor;

            consumer = new MomConsumer() {
                private boolean isRunning = false;

                @Override
                public void run() {
                    Channel channel = null;
                    try {
                        channel = connection.createChannel();
                        channel.queueDeclare(source, false, false, false, null);

                        QueueingConsumer consumer = new QueueingConsumer(channel);
                        channel.basicConsume(source, true, consumer);

                        isRunning = true;

                        while (isRunning) {
                            try {
                                QueueingConsumer.Delivery delivery = consumer.nextDelivery();
                                runnableReqActor.tell(delivery, null);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                        }

                        channel.close();

                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            channel.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                public boolean isRunning() {
                    return isRunning;
                }

                @Override
                public void start() {
                    new Thread(this).start();
                }

                @Override
                public void stop() {
                    isRunning = false;
                }
            };

            consumer.start();

            ret = new Service().setActorRef(requestActor).setConsumer(consumer).setActorRefFactory(momClient.getActorFactory());
            serviceList.add(ret);
        }

        return ret;
    }

    @Override
    public Service feederService(String destination, AppFeeder feederCB) {
        return null;
    }

    @Override
    public Service subscriberService(String source, AppMsgWorker feedCB) {
        return null;
    }

    @Override
    public List<Service> getServices() {
        return serviceList;
    }
}