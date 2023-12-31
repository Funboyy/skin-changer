package de.funboyy.skin.changer.config;

import de.funboyy.skin.changer.SkinChangerAddon;
import de.funboyy.skin.changer.gui.activity.SkinChangeActivity;
import java.util.HashMap;
import java.util.Map;
import net.labymod.api.addon.AddonConfig;
import net.labymod.api.client.gui.screen.activity.Activity;
import net.labymod.api.client.gui.screen.widget.widgets.activity.settings.ActivitySettingWidget.ActivitySetting;
import net.labymod.api.client.gui.screen.widget.widgets.input.ButtonWidget.ButtonSetting;
import net.labymod.api.client.gui.screen.widget.widgets.input.SwitchWidget.SwitchSetting;
import net.labymod.api.configuration.loader.annotation.ConfigName;
import net.labymod.api.configuration.loader.annotation.Exclude;
import net.labymod.api.configuration.loader.property.ConfigProperty;
import net.labymod.api.configuration.settings.annotation.SettingSection;
import net.labymod.api.util.MethodOrder;

@ConfigName("settings")
public class SkinChangerConfig extends AddonConfig {

  @SwitchSetting
  private final ConfigProperty<Boolean> enabled = new ConfigProperty<>(true).addChangeListener(
      (property, oldValue, newValue) -> SkinChangerAddon.get().getNameCache().reloadTextures());

  @Exclude
  private final Map<String, SkinChange> skinChanges = new HashMap<>();

  @Override
  public ConfigProperty<Boolean> enabled() {
    return this.enabled;
  }

  public Map<String, SkinChange> getSkinChanges() {
    return this.skinChanges;
  }

  @SettingSection("skins")
  @MethodOrder(after = "enabled")
  @ActivitySetting
  public Activity openSkinChanges() {
    return new SkinChangeActivity();
  }

  @MethodOrder(after = "openSkinChanges")
  @ButtonSetting
  public void refresh() {
    SkinChangerAddon.get().getNameCache().reloadTextures();
  }

  public void removeInvalidSkinChanges() {
    this.skinChanges.entrySet()
        .removeIf(entry -> entry.getKey().isEmpty() || !entry.getValue().hasSkin());
  }

}
