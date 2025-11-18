package com.vampir.mixin;

import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.mob.MobEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ZombieEntity.class)
public abstract class ZombieEntityMixin extends MobEntity {
    protected ZombieEntityMixin() {
        super(null, null); // required constructor
    }

    @Inject(method = "initGoals", at = @At("TAIL"))
    private void removeDefaultTargeting(CallbackInfo ci) {
        this.targetSelector.getGoals().removeIf(prioritizedGoal ->
                prioritizedGoal.getGoal() instanceof ActiveTargetGoal
        );
    }
}

