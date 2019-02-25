package net.mixednutz.app.server.entity;

public interface PostComment extends Comment {
	
	<P extends Post<C>, C extends PostComment> void setPost(P post);
	
}
