package org.lancoder.common.file_components.streams.original;

import java.io.Serializable;

import org.lancoder.common.codecs.base.Codec;
import org.lancoder.common.task.Unit;

public abstract class BaseStream implements Serializable {

	private static final long serialVersionUID = 774310730253165761L;

	protected String relativeFile;
	protected int index;
	protected Codec codec;
	protected String title = "";
	protected String language = "und";
	protected boolean isDefault = false;
	protected long unitCount;
	protected Unit unit = Unit.SECONDS;

	protected BaseStream() {
	}

	public BaseStream(String relativeFile, int index, Codec codec, long unitCount, Unit unit) {
		this.relativeFile = relativeFile;
		this.index = index;
		this.codec = codec;
		this.unitCount = unitCount;
		this.unit = unit;
	}

	public String getRelativeFile() {
		return relativeFile;
	}

	public int getIndex() {
		return index;
	}

	public Codec getCodec() {
		return codec;
	}

	public String getTitle() {
		return title;
	}

	public String getLanguage() {
		return language;
	}

	public boolean isDefault() {
		return isDefault;
	}

	public long getUnitCount() {
		return unitCount;
	}

	public Unit getUnit() {
		return unit;
	}

}
