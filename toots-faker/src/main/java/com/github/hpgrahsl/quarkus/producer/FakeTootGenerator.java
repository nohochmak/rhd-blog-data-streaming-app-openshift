package com.github.hpgrahsl.quarkus.producer;

import com.github.hpgrahsl.quarkus.producer.model.Account;
import com.github.hpgrahsl.quarkus.producer.model.Toot;
import com.vdurmont.emoji.Emoji;
import com.vdurmont.emoji.EmojiManager;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class FakeTootGenerator {

  public static final List<Emoji> ALL_EMOJIS = new ArrayList<>(EmojiManager.getAll());

  @ConfigProperty(name="tootsfaker.toot.seed.text")
  String tootSeedText;
  @ConfigProperty(name="tootsfaker.max.toot.length")
  int maxTweetLength;
  @ConfigProperty(name="tootsfaker.min.toot.length")
  int minTweetLength;
  @ConfigProperty(name="tootsfaker.toot.max.num.emojis")
  int tootMaxNumEmojis = 16;
  @ConfigProperty(name="tootsfaker.toot.max.count.per.emoji")
  int tootMaxCountPerEmoji = 4;

  public String generateFakeTextWithEmojis() {
    var words = new ArrayList<>(Arrays.asList(tootSeedText.split("\\s+")));
    words.addAll(getListWithRandomEmojis());
    Collections.shuffle(words);
    var toot = String.join(" ", words);
    return toot.substring(0,Math.min(toot.length(),ThreadLocalRandom.current()
        .nextInt(minTweetLength, maxTweetLength)));
  }

  public Toot createFakeToot() {
    return new Toot(
      ThreadLocalRandom.current().nextLong(1_000_000_000_000_000_000L,Long.MAX_VALUE),
      LocalDateTime.now().atZone(ZoneId.systemDefault()).toString(),
      null,
       generateFakeTextWithEmojis(),
       "https://", 
       new Account(
        ThreadLocalRandom.current().nextLong(1_000_000_000_000_000_000L,Long.MAX_VALUE),
        UUID.randomUUID().toString(),
        "https://", 
        false,
        0,
        0,
        0
        )
    );
  }

  private List<String> getListWithRandomEmojis() {
    return
        Stream.generate(
            () -> {
              var rndEmoji = ThreadLocalRandom.current()
                  .nextInt(0, ALL_EMOJIS.size());
              var rndCount = ThreadLocalRandom.current()
                  .nextInt(1, tootMaxCountPerEmoji + 1);
             return Stream.generate(
                  () -> ALL_EMOJIS.get(rndEmoji).getUnicode()
              ).limit(rndCount).collect(Collectors.toList());
            }
        ).flatMap(Collection::stream).limit(ThreadLocalRandom.current()
            .nextInt(0, tootMaxNumEmojis + 1)).collect(Collectors.toList());
  }

}
