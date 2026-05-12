package com.jaregu.database.queries.springboot;

import java.util.List;

import com.jaregu.database.queries.ext.dalesbred.ExecuteUpdate;
import com.jaregu.database.queries.ext.dalesbred.FindAll;
import com.jaregu.database.queries.ext.dalesbred.FindUnique;
import com.jaregu.database.queries.proxy.QueriesSourceClass;
import com.jaregu.database.queries.proxy.QueryRef;

@QueriesSourceClass
public interface StarterTestDAO {

	@QueryRef("create_table")
	@ExecuteUpdate
	void createTable();

	@QueryRef("insert")
	@ExecuteUpdate(unique = true)
	void insert(StarterItem item);

	@QueryRef("find_all")
	@FindAll(StarterItem.class)
	List<StarterItem> findAll();

	@QueryRef("count")
	@FindUnique(Integer.class)
	Integer count();
}
