package de.funboyy.skin.changer;

import com.google.gson.JsonObject;
import de.hdskins.textureload.api.TextureLoadExtension;
import de.hdskins.textureload.api.texture.PlayerTextureService;
import de.hdskins.textureload.api.texture.PlayerTextureType;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import net.labymod.api.util.io.web.request.types.GsonRequest;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SkinChangeCache {

  private static final String PROFILE_URL = "https://api.mojang.com/users/profiles/minecraft/%s";

  private final List<Entry> entries = new ArrayList<>();

  public void add(final String name) {
    final Entry entry = this.get(name);

    if (entry != null) {
      entry.reloadTexture();
      return;
    }

    this.entries.add(new Entry(name));
  }

  public Entry get(final String name) {
    for (final Entry entry : this.entries) {
      if (entry.getName().equalsIgnoreCase(name)) {
        return entry;
      }
    }

    return null;
  }

  public Entry get(final UUID uniqueId) {
    for (final Entry entry : this.entries) {
      if (uniqueId.equals(entry.getUniqueId())) {
        return entry;
      }
    }

    return null;
  }

  public void remove(final String name) {
    final Entry entry = this.get(name);

    if (entry != null) {
      this.entries.remove(entry);
    }
  }
  
  public void reloadTextures() {
    TextureLoadExtension.references().playerTextureService().invalidateTextures();
  }

  // FixMe: for some player it will not work, because of IDEA-15369
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

    /*Laby.labyAPI().labyNetController().loadUniqueIdByName(name, response -> {
      if (response.hasException() || response.isEmpty()) {
        result.accept(null);
        return;
      }

      result.accept(response.get());
    });*/
  }

  public static class Entry {

    private final String name;
    private UUID uniqueId;

    private Entry(@NotNull final String name) {
      this.name = name;

      loadUniqueId(this.name, response -> {
        if (response == null) {
          SkinChangerAddon.get().logger().warn("Failed to load unique id for " + this.name);
        }

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
      final PlayerTextureService textureService = TextureLoadExtension.references().playerTextureService();
      textureService.reloadTexture(PlayerTextureType.SKIN,
          textureService.createProfile(this.uniqueId, this.name));
    }

  }

}
