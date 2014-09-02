#!/bin/sh

cd $(dirname $0)
script_dir=`pwd`
base_dir=`echo $script_dir | sed -e "s/bin//g"`
cd - 1 > /dev/null

export CLASSPATH=${base_dir}conf:${base_dir}lib/akka-actor_2.10-2.3.4.jar:${base_dir}lib/amqp-client-3.3.5.jar:${base_dir}lib/commons-cli-1.1.jar:${base_dir}lib/commons-io-1.2.jar:${base_dir}lib/config-1.2.1.jar:${base_dir}lib/net.echinopsii.ariane.community.scenarios.commons.momcli-patterns-api-0.1.0-SNAPSHOT.jar:${base_dir}lib/net.echinopsii.ariane.community.scenarios.commons.momcli-patterns-rabbitmq-0.1.0-SNAPSHOT.jar:${base_dir}lib/net.echinopsii.ariane.community.scenarios.tradeworflow.frontapp-0.1.0-SNAPSHOT.jar:${base_dir}lib/scala-library-2.10.4.jar:${base_dir}lib/scala-reflect-2.10.4.jar

#echo $CLASSPATH

java -cp $CLASSPATH net.echinopsii.ariane.community.scenarios.tradeworkflow.frontapp.FrontOffice
