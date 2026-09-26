package client;

import java.util.List;

import net.minecraft.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.packs.PackSelectionModel;
import net.minecraft.client.gui.screens.packs.PackSelectionScreen;
import net.minecraft.client.gui.screens.packs.TransferableSelectionList;
import net.minecraft.client.input.MouseButtonEvent;

public final class PackDragState {

	private static TransferableSelectionList dragList;
	private static String dragId;
	private static boolean scrollUp; 
	private static boolean scrollDown; 
	private static long nextScrollMs;

	private PackDragState() {
	}

	public static boolean isActive() {
		return dragList != null;
	}

	public static TransferableSelectionList dragList() {
		return dragList;
	}

	public static void start(TransferableSelectionList list, String packId) {
		cancel();
		dragList = list;
		dragId = packId;
		TransferableSelectionList.Entry entry = findEntry(list);
		if (entry != null) {
			list.setSelected(entry);
		}
	}

	public static void cancel() {
		dragList = null;
		dragId = null;
		scrollUp = false;
		scrollDown = false;
	}

	public static TransferableSelectionList.PackEntry findEntry(TransferableSelectionList list) {
		if (!isActive()) {
			return null;
		}
		for (TransferableSelectionList.Entry child : list.children()) {
			if (child instanceof TransferableSelectionList.PackEntry && child.getPackId().equals(dragId)) {
				return (TransferableSelectionList.PackEntry) child;
			}
		}
		return null;
	}

	public static void handleDrag(MouseButtonEvent event) {
		TransferableSelectionList list = dragList;
		if (list == null) {
			return;
		}

		scrollUp = event.y() < list.getY();
		scrollDown = event.y() > list.getY() + list.getHeight();

		int target = findRowUnder(list, (int) event.y());
		if (target < 0) {
			return;
		}
		for (int step = 0; step < 64; step++) { 
			TransferableSelectionList.PackEntry packEntry = findEntry(list);
			if (packEntry == null) {
				return; 
			}
			TransferableSelectionList.Entry entry = (TransferableSelectionList.Entry) packEntry;
			List<TransferableSelectionList.Entry> children = list.children();
			int row = children.indexOf(entry);
			if (row < 0) {
				return;
			}
			list.setSelected(entry);
			if (row == target) {
				return;
			}
			PackSelectionModel.Entry model = ((PackEntryDuck) packEntry).packdrag$getModel();
			if (row > target) {
				if (!model.canMoveUp()) {
					return;
				}
				model.moveUp();
			} else {
				if (!model.canMoveDown()) {
					return;
				}
				model.moveDown();
			}
		}
	}

	private static int findRowUnder(TransferableSelectionList list, int mouseY) {
		List<TransferableSelectionList.Entry> children = list.children();
		int nearest = -1;
		int nearestDist = Integer.MAX_VALUE;
		for (int index = 1; index < children.size(); index++) {
			TransferableSelectionList.Entry child = children.get(index);
			if (!(child instanceof TransferableSelectionList.PackEntry)) {
				continue;
			}
			int top = child.getY();
			if (mouseY >= top && mouseY < top + child.getHeight()) {
				return index;
			}
			int dist = mouseY < top ? top - mouseY : mouseY - (top + child.getHeight());
			if (dist < nearestDist) {
				nearestDist = dist;
				nearest = index;
			}
		}
		return nearest;
	}

	public static void autoScroll() {
		if (!isActive()) {
			return;
		}
		Minecraft minecraft = Minecraft.getInstance();
		if (!(minecraft.gui.screen() instanceof PackSelectionScreen)) {
			cancel();
			return;
		}
		if (!scrollUp && !scrollDown) {
			return;
		}
		long now = Util.getMillis();
		if (now < nextScrollMs) {
			return;
		}
		nextScrollMs = now + 100;
		TransferableSelectionList.PackEntry entry = findEntry(dragList);
		if (entry == null) {
			return;
		}
		double rowHeight = ((TransferableSelectionList.Entry) entry).getHeight();
		double amount = scrollDown ? rowHeight : -rowHeight;
		dragList.setScrollAmount(dragList.scrollAmount() + amount);
	}
}