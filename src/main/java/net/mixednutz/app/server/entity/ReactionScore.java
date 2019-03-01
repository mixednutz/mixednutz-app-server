package net.mixednutz.app.server.entity;

public class ReactionScore implements Comparable<ReactionScore> {
	
	private Emoji emoji;
	private int score=0;
	private boolean userIncluded;
	
	public ReactionScore(Emoji emoji) {
		super();
		this.emoji = emoji;
	}
	
	public int getScore() {
		return score;
	}
	public Emoji getEmoji() {
		return emoji;
	}
	public void setEmoji(Emoji emoji) {
		this.emoji = emoji;
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
	public int compareTo(ReactionScore o) {
		//Higher scores first
		return score>o.score?-1:(score<o.score?1:0);
	}

}
