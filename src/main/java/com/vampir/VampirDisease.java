package com.vampir;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;

// Simple marker-style effect. Category HARMFUL keeps it consistent with bad effects.
// Override applyUpdateEffect if you want periodic behavior (poison, etc.).
public class VampirDisease extends StatusEffect {
    public VampirDisease() {
        super(StatusEffectCategory.HARMFUL, 0x330000); // color tint (optional)
    }

    public static void register() {
    }

    @Override
    public boolean applyUpdateEffect(LivingEntity entity, int amplifier) {
        // no periodic ticks by default; implement if you want slow drain or subtle effects
        return false;
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        return false; // disable periodic ticking; it's just a persistent marker
    }
}
