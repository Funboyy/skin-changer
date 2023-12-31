package de.funboyy.skin.changer.gui.widget.model;

import java.util.function.Consumer;
import net.labymod.api.client.render.matrix.Stack;
import net.labymod.api.client.render.model.Model;
import net.labymod.api.client.render.model.ModelPart;
import net.labymod.api.client.render.model.animation.AnimationController;
import net.labymod.api.client.render.model.animation.ModelAnimation;
import net.labymod.api.client.render.model.animation.TransformationType;
import net.labymod.api.util.function.FloatSupplier;
import net.labymod.api.util.math.vector.FloatVector3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EmptyAnimationController implements AnimationController {

  private static final EmptyAnimationApplier EMPTY_ANIMATION_APPLIER = new EmptyAnimationApplier();
  private static final PartTransformer PART_TRANSFORMER = new PartTransformer() {};

  @Override
  public AnimationController playNext(@NotNull final ModelAnimation animation) {
    return this;
  }

  @Override
  public AnimationController swap(@Nullable final ModelAnimation animation) {
    return this;
  }

  @Override
  public AnimationController stop() {
    return this;
  }

  @Override
  public boolean isPlaying() {
    return false;
  }

  @Override
  public AnimationController queue(@NotNull final ModelAnimation animation) {
    return this;
  }

  @Override
  public boolean isQueued(@NotNull final ModelAnimation animation) {
    return false;
  }

  @Nullable
  @Override
  public ModelAnimation getPlaying() {
    return null;
  }

  @Override
  public float getSpeed() {
    return 0;
  }

  @Override
  public AnimationController speed(final float speed) {
    return this;
  }

  @Override
  public int getMaxQueueSize() {
    return 0;
  }

  @Override
  public AnimationController maxQueueSize(final int maxQueueSize) {
    return this;
  }

  @Override
  public boolean isAnimateStrict() {
    return false;
  }

  @Override
  public AnimationController animateStrict(final boolean animateStrict) {
    return this;
  }

  @Override
  public AnimationController tickProvider(@NotNull final FloatSupplier tickProvider) {
    return this;
  }

  @NotNull
  @Override
  public AnimationApplier animationApplier() {
    return EMPTY_ANIMATION_APPLIER;
  }

  @Override
  public AnimationController animationApplier(@NotNull final AnimationApplier animationApplier) {
    return this;
  }

  @NotNull
  @Override
  public PartTransformer partTransformer() {
    return PART_TRANSFORMER;
  }

  @Override
  public AnimationController partTransformer(@NotNull final PartTransformer partTransformer) {
    return this;
  }

  @Override
  public AnimationController enableTransformation(final TransformationType type) {
    return this;
  }

  @Override
  public AnimationController disableTransformation(final TransformationType type) {
    return this;
  }

  @Override
  public AnimationController applyAnimation(final Model model, final String... excludedParts) {
    return this;
  }

  @Override
  public AnimationController transform(final Stack stack, final String partName) {
    return this;
  }

  @Nullable
  @Override
  public FloatVector3 getCurrentPosition(@NotNull final String partName) {
    return null;
  }

  @Nullable
  @Override
  public FloatVector3 getCurrentRotation(@NotNull final String partName) {
    return null;
  }

  @Nullable
  @Override
  public FloatVector3 getCurrentScale(@NotNull final String partName) {
    return null;
  }

  @Override
  public float getProgressTicks() {
    return 0;
  }

  @Override
  public long getProgress() {
    return 0;
  }

  @Override
  public AnimationController onStop(final Consumer<ModelAnimation> onStop) {
    return this;
  }

  public static class EmptyAnimationApplier implements AnimationApplier {

    @Override
    public void applyPosition(final Model model, final ModelPart modelPart, final FloatVector3 position) {
    }

    @Override
    public void applyRotation(final Model model, final ModelPart modelPart, final FloatVector3 rotation) {
    }

    @Override
    public void applyScale(final Model model, final ModelPart modelPart, final FloatVector3 scale) {
    }

  }

}
