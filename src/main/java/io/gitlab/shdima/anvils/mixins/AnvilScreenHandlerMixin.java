package io.gitlab.shdima.anvils.mixins;

import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
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
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@SuppressWarnings({"unused", "UnusedMixin"})
@Mixin(AnvilScreenHandler.class)
abstract public class AnvilScreenHandlerMixin extends ForgingScreenHandler {
	@Shadow
	@Final
	private Property levelCost;

	public AnvilScreenHandlerMixin(@Nullable ScreenHandlerType<?> type, int syncId, PlayerInventory playerInventory, ScreenHandlerContext context, ForgingSlotsManager forgingSlotsManager) {
		super(type, syncId, playerInventory, context, forgingSlotsManager);
	}

	@Unique
	private boolean anvils$shouldBeFree() {
		ItemStack firstStack = this.input.getStack(0);
		ItemStack resultStack = this.output.getStack(2);

		return firstStack.getEnchantments().equals(resultStack.getEnchantments());
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
		if (anvils$shouldBeFree()) levelCost.set(0);
	}

	@Redirect(method = {"updateResult"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;getOrDefault(Lnet/minecraft/component/ComponentType;Ljava/lang/Object;)Ljava/lang/Object;"))
	private <T> T getOrDefault(ItemStack instance, ComponentType<? extends T> componentType, T o) {
		if (componentType == DataComponentTypes.REPAIR_COST && anvils$shouldBeFree()) return o;
		return instance.getOrDefault(componentType, o);
	}

	@Redirect(method = {"updateResult"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/screen/Property;get()I"))
	private int getItemLevelCost(Property instance) {
		if (anvils$shouldBeFree()) return 0;
		return instance.get();
	}

	@Redirect(method = {"updateResult"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;set(Lnet/minecraft/component/ComponentType;Ljava/lang/Object;)Ljava/lang/Object;"))
	private <T> @Nullable T setRepairCost(ItemStack instance, ComponentType<T> type, T value) {
		if (type == DataComponentTypes.REPAIR_COST && anvils$shouldBeFree()) return null;
		return instance.set(type, value);
	}

	@Inject(method = "getLevelCost", at = @At("HEAD"), cancellable = true)
	private void getLevelCost(CallbackInfoReturnable<Integer> cir) {
		if (anvils$shouldBeFree()) cir.setReturnValue(0);
	}
}
