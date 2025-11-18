package com.vampir.mixin;

import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.mob.WitherSkeletonEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WitherSkeletonEntity.class)
public abstract class WitherSkeletonEntityMixin extends MobEntity {
    // Use the same constructor pattern your working Zombie mixin uses.
    protected WitherSkeletonEntityMixin() {
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