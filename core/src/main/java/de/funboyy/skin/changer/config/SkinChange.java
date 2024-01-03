package de.funboyy.skin.changer.config;

import com.google.gson.annotations.SerializedName;
import net.labymod.api.Constants.Urls;
import net.labymod.api.client.session.MinecraftServices.SkinVariant;
import org.jetbrains.annotations.NotNull;

public class SkinChange {

  public static SkinChange create(final boolean enabled, final String imageHash, final SkinVariant skinVariant) {
    return new SkinChange(enabled, imageHash, skinVariant);
  }

  public static SkinChange createDefault() {
    return create(true, "66088fe456abc1215cb0e918d8fe5bef", SkinVariant.CLASSIC);
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

  public String getImageHash() {
    return this.imageHash;
  }

  public void setImageHash(@NotNull final String imageHash) {
    this.imageHash = imageHash;
  }

  public String getDownloadUrl() {
    return String.format(Urls.LABYNET_TEXTURE_DOWNLOAD, this.getImageHash());
  }

  public SkinVariant getSkinVariant() {
    return this.skinVariant;
  }

  public void setSkinVariant(@NotNull final SkinVariant skinVariant) {
    this.skinVariant = skinVariant;
  }

}
