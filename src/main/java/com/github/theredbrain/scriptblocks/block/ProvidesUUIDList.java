package com.github.theredbrain.scriptblocks.block;

import java.util.List;
import java.util.UUID;

public interface ProvidesUUIDList extends Resetable {

	List<UUID> getUUIDList(boolean remove);

	void modifyUUIDList(List<UUID> list, boolean remove);

	void reset();
}
