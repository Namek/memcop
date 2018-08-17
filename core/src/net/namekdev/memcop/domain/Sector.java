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


    public static Sector newBroken() {
        Sector s = new Sector();
        s.broken = true;
        return s;
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
