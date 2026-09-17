package net.mcsm.extras;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * BUILD #445 -- THE INFINITE FUTURE-BOOK.
 *
 * <p>Off the wishlist: "the infinite future-book". It is built as the thing the
 * two words mean, not as scenery:
 *
 * <ul>
 *   <li><b>FUTURE.</b> Every page is dated, and the dates are real. The book counts
 *       to the same day the sky terminal counts to -- the first of February, 2027 --
 *       so the countdown in the terminal and the dates on the pages are the same
 *       number read two ways. Open it in March and the pages have moved: nothing
 *       here is a fixed string.</li>
 *   <li><b>BOOK.</b> Pages carry chapters, a title, a date line and a span -- the
 *       days that page holds -- and the first page that has come due is the one the
 *       book opens on. Pages that have not come due are read forward, never
 *       backwards.</li>
 *   <li><b>INFINITE.</b> The pages are not a list that has to be finished: the
 *       book either ends or it carries on, and either way it is the reader's own
 *       page that is written into it last. That page is kept, and it is dated.</li>
 * </ul>
 *
 * <p>WHAT IS ON THE PAGES. The book writes about the world's own systems -- the
 * storm's phases, the districts, the mazes under them, the racks in the server
 * rooms, the rituals, the rift, adams, the black sun, the Creator -- with the
 * numbers those systems actually use. A prophecy is only interesting if it can be
 * checked, and every page here can be checked.
 */
public final class McsmFutureBook {

    /** The day the countdown ends. The sky terminal counts to the same one. */
    public static final int END_YEAR = 2027;
    public static final int END_MONTH = 2;
    public static final int END_DAY = 1;

    /** One page of the book. */
    public record Page(String chapter, String title, int day, int span, String[] lines,
                       String omen) {
        /** True when this page is the future the book has reached today. */
        public boolean due(int daysLeft) {
            return day <= daysLeft;
        }
    }

    private McsmFutureBook() {
    }

    /**
     * Days from today to the day the countdown ends. Real clock, on purpose.
     *
     * <p>THERE IS ONE END DATE IN THIS BUILD, and it is the sky terminal's own
     * epoch ({@link McsmMassg#END_EPOCH_MS}). This method reads it and falls back
     * to the plain date only if it cannot: a book that disagreed with the terminal
     * about the one date the whole story hangs on would be worse than no book.
     */
    public static int daysLeft() {
        try {
            LocalDate end = LocalDate.of(END_YEAR, END_MONTH, END_DAY);
            LocalDate terminal = java.time.Instant.ofEpochMilli(McsmMassg.END_EPOCH_MS)
                    .atZone(java.time.ZoneOffset.UTC).toLocalDate();
            if (terminal.getYear() == END_YEAR && terminal.getMonthValue() == END_MONTH
                    && terminal.getDayOfMonth() == END_DAY) {
                end = terminal;
            }
            return (int) ChronoUnit.DAYS.between(LocalDate.now(), end);
        } catch (Throwable t) {
            return 0;
        }
    }

    /** The end day, written out: "1 February 2027". */
    public static String endDate() {
        return END_DAY + " February " + END_YEAR;
    }

    /**
     * The book, today. Every page's date is the countdown minus that page's own
     * offset, so the dates move with the clock and the last page lands on the last
     * day the countdown has.
     */
    public static List<Page> pages() {
        int left = daysLeft();
        List<Page> out = new ArrayList<>();
        int offset = 0;
        for (Page template : TEMPLATE) {
            out.add(new Page(template.chapter(), template.title(), left - offset, template.span(),
                    template.lines(), template.omen()));
            offset += template.span();
        }
        return out;
    }

    /** The page the book opens on: the first one that has come due. */
    public static int currentPage(int daysLeft) {
        List<Page> all = pages();
        int index = 0;
        for (int i = 0; i < all.size(); i++) {
            if (all.get(i).due(daysLeft)) {
                index = i;
            }
        }
        return index;
    }

    /** A one-line forecast, for chat and for the terminal's console page. */
    public static String forecast() {
        int left = daysLeft();
        Page page = pages().get(currentPage(left));
        return "the book is open at \"" + page.title() + "\" -- D-" + page.day() + " to "
                + endDate();
    }

    /** How many pages there are, including the reader's own last one. */
    public static int count() {
        return TEMPLATE.size() + 1;
    }

    private static Page p(String chapter, String title, int span, String omen, String... lines) {
        return new Page(chapter, title, 0, span, lines, omen);
    }

    /**
     * THE PAGES.
     *
     * Each carries the offset it holds, as its span. The bodies name the real
     * constants of the systems they describe, so a reader can go and check.
     */
    private static final List<Page> TEMPLATE = List.of(
        p("I", "THE BOOK OPENS", 4, "read forward, never backwards",
            "This is the book of what has not happened yet.",
            "It is dated. It is not a guess.",
            "",
            "Every page below is a thing the world already",
            "carries the pieces of. The storm's phases are",
            "written down. The districts are standing. The",
            "racks are blinking under the ground.",
            "",
            "The book does not say when. The book says the",
            "order, and the order is the part that matters."),

        p("II", "THE ANTENNA", 4, "it was in your hands the whole time",
            "It came to you the day you arrived, and it",
            "looked like a tool.",
            "",
            "Click it and the air fills with signals: sixteen",
            "of them, some older than the storm. One of them",
            "opens a door that does not lead outside.",
            "",
            "The door wants six letters. They are written",
            "down in the world, in a hospital that was",
            "abandoned before the storm and after the war,",
            "and the letters are IVOR."),

        p("III", "THE DISTRICTS", 4, "dead, and every window full of light",
            "The cities came back first. Nobody moved in.",
            "",
            "They stand in the regular world, three hundred",
            "and eighty-four blocks from where you woke, and",
            "again in the decayed reality, and they are full:",
            "banners, market streets, kitchens with food on",
            "the tables. Every one of them has a keycard",
            "somewhere in the rubble."),

        p("IV", "UNDER THE DISTRICTS", 5, "the far corner is the reason",
            "The warehouses are below the cities.",
            "",
            "A depth-first walk cut them: twelve cells by",
            "twelve, one route between any two, and the only",
            "way in is a plated hatch on the surface.",
            "",
            "The aisles carry crates. The dead ends carry",
            "barrels. One dead end in four carries a vault,",
            "and the vault is worth walking to."),

        p("V", "THE RACKS", 5, "a machine that still has power",
            "Three halls of rusted plate, joined by corridors,",
            "under a hatch of their own.",
            "",
            "The lamps do not sit still. They change a little",
            "every second, and every three minutes the whole",
            "room drops to emergency lighting for four and a",
            "half seconds so that you can hear it breathing.",
            "",
            "The mainframe on the dais reads out its own log",
            "to whoever stands in front of it. It has been",
            "waiting a long time to have somebody to tell."),

        p("VI", "THE RIFT", 5, "the world behind the world",
            "The decayed reality is a place, not a teleport.",
            "Take a rift key, crouch, and you are there.",
            "",
            "The stone in it is older than the stone here.",
            "The bone is older than the stone.",
            "",
            "Come back, and the sky is exactly where you left",
            "it, which is the part nobody believes until",
            "they have stood in the rift at night."),

        p("VII", "THE INFINITE DIMENSION", 5, "it does not end, and it is not empty",
            "Adams wrote it down before he went, and the",
            "world kept the copy.",
            "",
            "It is a grid of walkways with pillars between",
            "them, lit by a spine of lamps, chamber after",
            "chamber, and it does not run out. Walk in one",
            "direction for a day and there are lamps ahead.",
            "",
            "The chambers hold salvage. Some of the chambers",
            "hold worse. Two in three."),

        p("VIII", "THE RITUALS", 6, "ring, offering, condition, answer",
            "Six rites were written for this world. Each one",
            "is a ring of blocks on the ground with something",
            "placed on top, and each one answers if the world",
            "agrees with you.",
            "",
            "THE RIFT opens the way to the decayed reality.",
            "THE WAKING calls up the thing the whole book is",
            "about. THE SWARM, THE CORRUPTION, THE BLACK SUN,",
            "THE ADAMS GATE.",
            "",
            "The offering is spent when the rite fires, which",
            "is how you know it fired."),

        p("IX", "THE SWARM", 4, "it was always a door, never a monster",
            "There is a creature that the world refuses to",
            "put down.",
            "",
            "It is twenty-four blocks one way and forty the",
            "other, it has four thousand and ninety-six lives,",
            "and its eyes are the colour of the things behind",
            "your eyes when you shut them hard.",
            "",
            "It is summoned, never spawned. Once it is here",
            "the world is changed, and no command takes it",
            "back -- the book has checked."),

        p("X", "THE RADIO", 4, "the signal gets further the worse it is",
            "Nine stations, and one of them is not a",
            "station: it is a count. The count has been going",
            "somewhere since before you got here.",
            "",
            "Stand under the right sky on the right day and",
            "the number is ninety-nine.",
            "",
            "It is going to one, and the first of February",
            "2027 is the day it arrives at it."),

        p("XI", "THE COUNT", 6, "D-99 to D-1",
            "Ninety-nine days, and the number does not care",
            "whether anybody is listening.",
            "",
            "The terminal shows it. The antenna hears it. This",
            "book counts with it, which is why the pages have",
            "dates on them and why the dates move.",
            "",
            "The count is not a deadline. It is a door closing",
            "slowly, and the last page of this book is the day",
            "it shuts."),

        p("XII", "THE VORTEXES", 4, "the sky opens, and something comes down",
            "It starts in the fifth phase, when the sky is",
            "still deciding.",
            "",
            "A hole in the cloud with a rim on it, nine blocks",
            "across, standing twenty-six blocks up. What falls",
            "out of it is not weather.",
            "",
            "Six of them at once is the worst the world has",
            "recorded, and the record is short."),

        p("XIII", "THE TORNADOES", 4, "the ground goes up instead of the sky coming down",
            "Gigantic, and they drag.",
            "",
            "They lift blocks out of the world and they keep",
            "them. They stand in the middle of the storm's",
            "worst phases and they are the reason the",
            "roofline of a city is the roofline of a city.",
            "",
            "The storm does not need them. The storm has",
            "them anyway."),

        p("XIV", "THE BLACK SUN", 5, "a hole with an appetite, and it is patient",
            "There is a core under the world with nothing in",
            "it, and beside it an orb that was cut off",
            "something that is still alive.",
            "",
            "Open it and it does not move. It pulls.",
            "",
            "The lights go out at the edge of it first, then",
            "the blocks, then the noise. The book has no page",
            "for what it does after that, so this is the page."),

        p("XV", "THE TENTACLES", 4, "forty-four blocks and it still gets you",
            "The storm reaches. Forty-four blocks of reach,",
            "sixteen of mouth, twenty-four of lift.",
            "",
            "Struggle for seventy ticks and it lets go. Any",
            "less than that and the storm keeps what it took.",
            "",
            "There is a sound it makes when it takes somebody",
            "that the book will not write down."),

        p("XVI", "THE HUGE BACK", 5, "turn around and it is behind you",
            "The storm has a back that is bigger than the",
            "storm, and the back has its own copy of the",
            "storm's own model on it.",
            "",
            "In the phase after the fifth the enlargement",
            "takes the whole thing about its own centre, one",
            "point seven two times, and the copy lands where",
            "the copy should land.",
            "",
            "It took four attempts to make it land there. The",
            "book remembers. The welds are still at zero."),

        p("XVII", "THE CREATOR", 5, "ninety-six by one hundred and fifty",
            "Something made all of this, and it is not",
            "grateful.",
            "",
            "It is ninety-six blocks across, one hundred and",
            "fifty tall, and it has forty thousand lives,",
            "which is more than the swarm and less than the",
            "world.",
            "",
            "It only shows up when the ladder is finished,",
            "and the ladder is finished at the top of the",
            "storm's phases. Everything before it is warm-up."),

        p("XVIII", "THE WHALES", 4, "something enormous, minding its own business",
            "Forty blocks long, twenty-six tall, nine hundred",
            "lives, and it does not want anything from you.",
            "",
            "They move through the decayed reality the way",
            "swifts move through a cathedral.",
            "",
            "The book lists them here so that you know they",
            "are not omens. They are neighbours."),

        p("XIX", "THE PACK", 5, "it was a mod once, and now it is a world",
            "Everything in this book came out of a mod that",
            "was about one storm.",
            "",
            "There are glitch lamps, cracked roads, storm",
            "ribs, withered flesh, city tiles, memory",
            "crystals, abyss orbs, rift anchors, reality glass",
            "and a version number that has not moved once,",
            "because the number is part of the promise.",
            "",
            "Seven thousand point zero point zero dash M.",
            "It has said that since the first day and it will",
            "say it on the last page."),

        p("XX", "THE LAST PAGES", 4, "read forward, then write",
            "Everything that was going to happen in the",
            "ninety-nine days has happened in order.",
            "",
            "The last page is not in this book.",
            "",
            "The last page is the one you write, and the book",
            "will hold it, and date it, and keep it after the",
            "count reaches one."));
}
