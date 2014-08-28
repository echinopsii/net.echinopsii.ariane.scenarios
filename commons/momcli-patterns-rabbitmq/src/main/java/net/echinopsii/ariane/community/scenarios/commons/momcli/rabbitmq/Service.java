/**
 * [DEFINE YOUR PROJECT NAME/MODULE HERE]
 * [DEFINE YOUR PROJECT DESCRIPTION HERE] 
 * Copyright (C) 8/27/14 echinopsii
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
import net.echinopsii.ariane.community.scenarios.momcli.MomConsumer;
import net.echinopsii.ariane.community.scenarios.momcli.MomService;

public class Service implements MomService<ActorRef>{

    private MomConsumer     consumer;
    private ActorRef        actorRef;
    private ActorRefFactory actorRefFactory;

    public MomConsumer getConsumer() {
        return consumer;
    }

    public Service setConsumer(MomConsumer consumer) {
        this.consumer = consumer;
        return this;
    }

    public ActorRef getActorRef() {
        return actorRef;
    }

    public Service setActorRef(ActorRef actorRef) {
        this.actorRef = actorRef;
        return this;
    }

    public ActorRefFactory getActorRefFactory() {
        return actorRefFactory;
    }

    public Service setActorRefFactory(ActorRefFactory actorRefFactory) {
        this.actorRefFactory = actorRefFactory;
        return this;
    }

    public void stop() {
        if (consumer!=null) consumer.stop();
        if (actorRef!=null) actorRefFactory.stop(actorRef);
    }
}