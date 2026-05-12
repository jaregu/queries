package com.jaregu.database.queries.ext.spring;

import java.util.List;
import java.util.Optional;

import com.jaregu.database.queries.proxy.ExecuteUpdate;
import com.jaregu.database.queries.proxy.FindAll;
import com.jaregu.database.queries.proxy.FindOptional;
import com.jaregu.database.queries.proxy.FindUnique;
import com.jaregu.database.queries.proxy.QueriesSourceClass;
import com.jaregu.database.queries.proxy.QueryParam;
import com.jaregu.database.queries.proxy.QueryRef;

/**
 * Proxy interface exercised by {@link SpringFactoriesIntegrationTest}. Bound
 * to {@code SpringTestDAO.sql} via {@link QueriesSourceClass}. The same
 * annotations as in the README work unchanged — only the registered factory
 * implementations differ.
 */
@QueriesSourceClass
public interface SpringTestDAO {

	@QueryRef("create_table")
	@ExecuteUpdate
	void createTable();

	@QueryRef("drop_table")
	@ExecuteUpdate
	void dropTable();

	@QueryRef("insert")
	@ExecuteUpdate(unique = true)
	void insert(SpringTestItem item);

	@QueryRef("update_name")
	@ExecuteUpdate(unique = true)
	void updateName(@QueryParam("id") Integer id, @QueryParam("name") String name);

	@QueryRef("delete")
	@ExecuteUpdate(unique = true)
	void delete(@QueryParam("id") Integer id);

	@QueryRef("find_all")
	@FindAll(SpringTestItem.class)
	List<SpringTestItem> findAll();

	@QueryRef("find_by_id")
	@FindUnique(SpringTestItem.class)
	SpringTestItem getById(@QueryParam("id") Integer id);

	@QueryRef("find_by_id")
	@FindOptional(SpringTestItem.class)
	Optional<SpringTestItem> findById(@QueryParam("id") Integer id);

	@QueryRef("count")
	@FindUnique(Integer.class)
	Integer count();
}
