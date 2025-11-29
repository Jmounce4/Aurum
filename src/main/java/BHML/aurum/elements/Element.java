package BHML.aurum.elements;


import org.bukkit.ChatColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.Component;


public enum Element {
    AIR(TextColor.color(0, 255, 185)),        // cyan-ish
    FIRE(TextColor.color(205, 50, 0)),         // red
    LIGHTNING(TextColor.color(255, 255, 0)),  // yellow
    WATER(TextColor.color(0, 120, 255)),        // blue
    EARTH(TextColor.color(80, 118, 0)),        // green
    NORMAL(TextColor.color(205, 205, 205)),   // white
    ENDER(TextColor.color(128, 0, 128));      // purple

    private final TextColor color;

    Element(TextColor color) {
        this.color = color;
    }

    public TextColor getColor() {
        return color;
    }

    public Component coloredName(String name) {
        return Component.text(name).color(color);
    }

}
