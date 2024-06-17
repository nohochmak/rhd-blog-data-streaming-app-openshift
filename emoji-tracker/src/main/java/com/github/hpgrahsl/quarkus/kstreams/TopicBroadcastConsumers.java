package com.github.hpgrahsl.quarkus.kstreams;

import io.smallrye.mutiny.tuples.Tuple2;
import io.smallrye.reactive.messaging.kafka.IncomingKafkaRecord;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.reactive.messaging.Acknowledgment;
import org.eclipse.microprofile.reactive.messaging.Acknowledgment.Strategy;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Outgoing;

@ApplicationScoped
public class TopicBroadcastConsumers {

  @Incoming("quarkus-emoji-tracker-store-emoji-counts-changelog")
  @Outgoing("emoji-counts-changelog-stream")
  @Acknowledgment(Strategy.NONE)
  public EmojiCount broadcastPublisherEmojiCounts(IncomingKafkaRecord<String,Long> record) {
    return new EmojiCount(record.getKey(),record.getPayload());
  }

  @Incoming("emoji-to-toots")
  @Outgoing("emoji-to-toots-stream")
  @Acknowledgment(Strategy.NONE)
  public Tuple2<String,String> broadcastPublisherEmojiTweets(IncomingKafkaRecord<String,String> record) {
    return Tuple2.of(record.getKey(),record.getPayload());
  }

}
