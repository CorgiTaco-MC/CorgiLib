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
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import org.jetbrains.annotations.Nullable;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public record AnnouncementInfo(Component title, Component desc, Component actionButtonText,
                               long timeStamp, String url) {

    private static final String URL = "https://corgitaco.github.io/announcement.json";

    public static final Codec<AnnouncementInfo> CODEC = RecordCodecBuilder.create(announcementInfoInstance ->
            announcementInfoInstance.group(
                    ComponentSerialization.CODEC.fieldOf("title").forGetter(AnnouncementInfo::title),
                    ComponentSerialization.CODEC.fieldOf("description").forGetter(AnnouncementInfo::desc),
                    ComponentSerialization.CODEC.fieldOf("action_button_text").forGetter(AnnouncementInfo::actionButtonText),
                    Codec.LONG.fieldOf("time").forGetter(AnnouncementInfo::timeStamp),
                    Codec.STRING.fieldOf("action_link").forGetter(AnnouncementInfo::url)
            ).apply(announcementInfoInstance, AnnouncementInfo::new)
    );
    private static CompletableFuture<AnnouncementInfo> INSTANCE = CompletableFuture.supplyAsync(AnnouncementInfo::getTimeCheckedAnnouncement, Util.backgroundExecutor());


    public static void saveStoredAnnouncementInfo() {
        if (INSTANCE == null) {
            return;
        }
        AnnouncementInfo announcementInfo = INSTANCE.getNow(null);

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
            throw new RuntimeException(e);
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
        if (path.toFile().exists()) {
            try {
                JsonElement jsonElement = JsonParser.parseReader(new FileReader(path.toFile()));

                StoredAnnouncementInfo storedAnnouncementInfo = new GsonBuilder().create().fromJson(jsonElement, StoredAnnouncementInfo.class);

                if (storedAnnouncementInfo.unixTime < announcementInfo.timeStamp() || !storedAnnouncementInfo.player.equals(Minecraft.getInstance().getUser().getProfileId())) {
                    return announcementInfo;
                } else {
                    return null;
                }

            } catch (FileNotFoundException e) {
                return null;
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
                return decoded.result().orElseThrow().getFirst();
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
        try {
            HttpClient client = HttpClient.newHttpClient();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                String body = response.body();
                JsonElement jsonElement = JsonParser.parseString(body);
                return jsonElement.getAsJsonObject();
            } else {
                CorgiLib.LOGGER.error("GET request failed. Response Code: {}", response.statusCode());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Nullable
    public static AnnouncementInfo getInstance() {
        return INSTANCE == null ? null : INSTANCE.getNow(null);
    }

    private record StoredAnnouncementInfo(UUID player, long unixTime) {
    }
}