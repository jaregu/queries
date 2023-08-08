package com.jaregu.queries.example;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.sql.DataSource;

import org.dalesbred.Database;
import org.dalesbred.query.SqlQuery;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.jaregu.database.queries.Queries;
import com.jaregu.database.queries.RelativeQueries;
import com.jaregu.database.queries.building.Query;
import com.jaregu.database.queries.building.QueryBuildException;
import com.jaregu.database.queries.compiling.PreparedQuery;
import com.jaregu.database.queries.ext.AbstractSearch;
import com.jaregu.database.queries.parsing.QueriesSource;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class SampleQueries {

	private static boolean initialized = false;
	private Database db;
	private QueriesSource source;
	private Queries queries;
	private RelativeQueries rq;

	@BeforeAll
	public void setUp() {

		HikariConfig config = new HikariConfig("/com/jaregu/queries/example/hikari.properties");
		DataSource ds = new HikariDataSource(config);
		db = new Database(ds);

		// db = createLocalOracleDb(); // createLocalMariaDb();

		source = QueriesSource.ofResource("com/jaregu/queries/example/sample-queries.sql");
		queries = Queries.of(source);
		rq = queries.relativeTo(source.getId());

		// oracle.jdbc.driver.OracleDriver aa = new OracleDriver();

		if (!initialized) {
			createDummyTable();
			insertDummyData();
			initialized = true;
		}
	}

	@SuppressWarnings("unused")
	private Database createLocalMariaDb() {
		HikariConfig config = new HikariConfig();
		config.setJdbcUrl(
				"jdbc:mariadb://localhost:3406/test?autoReconnect=true&?useUnicode=yes&characterEncoding=UTF-8");
		config.setUsername("test");
		config.setPassword("test");
		config.setAutoCommit(false);
		config.setDriverClassName("org.mariadb.jdbc.Driver");
		Database mariaDb = new Database(new HikariDataSource(config));
		return mariaDb;
	}

	@SuppressWarnings("unused")
	private Database createLocalOracleDb() {
		HikariConfig config = new HikariConfig();
		config.setJdbcUrl("jdbc:oracle:thin:@localhost:1521:XE");
		config.setUsername("test");
		config.setPassword("somepwd");
		config.setAutoCommit(false);
		config.setDriverClassName("oracle.jdbc.driver.OracleDriver");
		Database oracleDb = new Database(new HikariDataSource(config));
		return oracleDb;
	}

	public void createDummyTable() {
		db.update(rq.get("create-dummy").build().getSql());
	}

	public void createDummyTableOracle() {
		String sql = rq.get("create-dummy-oracle").build().getSql();
		System.out.println(sql);
		db.update(sql);
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

		// it is error if there is no parameter at all named bar
		assertThatThrownBy(() -> preparedQuery.build("foo", 5)).isInstanceOf(QueryBuildException.class);
	}

	@Test
	public void inClauseCollectionSupportAnonymous() {
		// by default there is no in clause support but it is easy to add some
		RelativeQueries rq = Queries.builder().binderForCollectionsAndLastValueNull(Arrays.asList(1, 3, 10))
				.source(source).build().relativeTo(source.getId());

		Query query = rq.get("in-clause-support-anonymous").build((Object) Arrays.asList(1, 3));
		assertThat(query.getSql()).containsSequence("(?,?,?)");
		assertThat(new ArrayList<Object>(query.getParameters())).containsExactly(1, 3, null);
		List<Dummy> rows = db.findAll(Dummy.class, toQuery(query));
		assertThat(rows).extracting("id", "bar").containsOnly(tuple(1, "1-4"), tuple(3, "3-6"));
	}

	@Test
	public void inClauseCollectionSupportNamed() {
		// by default there is no in clause support but it is easy to add some
		RelativeQueries rq = Queries.builder().binderForCollectionsAndLastValueRepeated(Arrays.asList(1, 3, 10))
				.source(source).build().relativeTo(source.getId());

		Query query = rq.get("in-clause-support-named").build("collectionOfIds", Arrays.asList(1, 3));
		assertThat(query.getSql()).containsSequence("(?,?,?)");
		assertThat(new ArrayList<Object>(query.getParameters())).containsExactly(1, 3, 3);
		List<Dummy> rows = db.findAll(Dummy.class, toQuery(query));
		assertThat(rows).extracting("id", "bar").containsOnly(tuple(1, "1-4"), tuple(3, "3-6"));
	}

	@Test
	public void inClauseCollectionSupportOptionalNamed() {
		// by default there is no in clause support but it is easy to add some
		RelativeQueries rq = Queries.builder().binderForCollectionsAndLastValueRepeated(Arrays.asList(4)).source(source)
				.build().relativeTo(source.getId());

		Query query = rq.get("in-clause-support-optional-named").build("collectionOfIds", Arrays.asList(1));
		assertThat(query.getSql()).containsSequence("(?,?,?,?");

		assertThat(new ArrayList<Object>(query.getParameters())).containsExactly(1, 1, 1, 1);
		List<Dummy> rows = db.findAll(Dummy.class, toQuery(query));
		assertThat(rows).extracting("id", "bar").containsOnly(tuple(1, "1-4"));
	}

	@Test
	public void conditionalParameters() {
		// and foo > ? -- :foo + 1; :foo != null && :foo > 3
		PreparedQuery preparedQuery = rq.get("named-conditional-criterion-parameters");

		Query query = preparedQuery.build("foo", 4);
		assertThat(query.getSql()).containsSequence("and foo > ?");
		assertThat(new ArrayList<Object>(query.getParameters())).containsOnly(5);
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

	@Test
	public void searchExample() {
		PreparedQuery searchQuery = rq.get("search-example");
		DummySearch search = new DummySearch(0, 10, Collections.emptyList());
		Query noCriterionsQuery = searchQuery.build(search);
		assertThat(noCriterionsQuery.getSql()).doesNotContain("like").doesNotContain("LOWER");
		// parameter contains only offset and limit
		assertThat(new ArrayList<Object>(noCriterionsQuery.getParameters())).containsOnly(0, 10);

		search.setBarEq("");
		search.setBarStarts("");
		search.setBarContains("");
		Query noCriterionsQuery2 = searchQuery.build(search);
		assertThat(noCriterionsQuery2.getSql()).doesNotContain("like").doesNotContain("LOWER");
		// parameter contains only offset and limit
		assertThat(new ArrayList<Object>(noCriterionsQuery2.getParameters())).containsOnly(0, 10);

		search.setBarEq("equals");
		search.setBarStarts("STARTING");
		search.setBarContains("Containing");
		Query query = searchQuery.build(search);
		assertThat(query.getSql()).contains("LOWER", "like");
		assertThat(new ArrayList<Object>(query.getParameters())).containsOnly("equals", "STARTING%", "%Containing%", 0,
				10);
	}

	@Test
	public void optionalBlocks() {
		PreparedQuery blockQuery = rq.get("optional-blocks");
		Query noCriterionsQuery = blockQuery.build("addBlock", null);
		assertThat(noCriterionsQuery.getSql()).doesNotContain("inside optional block")
				.doesNotContain("inside nested block");
		assertThat(noCriterionsQuery.getParameters()).isEmpty();

		// bar, addNested variables is needed, so there is error
		assertThatThrownBy(() -> blockQuery.build("addBlock", true)).isInstanceOf(QueryBuildException.class);

		// block is added if addBlock is not null, so will be added even with
		// false value
		Query queryWithBlock = blockQuery.build("addBlock", false, "bar", null, "addNested", null);
		assertThat(queryWithBlock.getSql()).contains("inside optional block").doesNotContain("inside nested block")
				.doesNotContain("dd.foo =").doesNotContain("dd.bar =");
		assertThat(queryWithBlock.getParameters()).isEmpty();

		Query queryWithBlock1 = blockQuery.build("addBlock", false, "bar", "2-5", "addNested", null);
		assertThat(queryWithBlock1.getSql()).contains("inside optional block", "dd.bar = ?")
				.doesNotContain("inside nested block").doesNotContain("dd.foo =");
		assertThat(new ArrayList<Object>(queryWithBlock1.getParameters())).containsOnly("2-5");

		Query queryWithBlock2 = blockQuery.build("addBlock", false, "bar", "2-5", "addNested", false);
		assertThat(queryWithBlock2.getSql()).contains("inside optional block", "dd.bar = ?")
				.doesNotContain("inside nested block").doesNotContain("dd.foo =");
		assertThat(new ArrayList<Object>(queryWithBlock2.getParameters())).containsOnly("2-5");

		// foo variable is needed, so there is error
		assertThatThrownBy(() -> blockQuery.build("addBlock", false, "bar", "2-5", "addNested", true))
				.isInstanceOf(QueryBuildException.class);

		Query queryWithBlock3 = blockQuery.build("addBlock", false, "bar", "2-5", "addNested", true, "foo", 5);
		assertThat(queryWithBlock3.getSql()).contains("inside optional block", "dd.bar = ?", "inside nested block",
				"dd.foo = ?");
		assertThat(new ArrayList<Object>(queryWithBlock3.getParameters())).containsOnly("2-5", 5);

		// MME HSQL has problems with { brackets in comments see:
		// https://sourceforge.net/p/hsqldb/discussion/73674/thread/a586f937/

		/*
		 * List<Dummy> rows = db.findAll(Dummy.class, toQuery(queryWithBlock3));
		 * assertThat(rows).extracting("id", "bar").containsOnly(tuple(2,
		 * "2-5"));
		 */
	}

	@Test
	public void selectAttributes() {
		PreparedQuery blockQuery = rq.get("select-attrs");
		Query attrsQuery = blockQuery.build("passThrough", "XXX", "id", 999);

		assertThat(attrsQuery.getAttributes()).extracting("caching", "removeCachedAfterMin", "passThrough")
				.containsExactly(true, 30, "XXX");
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

	public static class DummySearch extends AbstractSearch<DummySearch> {

		public DummySearch() {
			this(null, null, null);
		}

		public DummySearch(Integer offset, Integer limit, List<String> orderByItems) {
			super(offset, limit, orderByItems);
		}

		private Integer foo;
		private String barEq;
		private String barStarts;
		private String barContains;

		public String getBarEq() {
			return barEq;
		}

		public void setBarEq(String barEq) {
			this.barEq = barEq;
		}

		public String getBarStarts() {
			return barStarts;
		}

		public void setBarStarts(String barStarts) {
			this.barStarts = barStarts;
		}

		public String getBarContains() {
			return barContains;
		}

		public void setBarContains(String barContains) {
			this.barContains = barContains;
		}

		public Integer getFoo() {
			return foo;
		}

		public void setFoo(Integer foo) {
			this.foo = foo;
		}
	}
}
