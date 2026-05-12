package com.jaregu.database.queries.ext.spring;

/**
 * Test row record used by {@link SpringFactoriesIntegrationTest}. Plain Java 17
 * record — JdbcClient's default row mapper binds it via the canonical
 * constructor.
 */
public record SpringTestItem(Integer id, String name, String description) {
}
