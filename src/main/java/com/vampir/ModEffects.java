package com.vampir;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.registry.Registry;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

public final class ModEffects {
    public static StatusEffect VAMPIR_DISEASE;
    public static RegistryEntry.Reference<StatusEffect> VAMPIR_DISEASE_ENTRY;

    private ModEffects() {}

    public static void register() {
        VAMPIR_DISEASE = Registry.register(
                Registries.STATUS_EFFECT,
                Identifier.of("vampir", "vampir_disease"),
                new VampirDisease()
        );

        // store the registry holder/reference your mappings expect
        VAMPIR_DISEASE_ENTRY = Registries.STATUS_EFFECT.getEntry(Identifier.of("vampir", "vampir_disease"))
                .orElseThrow(() -> new IllegalStateException("Failed to obtain registry entry for vampir_disease"));
    }
}