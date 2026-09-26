package mixin;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.SelectableEntry;
import net.minecraft.client.gui.screens.packs.PackSelectionModel;
import net.minecraft.client.gui.screens.packs.TransferableSelectionList;
import net.minecraft.client.input.MouseButtonEvent;

import client.PackDragState;
import client.PackEntryDuck;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TransferableSelectionList.PackEntry.class)
public class TransferableSelectionListPackEntryMixin implements PackEntryDuck {

	@Shadow
	@Final
	private TransferableSelectionList parent;

	@Shadow
	@Final
	private PackSelectionModel.Entry pack;

	@Override
	public PackSelectionModel.Entry packdrag$getModel() {
		return this.pack;
	}

	@Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
	private void packDrag$onClick(MouseButtonEvent event, boolean doubleClick, CallbackInfoReturnable<Boolean> cir) {
		if (event.button() != 0 || this.pack.isFixedPosition()) {
			return;
		}
		SelectableEntry self = (SelectableEntry) (Object) this;
		TransferableSelectionList.Entry entry = (TransferableSelectionList.Entry) (Object) this;
		int rx = (int) event.x() - entry.getContentX();
		int ry = (int) event.y() - entry.getContentY();

		if (this.pack.canSelect() && self.mouseOverIcon(rx, ry, 32)) {
			return;
		}
		if (this.pack.canUnselect() && self.mouseOverLeftHalf(rx, ry, 32)) {
			return;
		}
		if (this.pack.canMoveUp() && self.mouseOverTopRightQuarter(rx, ry, 32)) {
			return;
		}
		if (this.pack.canMoveDown() && self.mouseOverBottomRightQuarter(rx, ry, 32)) {
			return;
		}

		// Pressing the row body starts/restarts a drag.
		PackDragState.start(this.parent, this.pack.getId());
		cir.setReturnValue(true);
	}

	public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
		if (!PackDragState.isActive() || PackDragState.dragList() != this.parent) {
			return false;
		}
		PackDragState.handleDrag(event);
		return true;
	}

	public boolean mouseReleased(MouseButtonEvent event) {
		if (PackDragState.isActive() && PackDragState.dragList() == this.parent && event.button() == 0) {
			PackDragState.cancel();
			return true;
		}
		return false;
	}

	@Inject(method = "extractContent", at = @At("TAIL"))
	private void packDrag$renderHighlight(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovered, float delta, CallbackInfo ci) {
		if (!PackDragState.isActive() || PackDragState.findEntry(this.parent) != (Object) this) {
			return;
		}
		TransferableSelectionList.Entry entry = (TransferableSelectionList.Entry) (Object) this;
		int x = entry.getX();
		int y = entry.getY();
		graphics.fill(x, y, x + entry.getWidth(), y + entry.getHeight(), 0x28FFFFFF);
	}
}
