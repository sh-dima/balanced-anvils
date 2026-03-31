package io.gitlab.shdima.anvils.mixins;

import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.*;
import net.minecraft.screen.slot.ForgingSlotsManager;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@SuppressWarnings({"unused", "UnusedMixin"})
@Mixin(AnvilScreenHandler.class)
abstract public class AnvilScreenHandlerMixin extends ForgingScreenHandler {
	@Shadow
	@Final
	private Property levelCost;

	@Unique
	private boolean anvils$hasRepaired;
	@Unique
	private boolean anvils$hasRenamed;
	@Unique
	private int anvils$originalAnvilUses;

	public AnvilScreenHandlerMixin(@Nullable ScreenHandlerType<?> type, int syncId, PlayerInventory playerInventory, ScreenHandlerContext context, ForgingSlotsManager forgingSlotsManager) {
		super(type, syncId, playerInventory, context, forgingSlotsManager);
	}

	@Unique
	private boolean anvils$shouldBeFree() {
		ItemStack firstStack = this.input.getStack(0).copy();
		ItemStack resultStack = this.output.getStack(0).copy();

		return firstStack.getEnchantments().equals(resultStack.getEnchantments());
	}

	@Unique
	private boolean anvils$shouldBeFree(ItemStack result) {
		ItemStack firstStack = this.input.getStack(0).copy();

		return firstStack.getEnchantments().equals(result.copy().getEnchantments());
	}

	@Inject(method = "canTakeOutput", at = @At("HEAD"), cancellable = true)
	private void canTakeOutput(PlayerEntity player, boolean present, CallbackInfoReturnable<Boolean> cir) {
		if (anvils$shouldBeFree()) cir.setReturnValue(true);
	}

	@Inject(method = "onTakeOutput", at = @At("HEAD"))
	private void onTakeOutput(PlayerEntity player, ItemStack stack, CallbackInfo ci) {
		if (anvils$shouldBeFree()) levelCost.set(0);
	}

	@Inject(method = "updateResult", at = @At("TAIL"))
	private void updateResult(CallbackInfo ci) {
		this.anvils$hasRepaired = false;
		this.anvils$hasRenamed = false;
		this.anvils$originalAnvilUses = 0;

		if (anvils$shouldBeFree()) levelCost.set(0);
	}

	@Inject(
			method = "updateResult",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/item/ItemStack;setDamage(I)V",
					ordinal = 1
			)
	)
	private void checkHasRepaired(CallbackInfo ci) {
		this.anvils$hasRepaired = true;
	}

	@Inject(
			method = "updateResult",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/item/ItemStack;set(Lnet/minecraft/component/ComponentType;Ljava/lang/Object;)Ljava/lang/Object;",
					ordinal = 0
			)
	)
	private void checkHasRenamed(CallbackInfo ci) {
		this.anvils$hasRenamed = true;
	}

	@Inject(
			method = "updateResult",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/item/ItemStack;remove(Lnet/minecraft/component/ComponentType;)Ljava/lang/Object;"
			)
	)
	private void checkHasRenamedAgain(CallbackInfo ci) {
		this.anvils$hasRenamed = true;
	}

	@Redirect(
			method = "updateResult",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/screen/Property;set(I)V",
					ordinal = 5
			)
	)
	private void removeRepairAndRenameCosts(Property instance, int value) {
		int newValue = value;

		if (anvils$hasRenamed) {
			newValue--;
		}

		if (anvils$hasRepaired) {
			newValue -= 2;
		}

		instance.set(newValue);
	}

	@Redirect(
			method = "updateResult",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/item/ItemStack;set(Lnet/minecraft/component/ComponentType;Ljava/lang/Object;)Ljava/lang/Object;",
					ordinal = 1
			)
	)
	private <T> T saveAnvilUses(ItemStack instance, ComponentType<T> type, @Nullable T value) {
		this.anvils$originalAnvilUses = (int) instance.get(type);

		instance.set(type, value);

		return null;
	}

	@Redirect(
			method = "updateResult",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/enchantment/EnchantmentHelper;set(Lnet/minecraft/item/ItemStack;Lnet/minecraft/component/type/ItemEnchantmentsComponent;)V"
			)
	)
	private void removeAnvilUseIncrease(ItemStack stack, ItemEnchantmentsComponent enchantments) {
		EnchantmentHelper.set(stack, enchantments);

		if (anvils$shouldBeFree(stack)) {
			stack.set(DataComponentTypes.REPAIR_COST, anvils$originalAnvilUses);
		}
	}

	@Redirect(method = {"updateResult"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/screen/Property;get()I"))
	private int getItemLevelCost(Property instance) {
		if (anvils$shouldBeFree()) return 0;
		return instance.get();
	}
}
