package de.funboyy.skin.changer;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.labymod.api.Laby;
import net.labymod.api.client.resources.player.PlayerTextureService;
import net.labymod.api.client.resources.player.PlayerTextureType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class NameCache {

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
      entry.reloadTexture();
    }
  }
  
  public void reloadTextures() {
    this.entries.forEach(Entry::reloadTexture);
  }

  public static class Entry {

    private final String name;
    private UUID uniqueId;

    private Entry(@NotNull final String name) {
      this.name = name;

      Laby.labyAPI().labyNetController().loadUniqueIdByName(this.name, response -> {
        this.uniqueId = response.getNullable();
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
