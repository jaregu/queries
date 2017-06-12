package com.jaregu.database.queries.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class Lexer /*implements CharSequence*/ {

	public static LexerPattern newPattern() {
		return new LexerPatternImpl(Collections.emptyList(), Collections.emptyList());
	}

	public static LexerMatcher anyDigit() {
		return AnyDigit.INSTANCE;
	}

	public static LexerMatcher anyLetter() {
		return AnyLetter.INSTANCE;
	}

	public static LexerMatcher anyLetterOrDigit() {
		return AnyLetterOrDigit.INSTANCE;
	}

	public static LexerMatcher whitespace() {
		return Whitespace.INSTANCE;
	}

	public static LexerMatcher eof() {
		return EOF.INSTANCE;
	}

	public static LexerMatcher sequence(String seq) {
		return new SequenceMatcher(seq);
	}

	public static LexerMatcher regexp(Pattern pattern) {
		return new MatchingPattern(pattern);
	}

	private final String text;
	private int offset;

	public Lexer(String text) {
		this.text = Objects.requireNonNull(text);
	}

	public boolean hasMore() {
		return offset < text.length();
	}

	public boolean lookingAt(String prefix) {
		return text.startsWith(prefix, offset);
	}

	public boolean lookingAt(LexerMatcher matcher) {
		return matcher.getStart(text, offset) == offset;
	}

	public void expect(String expected) {
		if (lookingAt(expected)) {
			offset += expected.length();
		} else {
			throw new LexerException(
					"Expected '" + expected + "' has: '" + text.substring(offset, offset + expected.length()) + "'");
		}
	}

	public String read(LexerPattern pattern) {
		int stopAtIndex = pattern.getEnd(text, offset);
		return stopAtIndex == offset ? null : read(stopAtIndex);
	}

	private String read(int end) {
		String substring = text.substring(offset, end);
		offset = end;
		return substring;

	}

	/*@Override
	public int length() {
		return text.length() - offset;
	}
	
	@Override
	public char charAt(int index) {
		return text.charAt(offset + index);
	}
	
	@Override
	public CharSequence subSequence(int start, int end) {
		return text.substring(offset + start, offset + end);
	}*/

	@Override
	public String toString() {
		return text.substring(offset);
	}

	public interface LexerPattern {

		LexerPattern skipAllBetween(String startingWith, String endingWith);

		LexerPattern skipSequence(String sequence);

		LexerPattern skipSequences(String... sequences);

		LexerPattern stopBefore(String sequence);

		LexerPattern stopBeforeAnyOf(String... sequences);

		LexerPattern stopBefore(LexerMatcher matcher);

		LexerPattern stopBeforeAnyOf(LexerMatcher... matcher);

		LexerPattern stopAfter(String sequence);

		LexerPattern stopAfterAnyOf(String... sequences);

		LexerPattern stopAfter(LexerMatcher matcher);

		LexerPattern stopAfterAnyOf(LexerMatcher... matchers);

		LexerPattern stopAtEof();

		int getEnd(String text, int offset);
	}

	public interface LexerMatcher {

		/**
		 * Returns start index of match within passed text starting from offset
		 * or -1 if match is not found
		 * 
		 * @param text
		 * @param offset
		 * @return
		 */
		int getStart(String text, int offset);

		/**
		 * Returns end index of matched region within text starting from offset.
		 * Offset start is equal with index returned from
		 * {@link #getStart(String, int)}
		 * 
		 * @param text
		 * @param offset
		 * @return
		 */
		int getEnd(String text, int offset);
	}

	private static interface PositionedMatcher extends LexerMatcher {

		boolean isStopBefore();

		static PositionedMatcher before(LexerMatcher matcher) {
			return new PositionedMatcherImpl(true, matcher);
		}

		static PositionedMatcher after(LexerMatcher matcher) {
			return new PositionedMatcherImpl(false, matcher);
		}
	}

	private static interface CachedMatcher<T extends LexerMatcher> extends LexerMatcher {

		T getMatcher();

		int getStart();

		static <T extends LexerMatcher> CachedMatcher<T> from(int start, T matcher) {
			return new CachedMatcherImpl<T>(start, matcher);
		}
	}

	private static class LexerPatternImpl implements LexerPattern {

		final private List<LexerMatcher> skipFinders;
		final private List<PositionedMatcher> stopFinders;

		public LexerPatternImpl(List<LexerMatcher> skipFinders, List<PositionedMatcher> indexFinders) {
			this.skipFinders = skipFinders;
			this.stopFinders = indexFinders;
		}

		public int getEnd(String text, int origOffset) {

			Optional<CachedMatcher<LexerMatcher>> skipper;
			CachedMatcher<PositionedMatcher> stopper = null;
			int offset = origOffset;

			while (true) {
				if (stopper == null) {
					stopper = getStart(stopFinders, text, offset).orElseThrow(() -> {
						if (stopFinders.isEmpty()) {
							return new LexerException("No stop instructions defined!");
						} else {
							//TODO better error, maybe use offset to show real text part where something is expected
							return new LexerException("One of end sequences expected: " + stopFinders);
						}
					});
				}
				skipper = getStart(skipFinders, text, offset);
				if (skipper.isPresent() && skipper.get().getStart() <= stopper.getStart()) {
					offset = skipper.get().getMatcher().getEnd(text, offset);
					if (offset > stopper.getStart()) {
						stopper = null;
					}
				} else {
					return (stopper.getMatcher().isStopBefore() ? stopper.getStart()
							: stopper.getEnd(text, stopper.getStart()));
				}
			}
		}

		private <T extends LexerMatcher> Optional<CachedMatcher<T>> getStart(List<T> matchers, String text,
				int offset) {
			return matchers.stream().map(i -> CachedMatcher.from(i.getStart(text, offset), i))
					.filter(i -> i.getStart() >= 0).min((a, b) -> {
						return Integer.compare(a.getStart(), b.getStart());
					});
		}

		@Override
		public LexerPattern skipAllBetween(String startingWith, String endingWith) {
			return addSkipRule(new SkipBetween(startingWith, endingWith));
		}

		@Override
		public LexerPattern skipSequence(String sequence) {
			return addSkipRule(new SequenceMatcher(sequence));
		}

		@Override
		public LexerPattern skipSequences(String... sequences) {
			return addSkipRules(Arrays.stream(sequences).map(SequenceMatcher::new));
		}

		@Override
		public LexerPattern stopBefore(String sequence) {
			return addStopRule(PositionedMatcher.before(new SequenceMatcher(sequence)));
		}

		@Override
		public LexerPattern stopBeforeAnyOf(String... sequences) {
			return addStopRules(Arrays.stream(sequences).map(SequenceMatcher::new).map(PositionedMatcher::before));
		}

		@Override
		public LexerPattern stopBefore(LexerMatcher matcher) {
			return addStopRule(PositionedMatcher.before(matcher));
		}

		@Override
		public LexerPattern stopBeforeAnyOf(LexerMatcher... matchers) {
			return addStopRules(Arrays.stream(matchers).map(PositionedMatcher::before));
		}

		@Override
		public LexerPattern stopAfter(String sequence) {
			return addStopRule(PositionedMatcher.after(new SequenceMatcher(sequence)));
		}

		@Override
		public LexerPattern stopAfterAnyOf(String... sequences) {
			return addStopRules(Arrays.stream(sequences).map(SequenceMatcher::new).map(PositionedMatcher::after));
		}

		@Override
		public LexerPattern stopAfter(LexerMatcher matcher) {
			return addStopRule(PositionedMatcher.after(matcher));
		}

		@Override
		public LexerPattern stopAfterAnyOf(LexerMatcher... matchers) {
			return addStopRules(Arrays.stream(matchers).map(PositionedMatcher::after));
		}

		@Override
		public LexerPattern stopAtEof() {
			return addStopRule(PositionedMatcher.before(EOF.INSTANCE));
		}

		private LexerPatternImpl addStopRule(PositionedMatcher rule) {
			return new LexerPatternImpl(skipFinders, concat(stopFinders, rule));
		}

		private LexerPatternImpl addStopRules(Stream<PositionedMatcher> rules) {
			return new LexerPatternImpl(skipFinders, concat(stopFinders, rules));
		}

		private LexerPatternImpl addSkipRule(LexerMatcher rule) {
			return new LexerPatternImpl(concat(skipFinders, rule), stopFinders);
		}

		private LexerPatternImpl addSkipRules(Stream<LexerMatcher> rules) {
			return new LexerPatternImpl(concat(skipFinders, rules), stopFinders);
		}

		private <T extends LexerMatcher> List<T> concat(List<T> original, T rule) {
			ArrayList<T> newRules = new ArrayList<>(original.size() + 1);
			newRules.addAll(original);
			newRules.add(rule);
			return newRules;
		}

		private <T extends LexerMatcher> List<T> concat(List<T> original, Stream<T> rules) {
			ArrayList<T> newRules = new ArrayList<>(original.size() + 3);
			newRules.addAll(original);
			rules.forEach(newRules::add);
			return newRules;
		}
	}

	private static class CachedMatcherImpl<T extends LexerMatcher> implements CachedMatcher<T> {

		private int start;
		private T matcher;

		public CachedMatcherImpl(int start, T matcher) {
			this.start = start;
			this.matcher = matcher;
		}

		@Override
		public T getMatcher() {
			return matcher;
		}

		@Override
		public int getStart() {
			return start;
		}

		@Override
		public int getStart(String text, int offset) {
			throw new LexerException("use cached start value!");
		}

		@Override
		public int getEnd(String text, int offset) {
			return matcher.getEnd(text, offset);
		}
	}

	private static final class PositionedMatcherImpl implements PositionedMatcher {

		private boolean before;
		private LexerMatcher matcher;

		private PositionedMatcherImpl(boolean stopBefore, LexerMatcher matcher) {
			this.before = stopBefore;
			this.matcher = matcher;
		}

		@Override
		public int getStart(String text, int offset) {
			return matcher.getStart(text, offset);
		}

		@Override
		public int getEnd(String text, int offset) {
			return matcher.getEnd(text, offset);
		}

		@Override
		public boolean isStopBefore() {
			return before;
		}
	}

	private static final class SkipBetween implements LexerMatcher {

		private String start;
		private String end;

		SkipBetween(String start, String end) {
			this.start = start;
			this.end = end;
		}

		@Override
		public int getStart(String text, int offset) {
			return text.indexOf(start, offset);
		}

		@Override
		public int getEnd(String text, int offset) {
			int endIndex = text.indexOf(end, text.indexOf(start, offset) + start.length());
			if (endIndex < 0) {
				throw new LexerException(this + " instruction started at index: " + text.indexOf(start, offset)
						+ " but there is no end symbol!");
			}
			return endIndex + end.length();
		}

		@Override
		public String toString() {
			return "SkipBetween{" + start + " and " + end + "}";
		}
	}

	private static final class SequenceMatcher implements LexerMatcher {

		private String seq;

		private SequenceMatcher(String seq) {
			this.seq = seq;
		}

		@Override
		public int getStart(String text, int offset) {
			return text.indexOf(seq, offset);
		}

		@Override
		public int getEnd(String text, int offset) {
			return text.indexOf(seq, offset) + seq.length();
		}

		@Override
		public String toString() {
			return "SequenceMatcher{" + seq + "}";
		}
	}

	private static final class MatchingPattern implements LexerMatcher {

		private Pattern pattern;

		private MatchingPattern(Pattern pattern) {
			this.pattern = pattern;
		}

		@Override
		public int getStart(String text, int offset) {
			Matcher matcher = pattern.matcher(text.substring(offset));
			if (matcher.find()) {
				return matcher.start() + offset;
			} else {
				return -1;
			}
		}

		@Override
		public int getEnd(String text, int offset) {
			Matcher matcher = pattern.matcher(text.substring(offset));
			if (matcher.lookingAt()) {
				return matcher.end() + offset;
			} else {
				throw new LexerException(this + " end called when there is no matching pattern!");
			}
		}

		@Override
		public String toString() {
			return "MatchingPattern{" + pattern + "}";
		}
	}

	private static final class Whitespace extends CharacterMatcher {

		static final Whitespace INSTANCE = new Whitespace();

		private Whitespace() {
		}

		@Override
		protected boolean isMatching(char ch) {
			return Character.isWhitespace(ch);
		}
	}

	private static final class AnyLetterOrDigit extends CharacterMatcher {

		static final AnyLetterOrDigit INSTANCE = new AnyLetterOrDigit();

		private AnyLetterOrDigit() {
		}

		@Override
		protected boolean isMatching(char ch) {
			return Character.isLetterOrDigit(ch);
		}
	}

	private static final class AnyDigit extends CharacterMatcher {

		static final AnyDigit INSTANCE = new AnyDigit();

		private AnyDigit() {
		}

		@Override
		protected boolean isMatching(char ch) {
			return Character.isDigit(ch);
		}
	}

	private static final class AnyLetter extends CharacterMatcher {

		static final AnyLetter INSTANCE = new AnyLetter();

		private AnyLetter() {
		}

		@Override
		protected boolean isMatching(char ch) {
			return Character.isLetter(ch);
		}
	}

	private static abstract class CharacterMatcher implements LexerMatcher {

		protected abstract boolean isMatching(char ch);

		@Override
		public int getStart(String text, int offset) {
			for (int i = offset; i < text.length(); i++) {
				if (isMatching(text.charAt(i))) {
					return i;
				}
			}
			return -1;
		}

		@Override
		public int getEnd(String text, int offset) {
			for (int i = offset + 1; i < text.length(); i++) {
				if (!isMatching(text.charAt(i))) {
					return i;
				}
			}
			return text.length();
		}

		@Override
		public String toString() {
			return this.getClass().getSimpleName();
		}
	}

	private static final class EOF implements LexerMatcher {

		static final EOF INSTANCE = new EOF();

		private EOF() {
		}

		@Override
		public int getStart(String text, int offset) {
			return text.length();
		}

		@Override
		public int getEnd(String text, int offset) {
			return text.length();
		}

		@Override
		public String toString() {
			return "EOF";
		}
	}

}
