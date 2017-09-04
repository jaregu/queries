package com.jaregu.database.queries.parsing;

public interface ParsedQueryPart {

	String getContent();

	boolean isComment();

	CommentType getCommentType();

	String getCommentContent();

	boolean isNamedVariable();

	boolean isAnonymousVariable();

	boolean isSimplePart();

	String getVariableName();

	public static ParsedQueryPart create(String content) {
		return new ParsedQueryPartImpl(content);
	}
}