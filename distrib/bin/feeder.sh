#!/bin/sh

cd $(dirname $0)
script_dir=`pwd`
base_dir=`echo $script_dir | sed -e "s/bin//g"`
cd - 1 > /dev/null

export JAVAOPT="-Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=9010 -Dcom.sun.management.jmxremote.local.only=false -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false" #-Dorg.slf4j.simpleLogger.defaultLogLevel=DEBUG

export CLASSPATH=${base_dir}conf:${base_dir}lib/slf4j-simple-1.7.5.jar:${base_dir}lib/slf4j-api-1.7.5.jar:${base_dir}lib/cassandra-driver-core-3.1.0.jar:${base_dir}lib/guava-16.0.1.jar:${base_dir}conf:${base_dir}lib/cassandra-driver-core-3.1.0.jar:${base_dir}lib/guava-16.0.1.jar:${base_dir}lib/netty-common-4.0.37.Final.jar:${base_dir}lib/netty-transport-4.0.37.Final.jar:${base_dir}lib/netty-buffer-4.0.37.Final.jar:${base_dir}lib/netty-codec-4.0.37.Final.jar:${base_dir}lib/netty-handler-4.0.37.Final.jar:${base_dir}lib/metrics-core-3.1.2.jar:${base_dir}lib/net.echinopsii.ariane.community.scenarios.commons.cassandra-connector-0.3.0-SNAPSHOT.jar:${base_dir}lib/akka-actor_2.10-2.3.4.jar:${base_dir}lib/amqp-client-3.3.5.jar:${base_dir}lib/commons-cli-1.1.jar:${base_dir}lib/commons-io-1.2.jar:${base_dir}lib/config-1.2.1.jar:${base_dir}lib/net.echinopsii.ariane.community.messaging.api-0.2.2-SNAPSHOT.jar:${base_dir}lib/net.echinopsii.ariane.community.messaging.rabbitmq-0.2.2-SNAPSHOT.jar:${base_dir}lib/net.echinopsii.ariane.community.scenarios.tradeworflow.feederapp-0.3.0-SNAPSHOT.jar:${base_dir}lib/scala-library-2.10.4.jar:${base_dir}lib/scala-reflect-2.10.4.jar:${base_dir}lib/slf4j-api-1.6.4.jar

#echo $CLASSPATH

java $JAVAOPT -cp $CLASSPATH net.echinopsii.ariane.community.scenarios.tradeworkflow.feederapp.Feeder > /var/log/ariane/fd.log 2>&1
