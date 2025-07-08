package com.soarclient.management.websocket;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import com.soarclient.logger.SoarLogger;
import com.soarclient.management.websocket.client.SoarWebSocketClient;
import com.soarclient.management.websocket.packet.SoarPacket;
import com.soarclient.utils.HttpUtils;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.session.Session;

public class WebSocketManager implements AutoCloseable {

    private static final int MAX_RETRY = 3;
    
    private final MinecraftClient client = MinecraftClient.getInstance();
    private GameProfile gameProfile;
    private SoarWebSocketClient webSocket;
    private final AtomicInteger retryCount = new AtomicInteger();

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public WebSocketManager() {
        scheduler.scheduleWithFixedDelay(this::checkAndReconnect, 0, 10, TimeUnit.SECONDS);
    }

    private void checkAndReconnect() {
        try {
            GameProfile currentProfile = client.getGameProfile();

            if (gameProfile != null && currentProfile != null && !gameProfile.equals(currentProfile)) {
                retryCount.set(0);
            }

            if ((webSocket == null || !Objects.equals(gameProfile, currentProfile) || webSocket.isClosed())
                    && retryCount.get() < MAX_RETRY) {

                gameProfile = currentProfile;

                if (webSocket != null) {
                    webSocket.close();
                }

                JsonObject postObject = new JsonObject();
                Session session = client.getSession();

                postObject.addProperty("accessToken", session.getAccessToken());
                postObject.addProperty("selectedProfile", session.getUuidOrNull().toString().replace("-", ""));
                postObject.addProperty("serverId", "cbd2c3f65d7ba5cceba0cc9647ff9a85c371f4");
                HttpUtils.postJson("https://sessionserver.mojang.com/session/minecraft/join", postObject);

                Map<String, String> headers = new HashMap<>();
                headers.put("name", gameProfile.getName());
                headers.put("uuid", gameProfile.getId().toString().replace("-", ""));

                try {
                    webSocket = new SoarWebSocketClient(headers, retryCount::getAndIncrement);
                    webSocket.connect();
                } catch (URISyntaxException e) {
                    SoarLogger.error("RPC", "Bad syntax in the websocket connection URI");
                }
            }
        } catch (Exception e) {
            SoarLogger.error("RPC", "Failed to connect to RPC", e);
        }
    }
    
    public void send(SoarPacket packet) {
        if (webSocket != null && webSocket.isOpen()) {
            webSocket.send(packet.toJson().toString());
        }
    }

    @Override
    public void close() throws Exception {
        scheduler.close();
    }
}
