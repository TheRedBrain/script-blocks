package com.github.theredbrain.scriptblocks.block;

public interface ProvidesData extends Resetable{

	int getData(String id);

	void setData(String id, int value);

	void addData(String id, int value);

	void reset();
}
