package de.funboyy.skin.changer;

import net.labymod.api.client.resources.texture.GameImage;
import net.labymod.api.client.session.MinecraftServices.SkinVariant;
import net.labymod.api.mojang.texture.SkinPolicy;

public class SkinChangerPolicy {


  // pay attention the second pair of coordinates is excluded at getAverageColor
  public static SkinVariant guessVariant(final GameImage image) {
    final GameImage texture = SkinPolicy.applyPolicy(image);
    final int emptyColor = texture.getRGBA(54, 19);
    int counter = 0;

    // top/bottom of right arm
    if (texture.getAverageColor(44, 16, 50, 20) != emptyColor
        && texture.getAverageColor(50, 16, 52, 20) == emptyColor) {
      counter++;
    }

    // sides of right arm
    if (texture.getAverageColor(40, 20, 54, 32) != emptyColor
        && texture.getAverageColor(54, 20, 56, 32) == emptyColor) {
      counter++;
    }

    // top/bottom of left arm
    if (texture.getAverageColor(36, 48, 42, 52) != emptyColor
        && texture.getAverageColor(42, 48, 44, 52) == emptyColor) {
      counter++;
    }

    // sides of left arm
    if (texture.getAverageColor(32, 52, 46, 64) != emptyColor
        && texture.getAverageColor(46, 52, 48, 64) == emptyColor) {
      counter++;
    }

    // only 3 sides need to be black, because some people skins have artworks or just random pixels
    return counter >= 3 ? SkinVariant.SLIM : SkinVariant.CLASSIC;
  }

}
