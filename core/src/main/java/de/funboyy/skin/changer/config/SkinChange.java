package de.funboyy.skin.changer.config;

import com.google.gson.annotations.SerializedName;
import java.util.UUID;
import net.labymod.api.Constants.Urls;
import net.labymod.api.client.session.MinecraftServices.SkinVariant;
import org.jetbrains.annotations.Nullable;

public class SkinChange {

  public static SkinChange create(final boolean enabled, final String imageHash, final SkinVariant skinVariant) {
    return new SkinChange(enabled, imageHash, skinVariant);
  }

  public static SkinChange createDefault() {
    return new SkinChange(true, null, null);
  }

  private boolean enabled;
  @SerializedName("image_hash")
  private String imageHash;
  @SerializedName("skin_variant")
  private SkinVariant skinVariant;

  private SkinChange(final boolean enabled, final String imageHash, final SkinVariant skinVariant) {
    this.enabled = enabled;
    this.imageHash = imageHash;
    this.skinVariant = skinVariant;
  }

  public boolean isEnabled() {
    return this.enabled;
  }

  public void setEnabled(final boolean enabled) {
    this.enabled = enabled;
  }

  public @Nullable String getImageHash() {
    return this.imageHash;
  }

  public void setImageHash(@Nullable final String imageHash) {
    this.imageHash = imageHash;
  }

  public String getDownloadUrl() {
    return String.format(Urls.LABYNET_TEXTURE_DOWNLOAD, this.getImageHash());
  }

  public @Nullable SkinVariant getSkinVariant() {
    return this.skinVariant;
  }

  public void setSkinVariant(@Nullable final SkinVariant skinVariant) {
    this.skinVariant = skinVariant;
  }

  public boolean hasSkin() {
    return this.imageHash != null && this.skinVariant != null;
  }

}
