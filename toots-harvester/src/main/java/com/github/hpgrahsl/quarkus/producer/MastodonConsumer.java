package com.github.hpgrahsl.quarkus.producer;

import java.io.IOException;
import java.net.URI;

import org.eclipse.microprofile.config.ConfigProvider;

import io.quarkus.logging.Log;
import io.quarkus.runtime.Startup;
import jakarta.inject.Singleton;
import jakarta.websocket.ContainerProvider;
import jakarta.websocket.DeploymentException;
import jakarta.websocket.Session;

@Startup 
@Singleton
public class MastodonConsumer {

    public static final String CONFIG_PROPERTY_WSS_API_ENDPOINT = "mastodon.wss.api.endpoint";
    public static final String CONFIG_PROPERTY_STREAM_SUBSCRIPTION = "mastodon.stream.subscription";
    
    Session session;

    public MastodonConsumer() {        
        var mastodonWssApiEndpoint = ConfigProvider.getConfig().getValue(CONFIG_PROPERTY_WSS_API_ENDPOINT, String.class);
        do {
            Log.infov("connecting to websocket server @ {0}",mastodonWssApiEndpoint);
            try {
                session =
                    ContainerProvider
                        .getWebSocketContainer()
                        .connectToServer(MastodonWebSocketClient.class, URI.create(mastodonWssApiEndpoint));
                break;
            } catch (DeploymentException | IOException e) {
                Log.errorv("websocket connection failed due to -> {0}", e.getMessage());
            }        
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {}
        } while (session == null);

        Log.infov("connected (session id: {0})",session.getId());
        subscribeToFederatedTimelineFeed();
    }

    private void subscribeToFederatedTimelineFeed() {
        var mastodonStreamSubscription = ConfigProvider.getConfig().getValue(CONFIG_PROPERTY_STREAM_SUBSCRIPTION, String.class);
        session.getAsyncRemote().sendText(mastodonStreamSubscription, result -> {
            if (result.getException() != null) {
                Log.errorv("subscription failed for {0}", mastodonStreamSubscription, result.getException());
            } else {
                Log.infov("subscribed with {0}", mastodonStreamSubscription);
            }
        });
    }

}
