package de.funboyy.skin.changer;

import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import net.labymod.api.Laby;
import net.labymod.api.client.resources.player.PlayerTextureService;
import net.labymod.api.client.resources.player.PlayerTextureType;
import net.labymod.api.util.io.web.request.types.GsonRequest;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class NameCache {

  private static final String PROFILE_URL = "https://api.mojang.com/users/profiles/minecraft/%s";

  private final List<Entry> entries = new ArrayList<>();

  public void add(final String name) {
    final Entry entry = get(name);

    if (entry != null) {
      entry.reloadTexture();
      return;
    }

    this.entries.add(new Entry(name));
  }

  public Entry get(final String name) {
    return this.entries.stream().filter(entry -> entry.getName().equalsIgnoreCase(name))
        .findFirst().orElse(null);
  }

  public Entry get(final UUID uniqueId) {
    return this.entries.stream().filter(entry -> entry.getUniqueId() != null)
        .filter(entry -> entry.getUniqueId().equals(uniqueId))
        .findFirst().orElse(null);
  }

  public void remove(final String name) {
    final Entry entry = get(name);

    if (entry != null) {
      this.entries.remove(entry);
    }
  }
  
  public void reloadTextures() {
    Laby.labyAPI().playerTextureService().invalidateTextures();
  }

  // Using mojang api here because it has a better rate limit for my use case
  private static void loadUniqueId(final String name, final Consumer<UUID> result) {
    GsonRequest.of(JsonObject.class).url(PROFILE_URL, name).async().execute(response -> {
      if (response.hasException() || response.isEmpty()) {
        result.accept(null);
        return;
      }

      final JsonObject object = response.get();

      if (!object.has("id") || !object.get("id").isJsonPrimitive()) {
        result.accept(null);
        return;
      }

      try {
        result.accept(UUID.fromString(object.get("id").getAsString()
            .replaceFirst("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})",
                "$1-$2-$3-$4-$5")));
      } catch (final IllegalArgumentException ignored) {
        result.accept(null);
      }
    });
  }

  public static class Entry {

    private final String name;
    private UUID uniqueId;

    private Entry(@NotNull final String name) {
      this.name = name;

      loadUniqueId(this.name, response -> {
        this.uniqueId = response;
        this.reloadTexture();
      });
    }

    @NotNull
    public String getName() {
      return this.name;
    }

    @Nullable
    public UUID getUniqueId() {
      return this.uniqueId;
    }

    public void reloadTexture() {
      final PlayerTextureService textureService = Laby.labyAPI().playerTextureService();
      textureService.reloadTexture(PlayerTextureType.SKIN,
          textureService.createProfile(this.uniqueId, this.name));
    }

  }

}
