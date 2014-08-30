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

import java.util.Map;

public class MsgRequestActor extends UntypedActor {

    private MsgTranslator translator = new MsgTranslator();
    private AppMsgWorker msgWorker   = null;
    private Client       client      = null;
    private Channel      channel     = null;


    public static Props props(final Client mclient, final Channel channel, final AppMsgWorker worker) {
        return Props.create(new Creator<MsgRequestActor>() {
            private static final long serialVersionUID = 1L;

            @Override
            public MsgRequestActor create() throws Exception {
                return new MsgRequestActor(mclient, channel, worker);
            }
        });
    }

    public MsgRequestActor(Client mclient, Channel chan, AppMsgWorker worker) {
        client = mclient;
        channel = chan;
        msgWorker = worker;
    }

    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof QueueingConsumer.Delivery) {
            Envelope envelope = ((QueueingConsumer.Delivery) message).getEnvelope();
            BasicProperties properties = ((QueueingConsumer.Delivery) message).getProperties();
            byte[] body = ((QueueingConsumer.Delivery)message).getBody();

            Map<String, Object> finalMessage = translator.decode(new Message().setEnvelope(envelope).
                                                                 setProperties(properties).
                                                                 setBody(body));

            Map<String, Object> reply = msgWorker.apply(finalMessage);
            if (properties.getReplyTo()!=null && properties.getCorrelationId()!=null && reply!=null) {
                reply.put(MsgTranslator.MSG_CORRELATION_ID, properties.getCorrelationId());
                Message replyMessage = translator.encode(reply);
                channel.basicPublish("", properties.getReplyTo(), (AMQP.BasicProperties) replyMessage.getProperties(), replyMessage.getBody());
            }
            long deliveryTag = ((QueueingConsumer.Delivery)message).getEnvelope().getDeliveryTag();
            System.out.println("ack msg " + deliveryTag);
            channel.basicAck(deliveryTag, false);
        } else
            unhandled(message);
    }
}