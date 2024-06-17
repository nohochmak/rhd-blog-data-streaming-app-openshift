package com.github.hpgrahsl.quarkus.producer.model;

import java.util.List;

public record RawMessage (List<String> stream, String event, String payload) {}
