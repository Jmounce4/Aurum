package BHML.aurum.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

import java.util.ArrayList;
import java.util.List;

public class TextUtils {

    /**
     * Splits a long text into multiple lore lines of up to maxLineLength characters.
     */
    public static List<Component> wrapLore(String text, int maxLineLength, NamedTextColor color) {
        List<Component> lines = new ArrayList<>();
        String[] words = text.split(" ");
        StringBuilder currentLine = new StringBuilder();

        for (String word : words) {
            if (currentLine.length() + word.length() + 1 > maxLineLength) {
                lines.add(Component.text(currentLine.toString(), color).decoration(TextDecoration.ITALIC, false));
                currentLine = new StringBuilder(word);
            } else {
                if (currentLine.length() > 0) currentLine.append(" ");
                currentLine.append(word);
            }
        }

        if (currentLine.length() > 0) {
            lines.add(Component.text(currentLine.toString(), color).decoration(TextDecoration.ITALIC, false));
        }

        return lines;
    }


}
