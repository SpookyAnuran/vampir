package com.vampir.mixin;

import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.mob.BoggedEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture.*;

@Mixin(BoggedEntity.class)
public abstract class BoggedEntityMixin extends MobEntity {
    // Use the same constructor pattern your working Zombie mixin uses.
    protected BoggedEntityMixin() {
        super(null, (World) null);
    }

    // Inject into the constructor return so targetSelector is initialized
    @Inject(method = "<init>", at = @At("RETURN"))
    private void onConstructed(CallbackInfo ci) {
        this.targetSelector.getGoals().removeIf(prioritizedGoal ->
                prioritizedGoal.getGoal() instanceof ActiveTargetGoal
        );
    }
}
