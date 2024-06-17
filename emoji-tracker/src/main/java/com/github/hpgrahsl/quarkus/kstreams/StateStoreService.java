package com.github.hpgrahsl.quarkus.kstreams;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.core.buffer.Buffer;
import io.vertx.mutiny.ext.web.client.HttpResponse;
import io.vertx.mutiny.ext.web.client.WebClient;
import jakarta.annotation.PostConstruct;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response.Status;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyQueryMetadata;
import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.errors.InvalidStateStoreException;
import org.apache.kafka.streams.state.HostInfo;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

@ApplicationScoped
public class StateStoreService {

  private static final Logger LOGGER = Logger.getLogger(StateStoreService.class);

  @Inject
  Vertx vertx;

  WebClient webClient;

  @Inject
  KafkaStreams kafkaStreams;

  @ConfigProperty(name="quarkus.kafka-streams.application-server")
  String httpEndpoint;

  @ConfigProperty(name="emojitracker.state.store.emoji.counts")
  String stateStoreNameEmojiCounts;

  @ConfigProperty(name="emojitracker.state.store.emojis.top.n")
  String stateStoreNameEmojisTopN;

  @ConfigProperty(name="emojitracker.webapi.url.pattern")
  String webApiUrlPattern;

  private HostInfo myself;

  @PostConstruct
  void initialize() {
    webClient = WebClient.create(vertx);
    myself = HostInfo.buildFromEndpoint(httpEndpoint);
  }

  public Uni<EmojiCount> querySingleEmojiCount(String code) {
    try {
      KeyQueryMetadata metadata = kafkaStreams.queryMetadataForKey(
          stateStoreNameEmojiCounts, code, Serdes.String().serializer());
      LOGGER.debugv("kstreams metadata {0}",metadata.toString());

      if(myself.equals(metadata.activeHost())) {
        ReadOnlyKeyValueStore<String,Long> kvStoreEmojiCounts =
            kafkaStreams.store(StoreQueryParameters.fromNameAndType(
                stateStoreNameEmojiCounts, QueryableStoreTypes.keyValueStore())
            );
        LOGGER.debugv("state store for emoji {0} is locally available",code);
        Long count = kvStoreEmojiCounts.get(code);
        return Uni.createFrom().item(new EmojiCount(code, count != null ? count : 0L));
      }

      var remoteURL = String.format(webApiUrlPattern,
          metadata.activeHost().host(),metadata.activeHost().port(),"emojis/"+URLEncoder.encode(code, StandardCharsets.UTF_8.name()));
      
      LOGGER.debugv("state store for emoji code NOT locally available thus fetching from other kstreams instance @ {0}",remoteURL);
      return doEmojiApiGetRequest(remoteURL).onItem()
              .transform(response -> response.bodyAsJson(EmojiCount.class));
    } catch (Exception exc) {
      LOGGER.error(exc.getMessage());
      return Uni.createFrom().failure(exc);
    }
  }

  public Multi<EmojiCount> queryAllEmojiCounts() {
    LOGGER.debug("perform scatter & gather query across entire app state of emoji counts");
    var queries = Stream.concat(Stream.of(queryLocalEmojiCounts()),
        kafkaStreams.streamsMetadataForStore(stateStoreNameEmojiCounts)
            .stream()
            .filter(metadata -> !myself.equals(metadata.hostInfo()))
            .map(metadata -> doEmojiApiGetRequest(
                  String.format(webApiUrlPattern,metadata.host(),metadata.port(),"local/emojis")
                ).onItem().transformToMulti(StateStoreService::emojiCountMultiFromHttpResponse)
            )
    );
    return Multi.createBy().merging().streams(queries.collect(Collectors.toList()));
  }

  public Multi<EmojiCount> queryLocalEmojiCounts() {
    try {
      LOGGER.debug("querying local state store for all its managed emoji counts");
      ReadOnlyKeyValueStore<String,Long> kvStoreEmojiCounts =
          kafkaStreams.store(StoreQueryParameters.fromNameAndType(
              stateStoreNameEmojiCounts, QueryableStoreTypes.keyValueStore())
          );
      List<EmojiCount> result = new ArrayList<>();
      kvStoreEmojiCounts.all().forEachRemaining(
          entry -> result.add(new EmojiCount(entry.key, entry.value))
      );
      return Multi.createFrom().iterable(result);
    } catch (InvalidStateStoreException exc) {
      LOGGER.error(exc.getMessage());
      return Multi.createFrom().failure(exc);
    }
  }

  public Uni<Set<EmojiCount>> queryEmojisTopN() {
    var metadata = kafkaStreams.queryMetadataForKey(
        stateStoreNameEmojisTopN, EmojiTrackerTopology.TOP_N_RECORD_KEY, Serdes.String().serializer());
    LOGGER.debugv("kstreams metadata {0}",metadata.toString());

    if(myself.equals(metadata.activeHost())) {
      var kvStoreEmojisTopN =
          kafkaStreams.store(StoreQueryParameters.fromNameAndType(
              stateStoreNameEmojisTopN, QueryableStoreTypes.<String,TopEmojis>keyValueStore())
          );
      LOGGER.debugv("state store for top N emojis is locally available");
      return Uni.createFrom().item(
          Optional.ofNullable(kvStoreEmojisTopN.get(EmojiTrackerTopology.TOP_N_RECORD_KEY))
              .map(TopEmojis::getTopN)
              .orElseGet(TreeSet::new)
      );
    }

    var remoteURL = String.format(webApiUrlPattern,
        metadata.activeHost().host(),metadata.activeHost().port(),"emojis/stats/topN");

    LOGGER.debugv("state store for top N emojis NOT locally available thus fetching from other kstreams instance @ {0}",remoteURL);
    return doEmojiApiGetRequest(remoteURL)
        .onItem().transform(response ->
            response.bodyAsJsonArray().stream()
                .map(o -> (JsonObject)o)
                .map(json -> new EmojiCount(json.getString("emoji"),json.getLong("count")))
            .collect(Collectors.toCollection(TreeSet::new))
        );
  }

  private Uni<HttpResponse<Buffer>> doEmojiApiGetRequest(String url) {
    return webClient.getAbs(url)
        .putHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON)
        .send()
        .onFailure().invoke((exc) -> LOGGER.errorv("http request failure {0}",exc.getMessage()))
        .onSubscription().invoke(() -> LOGGER.debugv("querying remote host @ {0}",url));
  }

  private static Multi<EmojiCount> emojiCountMultiFromHttpResponse(HttpResponse<Buffer> response) {
    if(response.statusCode() != Status.OK.getStatusCode()) {
      LOGGER.errorv("http call expected to return 200 but was {} with body {}",response.statusCode(),response.bodyAsString());
      return Multi.createFrom().empty();
    }
    return Multi.createFrom().items(
            response.bodyAsJsonArray()
                .stream()
                .map(o -> (JsonObject)o)
                .map(json -> new EmojiCount(json.getString("emoji"),json.getLong("count")))
    );
  }

}
