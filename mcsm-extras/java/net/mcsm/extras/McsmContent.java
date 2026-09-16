package net.mcsm.extras;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockSetType;

/**
 * Devouring Storms: the Decayed Reality content pack (mandate D.8, phase 1).
 *
 * The user's brief: "there's only like five or 15 blocks in the game ... have
 * new trap doors, doors, items, even some weapons would be a really cool
 * expansion". This is that expansion: the building set for the torn world --
 * decayed terrain, the ruins of the abandoned cities, the reality-tear
 * materials, the storm's own anatomy as blocks -- plus the tools and weapons
 * the storyline hands out.
 *
 * HOW THIS COMPILES AGAINST A FROZEN JAR
 * --------------------------------------
 * mcsm-extras only ever sees the released base jar (ci/api/mod.txt is its javap
 * dump, and check_phase_uniform.py fails the build on any symbol that dump does
 * not declare). Every registration idiom below is therefore copied from the
 * base mod's own compiled ModBlocks/ModItems/ModItemGroups -- same
 * Registry.register on BuiltInRegistries, same Properties.setId(key) call, same
 * FabricCreativeModeTab builder -- so it is proven to exist in this exact
 * Minecraft version. Anything that could not be proven is done by reflection or
 * left to a require = 0 mixin; nothing here is a guess at a signature.
 *
 * Registration runs from the mod's own onInitialize (see McsmBuiltinPackMixin),
 * i.e. while the built-in registries are still open, next to the base mod's own
 * ModBlocks/ModItems/ModItemGroups pass.
 *
 * Namespace: {@code mcsm}. Everything registered here is also listed in the
 * mcsm content tab and has assets under jar-overrides/assets/mcsm.
 */
public final class McsmContent {

    /** Every block we add, in registration order (used by the panel + the gates). */
    public static final List<Block> ALL_BLOCKS = new ArrayList<>();
    /** Every item we add, in registration order. */
    public static final List<Item> ALL_ITEMS = new ArrayList<>();
    /** The block items, in registration order (the tab lists these first).
     *  Kept as a list rather than looked up afterwards: a registry lookup would
     *  be one more frozen-jar method this class cannot compile-prove. */
    public static final List<Item> ALL_BLOCK_ITEMS = new ArrayList<>();

    // ---------------------------------------------------------------------
    // Building set: the decayed world
    // ---------------------------------------------------------------------
    public static final Block DECAYED_STONE = block("decayed_stone",
            stone(2.0F, 6.0F));
    public static final Block DECAYED_COBBLESTONE = block("decayed_cobblestone",
            stone(2.0F, 6.0F));
    public static final Block DECAYED_STONE_BRICKS = block("decayed_stone_bricks",
            stone(2.5F, 6.0F));
    public static final Block DECAYED_DIRT = block("decayed_dirt",
            BlockBehaviour.Properties.of().strength(0.6F).sound(SoundType.GRASS));
    public static final Block DECAYED_SURFACE = block("decayed_surface",
            BlockBehaviour.Properties.of().strength(0.7F).sound(SoundType.GRASS));
    public static final Block DECAYED_SAND = block("decayed_sand",
            BlockBehaviour.Properties.of().strength(0.5F).sound(SoundType.SAND));
    public static final Block DECAYED_LOG = block("decayed_log",
            RotatedPillarBlock::new, wood(3.4F, 5.0F));
    public static final Block DECAYED_STRIPPED_LOG = block("decayed_stripped_log",
            RotatedPillarBlock::new, wood(2.0F, 3.0F));
    public static final Block DECAYED_PLANKS = block("decayed_planks", wood(2.0F, 3.0F));
    public static final Block DECAYED_LEAVES = block("decayed_leaves",
            BlockBehaviour.Properties.of().strength(0.3F).sound(SoundType.GRASS).noOcclusion());

    // ---------------------------------------------------------------------
    // Building set: the abandoned cities
    // ---------------------------------------------------------------------
    public static final Block CITY_BRICKS = block("city_bricks", stone(3.0F, 8.0F));
    public static final Block CITY_TILES = block("city_tiles", stone(3.0F, 8.0F));
    public static final Block RUSTED_PLATE = block("rusted_plate",
            BlockBehaviour.Properties.of().strength(3.5F, 10.0F).sound(SoundType.METAL)
                    .requiresCorrectToolForDrops());
    public static final Block REBAR_GRATE = block("rebar_grate",
            BlockBehaviour.Properties.of().strength(3.0F, 9.0F).sound(SoundType.METAL)
                    .noOcclusion().requiresCorrectToolForDrops());
    public static final Block CRACKED_ROAD = block("cracked_road", stone(2.0F, 7.0F));
    public static final Block HOLLOW_WALL = block("hollow_wall", stone(2.0F, 6.0F));
    public static final Block STORM_RIB = block("storm_rib",
            BlockBehaviour.Properties.of().strength(0.8F).sound(SoundType.BONE_BLOCK));
    public static final Block TENDON_BLOCK = block("tendon_block",
            BlockBehaviour.Properties.of().strength(0.9F).sound(SoundType.SLIME_BLOCK)
                    .friction(0.72F));
    public static final Block WITHERED_FLESH_BLOCK = block("withered_flesh_block",
            BlockBehaviour.Properties.of().strength(0.8F).sound(SoundType.SLIME_BLOCK)
                    .friction(0.72F));

    // ---------------------------------------------------------------------
    // Building set: reality-tear materials (the new tech tier)
    // ---------------------------------------------------------------------
    public static final Block REALITY_GLASS = block("reality_glass",
            BlockBehaviour.Properties.of().strength(0.8F).sound(SoundType.GLASS)
                    .noOcclusion().lightLevel(s -> 6).emissiveRendering(s -> true));
    public static final Block GLITCH_LAMP = block("glitch_lamp",
            BlockBehaviour.Properties.of().strength(1.2F).sound(SoundType.GLASS)
                    .lightLevel(s -> 15).emissiveRendering(s -> true));
    public static final Block MEMORY_CRYSTAL = block("memory_crystal",
            BlockBehaviour.Properties.of().strength(1.5F, 6.0F).sound(SoundType.GLASS)
                    .lightLevel(s -> 9).emissiveRendering(s -> true));
    public static final Block VOID_CORE = block("void_core",
            BlockBehaviour.Properties.of().strength(6.0F, 40.0F).sound(SoundType.METAL)
                    .lightLevel(s -> 12).emissiveRendering(s -> true)
                    .requiresCorrectToolForDrops());
    public static final Block BLACK_HOLE_CORE = block("black_hole_core",
            BlockBehaviour.Properties.of().strength(9.0F, 1200.0F).sound(SoundType.METAL)
                    .lightLevel(s -> 14).emissiveRendering(s -> true)
                    .requiresCorrectToolForDrops());
    public static final Block RIFT_ANCHOR = block("rift_anchor",
            BlockBehaviour.Properties.of().strength(4.0F, 30.0F).sound(SoundType.METAL)
                    .lightLevel(s -> 11).emissiveRendering(s -> true)
                    .requiresCorrectToolForDrops());

    // ---------------------------------------------------------------------
    // Shapes (slab / stairs / wall / fence share the decayed palette)
    // ---------------------------------------------------------------------
    public static final Block DECAYED_SLAB = block("decayed_slab",
            props -> new SlabBlock(props), stone(2.0F, 6.0F));
    public static final Block DECAYED_STAIRS = block("decayed_stairs",
            props -> new ExposedStair(DECAYED_STONE.defaultBlockState(), props),
            stone(2.0F, 6.0F));
    public static final Block DECAYED_WALL = block("decayed_wall",
            props -> new WallBlock(props), stone(2.0F, 6.0F));
    public static final Block DECAYED_FENCE = block("decayed_fence",
            props -> new FenceBlock(props), wood(2.0F, 3.0F));
    public static final Block CITY_BRICK_SLAB = block("city_brick_slab",
            props -> new SlabBlock(props), stone(3.0F, 8.0F));
    public static final Block CITY_BRICK_STAIRS = block("city_brick_stairs",
            props -> new ExposedStair(CITY_BRICKS.defaultBlockState(), props),
            stone(3.0F, 8.0F));

    // ---------------------------------------------------------------------
    // Doors and trap doors (the user asked for them by name)
    // ---------------------------------------------------------------------
    public static final Block WITHERED_DOOR = block("withered_door",
            props -> new ExposedDoor(BlockSetType.IRON, props),
            BlockBehaviour.Properties.of().strength(3.0F, 9.0F).sound(SoundType.METAL)
                    .requiresCorrectToolForDrops().noOcclusion());
    public static final Block WITHERED_TRAPDOOR = block("withered_trapdoor",
            props -> new ExposedTrapDoor(BlockSetType.IRON, props),
            BlockBehaviour.Properties.of().strength(3.0F, 9.0F).sound(SoundType.METAL)
                    .requiresCorrectToolForDrops().noOcclusion());
    public static final Block RUSTED_DOOR = block("rusted_door",
            props -> new ExposedDoor(BlockSetType.IRON, props),
            BlockBehaviour.Properties.of().strength(4.0F, 12.0F).sound(SoundType.METAL)
                    .requiresCorrectToolForDrops().noOcclusion());
    public static final Block RUSTED_TRAPDOOR = block("rusted_trapdoor",
            props -> new ExposedTrapDoor(BlockSetType.IRON, props),
            BlockBehaviour.Properties.of().strength(4.0F, 12.0F).sound(SoundType.METAL)
                    .requiresCorrectToolForDrops().noOcclusion());

    // ---------------------------------------------------------------------
    // City crates (phase 2): what the abandoned cities are worth looting.
    // Each one drops through a vanilla block loot table
    // (data/mcsm/loot_table/blocks/<name>.json), so no Java is involved in
    // making them rewarding -- and ci/check_datapack_schema.py checks those
    // tables against vanilla's own examples from client.jar before shipping.
    // ---------------------------------------------------------------------
    public static final Block CITY_CRATE = block("city_crate",
            BlockBehaviour.Properties.of().strength(1.6F).sound(SoundType.WOOD));
    public static final Block SUPPLY_CRATE = block("supply_crate",
            BlockBehaviour.Properties.of().strength(2.0F, 4.0F).sound(SoundType.WOOD));
    public static final Block VAULT_CRATE = block("vault_crate",
            BlockBehaviour.Properties.of().strength(4.0F, 20.0F).sound(SoundType.METAL)
                    .requiresCorrectToolForDrops());

    // ---------------------------------------------------------------------
    // Items: materials
    // ---------------------------------------------------------------------
    public static final Item RIFT_SHARD = item("rift_shard", item(p -> p.rarity(Rarity.UNCOMMON)));
    public static final Item VOID_THREAD = item("void_thread", item(p -> p));
    public static final Item DECAYED_BONE = item("decayed_bone", item(p -> p));
    public static final Item DECAYED_STEEL_INGOT = item("decayed_steel_ingot", item(p -> p));
    public static final Item STORM_HEART_SHARD = item("storm_heart_shard",
            item(p -> p.rarity(Rarity.RARE)));
    public static final Item GLITCH_ECHO = item("glitch_echo", item(p -> p.rarity(Rarity.RARE)));
    public static final Item MEMORY_FRAGMENT = item("memory_fragment",
            glinted(item(p -> p.rarity(Rarity.RARE))));
    public static final Item HALLUCINATION_DUST = item("hallucination_dust", item(p -> p));
    public static final Item CITY_KEYCARD = item("city_keycard", item(p -> p.rarity(Rarity.UNCOMMON)));
    public static final Item GLYPH_CELL = item("glyph_cell", glinted(item(p -> p.rarity(Rarity.RARE))));
    public static final Item CREATOR_FRAGMENT = item("creator_fragment",
            glinted(item(p -> p.rarity(Rarity.EPIC))));
    public static final Item ABYSS_ORB = item("abyss_orb", glinted(item(p -> p.rarity(Rarity.EPIC))));
    public static final Item TENTACLE_HOOK = item("tentacle_hook", item(p -> p.rarity(Rarity.UNCOMMON)));

    // ---------------------------------------------------------------------
    // Items: the tools and weapons the storyline hands out
    // ---------------------------------------------------------------------
    public static final Item RIFT_KEY = item("rift_key", glinted(item(p -> p.stacksTo(1).rarity(Rarity.EPIC))));
    public static final Item REALITY_RIPPER = item("reality_ripper",
            glinted(item(p -> p.stacksTo(1).rarity(Rarity.EPIC).fireResistant())));
    public static final Item WITHERED_BLADE = item("withered_blade",
            item(p -> p.stacksTo(1).rarity(Rarity.RARE)));
    public static final Item STORM_SPEAR = item("storm_spear",
            item(p -> p.stacksTo(1).rarity(Rarity.RARE)));
    public static final Item CREATORS_JUDGEMENT = item("creators_judgement",
            glinted(item(p -> p.stacksTo(1).rarity(Rarity.EPIC).fireResistant())));
    public static final Item ECHO_TOTEM = item("echo_totem", item(p -> p.stacksTo(1).rarity(Rarity.RARE)));

    private McsmContent() {
    }

    // ---------------------------------------------------------------------
    // Stairs / doors / trap doors need a subclass
    // ---------------------------------------------------------------------
    // The vanilla constructors are PROTECTED in this Minecraft version (proven
    // by the compiler in CI run 487: "StairBlock(BlockState,Properties) has
    // protected access", and the same for DoorBlock/TrapDoorBlock with a
    // BlockSetType). A protected constructor is callable from a subclass in any
    // package, which is exactly how vanilla itself exposes its own stairs,
    // doors and trap doors -- so these three thin subclasses are the supported
    // way in, not a workaround.
    public static class ExposedStair extends StairBlock {
        public ExposedStair(BlockState base, BlockBehaviour.Properties props) {
            super(base, props);
        }
    }

    public static class ExposedDoor extends DoorBlock {
        public ExposedDoor(BlockSetType type, BlockBehaviour.Properties props) {
            super(type, props);
        }
    }

    public static class ExposedTrapDoor extends TrapDoorBlock {
        public ExposedTrapDoor(BlockSetType type, BlockBehaviour.Properties props) {
            super(type, props);
        }
    }

    // ---------------------------------------------------------------------
    // Registration
    // ---------------------------------------------------------------------

    /** Class-init trigger; the static fields above are the registration. */
    public static void register() {
        // no-op: touching the class initialises it
    }

    /**
     * Our own creative tab, on the VANILLA builder.
     *
     * The first attempt copied the base mod's own idiom and named
     * net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab -- which is
     * not on this overlay's compile classpath at all (CI run 487: "package ...
     * does not exist"; build.sh probes the compile path every run now).
     *
     * The vanilla API is, but only up to a point, which the compiler said
     * precisely (run 489): "Output has protected access in CreativeModeTab".
     * CreativeModeTab.builder(Row, int), Builder.title/icon/build are public --
     * the classes are even in the run's API dump -- while the generator type and
     * its Output parameter are protected nested types, so the lambda cannot be
     * written here. The generator is therefore a Proxy that implements the
     * interface without naming it, and the output it is handed is filled
     * reflectively. Same result, no Fabric module, and no compile-time
     * dependency on a protected type.
     */
    public static void registerTab() {
        try {
            CreativeModeTab.Builder builder = CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0)
                    .title(Component.translatable("itemGroup.mcsm.content"))
                    .icon(() -> new ItemStack(RIFT_KEY));
            Class<?> generatorType = Class.forName(
                    "net.minecraft.world.item.CreativeModeTab$DisplayItemsGenerator");
            Object generator = java.lang.reflect.Proxy.newProxyInstance(
                    McsmContent.class.getClassLoader(),
                    new Class<?>[]{generatorType},
                    (proxy, method, args) -> {
                        switch (method.getName()) {
                            case "hashCode":
                                return 0;
                            case "equals":
                                return proxy == (args == null ? null : args[0]);
                            case "toString":
                                return "mcsm-content-tab-generator";
                            default:
                                break;
                        }
                        if (args != null && args.length == 2 && args[1] != null) {
                            feedTab(args[1]);
                        }
                        return null;
                    });
            for (java.lang.reflect.Method m : CreativeModeTab.Builder.class.getMethods()) {
                if (m.getName().equals("displayItems") && m.getParameterCount() == 1) {
                    m.invoke(builder, generator);
                    break;
                }
            }
            Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, MAIN_TAB, builder.build());
            System.out.println("[ds] mcsm content tab registered (" + ALL_BLOCKS.size()
                    + " blocks, " + (ALL_ITEMS.size() + ALL_BLOCK_ITEMS.size()) + " items)");
        } catch (Throwable t) {
            System.err.println("[ds] mcsm content tab unavailable (" + t
                    + ") -- every block and item is still craftable / minable in the decayed reality");
        }
    }

    /**
     * Feeds our stacks to the tab's output object. The one-arg accept overload is
     * chosen by what it takes: entries.accept(ItemStack) for a stack parameter,
     * accept(ItemLike) for an item parameter (an ItemStack is not an ItemLike).
     */
    private static void feedTab(Object output) {
        try {
            java.lang.reflect.Method accept = null;
            boolean wantsItem = false;
            for (java.lang.reflect.Method m : output.getClass().getMethods()) {
                if (!m.getName().equals("accept") || m.getParameterCount() != 1) {
                    continue;
                }
                Class<?> p = m.getParameterTypes()[0];
                if (p.isAssignableFrom(ItemStack.class)) {
                    accept = m;
                    wantsItem = false;
                    break;
                }
                if (p.isAssignableFrom(Item.class)) {
                    accept = m;
                    wantsItem = true;
                    break;
                }
            }
            if (accept == null) {
                return;
            }
            accept.setAccessible(true);
            for (Item item : ALL_BLOCK_ITEMS) {
                accept.invoke(output, wantsItem ? item : new ItemStack(item));
            }
            for (Item item : ALL_ITEMS) {
                accept.invoke(output, wantsItem ? item : new ItemStack(item));
            }
        } catch (Throwable t) {
            System.err.println("[ds] mcsm content tab fill failed: " + t);
        }
    }

    public static final ResourceKey<CreativeModeTab> MAIN_TAB = ResourceKey.create(
            Registries.CREATIVE_MODE_TAB, Identifier.fromNamespaceAndPath("mcsm", "content"));

    // ---------------------------------------------------------------------
    // Helpers (same shape as the base mod's own register(...) helpers)
    // ---------------------------------------------------------------------

    private static Block block(String name, BlockBehaviour.Properties props) {
        return block(name, Block::new, props);
    }

    private static <T extends Block> T block(String name, Function<BlockBehaviour.Properties, T> factory,
                                             BlockBehaviour.Properties props) {
        ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK,
                Identifier.fromNamespaceAndPath("mcsm", name));
        T b;
        try {
            b = factory.apply(props.setId(key));
            Registry.register(BuiltInRegistries.BLOCK, key, b);
        } catch (Throwable t) {
            System.err.println("[ds] block mcsm:" + name + " not registered: " + t);
            return null;
        }
        if (b == null) {
            return null;
        }
        ALL_BLOCKS.add(b);
        // every block gets its block item, so it is placeable and in the tab
        try {
            ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM,
                    Identifier.fromNamespaceAndPath("mcsm", name));
            Item it = new BlockItem(b, new Properties().setId(itemKey));
            Registry.register(BuiltInRegistries.ITEM, itemKey, it);
            ALL_BLOCK_ITEMS.add(it);
        } catch (Throwable t) {
            System.err.println("[ds] block item mcsm:" + name + " not registered: " + t);
        }
        return b;
    }

    private static <T extends Item> T item(String name, Function<Properties, T> factory) {
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM,
                Identifier.fromNamespaceAndPath("mcsm", name));
        T it;
        try {
            it = factory.apply(new Properties().setId(key));
            Registry.register(BuiltInRegistries.ITEM, key, it);
        } catch (Throwable t) {
            System.err.println("[ds] item mcsm:" + name + " not registered: " + t);
            return null;
        }
        ALL_ITEMS.add(it);
        return it;
    }

    private static Function<Properties, Item> item(Function<Properties, Properties> tune) {
        return props -> new Item(tune.apply(props));
    }

    /** The base mod's own glint idiom (ModItems uses ENCHANTMENT_GLINT_OVERRIDE). */
    private static Function<Properties, Item> glinted(Function<Properties, Item> inner) {
        return props -> inner.apply(props.component(
                net.minecraft.core.component.DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true));
    }

    private static BlockBehaviour.Properties stone(float hardness, float resistance) {
        return BlockBehaviour.Properties.of().strength(hardness, resistance)
                .sound(SoundType.STONE).requiresCorrectToolForDrops();
    }

    private static BlockBehaviour.Properties wood(float hardness, float resistance) {
        return BlockBehaviour.Properties.of().strength(hardness, resistance)
                .sound(SoundType.WOOD).ignitedByLava();
    }
}
