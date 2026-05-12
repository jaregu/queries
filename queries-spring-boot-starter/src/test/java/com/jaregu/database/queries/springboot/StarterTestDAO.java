package com.jaregu.database.queries.springboot;

import java.util.List;

import com.jaregu.database.queries.proxy.ExecuteUpdate;
import com.jaregu.database.queries.proxy.FindAll;
import com.jaregu.database.queries.proxy.FindUnique;
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

	@QueryRef(value = "search_paged", toSorted = true, toPaged = true)
	@FindAll(StarterItem.class)
	List<StarterItem> searchPaged(StarterSearch search);
}
