package mixin;

import net.minecraft.client.gui.screens.packs.PackSelectionScreen;

import client.PackDragState;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PackSelectionScreen.class)
public class PackSelectionScreenMixin {

	@Inject(method = "tick", at = @At("HEAD"))
	private void packDrag$onTick(CallbackInfo ci) {
		PackDragState.autoScroll();
	}

	@Inject(method = "onClose", at = @At("HEAD"))
	private void packDrag$onClose(CallbackInfo ci) {
		PackDragState.cancel();
	}
}
