/**
 * [DEFINE YOUR PROJECT NAME/MODULE HERE]
 * [DEFINE YOUR PROJECT DESCRIPTION HERE] 
 * Copyright (C) 8/25/14 echinopsii
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

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import net.echinopsii.ariane.community.scenarios.momcli.AppMsgWorker;
import net.echinopsii.ariane.community.scenarios.momcli.MomRequestFactory;

import java.io.IOException;
import java.util.Map;

public class RequestFactory extends Client implements MomRequestFactory<String, AppMsgWorker> {

    private Client momClient;

    public RequestFactory(Client client) {
        momClient = client;
    }

    @Override
    public Map<String, Object> fireAndForget(Map<String, Object> request, String destination) {
        final Connection connection = momClient.getConnection();
        Channel channel = null;
        try {
            channel = connection.createChannel();
            channel.exchangeDeclare("FAF","direct");
            channel.queueDeclare(destination, false, false, false, null);
            channel.queueBind(destination, "FAF", destination);

            Message message = new MsgTranslator().encode(request);
            channel.basicPublish("FAF", destination, message.getProperties(), message.getBody());

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (channel!=null)
                try {
                    channel.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
        return request;
    }

    @Override
    public Map<String, Object> RPC(Map<String, Object> request, String destination, String answerSource, AppMsgWorker answerCB) {
        return null;
    }
}