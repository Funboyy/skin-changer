package de.funboyy.skin.changer.listener;

import de.funboyy.skin.changer.SkinChangeCache.Entry;
import de.funboyy.skin.changer.SkinChangerAddon;
import de.funboyy.skin.changer.SkinChangerPolicy;
import de.funboyy.skin.changer.config.SkinChange;
import de.hdskins.textureload.api.TextureLoadExtension;
import de.hdskins.textureload.api.event.TextureLoadEvent;
import de.hdskins.textureload.api.texture.PlayerTexture;
import de.hdskins.textureload.api.texture.PlayerTextureType;
import de.hdskins.textureload.api.texture.meta.SkinPlayerTextureMeta;
import java.util.concurrent.CompletableFuture;
import net.labymod.api.Laby;
import net.labymod.api.client.network.ClientPacketListener;
import net.labymod.api.client.network.NetworkPlayerInfo;
import net.labymod.api.client.network.PlayerSkin;
import net.labymod.api.client.resources.CompletableResourceLocation;
import net.labymod.api.client.resources.ResourceLocation;
import net.labymod.api.client.resources.ResourceLocationFactory;
import net.labymod.api.client.resources.texture.GameImage;
import net.labymod.api.client.resources.texture.Texture;
import net.labymod.api.client.session.MinecraftServices.SkinVariant;
import net.labymod.api.event.Subscribe;
import net.labymod.api.event.client.network.playerinfo.PlayerInfoAddEvent;
import net.labymod.api.event.client.network.playerinfo.PlayerInfoUpdateEvent;
import net.labymod.api.event.client.network.server.ServerJoinEvent;
import net.labymod.api.event.client.network.server.ServerSwitchEvent;
import net.labymod.api.event.client.network.server.SubServerSwitchEvent;
import net.labymod.api.metadata.Metadata;
import net.labymod.api.mojang.GameProfile;

public class SkinListener {

  private final SkinChangerAddon addon = SkinChangerAddon.get();

  @Subscribe
  public void onServerJoin(final ServerJoinEvent event) {
    this.handleJoinAndSwitch();
  }

  @Subscribe
  public void onServerSwitch(final ServerSwitchEvent event) {
    this.handleJoinAndSwitch();
  }

  @Subscribe
  public void onSubServerSwitch(final SubServerSwitchEvent event) {
    this.handleJoinAndSwitch();
  }

  private void handleJoinAndSwitch() {
    if (!this.addon.configuration().enabled().get() || !this.addon.configuration().variant().get()) {
      return;
    }

    final ClientPacketListener packetListener = Laby.labyAPI().minecraft().getClientPacketListener();

    if (packetListener == null) {
      return;
    }

    for (final NetworkPlayerInfo playerInfo : packetListener.getNetworkPlayerInfos()) {
      this.updateSkinVariant(playerInfo);
    }
  }

  @Subscribe
  public void onPlayerInfoAdd(final PlayerInfoAddEvent event) {
    if (!this.addon.configuration().enabled().get() || !this.addon.configuration().variant().get()) {
      return;
    }

    this.updateSkinVariant(event.playerInfo());
  }

  @Subscribe
  public void onPlayerInfoUpdate(final PlayerInfoUpdateEvent event) {
    if (!this.addon.configuration().enabled().get() || !this.addon.configuration().variant().get()) {
      return;
    }

    this.updateSkinVariant(event.playerInfo());
  }

  private void updateSkinVariant(final NetworkPlayerInfo playerInfo) {
    final PlayerSkin skin = playerInfo.getSkin();

    if (skin == null) {
      return;
    }

    final ResourceLocation location = skin.getSkinTexture();
    final Metadata metadata = location.metadata();

    if (metadata.has("skin_variant")) {
      skin.setSkinVariant(metadata.get("skin_variant"));
      return;
    }

    final Texture texture = Laby.references().textureRepository().getTexture(location);

    if (texture == null) {
      return;
    }

    final GameImage image = GameImage.IMAGE_PROVIDER.loadImage(texture);
    final SkinVariant variant = SkinChangerPolicy.guessVariant(image);

    if (skin.getSkinVariant() == variant) {
      return;
    }

    this.addon.logger().debug(String.format("Changes skin variant of %s (%s) from %s to %s",
        playerInfo.profile().getUsername(), playerInfo.profile().getUniqueId(), skin.getSkinVariant(), variant));

    metadata.set("skin_variant", variant);
    skin.setSkinVariant(variant);
  }

  @Subscribe
  public void onTextureLoad(final TextureLoadEvent event) {
    if (event.type() != PlayerTextureType.SKIN) {
      return;
    }

    if (!this.addon.configuration().enabled().get()) {
      return;
    }

    final SkinChange skinChange = this.getSkinChange(event.profile());

    if (skinChange == null || !skinChange.isEnabled()) {
      return;
    }

    final CompletableFuture<PlayerTexture> future = new CompletableFuture<>();
    event.setLoadFuture(future);

    this.loadSkin(skinChange, future);
  }

  private SkinChange getSkinChange(final GameProfile profile) {
    SkinChange skinChange = null;

    if (profile.getUsername() != null) {
      skinChange = this.addon.configuration().getSkinChanges().get(profile.getUsername());
    }

    if (skinChange != null) {
      return skinChange;
    }

    if (profile.getUniqueId() == null) {
      return null;
    }

    final Entry entry = this.addon.getSkinChange().get(profile.getUniqueId());

    if (entry == null) {
      return null;
    }

    return this.addon.configuration().getSkinChanges().get(entry.getName());
  }

  private void loadSkin(final SkinChange skin, final CompletableFuture<PlayerTexture> future) {
    final ResourceLocationFactory resourceFactory = Laby.references().resourceLocationFactory();
    final ResourceLocation resourceLocation = resourceFactory.create(
        this.addon.addonInfo().getNamespace(), skin.getImageHash());
    final PlayerTexture playerTexture = new PlayerTexture(PlayerTextureType.SKIN, skin.getDownloadUrl(),
        resourceLocation, new SkinPlayerTextureMeta(skin.getSkinVariant() == SkinVariant.SLIM));
    final CompletableResourceLocation cached = Laby.references().textureRepository()
        .getOrRegisterTexture(resourceLocation, skin.getDownloadUrl(),
            TextureLoadExtension.references().playerTextureService().policyProcessor(PlayerTextureType.SKIN),
            texture -> future.complete(playerTexture));

    if (cached != null && cached.hasResult()) {
      future.complete(playerTexture);
    }
  }

}
