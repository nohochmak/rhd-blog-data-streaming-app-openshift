package com.github.hpgrahsl.quarkus.kstreams.model;

public record Account(
    long id,
    String username,
    String url,
    boolean bot,
    long followers_count,
    long following_count,
    long statuses_count
) {}
