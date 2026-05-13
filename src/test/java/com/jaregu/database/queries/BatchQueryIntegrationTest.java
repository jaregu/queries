package com.jaregu.database.queries;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.jaregu.database.queries.building.ParametersResolver;
import com.jaregu.database.queries.building.Query;
import com.jaregu.database.queries.compiling.PreparedQuery;

/**
 * End-to-end coverage for the {@code (batch)} marker — the parser → compiler →
 * SQL-render pipeline must keep all {@code ;} terminators inside a batch
 * query so the resulting SQL is a valid multi-statement batch the JDBC driver
 * can ship as one literal string.
 */
public class BatchQueryIntegrationTest {

	private static final SourceId SOURCE = SourceId.ofId("batch.integration");

	@Test
	public void singleBatchQueryRendersAllStatementsAndSemicolons() {
		String sql = "-- insertWithOutput (batch)\n"
				+ "DECLARE @ids TABLE (u_nr BIGINT);\n"
				+ "INSERT INTO par_k (col1) OUTPUT inserted.u_nr INTO @ids VALUES (:p1);\n"
				+ "SELECT u_nr FROM @ids";

		Queries queries = Queries.builder().sourceOfContent(SOURCE, () -> sql).build();

		PreparedQuery prepared = queries.get(SOURCE.getQueryId("insertWithOutput"));
		assertThat(prepared).isNotNull();

		Map<String, Object> params = new HashMap<>();
		params.put("p1", 7L);
		Query built = prepared.build(ParametersResolver.ofMap(params));

		String rendered = built.getSql();

		// Each non-terminal statement is followed by its ; separator — without
		// this the JDBC driver wouldn't recognize it as a multi-statement
		// batch and SQL Server's table variable would not survive between
		// statements.
		assertThat(rendered).contains("DECLARE @ids TABLE (u_nr BIGINT);");
		assertThat(rendered).contains("OUTPUT inserted.u_nr INTO @ids VALUES (?);");
		assertThat(rendered).contains("SELECT u_nr FROM @ids");

		// Exactly two ; separators — between the three statements — must
		// appear in the final SQL string. This is the primary contract of
		// the (batch) marker.
		assertThat(countOccurrences(rendered, ";")).isEqualTo(2);

		// Statement ordering is preserved in the rendered SQL.
		int declareAt = rendered.indexOf("DECLARE @ids");
		int insertAt = rendered.indexOf("INSERT INTO par_k");
		int selectAt = rendered.indexOf("SELECT u_nr FROM @ids");
		assertThat(declareAt).isGreaterThanOrEqualTo(0);
		assertThat(insertAt).isGreaterThan(declareAt);
		assertThat(selectAt).isGreaterThan(insertAt);

		assertThat(built.getParameters()).hasSize(1);
		assertThat(built.getParameters().get(0)).isEqualTo(7L);
	}

	private static int countOccurrences(String haystack, String needle) {
		int count = 0;
		int idx = 0;
		while ((idx = haystack.indexOf(needle, idx)) != -1) {
			count++;
			idx += needle.length();
		}
		return count;
	}

	@Test
	public void mixedFileWithBatchAndRegularQueriesAllResolveIndependently() {
		String sql = "-- regularInsert\n"
				+ "INSERT INTO foo (id) VALUES (:id);\n"
				+ "-- batched (batch)\n"
				+ "DECLARE @t TABLE (u BIGINT);\n"
				+ "INSERT INTO @t VALUES (:id);\n"
				+ "SELECT u FROM @t WHERE u = :id;\n"
				+ "-- regularSelect\n"
				+ "SELECT * FROM foo WHERE id = :id";

		Queries queries = Queries.builder().sourceOfContent(SOURCE, () -> sql).build();

		PreparedQuery regularInsert = queries.get(SOURCE.getQueryId("regularInsert"));
		PreparedQuery batched = queries.get(SOURCE.getQueryId("batched"));
		PreparedQuery regularSelect = queries.get(SOURCE.getQueryId("regularSelect"));

		Map<String, Object> params = new HashMap<>();
		params.put("id", 42L);

		String regularInsertSql = regularInsert.build(ParametersResolver.ofMap(params)).getSql();
		String batchedSql = batched.build(ParametersResolver.ofMap(params)).getSql();
		String regularSelectSql = regularSelect.build(ParametersResolver.ofMap(params)).getSql();

		// Regular queries have their terminating ; stripped by the parser.
		assertThat(regularInsertSql).contains("INSERT INTO foo (id) VALUES (?)");
		assertThat(regularInsertSql).doesNotContain(";");
		assertThat(regularInsertSql).doesNotContain("DECLARE");

		// Batch keeps every ; between its three statements — including the
		// trailing one that preceded the next named query in the source.
		assertThat(batchedSql).contains("DECLARE @t TABLE (u BIGINT);");
		assertThat(batchedSql).contains("INSERT INTO @t VALUES (?);");
		assertThat(batchedSql).contains("SELECT u FROM @t WHERE u = ?;");
		assertThat(batchedSql).doesNotContain("SELECT * FROM foo");
		// Three ; characters survive in the rendered batch SQL.
		assertThat(countOccurrences(batchedSql, ";")).isEqualTo(3);

		assertThat(regularSelectSql).contains("SELECT * FROM foo WHERE id = ?");
		assertThat(regularSelectSql).doesNotContain(";");

		// All three queries bound the same :id param successfully — and the
		// batch one bound it twice (one per statement that referenced it).
		assertThat(regularInsert.build(ParametersResolver.ofMap(params)).getParameters()).hasSize(1);
		assertThat(batched.build(ParametersResolver.ofMap(params)).getParameters()).hasSize(2);
		assertThat(regularSelect.build(ParametersResolver.ofMap(params)).getParameters()).hasSize(1);
	}

	@Test
	public void batchQueryAtEofWithNoTrailingSemicolon() {
		String sql = "-- atEof (batch)\n"
				+ "DECLARE @t TABLE (u BIGINT);\n"
				+ "INSERT INTO @t VALUES (:u);\n"
				+ "SELECT u FROM @t";

		Queries queries = Queries.builder().sourceOfContent(SOURCE, () -> sql).build();

		PreparedQuery prepared = queries.get(SOURCE.getQueryId("atEof"));
		Map<String, Object> params = new HashMap<>();
		params.put("u", 99L);
		String rendered = prepared.build(ParametersResolver.ofMap(params)).getSql();

		assertThat(rendered).contains("DECLARE @t TABLE (u BIGINT);");
		assertThat(rendered).contains("INSERT INTO @t VALUES (?);");
		assertThat(rendered).contains("SELECT u FROM @t");
		assertThat(rendered.stripTrailing()).doesNotEndWith(";");
		// Two ; characters — only between statements, no synthetic trailing one.
		assertThat(countOccurrences(rendered, ";")).isEqualTo(2);
	}
}
