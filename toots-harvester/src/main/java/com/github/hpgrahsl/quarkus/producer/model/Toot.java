package com.github.hpgrahsl.quarkus.producer.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Toot(
    long id,
    String created_at,
    String language,
    String content,
    String url,
    Account account
) {}


