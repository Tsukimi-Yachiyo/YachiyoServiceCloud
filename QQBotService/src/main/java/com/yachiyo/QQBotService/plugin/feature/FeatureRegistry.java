package com.yachiyo.QQBotService.plugin.feature;

import java.util.*;

public class FeatureRegistry {
    private static final Map<FeatureCategory, Set<Feature>> CONTAINERS = new HashMap<>();

    public static void register(FeatureCategory category, Feature feature) {
        CONTAINERS.computeIfAbsent(category, _ -> new HashSet<>()).add(feature);
    }

    public static void register(Feature feature) {
        register(FeatureCategory.MISC, feature);
    }

    public static Set<Feature> getFeatures(FeatureCategory category) {
        return CONTAINERS.getOrDefault(category, Collections.emptySet());
    }

    public static Set<Feature> getFeatures() {
        Set<Feature> all = new HashSet<>();
        for (var features : CONTAINERS.values()) {
            all.addAll(features);
        }
        return all;
    }
}
