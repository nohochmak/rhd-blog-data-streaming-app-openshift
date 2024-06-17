package com.github.hpgrahsl.quarkus.kstreams;

import io.quarkus.kafka.client.serialization.JsonbSerde;
import io.quarkus.logging.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.json.bind.JsonbBuilder;

import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Grouped;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.state.KeyValueBytesStoreSupplier;
import org.apache.kafka.streams.state.Stores;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.hpgrahsl.quarkus.kstreams.model.Toot;

@ApplicationScoped
public class EmojiTrackerTopology {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
  private static final Logger LOG = Logger.getLogger(EmojiTrackerTopology.class);
  public static final String TOP_N_RECORD_KEY = "topN";

  @ConfigProperty(name="emojitracker.toots.topic")
  String topicNameToots;

  @ConfigProperty(name="emojitracker.toots.topic.partitions")
  int topicPartitionsToots;

  @ConfigProperty(name="emojitracker.emoji-counts-changelog.topic")
  String topicNameEmojiCountsStoreChangelog;

  @ConfigProperty(name="emojitracker.emoji-counts-changelog.topic.partitions")
  int topicPartitionsEmojiCountsStoreChangelog;

  @ConfigProperty(name="emojitracker.emoji.to.toots.topic")
  String topicNameEmojiToToots;

  @ConfigProperty(name="emojitracker.top.n.emojicount")
  int emojiCountTopN;

  @ConfigProperty(name="emojitracker.state.store.type")
  String stateStoreType;

  @ConfigProperty(name="emojitracker.state.store.emoji.counts")
  String stateStoreNameEmojiCounts;

  @ConfigProperty(name="emojitracker.state.store.emojis.top.n")
  String stateStoreNameEmojisTopN;

  @ConfigProperty(name="kafka.bootstrap.servers")
  String kafkaBootstrapServers;

  @PostConstruct
  void kafkaTopicInit() {
    try {
      Admin adminClient = Admin.create(
        Map.of(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG,kafkaBootstrapServers)
      );
      createTopicIfNotExists(adminClient, topicNameToots, topicPartitionsToots);
      createTopicIfNotExists(adminClient, topicNameEmojiCountsStoreChangelog, topicPartitionsEmojiCountsStoreChangelog);
    } catch (Exception e) {
      Log.errorv("topic(s) creation failed due to '{0}'",e.getMessage());
      e.printStackTrace();
    }
  }

  private void createTopicIfNotExists(Admin client, String topicName, int topicPartitions) throws InterruptedException,ExecutionException {
    if(!client.listTopics().names().get().contains(topicName)) {
      Log.infov("topic {0} doesn't exist -> prepare topic creation",topicName);
      var topics = Collections.singletonList(new NewTopic(topicName, topicPartitions, (short)1));
      var topicResults = client.createTopics(topics);
      topicResults.values().get(topicName).get();
      Log.infov("pre-create topic(s) {0}",topics);
    } else {
      Log.infov("topic {0} already exists",topicName);
    }
  }

  @Produces
  public Topology kstreamsTopology() {

    var tootSerde = new JsonbSerde<>(Toot.class);
    var countSerde = new JsonbSerde<>(EmojiCount.class);
    var topNSerde = new TopNSerdeEC(emojiCountTopN);
    var builder = new StreamsBuilder();
    var toots = builder.stream(topicNameToots, Consumed.with(Serdes.Long(), tootSerde));

    //FOR EACH UNIQUE EMOJI WITHIN A TOOT, EMIT & STORE THE TOOT ONCE
    toots.map((id,toot) -> KeyValue.pair(toot,new LinkedHashSet<>(EmojiUtils.extractEmojisAsString(toot.content()))))
        .flatMapValues(uniqueEmojis -> uniqueEmojis)
        .map((toot,emoji) -> KeyValue.pair(emoji, JsonbBuilder.create().toJson(toot)))
        .to(topicNameEmojiToToots, Produced.with(Serdes.String(),Serdes.String()));

    //STATEFUL COUNTING OF ALL EMOJIS CONTAINED IN THE TOOTS
    KTable<String, Long> emojiCounts = toots
        .map((id,toot) -> KeyValue.pair(id,EmojiUtils.extractEmojisAsString(toot.content())))
        .flatMapValues(emojis -> emojis)
        .map((id,emoji) -> KeyValue.pair(emoji,""))
        .groupByKey(Grouped.with(Serdes.String(), Serdes.String()))
        .count(Materialized.as(stateStoreSupplier(stateStoreType, stateStoreNameEmojiCounts)));

    emojiCounts.toStream().foreach((emoji,count) -> LOG.debugv("emoji: {0} - count: {1}",emoji,count));

    //MAINTAIN OVERALL TOP N EMOJIS SEEN SO FAR
    KTable<String, TopEmojis> mostFrequent = emojiCounts.toStream()
        .map((e, cnt) -> KeyValue.pair(TOP_N_RECORD_KEY, new EmojiCount(e, cnt)))
        .groupByKey(Grouped.with(Serdes.String(), countSerde))
        .aggregate(
            () -> new TopEmojis(emojiCountTopN),
            (key, emojiCount, topEmojis) -> topEmojis.add(emojiCount),
            Materialized.<String, TopEmojis>as(stateStoreSupplier(stateStoreType, stateStoreNameEmojisTopN))
                .withKeySerde(Serdes.String())
                .withValueSerde(topNSerde)
        );
    return builder.build();
  }

  private KeyValueBytesStoreSupplier stateStoreSupplier(String type, String storeName) {
    switch(type) {
      case "inmemory":
        return Stores.inMemoryKeyValueStore(storeName);
      case "rocksdb":
        return Stores.persistentKeyValueStore(storeName);
    }
    throw new IllegalArgumentException("state store persistence type must be either 'inmemory' or 'rocksdb'");
  }

}
