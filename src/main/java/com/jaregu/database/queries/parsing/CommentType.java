package com.jaregu.database.queries.parsing;

import java.util.Optional;

public enum CommentType {

	/**
	 * SQL comment starting with two hyphens, ends with new line symbol
	 * 
	 * <pre>
	 * [Before some SQL ]-- some comment
	 * [After some SQL]
	 * </pre>
	 * 
	 */
	HYPHENS("--", "\r\n", false),

	/**
	 * SQL comment starting with slash and asterisk, ends with asterisk and
	 * slash
	 * 
	 * <pre>
	 * [Before some SQL ]/* some comment *&#x2F;[After some SQL]
	 * </pre>
	 * 
	 */
	SLASH_AND_ASTERISK("/*", "*/", true);

	private String startSequence;
	private String endSequence;
	private boolean endingRequired;

	private CommentType(String startSequence, String endSequence, boolean endingRequired) {
		this.startSequence = startSequence;
		this.endSequence = endSequence;
		this.endingRequired = endingRequired;
	}

	public String getStartSequence() {
		return startSequence;
	}

	public String getEndSequence() {
		return endSequence;
	}

	/**
	 * Returns true if this string is a SQL comment of this type
	 * 
	 * @param comment
	 * @return
	 */
	public boolean matches(String comment) {
		return comment != null
				&& comment.length() > (startSequence.length() + (endingRequired ? endSequence.length() : 0))
				&& comment.startsWith(startSequence) && (endingRequired ? comment.endsWith(endSequence) : true);
	}

	/**
	 * Removes comment symbols from comment and return trimmed (whitespace
	 * removed from both ends) result
	 * 
	 * @param comment
	 * @return
	 * @throws QueryParseException
	 *             if comment is not this type of comment
	 *             ({@link #matches(String)} is false)
	 */
	public String unwrap(String comment) throws QueryParseException {
		if (matches(comment)) {
			return comment.substring(startSequence.length(), (endingRequired || comment.endsWith(endSequence)
					? comment.length() - endSequence.length() : comment.length())).trim();
		} else {
			throw new QueryParseException("Passed string: " + comment + " is not SQL comment of this type!");
		}
	}

	/**
	 * Wraps text with this comment style char sequences, and creates valid SQL
	 * comment
	 * 
	 * @param text
	 * @return
	 * @throws QueryParseException
	 */
	public String wrap(String text) throws QueryParseException {
		return startSequence + text + endSequence;
	}

	public static Optional<CommentType> parseCommentType(String comment) {
		if (comment != null && comment.length() > 2) {
			for (CommentType commentType : values()) {
				if (commentType.matches(comment)) {
					return Optional.of(commentType);
				}
			}
		}
		return Optional.empty();
	}
}
