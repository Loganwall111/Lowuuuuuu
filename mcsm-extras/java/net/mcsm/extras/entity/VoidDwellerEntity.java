package net.mcsm.extras.entity;

import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * BUILD #483 -- THE VOID DWELLER: the thing in the gel that talks to you.
 *
 * <p>THE BRIEF. "Register your talking creature entities (mcsm:void_dweller) with
 * highly stylized, cartoonish fish-like silhouettes that glide smoothly through the
 * Gel Horizon ... emit deep, echoing, cosmic mechanical whale clicks and songs",
 * and show lore to the player in a typewriter overlay.
 *
 * <p>WHAT IT IS. A weightless drifter of the deep: no walking goals, no gravity of
 * its own, a slow glide with damping (the same motion the mod's whale already swims
 * with, so the two read as one family), and a mouth. It notices a player, says one
 * line of the deep's own lore, and the line is carried on a SYNCED channel -- the
 * same mechanism the cast uses for their talk animation -- so the client can set it
 * down in the frame letter by letter without a packet of its own. The line is the
 * packet.
 *
 * <p>ITS VOICE IS THE MOD'S OWN, PITCHED AND TIMED. The plan asks for whale clicks
 * and songs "through your low-latency Ogg Vorbis engine": that engine is
 * {@code McsmSounds}, and every cue in it is already an Ogg this mod generated
 * itself. There is no way to author a NEW Ogg in this workspace -- no encoder exists
 * here and none exists in the build image (the build's own sound check says so) --
 * so the dweller speaks with what the mod has: the beacon's keystrokes for its
 * clicks, the whisper for its breath, and the vast drone, pitched down, for its
 * song. The whale's own voice was already that drone; this is the same animal
 * saying something.
 */
public class VoidDwellerEntity extends PathfinderMob {

    private static final EntityDataAccessor<String> SAY =
            SynchedEntityData.defineId(VoidDwellerEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Integer> SAY_TICKS =
            SynchedEntityData.defineId(VoidDwellerEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> SAY_TOTAL =
            SynchedEntityData.defineId(VoidDwellerEntity.class, EntityDataSerializers.INT);

    /** The deep's own lore, in the show's voice: short, spoken, and never explained. */
    private static final String[] LORE = {
            "you fell. everyone falls. the gel keeps what it catches.",
            "there was a sky up there once. i have seen it in the rifts.",
            "the whales sing the storm's name. do not learn it.",
            "the sponge grows toward the noise you make.",
            "i was a builder. the void took the building, not the hands.",
            "the seam above us is a door. nothing else down here is.",
            "the lights in the cavern are not lamps. they are looking.",
            "the abyss has no light because something down there eats it.",
            "do not trust the rifts. they show you a city that is not there.",
            "you are the first thing with a heartbeat in a long time.",
            "we do not go below the gel. the gel goes below us.",
            "when you stop falling, that is when it has you.",
    };

    private int quiet = 80 + (int) (Math.random() * 200.0D);
    private int soak;

    public VoidDwellerEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        this.setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.22D)
                .add(Attributes.FOLLOW_RANGE, 32.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.6D);
    }

    @Override
    protected void registerGoals() {
        // and nothing that walks: it glides, so it looks at things and drifts
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 14.0F, 0.8F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(SAY, "");
        builder.define(SAY_TICKS, 0);
        builder.define(SAY_TOTAL, 0);
    }

    // ---- the talking ---------------------------------------------------------

    /** Say a line for {@code ticks}. The client typewrites it while it lasts. */
    public void speak(String line, int ticks) {
        this.entityData.set(SAY, line == null ? "" : line);
        this.entityData.set(SAY_TOTAL, Math.max(1, ticks));
        this.entityData.set(SAY_TICKS, Math.max(1, ticks));
        if (!this.level().isClientSide()) {
            // its voice: the beacon's own keys for clicks, the whisper for breath,
            // and the deep drone, pitched down, for the song under the words
            float pitch = 0.55F + this.random.nextFloat() * 0.25F;
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    net.mcsm.extras.McsmSounds.RADIO_MORSE, SoundSource.NEUTRAL, 0.7F, pitch);
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    net.mcsm.extras.McsmSounds.MASSG_WHISPER, SoundSource.NEUTRAL, 0.5F,
                    pitch + 0.2F);
            if (this.random.nextInt(3) == 0) {
                this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                        net.mcsm.extras.McsmSounds.OBLIVION_DRONE, SoundSource.NEUTRAL, 0.9F,
                        0.45F);
            }
        }
    }

    /** The line it is saying, or the empty string. Read by the client's overlay. */
    public String saying() {
        return this.entityData.get(SAY);
    }

    public int sayTicks() {
        return this.entityData.get(SAY_TICKS);
    }

    public int sayTotal() {
        return Math.max(1, this.entityData.get(SAY_TOTAL));
    }

    // ---- and the drifting ----------------------------------------------------

    @Override
    public void aiStep() {
        super.aiStep();
        try {
            // weightless: it is inside the gel, and the gel does the holding
            Vec3 motion = this.getDeltaMovement();
            if (!this.level().isClientSide()) {
                if (this.getDeltaMovement().y > -0.02D) {
                    this.setDeltaMovement(motion.x * 0.97D, motion.y * 0.97D - 0.0016D,
                            motion.z * 0.97D);
                }
                int t = this.entityData.get(SAY_TICKS);
                if (t > 0) {
                    this.entityData.set(SAY_TICKS, t - 1);
                }
                if (--this.quiet <= 0) {
                    this.quiet = 300 + this.random.nextInt(600);
                    Player near = this.level().getNearestPlayer(this, 12.0D);
                    if (near != null && this.entityData.get(SAY_TICKS) <= 0) {
                        this.getLookControl().setLookAt(near, 30.0F, 30.0F);
                        speak(LORE[this.random.nextInt(LORE.length)],
                                90 + this.random.nextInt(60));
                    }
                }
            } else if (this.sayTicks() > 0 && this.tickCount % 6 == 0) {
                // the bubbles it breathes, and the glow it is lit by
                this.level().addParticle(net.minecraft.core.particles.ParticleTypes.BUBBLE,
                        this.getX() + (this.random.nextDouble() - 0.5D) * 0.8D,
                        this.getEyeY() - 0.2D,
                        this.getZ() + (this.random.nextDouble() - 0.5D) * 0.8D,
                        0.0D, 0.02D, 0.0D);
            }
        } catch (Throwable ignored) {
            // a dweller that throws is worse than a quiet one
        }
        this.soak++;
    }

    @Override
    public boolean removeWhenFarAway(double dist) {
        return false;
    }

    @Override
    public Component getName() {
        return Component.literal("Void Dweller");
    }
}
