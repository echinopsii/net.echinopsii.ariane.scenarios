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

import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.japi.Creator;
import com.rabbitmq.client.*;
import net.echinopsii.ariane.community.scenarios.momcli.AppMsgWorker;
import net.echinopsii.ariane.community.scenarios.momcli.MomClient;
import net.echinopsii.ariane.community.scenarios.momcli.MomRequestFactory;

import java.util.LinkedHashMap;
import java.util.Map;

public class MsgWorkerActor extends UntypedActor {

    private AppMsgWorker msgWorker = null;
    private Client       client    = null;


    public static Props props(final Client mclient, final AppMsgWorker worker) {
        return Props.create(new Creator<MsgWorkerActor>() {
            private static final long serialVersionUID = 1L;

            @Override
            public MsgWorkerActor create() throws Exception {
                return new MsgWorkerActor(mclient, worker);
            }
        });
    }

    public MsgWorkerActor(Client mclient,  AppMsgWorker worker) {
        client = mclient;
        msgWorker = worker;
    }

    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof QueueingConsumer.Delivery) {
            Envelope envelope = ((QueueingConsumer.Delivery) message).getEnvelope();
            BasicProperties properties = ((QueueingConsumer.Delivery) message).getProperties();
            byte[] body = ((QueueingConsumer.Delivery)message).getBody();

            Map<String, Object> finalMessage = new MsgTranslator().decode(
                                                   new Message().setEnvelope(((QueueingConsumer.Delivery) message).getEnvelope()).
                                                                 setProperties(((QueueingConsumer.Delivery) message).getProperties()).
                                                                 setBody(((QueueingConsumer.Delivery) message).getBody()));

            Map<String, Object> reply = msgWorker.apply(finalMessage);

            if (properties.getReplyTo()!=null && properties.getCorrelationId()!=null && reply!=null) {

                reply.put(MsgTranslator.MSG_CORRELATION_ID, properties.getCorrelationId());
                Message replyMessage = new MsgTranslator().encode(reply);

                Connection cnx = client.getConnection();
                Channel channel = cnx.createChannel();
                channel.basicPublish("", properties.getReplyTo(), replyMessage.getProperties(), replyMessage.getBody());
                channel.basicAck(((QueueingConsumer.Delivery)message).getEnvelope().getDeliveryTag(), false);
            }
        } else
            unhandled(message);
    }
}