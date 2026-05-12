package com.jaregu.database.queries.springboot;

import com.jaregu.database.queries.annotation.Column;
import com.jaregu.database.queries.annotation.Table;

/**
 * Minimal entity used by {@link QueriesAutoConfigurationTest} to verify the
 * scanner picks up {@code @Table} classes and registers them as
 * {@link QueriesEntity} beans.
 */
@Table(name = "starter_entity")
public class StarterEntity {

	@Column(name = "id")
	private Integer id;

	@Column(name = "name")
	private String name;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
