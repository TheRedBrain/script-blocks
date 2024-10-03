package com.github.theredbrain.scriptblocks.mixin.item;

import com.github.theredbrain.scriptblocks.registry.StatusEffectsRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {

	@Shadow
	public Item getItem() {
		throw new AssertionError();
	}

	@Shadow
	public Text getName() {
		throw new AssertionError();
	}

	@Shadow public abstract boolean canPlaceOn(CachedBlockPosition pos);

	/**
	 * @author TheRedBrain
	 * @reason TODO
	 */
	@Overwrite
	public ActionResult useOnBlock(ItemUsageContext context) {
		PlayerEntity playerEntity = context.getPlayer();
		BlockPos blockPos = context.getBlockPos();

		CachedBlockPosition cachedBlockPosition = new CachedBlockPosition(context.getWorld(), blockPos, false);
		RegistryEntry<StatusEffect> building_status_effect = Registries.STATUS_EFFECT.getEntry(StatusEffectsRegistry.BUILDING_MODE);
		if (playerEntity != null && !playerEntity.getAbilities().allowModifyWorld && !playerEntity.hasStatusEffect(building_status_effect)/* && !bl*/ && !this.canPlaceOn(cachedBlockPosition)) {
			return ActionResult.PASS;
		}
		Item item = this.getItem();
		ActionResult actionResult = item.useOnBlock(context);
		if (playerEntity != null && actionResult.shouldIncrementStat()) {
			playerEntity.incrementStat(Stats.USED.getOrCreateStat(item));
		}
		return actionResult;
	}
}
