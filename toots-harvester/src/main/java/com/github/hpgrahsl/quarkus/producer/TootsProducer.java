package com.github.hpgrahsl.quarkus.producer;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import com.github.hpgrahsl.quarkus.producer.model.Toot;

import io.quarkus.logging.Log;
import io.smallrye.reactive.messaging.kafka.Record;

@ApplicationScoped
public class TootsProducer {

  private static final Jsonb JSONB = JsonbBuilder.create();

  @ConfigProperty(name="kafka.bootstrap.servers")
  String kafkaBootstrapServers;

  @ConfigProperty(name="tootsharvester.toots.topic.name")
  String topicName;

  @ConfigProperty(name="tootsharvester.toots.topic.partitions")
  int topicPartitions;

  @PostConstruct
  void kafkaTopicInit() {
    try {
      Admin adminClient = Admin.create(
        Map.of(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG,kafkaBootstrapServers)
      );
      if(!adminClient.listTopics().names().get().contains(topicName)) {
        var topic = new NewTopic(topicName, topicPartitions, (short)1);
        adminClient.createTopics(Collections.singleton(topic)).values().get(topicName).get();
      }
    } catch (InterruptedException | ExecutionException e) {
      Log.errorv("topic creation failed for topic {0}",topicName);
      e.printStackTrace();
    }
  }

  @Inject
  @Channel("live-toots")
  Emitter<Record<Long,String>> emitter;

  public void sendToot(Toot toot) {
    if(toot != null) {
      emitter.send(Record.of(toot.id(),JSONB.toJson(toot)));  
    }
  }

}
