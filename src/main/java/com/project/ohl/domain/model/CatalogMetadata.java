package com.project.ohl.domain.model;

import com.project.ohl.domain.exception.InvalidRequestException;
import java.text.Normalizer;
import java.util.Locale;

public final class CatalogMetadata {
    private CatalogMetadata() {}

    public static String mediaKey(String requested, String current, String name) {
        if (requested != null) {
            if (!requested.matches("[a-z0-9_]+")) throw new InvalidRequestException("Invalid media key.");
            return requested;
        }
        if (current != null) return current;
        String canonical = Normalizer.normalize(name, Normalizer.Form.NFC).toLowerCase(Locale.ROOT);
        return switch (canonical) {
            case "ă" -> "a_breve";
            case "â" -> "a_circumflex";
            case "î" -> "i_circumflex";
            case "ș", "ş" -> "s_comma";
            case "ț", "ţ" -> "t_comma";
            default -> Normalizer.normalize(canonical, Normalizer.Form.NFD)
                    .replaceAll("\\p{M}+", "").replaceAll("[^a-z0-9_]+", "_");
        };
    }
}
