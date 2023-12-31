package de.funboyy.skin.changer.gui.widget.model;

import java.util.Collections;
import java.util.Map;
import net.labymod.api.Textures;
import net.labymod.api.client.render.model.Model;
import net.labymod.api.client.render.model.ModelPart;
import net.labymod.api.client.resources.ResourceLocation;

public class EmptyModel implements Model {

  private static final Map<String, ModelPart> EMPTY_MAP = Collections.emptyMap();

  @Override
  public void addPart(final String name, final ModelPart part) {
  }

  @Override
  public boolean isInvalidPart(final String name) {
    return true;
  }

  @Override
  public ModelPart getPart(final String name) {
    return null;
  }

  @Override
  public Map<String, ModelPart> getParts() {
    return EMPTY_MAP;
  }

  @Override
  public ResourceLocation getTextureLocation() {
    return Textures.EMPTY;
  }

  @Override
  public void setTextureLocation(final ResourceLocation textureLocation) {
  }

  @Override
  public void addChild(final String name, final ModelPart child) {
  }

  @Override
  public ModelPart getChild(final String name) {
    return null;
  }

  @Override
  public Map<String, ModelPart> getChildren() {
    return EMPTY_MAP;
  }

}
