package com.github.hpgrahsl.quarkus.kstreams;

import io.quarkus.logging.Log;
import io.quarkus.runtime.Startup;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.tuples.Tuple2;
import java.util.Set;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.jboss.resteasy.annotations.SseElementType;

@Startup
@ApplicationScoped
@Path("/api")
public class EmojisQueryAPI {

  private static final Jsonb JSONB = JsonbBuilder.create();
  
  @Inject
  StateStoreService service;

  Multi<EmojiCount> emojiCountsStream;
  Multi<Tuple2<String,String>> emojiTootsStream;

  public EmojisQueryAPI(
      @Channel("emoji-counts-changelog-stream") Multi<EmojiCount> emojiCountsStream,
      @Channel("emoji-to-toots-stream") Multi<Tuple2<String,String>> emojiTootsStream
  ) {
    this.emojiCountsStream = emojiCountsStream;
    this.emojiCountsStream.subscribe().with((i) -> {});
    this.emojiTootsStream = emojiTootsStream;
    this.emojiTootsStream.subscribe().with((i) -> {});
  }

  @GET
  @Path("emojis/{code}")
  @Produces(MediaType.APPLICATION_JSON)
  public Uni<EmojiCount> getEmojiCount(@PathParam("code") String code) {
    return service.querySingleEmojiCount(code);
  }

  @GET
  @Path("emojis")
  @Produces(MediaType.APPLICATION_JSON)
  public Multi<EmojiCount> getEmojiCounts() {
    return service.queryAllEmojiCounts();
  }

  @GET
  @Path("local/emojis")
  @Produces(MediaType.APPLICATION_JSON)
  public Multi<EmojiCount> getEmojiCountsLocal() {
    return service.queryLocalEmojiCounts();
  }

  @GET
  @Path("emojis/stats/topN")
  @Produces(MediaType.APPLICATION_JSON)
  public Uni<Set<EmojiCount>> getEmojisTopN() {
    return service.queryEmojisTopN();
  }

  @GET
  @Path("emojis/updates/notify")
  @Produces(MediaType.SERVER_SENT_EVENTS)
  @SseElementType(MediaType.APPLICATION_JSON)
  public Multi<EmojiCount> getEmojiCountsStream() {
    return emojiCountsStream
        .invoke(ec -> Log.debugv("SSE to client {0}",ec.toString()));
  }

  @GET
  @Path("emojis/{code}/toots")
  @Produces(MediaType.SERVER_SENT_EVENTS)
  @SseElementType(MediaType.TEXT_PLAIN)
  public Multi<String> getEmojiTootsStream(@PathParam("code") String code) {
    return emojiTootsStream
        .filter(t2 -> t2.getItem1().equals(code))
        .map(t2 -> JSONB.toJson(t2))
        .invoke(s -> Log.debugv("SSE to client {0}",s));
  }

}
