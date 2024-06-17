package com.github.hpgrahsl.quarkus.producer.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Account(
    long id,
    String username,
    String url,
    boolean bot,
    long followers_count,
    long following_count,
    long statuses_count
) {}
