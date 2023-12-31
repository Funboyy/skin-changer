package de.funboyy.skin.changer.gui.widget;

import de.funboyy.skin.changer.config.SkinChange;
import net.labymod.api.client.component.Component;
import net.labymod.api.client.gui.icon.Icon;
import net.labymod.api.client.gui.lss.property.annotation.AutoWidget;
import net.labymod.api.client.gui.screen.Parent;
import net.labymod.api.client.gui.screen.widget.SimpleWidget;
import net.labymod.api.client.gui.screen.widget.widgets.ComponentWidget;
import net.labymod.api.client.gui.screen.widget.widgets.renderer.IconWidget;

@AutoWidget
public class SkinChangeWidget extends SimpleWidget {

  /**
   * This class was inspired by
   * {@code net.labymod.addons.customnametags.gui.activity.NameTagWidget}
   * **/

  private String userName;
  private String originalName;
  private SkinChange skinChange;

  public SkinChangeWidget(final String userName, final SkinChange skinChange) {
    this.userName = userName;
    this.originalName = userName;
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
    return Icon.head(userName.length() == 0 ? "MHF_Question" : userName);
  }

  public String getUserName() {
    return this.userName;
  }

  public void setUserName(final String userName) {
    this.userName = userName;
  }

  public String getOriginalName() {
    return this.originalName;
  }

  public void applyUserName() {
    this.originalName = this.userName;
  }

  public SkinChange getSkinChange() {
    return this.skinChange;
  }

  public void setSkinChange(final SkinChange skinChange) {
    this.skinChange = skinChange;
  }

}
