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

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Envelope;
import net.echinopsii.ariane.community.scenarios.momcli.MomMsgTranslator;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

public class MsgTranslator implements MomMsgTranslator<Message>{

    public final static String MSG_RBQ_DELIVERY_TAG     = "MSG_RBQ_DELIVERY_TAG";
    public final static String MSG_RBQ_EXCHANGE         = "MSG_RBQ_EXCHANGE";
    public final static String MSG_RBQ_ROUTINGKEY       = "MSG_RBQ_ROUTINGKEY";
    public final static String MSG_RBQ_HEADER           = "MSG_RBQ_HEADER";
    public final static String MSG_RBQ_CONTENT_ENCODING = "MSG_RBQ_CONTENT_ENCODING";
    public final static String MSG_RBQ_CONTENT_TYPE     = "MSG_RBQ_CONTENT_TYPE";
    public final static String MSG_RBQ_USER_ID          = "MSG_RBQ_USER_ID";

    @Override
    public Map<String, Class> getMessageTypo() {
        return null;
    }

    @Override
    public Message encode(Map<String, Object> message) {
        AMQP.BasicProperties.Builder propsBuilder = new AMQP.BasicProperties.Builder();
        BasicProperties properties = null;
        byte[] body = null;

        if (message.get(MSG_APPLICATION_ID)!=null)
            propsBuilder.appId((String)message.get(MSG_APPLICATION_ID));
        if (message.get(MSG_RBQ_CONTENT_ENCODING)!=null)
            propsBuilder.contentEncoding((String)message.get(MSG_RBQ_CONTENT_ENCODING));
        if (message.get(MSG_RBQ_CONTENT_TYPE)!=null)
            propsBuilder.contentType((String)message.get(MSG_RBQ_CONTENT_TYPE));
        if (message.get(MSG_CORRELATION_ID)!=null)
            propsBuilder.correlationId((String)message.get(MSG_CORRELATION_ID));
        if (message.get(MSG_DELIVERY_MODE)!=null)
            propsBuilder.deliveryMode((Integer)message.get(MSG_DELIVERY_MODE));
        if (message.get(MSG_EXPIRATION)!=null)
            propsBuilder.expiration((String)message.get(MSG_EXPIRATION));
        if (message.get(MSG_RBQ_HEADER)!=null)
            propsBuilder.headers((Map<String, Object>)message.get(MSG_RBQ_HEADER));
        if (message.get(MSG_PRIORITY)!=null)
            propsBuilder.priority((Integer)message.get(MSG_PRIORITY));
        if (message.get(MSG_REPLY_TO)!=null)
            propsBuilder.replyTo((String)message.get(MSG_REPLY_TO));
        if (message.get(MSG_TIMESTAMP)!=null)
            propsBuilder.timestamp((Date)message.get(MSG_TIMESTAMP));
        if (message.get(MSG_TYPE)!=null)
            propsBuilder.type((String)message.get(MSG_TYPE));
        if (message.get(MSG_RBQ_USER_ID)!=null)
            propsBuilder.userId((String)message.get(MSG_RBQ_USER_ID));

        properties = propsBuilder.build();

        Object bodyObject = message.get(MSG_BODY);
        if (bodyObject instanceof String)
            body = ((String)message.get(MSG_BODY)).getBytes();
        else if (bodyObject instanceof byte[])
            body = (byte[])bodyObject;

        return new Message().setProperties(properties).setBody(body);
    }

    @Override
    public Map<String, Object> decode(Message message) {
        Envelope envelope = message.getEnvelope();
        BasicProperties properties = message.getProperties();
        byte[] body = message.getBody();

        LinkedHashMap<String, Object> decodedMessage = new LinkedHashMap<String, Object>();
        if (envelope!=null) {
            decodedMessage.put(MSG_RBQ_DELIVERY_TAG, envelope.getDeliveryTag());
            decodedMessage.put(MSG_RBQ_EXCHANGE, envelope.getExchange());
            decodedMessage.put(MSG_RBQ_ROUTINGKEY, envelope.getRoutingKey());
        }

        if (properties!=null) {
            decodedMessage.put(MSG_RBQ_HEADER, properties.getHeaders());
            decodedMessage.put(MSG_APPLICATION_ID, properties.getAppId());
            decodedMessage.put(MSG_RBQ_CONTENT_ENCODING, properties.getContentEncoding());
            decodedMessage.put(MSG_RBQ_CONTENT_TYPE, properties.getContentType());
            decodedMessage.put(MSG_CORRELATION_ID, properties.getCorrelationId());
            decodedMessage.put(MSG_DELIVERY_MODE, properties.getDeliveryMode());
            decodedMessage.put(MSG_EXPIRATION, properties.getExpiration());
            decodedMessage.put(MSG_MESSAGE_ID, properties.getMessageId());
            decodedMessage.put(MSG_PRIORITY, properties.getPriority());
            decodedMessage.put(MSG_REPLY_TO, properties.getReplyTo());
            decodedMessage.put(MSG_TIMESTAMP, properties.getTimestamp());
            decodedMessage.put(MSG_TYPE, properties.getType());
            decodedMessage.put(MSG_RBQ_USER_ID, properties.getUserId());
        }

        decodedMessage.put(MSG_BODY, body);

        return decodedMessage;
    }
}