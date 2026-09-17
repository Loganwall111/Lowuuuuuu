package net.mcsm.extras.client;

/**
 * BUILD #463 -- the value rides the vanilla render state.
 *
 * <p>A renderer hook cannot look up "which player is this" cheaply and must not
 * touch the world while drawing, so the number is written into the render state
 * during {@code extractRenderState} and read back during {@code getModelTint}.
 * This is the same arrangement the base mod's own wither-sickness tint uses, and
 * the interface lives in {@code net.mcsm.extras.client} the same way theirs does.
 */
public interface McsmVoidAgingState {

    float mcsm$voidAge();

    void mcsm$setVoidAge(float age);
}
