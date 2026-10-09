package corgitaco.corgilib.client;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import corgitaco.corgilib.CorgiLib;
import corgitaco.corgilib.platform.ModPlatform;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.Reader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public record AnnouncementInfo(Component title, Component desc, Component actionButtonText,
                               long timeStamp, String url) {

    private static final String URL = "https://corgitaco.github.io/announcement_v2.json";

    public static final Codec<AnnouncementInfo> CODEC = RecordCodecBuilder.create(announcementInfoInstance ->
            announcementInfoInstance.group(
                    ComponentSerialization.CODEC.fieldOf("title").forGetter(AnnouncementInfo::title),
                    ComponentSerialization.CODEC.fieldOf("description").forGetter(AnnouncementInfo::desc),
                    ComponentSerialization.CODEC.fieldOf("action_button_text").forGetter(AnnouncementInfo::actionButtonText),
                    Codec.LONG.fieldOf("time").forGetter(AnnouncementInfo::timeStamp),
                    Codec.STRING.fieldOf("action_link").forGetter(AnnouncementInfo::url)
            ).apply(announcementInfoInstance, AnnouncementInfo::new)
    );
    private static volatile CompletableFuture<AnnouncementInfo> INSTANCE = CompletableFuture.supplyAsync(AnnouncementInfo::getTimeCheckedAnnouncement, Util.nonCriticalIoPool())
            .exceptionally(exception -> {
                CorgiLib.LOGGER.warn("Could not prepare the announcement", exception);
                return null;
            });


    public static void saveStoredAnnouncementInfo() {
        AnnouncementInfo announcementInfo = getInstance();

        if (announcementInfo == null) {
            return;
        }
        Path path = ModPlatform.PLATFORM.modConfigDir().resolve("announcement_store.json");

        try {
            Files.createDirectories(path.getParent());
            StoredAnnouncementInfo storedAnnouncementInfo = new StoredAnnouncementInfo(Minecraft.getInstance().getUser().getProfileId(), announcementInfo.timeStamp);
            String json = new GsonBuilder().create().toJson(storedAnnouncementInfo);
            Files.writeString(path, json);
        } catch (IOException e) {
            CorgiLib.LOGGER.warn("Could not store the dismissed announcement", e);
            return;
        }

        INSTANCE = null;

    }


    @Nullable
    private static AnnouncementInfo getTimeCheckedAnnouncement() {
        AnnouncementInfo announcementInfo = createInstance();

        if (announcementInfo == null) {
            return null;
        }

        Path path = ModPlatform.PLATFORM.modConfigDir().resolve("announcement_store.json");
        if (Files.exists(path)) {
            try (Reader reader = Files.newBufferedReader(path)) {
                JsonElement jsonElement = JsonParser.parseReader(reader);

                StoredAnnouncementInfo storedAnnouncementInfo = new GsonBuilder().create().fromJson(jsonElement, StoredAnnouncementInfo.class);

                if (storedAnnouncementInfo == null || storedAnnouncementInfo.unixTime < announcementInfo.timeStamp() || !Minecraft.getInstance().getUser().getProfileId().equals(storedAnnouncementInfo.player)) {
                    return announcementInfo;
                } else {
                    return null;
                }

            } catch (IOException | RuntimeException e) {
                CorgiLib.LOGGER.warn("Could not read the stored announcement; displaying the current announcement", e);
            }
        }

        return announcementInfo;
    }


    @Nullable
    private static AnnouncementInfo createInstance() {
        JsonObject jsonObject = fetchAnnouncementJson(URL);

        if (jsonObject != null) {
            DataResult<Pair<AnnouncementInfo, JsonElement>> decoded = CODEC.decode(JsonOps.INSTANCE, jsonObject);
            if (decoded.result().isPresent()) {
                AnnouncementInfo info = decoded.result().orElseThrow().getFirst();
                try {
                    URI actionLink = URI.create(info.url());
                    if (actionLink.getHost() != null && ("https".equalsIgnoreCase(actionLink.getScheme()) || "http".equalsIgnoreCase(actionLink.getScheme()))) {
                        return info;
                    }
                } catch (IllegalArgumentException e) {
                    CorgiLib.LOGGER.warn("The announcement action link is invalid", e);
                }
                CorgiLib.LOGGER.warn("The announcement action link must be an HTTP or HTTPS URL");
            } else {
                if (decoded.error().isPresent()) {
                    CorgiLib.LOGGER.error("Could not parse announcement json due to: %s".formatted(decoded.error().orElseThrow()));
                }
            }
        }
        return null;
    }


    @Nullable
    private static JsonObject fetchAnnouncementJson(String url) {
        try (HttpClient client = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.ALWAYS).connectTimeout(Duration.ofSeconds(10)).build()) {

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(15))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                String body = response.body();
                JsonElement jsonElement = JsonParser.parseString(body);
                if (jsonElement.isJsonObject()) {
                    return jsonElement.getAsJsonObject();
                }
                CorgiLib.LOGGER.warn("The announcement response is not a JSON object");
            } else {
                CorgiLib.LOGGER.info("Announcement GET request failed. Response Code: {}", response.statusCode());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (IOException | RuntimeException e) {
            CorgiLib.LOGGER.warn("Could not retrieve the announcement", e);
        }
        return null;
    }

    @Nullable
    public static AnnouncementInfo getInstance() {
        CompletableFuture<AnnouncementInfo> future = INSTANCE;
        return future == null ? null : future.getNow(null);
    }

    public static void whenLoaded(Consumer<AnnouncementInfo> listener) {
        CompletableFuture<AnnouncementInfo> future = INSTANCE;
        if (future != null) {
            future.thenAccept(info -> {
                if (info != null) {
                    Minecraft.getInstance().execute(() -> {
                        if (getInstance() == info) {
                            listener.accept(info);
                        }
                    });
                }
            });
        }
    }

    private record StoredAnnouncementInfo(UUID player, long unixTime) {
    }
}