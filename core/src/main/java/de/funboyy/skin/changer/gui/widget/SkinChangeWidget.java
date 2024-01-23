package de.funboyy.skin.changer.gui.widget;

import de.funboyy.skin.changer.config.SkinChange;
import net.labymod.api.Constants.Urls;
import net.labymod.api.client.component.Component;
import net.labymod.api.client.gui.icon.Icon;
import net.labymod.api.client.gui.lss.property.annotation.AutoWidget;
import net.labymod.api.client.gui.screen.Parent;
import net.labymod.api.client.gui.screen.widget.SimpleWidget;
import net.labymod.api.client.gui.screen.widget.widgets.ComponentWidget;
import net.labymod.api.client.gui.screen.widget.widgets.renderer.IconWidget;

@AutoWidget
public class SkinChangeWidget extends SimpleWidget {

  private static final String HEAD_URL = Urls.LABYNET_BASE + "api/v3/texture/%s/head.png";

  /**
   * This class was inspired by
   * {@code net.labymod.addons.customnametags.gui.activity.NameTagWidget}
   * **/

  private String userName;
  private SkinChange skinChange;

  public SkinChangeWidget(final String userName, final SkinChange skinChange) {
    this.userName = userName;
    this.skinChange = skinChange;
  }

  @Override
  public void initialize(final Parent parent) {
    super.initialize(parent);

    if (this.skinChange.isEnabled()) {
      this.removeId("disabled");
    } else {
      this.addId("disabled");
    }

    final IconWidget iconWidget = new IconWidget(this.getIconWidget(this.userName));
    iconWidget.addId("avatar");
    this.addChild(iconWidget);

    final ComponentWidget nameWidget = ComponentWidget.component(Component.text(this.userName));
    nameWidget.addId("name");
    this.addChild(nameWidget);
  }

  public Icon getIconWidget(final String userName) {
    final String imageHash = userName.isEmpty() ? "03dce9e898cfa3bdc60320a8ca431502"
        : this.metadata().get("image_hash", this.skinChange.getImageHash());

    return Icon.url(String.format(HEAD_URL, imageHash));
  }

  public String getUserName() {
    return this.userName;
  }

  public void setUserName(final String userName) {
    this.userName = userName;
  }

  public SkinChange getSkinChange() {
    return this.skinChange;
  }

  public void setSkinChange(final SkinChange skinChange) {
    this.skinChange = skinChange;
  }

  public void resetMeta() {
    this.metadata().remove("user_name");
    this.metadata().remove("enabled");
    this.metadata().remove("skin_variant");
    this.metadata().remove("image_hash");
  }

}
