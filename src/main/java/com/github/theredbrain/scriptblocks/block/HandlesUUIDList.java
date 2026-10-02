package com.github.theredbrain.scriptblocks.block;

import java.util.List;
import java.util.UUID;

public interface HandlesUUIDList extends Resetable {

	/**
	 * Supplies a list of UUIDs. The list can be removed afterwards.
	 * This action is typically called by other blocks.
	 *
	 * @param remove Whether the list is removed by the handler afterwards.
	 */
	List<UUID> supplyUUIDList(boolean remove);

	/**
	 * Handles a list of UUIDs. The list can be added or removed by the handler.
	 * This action is typically called by other blocks.
	 *
	 * @param list The UUID list
	 * @param remove Whether the list is added or removed by the handler.
	 */
	void handleUUIDList(List<UUID> list, boolean remove);

	void reset();
}
