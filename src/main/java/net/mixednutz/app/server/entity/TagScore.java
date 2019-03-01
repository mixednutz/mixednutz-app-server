package net.mixednutz.app.server.entity;

public class TagScore implements Comparable<TagScore> {
	
	private String tag;
	private int score=0;
	private boolean userIncluded;
	
	public TagScore(String tag) {
		super();
		this.tag = tag;
	}
	
	public String getTag() {
		return tag;
	}
	public void setTag(String tag) {
		this.tag = tag;
	}
	public int getScore() {
		return score;
	}
	public void setScore(int score) {
		this.score = score;
	}
	public void incrementScore() {
		this.score++;
	}
	public boolean isUserIncluded() {
		return userIncluded;
	}
	public void setUserIncluded(boolean userIncluded) {
		this.userIncluded = userIncluded;
	}
	public void setUserIncluded() {
		this.setUserIncluded(true);
	}
	
	@Override
	public int compareTo(TagScore o) {
		//Higher scores first
		return score>o.score?-1:(score<o.score?1:0);
	}

}
