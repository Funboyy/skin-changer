package de.funboyy.skin.changer.gui.widget;

import de.funboyy.skin.changer.SkinChangerAddon;
import de.funboyy.skin.changer.gui.widget.model.EmptyAnimationController;
import de.funboyy.skin.changer.gui.widget.model.EmptyModel;
import net.labymod.api.Laby;
import net.labymod.api.client.gui.lss.property.annotation.AutoWidget;
import net.labymod.api.client.gui.mouse.MutableMouse;
import net.labymod.api.client.gui.screen.key.MouseButton;
import net.labymod.api.client.gui.screen.widget.widgets.ModelWidget;
import net.labymod.api.client.render.matrix.Stack;
import net.labymod.api.client.resources.CompletableResourceLocation;
import net.labymod.api.client.resources.ResourceLocation;
import net.labymod.api.client.resources.ResourceLocationFactory;
import net.labymod.api.client.resources.player.PlayerTextureType;
import net.labymod.api.labynet.models.textures.Skin;

@AutoWidget
public class PlayerModelWidget extends ModelWidget {

  /**
   * This class was inspired by
   * {@code net.labymod.core.client.gui.screen.widget.widgets.customization.PlayerModelWidget}
   * **/

  private Skin skin;
  private float clickX;

  public PlayerModelWidget() {
    super(new EmptyModel(), new EmptyAnimationController(), 16.0F, 32.0F);
    this.animationController.onStop((animation) -> this.model.reset());
  }

  public void setModel(final Skin skin) {
    this.skin = skin;

    final ResourceLocationFactory resourceFactory = Laby.references().resourceLocationFactory();
    final ResourceLocation resourceLocation = resourceFactory.create(
        SkinChangerAddon.get().addonInfo().getNamespace(), skin.getImageHash());
    final CompletableResourceLocation completable = Laby.references().textureRepository()
        .getOrRegisterTexture(resourceLocation, skin.getDownloadUrl(),
            this.labyAPI.playerTextureService().policyProcessor(PlayerTextureType.SKIN),
            texture -> {});

    this.labyAPI.minecraft().executeOnRenderThread(() -> this.setModel(completable));
  }

  public void setModel(final CompletableResourceLocation resourceLocation) {
    this.setModel(Laby.labyAPI().playerTextureService().loadModel(PlayerTextureType.SKIN,
        resourceLocation, this::setModel));
  }

  @Override
  public void renderWidget(final Stack stack, final MutableMouse mouse, final float tickDelta) {
    if (this.isDragging()) {
      super.rotation.set(0.0F, (this.clickX - (float) mouse.getX()) / 50.0F, 0.0F);
    }

    super.renderWidget(stack, mouse, tickDelta);
  }

  @Override
  public boolean mouseClicked(final MutableMouse mouse, final MouseButton mouseButton) {
    this.clickX = (float) ((int) (mouse.getXDouble() + (double) (super.rotation.getY() * 50.0F)));
    return super.mouseClicked(mouse, mouseButton);
  }

  public Skin getSkin() {
    return this.skin;
  }

}
