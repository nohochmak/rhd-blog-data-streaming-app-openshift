package com.github.hpgrahsl.quarkus.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.hpgrahsl.quarkus.producer.model.RawMessage;
import com.github.hpgrahsl.quarkus.producer.model.Toot;

import io.quarkus.logging.Log;
import jakarta.inject.Inject;
import jakarta.websocket.ClientEndpoint;
import jakarta.websocket.DecodeException;
import jakarta.websocket.Decoder;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;

@ClientEndpoint(
    decoders = {MastodonWebSocketClient.MastodonWebSocketDecoder.class}
)
public class MastodonWebSocketClient {

    public static final String MASTODON_MESSAGE_FILTER = "\"event\":\"update\"";

    @Inject
    TootsProducer producer;

    public static class MastodonWebSocketDecoder implements Decoder.Text<Toot> {

		private static ObjectMapper OBJECT_MAPPER = new ObjectMapper();

        @Override
        public Toot decode(String s) throws DecodeException {
            try {
                if (s.contains(MASTODON_MESSAGE_FILTER)) {
                    var raw = OBJECT_MAPPER.readValue(s, RawMessage.class);
                    return OBJECT_MAPPER.readValue(raw.payload(),Toot.class);            
                } 
            } catch (JsonProcessingException e) {
                Log.errorv("failed to decode {0}",s);
                throw new DecodeException(s,"failed to decode string",e);
            }
            return null;
        }

        @Override
        public boolean willDecode(String s) {
            return true;
        }

	}

    @OnOpen
    public void open(Session session) {
        Log.infov("session id '{0}'",session.getId());
    }

    @OnMessage
    void handleMessage(Session session, Toot toot) throws JsonProcessingException {
        Log.debugv("wss incoming (on {0}) -> {1}", session.getId(), toot);
        producer.sendToot(toot);
    }

}
