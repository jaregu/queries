package com.jaregu.database.queries.ext.spring;

import java.util.List;
import java.util.Optional;

import com.jaregu.database.queries.proxy.ExecuteUpdate;
import com.jaregu.database.queries.proxy.FindAll;
import com.jaregu.database.queries.proxy.FindOptional;
import com.jaregu.database.queries.proxy.QueriesSourceClass;
import com.jaregu.database.queries.proxy.QueryParam;
import com.jaregu.database.queries.proxy.QueryRef;

@QueriesSourceClass
public interface ColumnAwareDAO {

	@QueryRef("create_table") @ExecuteUpdate
	void createTable();

	@QueryRef("drop_table") @ExecuteUpdate
	void dropTable();

	@QueryRef("insert") @ExecuteUpdate(unique = true)
	void insert(ColumnAwareUser user);

	@QueryRef("find_all") @FindAll(ColumnAwareUser.class)
	List<ColumnAwareUser> findAll();

	@QueryRef("find_by_id") @FindOptional(ColumnAwareUser.class)
	Optional<ColumnAwareUser> findById(@QueryParam("id") Integer id);
}
