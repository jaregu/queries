package com.jaregu.database.queries.springboot;

import java.util.List;

import com.jaregu.database.queries.proxy.ExecuteUpdate;
import com.jaregu.database.queries.proxy.FindAll;
import com.jaregu.database.queries.proxy.QueriesSourceClass;
import com.jaregu.database.queries.proxy.QueryRef;

/**
 * Exercises the {@code entityFieldGenerator(...)} SQL macro end-to-end:
 * every query in {@code StarterEntityDAO.sql} resolves its column list by
 * reflecting over {@link StarterEntity}'s {@code @Column} fields. Tests in
 * {@link QueriesAutoConfigurationTest} verify the full Spring Boot pipeline:
 * {@code @Table} discovery via {@link QueriesScan}, {@link QueriesEntity}
 * bean registration, macro expansion at query compile time, and row mapping
 * back via {@code JdbcClient}'s {@code SimplePropertyRowMapper}.
 */
@QueriesSourceClass
public interface StarterEntityDAO {

	@QueryRef("create_table")
	@ExecuteUpdate
	void createTable();

	@QueryRef("drop_table")
	@ExecuteUpdate
	void dropTable();

	@QueryRef("insert")
	@ExecuteUpdate(unique = true)
	void insert(StarterEntity entity);

	@QueryRef("update_by_id")
	@ExecuteUpdate(unique = true)
	void update(StarterEntity entity);

	@QueryRef("find_all")
	@FindAll(StarterEntity.class)
	List<StarterEntity> findAll();
}
