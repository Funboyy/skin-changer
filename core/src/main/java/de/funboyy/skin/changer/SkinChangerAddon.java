package de.funboyy.skin.changer;

import de.funboyy.skin.changer.config.SkinChangerConfig;
import de.funboyy.skin.changer.listener.SkinListener;
import net.labymod.api.addon.LabyAddon;
import net.labymod.api.models.addon.annotation.AddonMain;

@AddonMain
public class SkinChangerAddon extends LabyAddon<SkinChangerConfig> {

  private static SkinChangerAddon instance;

  public SkinChangerAddon() {
    instance = this;
  }

  public static SkinChangerAddon get() {
    return instance;
  }

  private final SkinChangeCache skinChangeCache = new SkinChangeCache();

  @Override
  protected void enable() {
    this.registerSettingCategory();
    this.registerListener(new SkinListener());

    this.configuration().removeInvalidSkinChanges();
    this.configuration().getSkinChanges().keySet().forEach(this.skinChangeCache::add);

    if (this.wasLoadedInRuntime()) {
      this.skinChangeCache.reloadTextures();
    }
  }

  @Override
  protected Class<SkinChangerConfig> configurationClass() {
    return SkinChangerConfig.class;
  }

  public SkinChangeCache getSkinChange() {
    return this.skinChangeCache;
  }

}
