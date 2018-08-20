package net.namekdev.memcop.domain;

import java.util.Random;

/**
 * in-game memory sector
 */
public class Sector {
    public static Random random = new Random();

    /**
     * Have algorithm tried to write to it?
     */
    public boolean written = false;

    /**
     * Is this sector a bad sector?
     */
    public boolean broken = false;


    public final boolean originallyBroken;

    private int value;


    public Sector() {
        this(false);
        reset();
    }

    public Sector(boolean originallyBroken) {
        reset();
        this.originallyBroken = originallyBroken;
        this.broken = originallyBroken;
    }

    public void reset() {
        written = false;
        broken = originallyBroken;
        value = broken ? random.nextInt()  : 0;
    }

    public boolean isWritable() {
        return !broken;
    }

    public void write(int value) {
        // let's ditch the value for now
        written = true;
        this.value = value;
    }

    public int value() {
        return value;
    }


    public static Sector newBroken() {
        return new Sector(true);
    }

    @Override
    public String toString() {
        if (written)
            return "_";
        if (broken)
            return "x";

        return " ";
    }
}
