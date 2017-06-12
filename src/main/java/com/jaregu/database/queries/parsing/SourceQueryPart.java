package com.jaregu.database.queries.parsing;

public interface SourceQueryPart {

	String getContent();

	boolean isComment();

	CommentType getCommentType();

	String getCommentContent();

	boolean isBinding();

	String getVariableName();

	public static SourceQueryPart create(String content) {
		return new SourceQueryPartImpl(content);
	}
}