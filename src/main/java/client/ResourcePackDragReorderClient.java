package client;

import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResourcePackDragReorderClient implements ClientModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("packdragreorder");

	@Override
	public void onInitializeClient() {
		LOGGER.info("Resource Pack Drag Reorder loaded: hold a pack and drag it up/down");
	}
}
