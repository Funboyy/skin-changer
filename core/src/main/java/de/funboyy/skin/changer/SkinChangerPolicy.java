package de.funboyy.skin.changer;

import net.labymod.api.Laby;
import net.labymod.api.client.resources.texture.GameImage;
import net.labymod.api.client.session.MinecraftServices.SkinVariant;

public class SkinChangerPolicy {

  public static GameImage convertLegacy(final GameImage image) {
    if (image.getHeight() != 32) {
      return image;
    }

    final GameImage converted = Laby.references()
        .gameImageProvider()
        .createImage(64, 64);
    converted.copyFrom(image);
    converted.fillRect(0, 32, 64, 32, 0);
    converted.copyRect(4, 16, 16, 32, 4, 4, true, false);
    converted.copyRect(8, 16, 16, 32, 4, 4, true, false);
    converted.copyRect(0, 20, 24, 32, 4, 12, true, false);
    converted.copyRect(4, 20, 16, 32, 4, 12, true, false);
    converted.copyRect(8, 20, 8, 32, 4, 12, true, false);
    converted.copyRect(12, 20, 16, 32, 4, 12, true, false);
    converted.copyRect(44, 16, -8, 32, 4, 4, true, false);
    converted.copyRect(48, 16, -8, 32, 4, 4, true, false);
    converted.copyRect(40, 20, 0, 32, 4, 12, true, false);
    converted.copyRect(44, 20, -8, 32, 4, 12, true, false);
    converted.copyRect(48, 20, -16, 32, 4, 12, true, false);
    converted.copyRect(52, 20, -8, 32, 4, 12, true, false);

    return converted;
  }

  public static SkinVariant guessVariant(final GameImage image) {
    final GameImage texture = convertLegacy(image);
    final int emptyColor = texture.getRGBA(0, 0);

    // top/bottom of right arm
    if (texture.getAverageColor(44, 16, 49, 19) != emptyColor
        && texture.getAverageColor(50, 16, 51, 19) == emptyColor) {
      return SkinVariant.SLIM;
    }

    // front/back of right arm
    if (texture.getAverageColor(40, 20, 53, 31) != emptyColor
        && texture.getAverageColor(54, 20, 55, 31) == emptyColor) {
      return SkinVariant.SLIM;
    }

    // top/bottom of left arm
    if (texture.getAverageColor(36, 48, 41, 51) != emptyColor
        && texture.getAverageColor(42, 48, 43, 51) == emptyColor) {
      return SkinVariant.SLIM;
    }

    // front/back of left arm
    if (texture.getAverageColor(32, 52, 45, 63) != emptyColor
        && texture.getAverageColor(46, 52, 47, 63) == emptyColor) {
      return SkinVariant.SLIM;
    }

    return SkinVariant.CLASSIC;
  }

}
