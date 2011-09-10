package com.danielpecos.gtdtm.activities.tasks;

public abstract class ProgressHandler {
	protected int rangeSize;
	protected int rangeStart;
	protected int total;
	protected int lastStep;
	
	public ProgressHandler() {
	}
	
	public ProgressHandler(int total, int range_start, int range_end) {
		this.total = total;
		this.rangeStart = range_start;
		this.rangeSize = range_end - range_start;
	}
	
	public abstract void onFinish(String response);
	public abstract void updateProgress(Integer progress, Integer secondaryProgress);
}
