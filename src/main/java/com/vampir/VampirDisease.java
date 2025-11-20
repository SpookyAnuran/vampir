package com.vampir;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.util.Identifier;

// Simple marker-style effect. No periodic ticks by default.
public final class VampirDisease extends StatusEffect {
    public VampirDisease() {
        super(StatusEffectCategory.HARMFUL, 0x330000);
    }

    Identifier id = Identifier.of("vampir", "textures/mob_effect/vampir_disease.png");

    public static void register() {
    }

    @Override
    public boolean applyUpdateEffect(LivingEntity entity, int amplifier) {
        // no periodic behavior
        return false;
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        return false;
    }
}