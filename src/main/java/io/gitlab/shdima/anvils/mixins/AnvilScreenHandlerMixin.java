package io.gitlab.shdima.anvils.mixins;

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

	@ModifyVariable(
			method = "updateResult",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/item/ItemStack;setDamage(I)V",
					shift = At.Shift.AFTER,
					ordinal = 1
			),
			index = 2
	)
	private int removeUselessTwoLevels(int value) {
		return value - 2;
	}

	@Redirect(method = {"updateResult"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/screen/Property;get()I"))
	private int getItemLevelCost(Property instance) {
		if (anvils$shouldBeFree()) return 0;
		return instance.get();
	}
}
