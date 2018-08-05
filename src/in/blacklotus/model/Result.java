package in.blacklotus.model;

public class Result {
	
	private Metadata meta;
	
	private long timestamp[];
	
	private Indicators indicators;
	
	public Metadata getMeta() {
		return meta;
	}

	public void setMeta(Metadata meta) {
		this.meta = meta;
	}

	public long[] getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long[] timestamp) {
		this.timestamp = timestamp;
	}

	public Indicators getIndicators() {
		return indicators;
	}

	public void setIndicators(Indicators indicators) {
		this.indicators = indicators;
	}
	
	

}
