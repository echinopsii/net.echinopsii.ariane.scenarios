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
import com.rabbitmq.client.BasicProperties;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.QueueingConsumer;
import net.echinopsii.ariane.community.scenarios.momcli.AppMsgWorker;

import java.util.LinkedHashMap;
import java.util.Map;

public class MsgWorkerActor extends UntypedActor {

    private AppMsgWorker msgWorker = null;



    public static Props props(final AppMsgWorker worker) {
        return Props.create(new Creator<MsgWorkerActor>() {
            private static final long serialVersionUID = 1L;

            @Override
            public MsgWorkerActor create() throws Exception {
                return new MsgWorkerActor(worker);
            }
        });
    }

    public MsgWorkerActor(AppMsgWorker worker) {
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
            msgWorker.apply(finalMessage);
        } else
            unhandled(message);
    }
}