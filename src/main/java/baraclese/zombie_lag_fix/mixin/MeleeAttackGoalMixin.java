package baraclese.zombie_lag_fix.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.PathAwareEntity;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;


@Mixin(MeleeAttackGoal.class)
public  class MeleeAttackGoalMixin
{
	@Shadow
	PathAwareEntity mob;
	@Shadow
	int field_3534; // ticks until mob attacks
	@Shadow
	double speed;
	@Shadow
	private int updateCountdownTicks;
	@Shadow
	boolean pauseWhenMobIdle;

	private double targetX;
	private double targetY;
	private double targetZ;

	protected double getSquaredMaxAttackDistance(LivingEntity entity)
	{
		return this.mob.width * 2.0f * (this.mob.width * 2.0f) + entity.width;
	}

	// Overwrite tick method with the code used in 1.9.4
	@Overwrite
	public void tick()
	{
		LivingEntity targetEntity = this.mob.getTarget();
		this.mob.getLookControl().lookAt(targetEntity, 30.0f, 30.0f);
		double d = this.mob.squaredDistanceTo(targetEntity.x, targetEntity.boundingBox.minY, targetEntity.z);
		double e = this.getSquaredMaxAttackDistance(targetEntity);
		--this.updateCountdownTicks;
		if ((this.pauseWhenMobIdle || this.mob.getVisibilityCache().canSee(targetEntity)) &&
				this.updateCountdownTicks <= 0 &&
				(
					this.targetX == 0.0 && this.targetY == 0.0 && this.targetZ == 0.0 ||
					targetEntity.squaredDistanceTo(this.targetX, this.targetY, this.targetZ) >= 1.0 ||
					this.mob.getRandom().nextFloat() < 0.05f
				)
		)
		{
			this.targetX = targetEntity.x;
			this.targetY = targetEntity.boundingBox.minY;
			this.targetZ = targetEntity.z;
			this.updateCountdownTicks = 4 + this.mob.getRandom().nextInt(7);
			if (d > 1024.0)
			{
				this.updateCountdownTicks += 10;
			}
			else if (d > 256.0)
			{
				this.updateCountdownTicks += 5;
			}
			if (!this.mob.getNavigation().startMovingTo(targetEntity, this.speed))
			{
				this.updateCountdownTicks += 15;
			}
		}
		this.field_3534 = Math.max(this.field_3534 - 1, 0);
		if (d <= e && this.field_3534 <= 0)
		{
			this.field_3534 = 20;
			if (this.mob.getStackInHand() != null)
			{
				this.mob.swingHand();
			}
			this.mob.tryAttack(targetEntity);
		}
	}
}
