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
        String key = Normalizer.normalize(name, Normalizer.Form.NFD).replaceAll("\\p{M}+", "")
                .toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9_]+", "_");
        return key.equals("a") ? "dactilema_a" : key;
    }
}
