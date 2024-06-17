package com.github.hpgrahsl.quarkus.kstreams.model;

public record Toot(
    long id,
    String created_at,
    String language,
    String content,
    String url,
    Account account
) {}


