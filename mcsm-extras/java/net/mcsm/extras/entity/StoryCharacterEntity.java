package net.mcsm.extras.entity;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.OpenDoorGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * 1.9.205 -- the Story Mode cast as a real humanoid entity.
 *
 * A player-shaped PathfinderMob (Steve proportions, 64x64 skin) with the
 * vanilla walk/arm-swing cycle, look-at-player, wander and door-opening AI,
 * plus two synced "acting" channels the renderer animates:
 *   TALK  -- head nods and a raised gesturing arm, with speech dust motes
 *   LAUGH -- quick head-bob / shoulder shake with villager-laugh audio
 * The dialogue engine (McsmNpcs) flips these when a line plays; idle
 * characters also chatter to each other every so often.
 *
 * Character identity is the synced CHARACTER string, which is also the skin
 * file name (textures/entity/story/<character>.png).
 */
public class StoryCharacterEntity extends PathfinderMob {

    private static final EntityDataAccessor<String> CHARACTER =
            SynchedEntityData.defineId(StoryCharacterEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Integer> TALK =
            SynchedEntityData.defineId(StoryCharacterEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> LAUGH =
            SynchedEntityData.defineId(StoryCharacterEntity.class, EntityDataSerializers.INT);

    private int idleChatter = 200 + (int) (Math.random() * 400);
    /** BUILD #480 -- ticks of voice left, and the gap between syllables. */
    private int voiceTicks;
    private int voiceGap;

    public StoryCharacterEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        this.setCanPickUpLoot(false);
        this.setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 40.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.30D)
                .add(Attributes.FOLLOW_RANGE, 24.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.4D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new OpenDoorGoal(this, true));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.55D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 10.0F, 0.9F));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, StoryCharacterEntity.class, 6.0F, 0.6F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(CHARACTER, "jesse");
        builder.define(TALK, 0);
        builder.define(LAUGH, 0);
    }

    // ---- identity ---------------------------------------------------------

    public String getCharacter() {
        return this.entityData.get(CHARACTER);
    }

    public void setCharacter(String name) {
        this.entityData.set(CHARACTER, name == null ? "jesse" : name.toLowerCase().replace(' ', '_'));
    }

    // ---- acting channels ----------------------------------------------------

    /** Play the talk gesture for {@code ticks}. */
    public void talk(int ticks) {
        this.entityData.set(TALK, Math.max(this.entityData.get(TALK), ticks));
    }

    /**
     * BUILD #480 -- SPEAK: the gesture AND the voice.
     *
     * <p>Until now the cast could move their mouths and nothing came out of them:
     * {@code talk()} drove the head and the gesturing arm, and the only audio a cast
     * member ever made was a vanilla villager laugh. That is the "they don't talk,
     * they just stand there" half of the report. A line now plays as a mouth that
     * moves and a voice that lands syllable by syllable for as long as the line runs.
     */
    public void speak(int ticks) {
        this.talk(ticks);
        this.voiceTicks = Math.max(this.voiceTicks, ticks);
    }

    /** Every line a cast member says gets the voice that fits who they are. */
    private void voice() {
        if (this.voiceTicks <= 0) {
            return;
        }
        this.voiceTicks--;
        if (--this.voiceGap > 0) {
            return;
        }
        this.voiceGap = 7 + this.random.nextInt(6);
        String who = getCharacter();
        net.minecraft.sounds.SoundEvent cue;
        float pitch;
        if (who.contains("pama")) {
            // machines: keys, not vowels
            cue = net.mcsm.extras.McsmSounds.TERMINAL_KEY;
            pitch = 1.35F + this.random.nextFloat() * 0.2F;
        } else if (who.contains("nurm") || who.contains("keeper")
                || who.contains("tracker") || who.contains("pumpkin")) {
            // the ones who are not people: whispers and little laughs
            cue = this.random.nextInt(3) == 0
                    ? net.mcsm.extras.McsmSounds.MASSG_GIGGLE
                    : net.mcsm.extras.McsmSounds.MASSG_WHISPER;
            pitch = 1.25F + this.random.nextFloat() * 0.5F;
        } else {
            // a person: formant blips, pitched to the character rather than at random
            cue = net.mcsm.extras.McsmSounds.RADIO_VOICE;
            pitch = 0.85F + (Math.abs(who.hashCode()) % 45) / 100.0F
                    + this.random.nextFloat() * 0.12F;
        }
        this.level().playSound(null, this.getX(), this.getY() + 1.6D, this.getZ(), cue,
                SoundSource.NEUTRAL, 0.55F, pitch);
    }

    /** Play the laugh for {@code ticks}. */
    public void laugh(int ticks) {
        this.entityData.set(LAUGH, Math.max(this.entityData.get(LAUGH), ticks));
        if (!this.level().isClientSide()) {
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.VILLAGER_CELEBRATE, SoundSource.NEUTRAL, 0.9F,
                    1.15F + this.random.nextFloat() * 0.25F);
        }
    }

    public boolean isTalking() {
        return this.entityData.get(TALK) > 0;
    }

    public boolean isLaughing() {
        return this.entityData.get(LAUGH) > 0;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.level().isClientSide()) {
            int t = this.entityData.get(TALK);
            if (t > 0) {
                this.entityData.set(TALK, t - 1);
            }
            int l = this.entityData.get(LAUGH);
            if (l > 0) {
                this.entityData.set(LAUGH, l - 1);
            }
            // BUILD #480 -- and the voice, on its own clock, all the way through the line
            this.voice();
            // idle chatter between cast members: a short talk, sometimes a laugh
            if (--idleChatter <= 0) {
                idleChatter = 300 + this.random.nextInt(600);
                StoryCharacterEntity other = null;
                double best = Double.MAX_VALUE;
                for (StoryCharacterEntity e : this.level().getEntitiesOfClass(StoryCharacterEntity.class,
                        this.getBoundingBox().inflate(6.0D))) {
                    if (e == this) continue;
                    double d = e.distanceToSqr(this);
                    if (d < best) { best = d; other = e; }
                }
                if (other != null) {
                    this.getLookControl().setLookAt(other, 30.0F, 30.0F);
                    int line = 40 + this.random.nextInt(40);
                    this.speak(line);
                    if (this.random.nextInt(3) == 0) {
                        other.laugh(30);
                    } else {
                        other.speak(30);
                    }
                }
            }
        } else if (this.isTalking() && this.tickCount % 4 == 0) {
            // speech motes drifting from the mouth
            this.level().addParticle(new DustParticleOptions(0xFFFFFFFF, 0.55F),
                    this.getX() + (this.random.nextDouble() - 0.5D) * 0.3D,
                    this.getEyeY() - 0.15D,
                    this.getZ() + (this.random.nextDouble() - 0.5D) * 0.3D,
                    0.0D, 0.02D, 0.0D);
        }
    }

    @Override
    public HumanoidArm getMainArm() {
        return HumanoidArm.RIGHT;
    }

    @Override
    public boolean removeWhenFarAway(double dist) {
        return false;
    }

    @Override
    protected void addAdditionalSaveData(net.minecraft.world.level.storage.ValueOutput out) {
        super.addAdditionalSaveData(out);
        out.putString("McsmCharacter", getCharacter());
    }

    @Override
    protected void readAdditionalSaveData(net.minecraft.world.level.storage.ValueInput in) {
        super.readAdditionalSaveData(in);
        setCharacter(in.getStringOr("McsmCharacter", "jesse"));
    }
}
