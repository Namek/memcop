package net.namekdev.memcop.domain;

/**
 * in-game memory sector
 */
public class Sector {
    /**
     * Have algorithm tried to write to it?
     */
    public boolean written = false;

    /**
     * Is this sector a bad sector?
     */
    public boolean broken = false;


    public final boolean originallyBroken;


    public Sector() {
        this(false);
    }

    public Sector(boolean originallyBroken) {
        this.originallyBroken = originallyBroken;
        this.broken = originallyBroken;
    }

    public void reset() {
        written = false;
        broken = originallyBroken;
    }

    public void write(byte value) {
        // let's ditch the value for now
        written = true;
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
