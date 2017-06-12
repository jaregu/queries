package com.jaregu.database.queries.parsing;

import static java.util.Objects.requireNonNull;

import java.util.Optional;

class SourceQueryPartImpl implements SourceQueryPart {

	private final String content;
	private final Optional<CommentType> commentType;
	private final Optional<String> variableName;

	SourceQueryPartImpl(String content) {
		requireNonNull(content);

		this.content = content;
		this.commentType = CommentType.parseCommentType(content);
		this.variableName = Optional.ofNullable(content.startsWith(":") ? content.substring(1) : null);
	}

	@Override
	public String getContent() {
		return content;
	}

	@Override
	public boolean isComment() {
		return commentType.isPresent();
	}

	@Override
	public CommentType getCommentType() {
		return commentType.orElseThrow(() -> new QueriesParseException("This is not comment part!"));
	}

	@Override
	public String getCommentContent() {
		return getCommentType().unwrap(content);
	}

	@Override
	public boolean isBinding() {
		return variableName.isPresent();
	}

	@Override
	public String getVariableName() {
		return variableName.orElseThrow(() -> new QueriesParseException("This is not binded variable part!"));
	}

	@Override
	public String toString() {
		return "SourceQueryPart{" + content + "}";
	}
}
