package io.gitlab.shdima.anvils.mixins;

import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.ScreenHandlerPropertyUpdateS2CPacket;
import net.minecraft.screen.*;
import net.minecraft.screen.slot.ForgingSlotsManager;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings({"unused", "UnusedMixin"})
@Mixin(AnvilScreenHandler.class)
abstract public class AnvilScreenHandlerMixin extends ForgingScreenHandler {

	@Shadow @Final
	private Property levelCost;

	@Unique
	private boolean anvils$hasCombined;
	@Unique
	private boolean anvils$hasChangedName;
	@Unique
	private boolean anvils$hasRepairedWithMaterial;
	@Unique
	private int anvils$originalRepairCost;

	public AnvilScreenHandlerMixin(@Nullable ScreenHandlerType<?> type, int syncId, PlayerInventory playerInventory, ScreenHandlerContext context, ForgingSlotsManager forgingSlotsManager) {
		super(type, syncId, playerInventory, context, forgingSlotsManager);
	}

	@Unique
	private boolean anvils$shouldBeFree() {
		ItemStack firstStack = input.getStack(0).copy();
		ItemStack resultStack = output.getStack(0).copy();

		return EnchantmentHelper.getEnchantments(firstStack).equals(EnchantmentHelper.getEnchantments(resultStack));
	}

	@Inject(method = "updateResult", at = @At("HEAD"))
	private void resetTrackingValues(CallbackInfo ci) {
		anvils$hasRepairedWithMaterial = false;
		anvils$hasCombined = false;
		anvils$hasChangedName = false;
		anvils$originalRepairCost = 0;
	}

	@Inject(
			method = "updateResult",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/item/ItemStack;setDamage(I)V",
					ordinal = 0
			)
	)
	private void checkMaterialRepair(CallbackInfo ci) {
		anvils$hasRepairedWithMaterial = true;
	}

	@Inject(
			method = "updateResult",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/item/ItemStack;setDamage(I)V",
					ordinal = 1
			)
	)
	private void checkCombine(CallbackInfo ci) {
		anvils$hasCombined = true;
	}

	@Inject(
			method = "updateResult",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/item/ItemStack;set(Lnet/minecraft/component/ComponentType;Ljava/lang/Object;)Ljava/lang/Object;",
					ordinal = 0
			)
	)
	private void checkRename(CallbackInfo ci) {
		anvils$hasChangedName = true;
	}

	@Inject(
			method = "updateResult",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/item/ItemStack;remove(Lnet/minecraft/component/ComponentType;)Ljava/lang/Object;"
			)
	)
	private void checkUnname(CallbackInfo ci) {
		anvils$hasChangedName = true;
	}

	@Redirect(
			method = "updateResult",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/item/ItemStack;set(Lnet/minecraft/component/ComponentType;Ljava/lang/Object;)Ljava/lang/Object;",
					ordinal = 1
			)
	)
	private <T> @Nullable T storeAnvilUses(@NonNull ItemStack instance, ComponentType<T> type, @Nullable T value) {
		anvils$originalRepairCost = (int) instance.get(type);
		instance.set(type, value);
		return null;
	}

	@Redirect(
			method = "canTakeOutput",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/screen/Property;get()I",
					ordinal = 1
			)
	)
	private int canTakeOutput(Property instance) {
		if (anvils$hasChangedName || anvils$hasCombined || anvils$hasRepairedWithMaterial) return 1;

		return instance.get();
	}

	@Inject(
			method = "updateResult",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/inventory/CraftingResultInventory;setStack(ILnet/minecraft/item/ItemStack;)V",
					shift = At.Shift.AFTER,
					ordinal = 4
			)
	)
	private void calculateActualCost(CallbackInfo ci) {
		if (anvils$shouldBeFree()) {
			levelCost.set(0);
			output.getStack(0).set(DataComponentTypes.REPAIR_COST, anvils$originalRepairCost);
			return;
		}

		if (anvils$hasChangedName) {
			levelCost.set(levelCost.get() - 1);
		}

		if (anvils$hasCombined) {
			levelCost.set(levelCost.get() - 2);
		}
	}

	@Inject(
			method = "updateResult",
			at = @At("TAIL")
	)
	private void sendLevelCostUpdate(CallbackInfo ci) {
		if (player instanceof ServerPlayerEntity serverPlayer) {
			serverPlayer.networkHandler.sendPacket(new ScreenHandlerPropertyUpdateS2CPacket(
					syncId,
					0,
					levelCost.get()
			));
		}
	}
}
