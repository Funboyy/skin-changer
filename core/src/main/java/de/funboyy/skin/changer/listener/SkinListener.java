package de.funboyy.skin.changer.listener;

import de.funboyy.skin.changer.NameCache.Entry;
import de.funboyy.skin.changer.SkinChangerAddon;
import de.funboyy.skin.changer.config.SkinChange;
import java.util.concurrent.CompletableFuture;
import net.labymod.api.Laby;
import net.labymod.api.client.resources.CompletableResourceLocation;
import net.labymod.api.client.resources.ResourceLocation;
import net.labymod.api.client.resources.ResourceLocationFactory;
import net.labymod.api.client.resources.player.PlayerTexture;
import net.labymod.api.client.resources.player.PlayerTextureType;
import net.labymod.api.client.resources.player.meta.SkinPlayerTextureMeta;
import net.labymod.api.client.session.MinecraftServices.SkinVariant;
import net.labymod.api.event.Subscribe;
import net.labymod.api.event.client.entity.player.TextureLoadEvent;
import net.labymod.api.mojang.GameProfile;

public class SkinListener {

  private final SkinChangerAddon addon = SkinChangerAddon.get();

  @Subscribe
  public void onTextureLoad(final TextureLoadEvent event) {
    if (event.type() != PlayerTextureType.SKIN) {
      return;
    }

    if (!this.addon.configuration().enabled().get()) {
      return;
    }

    final SkinChange skinChange = getSkinChange(event.profile());

    if (skinChange == null || !skinChange.isEnabled()) {
      return;
    }

    final CompletableFuture<PlayerTexture> future = new CompletableFuture<>();
    event.setLoadFuture(future);

    loadSkin(skinChange, future);
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

    final Entry entry = this.addon.getNameCache().get(profile.getUniqueId());

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
            Laby.references().playerTextureService().policyProcessor(PlayerTextureType.SKIN),
            texture -> future.complete(playerTexture));

    if (cached != null && cached.hasResult()) {
      future.complete(playerTexture);
    }
  }

}
