package com.vampir;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.registry.Registry;


public final class ModEffects {
    public static StatusEffect VAMPIR_DISEASE;

    private ModEffects() {}

    public static void register() {
        VAMPIR_DISEASE = Registry.register(Registries.STATUS_EFFECT,
                Identifier.of("vampir", "vampir_disease"),
                new VampirDisease()); // default constructor in VampirDiseaseEffect
    }
}
