package com.jaregu.queries.example;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.dalesbred.Database;
import org.dalesbred.query.SqlQuery;
import org.junit.Before;
import org.junit.Test;

import com.jaregu.database.queries.Queries;
import com.jaregu.database.queries.RetativeQueries;
import com.jaregu.database.queries.building.Query;
import com.jaregu.database.queries.compiling.PreparedQuery;
import com.jaregu.database.queries.parsing.QueriesSource;

public class SampleQueries {

	private static boolean initialized = false;
	private Database db;
	private Queries queries;
	private RetativeQueries rq;

	@Before
	public void setUp() {
		db = new MemoryDb().getDb();
		QueriesSource source = Queries.sourceOfResource("com/jaregu/queries/example/sample-queries.sql");
		queries = Queries.ofSources(source);
		rq = queries.ofSource(source.getId());

		if (!initialized) {
			createDummyTable();
			insertDummyData();
			initialized = true;
		}
	}

	public void createDummyTable() {
		db.update(rq.get("create-dummy").build().getSql());
	}

	public void insertDummyData() {
		PreparedQuery query = rq.get("insert-dummy");
		query.build(new Dummy(1, 4, "1-4")).stream().map(this::toQuery).forEach(db::update);
		query.build(new Dummy(2, 5, "2-5")).stream().map(this::toQuery).forEach(db::update);
		query.build(new Dummy(3, 6, "3-6")).stream().map(this::toQuery).forEach(db::update);
	}

	@Test
	public void simplestQuery() {
		List<Dummy> rows = db.findAll(Dummy.class, rq.get("all-dummies").build().getSql());
		assertThat(rows).size().isEqualTo(3);
	}

	@Test
	public void countDummyData() {
		// parameter as array
		assertThat(rq.get("count-dummies").build(1).stream().map(this::toQuery).map(db::findUniqueInt).findFirst())
				.hasValue(2);
		// parameter as list
		assertThat(rq.get("count-dummies").build(Collections.singletonList(1)).stream().map(this::toQuery)
				.map(db::findUniqueInt).findFirst()).hasValue(2);
		// parameter as iterable
		assertThat(rq.get("count-dummies").build(Collections.singleton(1)).stream().map(this::toQuery)
				.map(db::findUniqueInt).findFirst()).hasValue(2);
	}

	@Test
	public void multipleIterableParams() {
		List<Dummy> rows = rq.get("multiple-iterable-params").build(5, "3-6").stream().map(this::toQuery)
				.map(q -> db.findAll(Dummy.class, q)).findAny().get();
		assertThat(rows).extracting("foo", "bar").containsOnly(tuple(5, "2-5"), tuple(6, "3-6"));
	}

	@Test
	public void namedIterableParams() {
		// parameters as array with index name
		List<Dummy> rows = rq.get("named-list-parameters").build(5, "3-6").stream().map(this::toQuery)
				.map(q -> db.findAll(Dummy.class, q)).findAny().get();
		assertThat(rows).extracting("foo", "bar").containsOnly(tuple(5, "2-5"), tuple(6, "3-6"));

		// named parameters as list with index name
		rows = rq.get("named-list-parameters").build(Arrays.asList(5, "3-6")).stream().map(this::toQuery)
				.map(q -> db.findAll(Dummy.class, q)).findAny().get();
		assertThat(rows).extracting("foo", "bar").containsOnly(tuple(5, "2-5"), tuple(6, "3-6"));
	}

	@Test
	public void mixedResolverError() {
		assertThatThrownBy(() -> rq.get("error-variable-resolvers-mixed").build(5, "3-6").stream().map(this::toQuery)
				.map(q -> db.findAll(Dummy.class, q))).hasMessageContaining("already resolved");
	}

	@Test
	public void namedOptionalParameters() {
		PreparedQuery preparedQuery = rq.get("named-optional-criterion-parameters");

		// it is possible to build multiple times with different parameters one
		// prepared query
		Query query = preparedQuery.build("foo", 5, "bar", "2-5");
		assertThat(query.getSql()).containsSequence("and foo = ?").containsSequence("and (bar = ?");
		List<Dummy> rows = db.findAll(Dummy.class, toQuery(query));
		assertThat(rows).extracting("foo", "bar").containsOnly(tuple(5, "2-5"));

		Query query2 = preparedQuery.build("foo", null, "bar", "3-6");
		assertThat(query2.getSql()).doesNotContain("and foo = ?").containsSequence("and (bar = ?");
		List<Dummy> rows2 = db.findAll(Dummy.class, toQuery(query2));
		assertThat(rows2).extracting("foo", "bar").containsOnly(tuple(6, "3-6"));
	}

	@Test
	public void inClauseCollectionSupport() {
		Query query = rq.get("in-clause-support").build("collectionOfIds", Arrays.asList(1, 3));
		assertThat(query.getSql()).containsSequence("where id IN (?, ?");
		List<Dummy> rows = db.findAll(Dummy.class, toQuery(query));
		assertThat(rows).extracting("id", "bar").containsOnly(tuple(1, "1-4"), tuple(3, "3-6"));
	}

	@Test
	public void conditionalParameters() {
		// and foo > ? -- :foo + 1; :foo != null && :foo > 3
		PreparedQuery preparedQuery = rq.get("named-conditional-criterion-parameters");

		Query query = preparedQuery.build("foo", 4);
		assertThat(query.getSql()).containsSequence("and foo > ?");
		// should be only one row with foo greater than 5
		List<Integer> ids = db.findAll(Integer.class, toQuery(query));
		assertThat(ids).containsExactly(3);

		Query query2 = preparedQuery.build("foo", null);
		assertThat(query2.getSql()).doesNotContain("and foo > ?");
		List<Integer> ids2 = db.findAll(Integer.class, toQuery(query2));
		assertThat(ids2).containsExactly(1, 2, 3);

		Query query3 = preparedQuery.build("foo", 3);
		assertThat(query3.getSql()).doesNotContain("and foo > ?");
		List<Integer> ids3 = db.findAll(Integer.class, toQuery(query3));
		assertThat(ids3).containsExactly(1, 2, 3);
	}

	private SqlQuery toQuery(Query q) {
		return SqlQuery.query(q.getSql(), q.getParameters());
	}

	public static class Dummy {

		public int id;
		private Integer foo;
		private String bar;

		public Dummy() {
		}

		public Dummy(int id, Integer foo, String bar) {
			super();
			this.id = id;
			this.foo = foo;
			this.bar = bar;
		}

		public Integer getFoo() {
			return foo;
		}

		public void setFoo(Integer foo) {
			this.foo = foo;
		}

		public String getBar() {
			return bar;
		}

		public void setBar(String bar) {
			this.bar = bar;
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("Dummy [id=").append(id).append(", foo=").append(foo).append(", bar=").append(bar)
					.append("]");
			return builder.toString();
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((bar == null) ? 0 : bar.hashCode());
			result = prime * result + ((foo == null) ? 0 : foo.hashCode());
			result = prime * result + id;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Dummy other = (Dummy) obj;
			if (bar == null) {
				if (other.bar != null)
					return false;
			} else if (!bar.equals(other.bar))
				return false;
			if (foo == null) {
				if (other.foo != null)
					return false;
			} else if (!foo.equals(other.foo))
				return false;
			if (id != other.id)
				return false;
			return true;
		}
	}
}
