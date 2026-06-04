package io.gitlab.shdima.anvils.mixins;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.protocol.game.ClientboundContainerSetDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings({"unused", "UnusedMixin"})
@Mixin(AnvilMenu.class)
abstract class AnvilMenuMixin extends ItemCombinerMenu {

	@Shadow @Final
	private DataSlot cost;

	@Unique
	private boolean anvils$hasCombined;
	@Unique
	private boolean anvils$hasChangedName;
	@Unique
	private boolean anvils$hasRepairedWithMaterial;
	@Unique
	private int anvils$originalRepairCost;

	public AnvilMenuMixin(@Nullable MenuType<?> menuType, int containerId, Inventory inventory, ContainerLevelAccess access, ItemCombinerMenuSlotDefinition itemInputSlots) {
		super(menuType, containerId, inventory, access, itemInputSlots);
	}

	@Unique
	private boolean anvils$shouldBeFree() {
		ItemStack firstStack = inputSlots.getItem(0).copy();
		ItemStack resultStack = resultSlots.getItem(0).copy();

		return EnchantmentHelper.getEnchantmentsForCrafting(firstStack).equals(EnchantmentHelper.getEnchantmentsForCrafting(resultStack));
	}

	@Inject(method = "createResult", at = @At("HEAD"))
	private void resetTrackingValues(CallbackInfo ci) {
		anvils$hasRepairedWithMaterial = false;
		anvils$hasCombined = false;
		anvils$hasChangedName = false;
		anvils$originalRepairCost = 0;
	}

	@Inject(
			method = "createResult",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/item/ItemStack;setDamageValue(I)V",
					ordinal = 0
			)
	)
	private void checkMaterialRepair(CallbackInfo ci) {
		anvils$hasRepairedWithMaterial = true;
	}

	@Inject(
			method = "createResult",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/item/ItemStack;setDamageValue(I)V",
					ordinal = 1
			)
	)
	private void checkCombine(CallbackInfo ci) {
		anvils$hasCombined = true;
	}

	@Inject(
			method = "createResult",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/item/ItemStack;set(Lnet/minecraft/core/component/DataComponentType;Ljava/lang/Object;)Ljava/lang/Object;",
					ordinal = 0
			)
	)
	private void checkRename(CallbackInfo ci) {
		anvils$hasChangedName = true;
	}

	@Inject(
			method = "createResult",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/item/ItemStack;remove(Lnet/minecraft/core/component/DataComponentType;)Ljava/lang/Object;"
			)
	)
	private void checkUnname(CallbackInfo ci) {
		anvils$hasChangedName = true;
	}

	@Redirect(
			method = "createResult",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/item/ItemStack;set(Lnet/minecraft/core/component/DataComponentType;Ljava/lang/Object;)Ljava/lang/Object;",
					ordinal = 1
			)
	)
	private <T> @Nullable T storeAnvilUses(@NonNull ItemStack instance, DataComponentType<T> type, @Nullable T value) {
		anvils$originalRepairCost = (int) instance.get(type);
		instance.set(type, value);
		return null;
	}

	@Redirect(
			method = "mayPickup",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/inventory/DataSlot;get()I",
					ordinal = 1
			)
	)
	private int canTakeOutput(DataSlot instance) {
		if (anvils$hasChangedName || anvils$hasCombined || anvils$hasRepairedWithMaterial) return 1;

		return instance.get();
	}

	@Inject(
			method = "createResult",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/inventory/ResultContainer;setItem(ILnet/minecraft/world/item/ItemStack;)V",
					shift = At.Shift.AFTER,
					ordinal = 4
			)
	)
	private void calculateActualCost(CallbackInfo ci) {
		if (anvils$shouldBeFree()) {
			cost.set(0);
			resultSlots.getItem(0).set(DataComponents.REPAIR_COST, anvils$originalRepairCost);
			return;
		}

		if (anvils$hasChangedName) {
			cost.set(cost.get() - 1);
		}

		if (anvils$hasCombined) {
			cost.set(cost.get() - 2);
		}
	}

	@Inject(
			method = "createResult",
			at = @At("TAIL")
	)
	private void sendLevelCostUpdate(CallbackInfo ci) {
		if (player instanceof ServerPlayer serverPlayer) {
			serverPlayer.connection.send(new ClientboundContainerSetDataPacket(
					containerId,
					0,
					cost.get()
			));
		}
	}
}
