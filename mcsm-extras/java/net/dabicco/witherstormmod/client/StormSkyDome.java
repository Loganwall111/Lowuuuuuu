package net.dabicco.witherstormmod.client;

/**
 * BUILD #441 -- THE DOME IS GONE.
 *
 * What used to be here was a world-space dome: a shell around the camera that
 * painted the storm's colours. It was retired in favour of the native sky
 * renderer (McsmNativeSkyRenderer owns sky colour and fog) and this file was left
 * behind as a compatibility shell with a name full of promises it could not keep
 * -- five methods that returned nothing and a zero colour between them.
 *
 * Nothing in this build called any of them, nothing in the base mod can (it is
 * compiled against the same frozen jars this overlay is), and the previous
 * behaviour was inert. So the shell carries one honest answer instead: there is
 * no dome, and no strength. (The retired entry points are not named here again;
 * `ci/check_phase_uniform.py` fails the build if any of them comes back.)
 */
public final class StormSkyDome {
    private StormSkyDome() {
    }

    /** There is no dome. The sky is drawn by the native sky renderer. */
    public static float strength() {
        return 0.0F;
    }
}
