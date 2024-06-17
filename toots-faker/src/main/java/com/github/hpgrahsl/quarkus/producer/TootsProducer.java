package com.github.hpgrahsl.quarkus.producer;

import io.quarkus.logging.Log;
import io.smallrye.mutiny.Multi;
import io.smallrye.reactive.messaging.kafka.Record;
import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;

import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.reactive.messaging.Outgoing;

@ApplicationScoped
public class TootsProducer {

  private static final Jsonb JSONB = JsonbBuilder.create();

  @ConfigProperty(name="kafka.bootstrap.servers")
  String kafkaBootstrapServers;

  @ConfigProperty(name="tootsfaker.toots.topic.name")
  String topicName;

  @ConfigProperty(name="tootsfaker.toots.topic.partitions")
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
  FakeTootGenerator fakeTootGenerator;

  @ConfigProperty(name="tootsfaker.toot.interval.ms")
  long tootIntervalMs;

  @Outgoing("live-toots")
  public Multi<Record<Long,String>> simulateFakeToots() {
    return Multi.createFrom().ticks().every(Duration.ofMillis(tootIntervalMs))
            .map(tick -> fakeTootGenerator.createFakeToot())
            .map(toot -> Record.of(toot.id(),JSONB.toJson(toot)))
            .invoke(r -> Log.debugv("fake toot {0} -> {1}",r.key(),r.value()));
  }

}
