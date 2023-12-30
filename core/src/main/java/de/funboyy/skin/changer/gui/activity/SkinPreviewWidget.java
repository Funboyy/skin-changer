package de.funboyy.skin.changer.gui.activity;

import net.labymod.api.Constants.Urls;
import net.labymod.api.client.component.Component;
import net.labymod.api.client.gui.lss.property.annotation.AutoWidget;
import net.labymod.api.client.gui.lss.style.modifier.attribute.AttributeState;
import net.labymod.api.client.gui.mouse.MutableMouse;
import net.labymod.api.client.gui.screen.Parent;
import net.labymod.api.client.gui.screen.key.MouseButton;
import net.labymod.api.client.gui.screen.widget.AbstractWidget;
import net.labymod.api.client.gui.screen.widget.Widget;
import net.labymod.api.client.gui.screen.widget.context.ContextMenu;
import net.labymod.api.client.gui.screen.widget.context.ContextMenuEntry;
import net.labymod.api.client.gui.screen.widget.widgets.input.SimpleButtonWidget;
import net.labymod.api.client.gui.screen.widget.widgets.renderer.IconWidget;
import net.labymod.api.labynet.models.textures.Skin;

@AutoWidget
public class SkinPreviewWidget extends AbstractWidget<Widget> {

  private final SkinBrowseActivity skinActivity;
  private final Skin skin;

  public SkinPreviewWidget(final SkinBrowseActivity skinActivity, final Skin skin) {
    this.skinActivity = skinActivity;
    this.skin = skin;
    this.lazy = true;
  }

  @Override
  public void initialize(final Parent parent) {
    super.initialize(parent);

    final IconWidget iconWidget = new IconWidget(this.skin.previewIcon());
    iconWidget.setCleanupOnDispose(true);
    this.addChild(iconWidget);

    final SimpleButtonWidget applyButton = new SimpleButtonWidget(
        Component.translatable("skinchanger.gui.manage.skin.apply"));
    applyButton.addId("button", "apply-button");
    applyButton.setAttributeState(AttributeState.ENABLED, true);
    applyButton.setPressable(() -> this.skinActivity.setSkin(this.skin));
    this.addChild(applyButton);
  }

  public boolean mouseClicked(final MutableMouse mouse, final MouseButton mouseButton) {
    if (mouseButton == MouseButton.RIGHT && !this.hasContextMenu()) {
      final ContextMenu contextMenu = this.createContextMenu();
      contextMenu.addEntry(ContextMenuEntry.builder()
          .text(Component.translatable("skinchanger.gui.manage.skin.open"))
          .clickHandler((entry) -> {
        this.labyAPI.minecraft().chatExecutor().openUrl(String
            .format(Urls.LABYNET_SKIN, this.skin.getImageHash()));
        return true;
      }).build());
    }

    return super.mouseClicked(mouse, mouseButton);
  }

}
