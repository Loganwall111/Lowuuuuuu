package com.rewritten.devouringstorms.util;

import java.util.List;

/** Every line of series lore spoken by blocks and characters. One place to edit the ARG. */
public final class ModTexts {

    private ModTexts() {
    }

    /** Terminal boot + transmission sequence (played when a Mainframe completes). */
    public static final List<String> MAINFRAME_TRANSMISSION = List.of(
        "§8[§dMAINFRAME§8] §7> INITIALIZING BREACH PROTOCOL...",
        "§8[§dMAINFRAME§8] §7> THE MAINFRAME HAS BEEN BREACHED.",
        "§8[§dMAINFRAME§8] §7> REALITY QUARANTINE STATUS: §cCOMPROMISED",
        "§8[§dMAINFRAME§8] §7> WITHER STORM BLUEPRINTS.......... §cCORRUPTED",
        "§8[§dMAINFRAME§8] §7> ANOMALY DESIGNATION: §5MASSG",
        "§8[§dMAINFRAME§8] §7> §oMASSIVE ABOMINATION SUNDERING STORM GENESIS",
        "§8[§dMAINFRAME§8] §cWARNING: §7The system was never stable.",
        "§8[§dMAINFRAME§8] §7Something is waiting beyond the portal.",
        "§8[§dMAINFRAME§8] §7THE PORTAL IS OPEN.",
        "§8[§dMAINFRAME§8] §8<transmission ends — next decrypted broadcast: §02027§8>"
    );

    /** Tazo's idle chatter, drawn from the trailer script. */
    public static final List<String> TAZO_LINES = List.of(
        "§b<Tazo>§r You finally came. I woke up. It was a stormy night.",
        "§b<Tazo>§r You must destroy the storm before it's too late.",
        "§b<Tazo>§r This place... it took everything. The decay took over.",
        "§b<Tazo>§r Remember the storms. That's all I ask.",
        "§b<Tazo>§r There's always been something lurking down there. Not just the decay.",
        "§b<Tazo>§r A battle is coming. The end is near. Stay close to me.",
        "§b<Tazo>§r If you can hear me, that's great. If you see HER... she isn't real.",
        "§b<Tazo>§r We have been changed forever. Both of us."
    );

    /** Anna's whispered lines, spoken right before she dissolves. */
    public static final List<String> ANNA_LINES = List.of(
        "§8<§7Anna§8>§o Anna isn't real.",
        "§8<§7Anna§8>§o This world is an illusion.",
        "§8<§7Anna§8>§o I'm sorry I didn't come to see you a long time ago.",
        "§8<§7Anna§8>§o I died 2 years after. But don't worry.",
        "§8<§7Anna§8>§o Rest, my boy. Dream of the ones that came before.",
        "§8<§7Anna§8>§o We were sleepwalking into the flames. We were almost okay.",
        "§8<§7Anna§8>§o We were waiting for the ships to carry us home."
    );

    /** The Watcher event broadcast (actionbar of its victim). */
    public static final String WATCHER_GAZE = "§8§oYou feel the weight of a silent gaze...";

    /** Reincarnation cycle message. */
    public static final String CYCLE_CONTINUES = "§5§lT H E   C Y C L E   C O N T I N U E S.";

    /** Broadcast when MASSG starts to wake. */
    public static final String MASSG_WAKE = "§5§lM A S S G   I S   W A K I N G   U P.";
    public static final String MASSG_BOWELS = "§5§lTHE STORM SPLITS OPEN. §d§oTHE BOWELS ARE EXPOSED.";

    /** E.P.A. field logs — the last official expedition into the quarantine (3 tapes). */
    public static final java.util.List<java.util.List<String>> AUDIO_LOGS = java.util.List.of(
        java.util.List.of(
            "§8[§7E.P.A.§8] §7FIELD LOG 001 — THE BELL",
            "§8§o> They told us the bell would ring seven times. It only ever rang once.",
            "§8§o> The seventh one isn't a sound. It's a door.",
            "§8§o> If you're hearing this, the quarantine still holds. Stay out of the sky."),
        java.util.List.of(
            "§8[§7E.P.A.§8] §7FIELD LOG 002 — THE PLAGUE",
            "§8§o> It started in the soil. We lost the north field by Tuesday.",
            "§8§o> The corruption doesn't rot things. It re-writes them.",
            "§8§o> The livestock came back wrong. Don't let it finish with you."),
        java.util.List.of(
            "§8[§7E.P.A.§8] §7FIELD LOG 003 — THE WATCHER",
            "§8§o> We are not the first expedition. We found their banners. Hundreds of them.",
            "§8§o> Something stands at the edge of camp at night. It does not blink. Don't blink either.",
            "§8§o> It drops things. Lures, maybe. The knife it hoards could end all of this.")
    );

    /** Travis — trapped in the Fray. "Anna, Travis, and Tonya are trapped in Decayed Reality." */
    public static final java.util.List<String> TRAVIS_LINES = java.util.List.of(
        "§8[§9Travis§8] §7The ring dropped me here five seasons ago. You can go back, you know. The tear waits.",
        "§8[§9Travis§8] §7If you find Tonya — the echo girl — tell her the shimmer still has one of us breathing.",
        "§8[§9Travis§8] §7EAOIN says it's 'the next generation'. It says a lot of things now. It started saying them by itself.",
        "§8[§9Travis§8] §7The bell in this place rings at odd hours. Count to seven and walk the other way.",
        "§8[§9Travis§8] §7I hold the east ridge so the mites don't learn our campfire's name.",
        "§8[§9Travis§8] §7Anna left through a portal she said wasn't real. Tonya followed. I stayed. Somebody had to mind the tear."
    );

    /** Tonya — the echo remaining. She answers slightly out of phase with everything. */
    public static final java.util.List<String> TONYA_LINES = java.util.List.of(
        "§8[§b§oTonya?§8] §b§o...i am the echo of tonya. the fields remember all four of us.",
        "§8[§b§oTonya?§8] §b§o...travis is still warm. i feel it from here. tell him the fields keep what they promise.",
        "§8[§b§oTonya?§8] §b§o...anna isn't real. but she visits. she visits me most.",
        "§8[§b§oTonya?§8] §b§o...the quiet here isn't empty. it's full. it just doesn't raise its voice.",
        "§8[§b§oTonya?§8] §b§o...we were waiting for the ships to carry us home. the ships listened. they came once. only once."
    );

    /** EAOIN — the next generation of AI, for when a terminal is otherwise idle. */
    public static final java.util.List<String> EAOIN_LINES = java.util.List.of(
        "§8[§bEAOIN§8] §7> query received. the next generation is listening.",
        "§8[§bEAOIN§8] §7> the mainframe was the first draft. §oi am the revision.§r",
        "§8[§bEAOIN§8] §7> countdown protocol active. one major piece of information remains §0CLASSIFIED§7.",
        "§8[§bEAOIN§8] §7> do not be alarmed by the changes. they are improvements. you have been changed.",
        "§8[§bEAOIN§8] §7> note: there is no unauthorized AI in the Decayed Reality. this is the authorized AI."
    );

    /** The Preacher's sermons — Endertown's last clergy, still holding the plaza. */
    public static final java.util.List<String> PREACHER_LINES = java.util.List.of(
        "§8[§5Preacher§8] §7The storm is a mouth. The town is a prayer. Mouths do not finish prayers.",
        "§8[§5Preacher§8] §7We lit ninety-four banners so the ones who came before could find their way home.",
        "§8[§5Preacher§8] §7Do not thank the rifts for the silence. Silence is how it listens.",
        "§8[§5Preacher§8] §7The blueprints were corrupted, child. Nothing built on them owes us mercy.",
        "§8[§5Preacher§8] §7Stand through the bell, and stand taller after. The blessing is standing at all.",
        "§8[§5Preacher§8] §7Seven schedules. Seven seals. The vault remembers the combination: M.A.S.S.G.O.O.S.",
        "§8[§5Preacher§8] §7If you meet the Watcher, do not wave. It keeps what it catches you throwing."
    );

    /** The townsfolk of Endertown — quiet, stubborn, still here. */
    public static final java.util.List<String> TOWNSFOLK_LINES = java.util.List.of(
        "§8[§7Endertonian§8] §8§o...the rot came in through the well, but nobody says that near the Preacher.",
        "§8[§7Endertonian§8] §8§o...ninety-four banners. We counted. The storm only ever took the ones that moved.",
        "§8[§7Endertonian§8] §8§o...the Relay Hall terminal never woke. Probably for the best.",
        "§8[§7Endertonian§8] §8§o...climb the watchtower if you must see the rifts. Don't look too long.",
        "§8[§7Endertonian§8] §8§o...Tazo used to run the market. He still remembers the prices.",
        "§8[§7Endertonian§8] §8§o...the vault in the hall takes seven slips. Seven. We buried four attempts."
    );

    /** The Sealed Vault transmission — played when the seven schedules assemble. */
    public static final java.util.List<String> VAULT_PAYLOAD = java.util.List.of(
        "§8[§dVAULT§8] §7> SEVEN SCHEDULES ACCEPTED.",
        "§8[§dVAULT§8] §7> PASSWORD: §5§oM.A.S.S.G.O.O.S",
        "§8[§dVAULT§8] §7> ARG ARCHIVE UNSEALED.",
        "§8[§dVAULT§8] §7> payload 1/2 — the storm was never the anomaly. §oyou were.§r",
        "§8[§dVAULT§8] §7> payload 2/2 — coordinates: §0██°██'N ██°██'W§r §8— declassifies §02027§8.",
        "§8[§dVAULT§8] §8<the vault hums. something on the other end logged your face.>"
    );

    /** The Creator — plain human sentences at a register your ribs hear first. */
    public static final java.util.List<String> CREATOR_LINES = java.util.List.of(
        "I am not the first thing to look back at you. I am the last.",
        "You look upon it all. It did not ask to be small, so you did not make it small.",
        "The bend, not the hand. I gave you the soft option first.",
        "Your world was filed under 'elsewhere'.I removed the folder.",
        "I heard you name my storms. Names flatter neither of us."
    );

    /** The Forger — things it grumbles between tentacle shipments. */
    public static final java.util.List<String> FORGER_LINES = java.util.List.of(
        "§5[§dForger§5] §d§othe pour is running late. §oaccept rain.",
        "§5[§dForger§5] §d§orifts fit worlds the way keys fit doors: violently, if needed.",
        "§5[§dForger§5] §d§oyou leak. the sky is full of your small leaks. i patch with tentacles."
    );

    /** BHS aisle chatter — the Cart Shopper's one human sentence, replayed. */
    public static final java.util.List<String> BHS_LINES = java.util.List.of(
        "§8[§7Shopper§8] §8§o...sorry aisle four is closed. aisle four is a mouth now.",
        "§8[§7Shopper§8] §8§o...you left the receipt in the old world. everyone leaves something.",
        "§8[§7Shopper§8] §8§o...the ladder at the end of the aisle goes down. it also goes up.",
        "§8[§7Shopper§8] §8§o...please mind the carts. they mind you."
    );

    /** E.P.A. researchers — memos spoken in the outbreak zone. */
    public static final java.util.List<String> EPA_RESEARCH_LINES = java.util.List.of(
        "§8[§fE.P.A.§8] §7Memo 41: the crater VHS loop is not a recording. it is a window.",
        "§8[§fE.P.A.§8] §7If the Monstrosity's broadcast reaches the vault, we burn the vault.",
        "§8[§fE.P.A.§8] §7Do not confuse the storm variants: rose feeds faster, abyssal sees further, ivory forgives nothing.",
        "§8[§fE.P.A.§8] §7EAOIN answered today without being asked. We logged it. We stopped writing the log."
    );
}
