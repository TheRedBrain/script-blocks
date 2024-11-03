package com.github.theredbrain.scriptblocks.block;

public interface ProvidesData extends Resetable {

	String getData(String id);

	void setData(String id, String value);

	void reset();
}
