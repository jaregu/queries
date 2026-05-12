# Jaregu Queries

[![CI](https://github.com/jaregu/queries/actions/workflows/ci.yml/badge.svg)](https://github.com/jaregu/queries/actions/workflows/ci.yml)
[![Maven Central](https://img.shields.io/maven-central/v/com.jaregu/queries.svg?label=Maven%20Central)](https://central.sonatype.com/artifact/com.jaregu/queries)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

> **SQL templating for Java.** Keep your SQL in `.sql` files. Bind parameters by name. Drop unused `WHERE` clauses with one inline comment. Let a typed interface stand in for the boilerplate.

```sql
-- search_jobs
SELECT id, name FROM job
WHERE 1 = 1
    AND name LIKE '%' /* '%' + :name + '%'; :name != null && :name != '' */
    AND id = 1        -- :id
ORDER BY id DESC
LIMIT :offset, :limit
```

```java
@QueriesSourceClass
public interface JobDAO {

    @QueryRef("search_jobs") @FindAll(Job.class)
    List<Job> search(JobsSearch search);
}
```

That's it. The conditional in `/* ... */` drops out when `name` is null or empty. The `-- :id` rewrites the literal `1` to a bound parameter. No JPQL, no DSL, no string-building.

---

## Why

The Java ecosystem has a gap between **"raw `JdbcClient` everywhere"** (painful at scale) and **"full ORM with magic"** (heavy, opinionated). Jaregu Queries lives there.

- **External `.sql` files** — your DBA can read them, your IDE highlights them, your DB tooling validates them.
- **Conditional SQL** — `/* ... */` and `-- ...` comments mark optional clauses driven by parameter expressions, so one query handles many filter combinations without dynamic string concat.
- **Typed proxy DAOs** — declare an interface, annotate methods with `@QueryRef`, let the library wire the SQL to your beans.
- **Pluggable execution layer** — works with Spring's `JdbcClient`, Dalesbred, or roll your own.
- **One transitive dependency** — `slf4j-api`. Every integration (Spring, Guice, Hikari, Caffeine, Dalesbred) is `compileOnly` — you pay only for what you import.

If you like writing SQL but hate writing `PreparedStatement` plumbing, this is for you.

---

## Requirements

| | Version |
|---|---|
| **Java runtime** | **17+** (tested on JDK 17 and 21) |
| **Spring Boot starter** | requires Spring Boot **4.0+** (which bundles Spring Framework **7.0+**) |
| **Plain Spring (no starter)** | Spring Framework **7.0+** with `JdbcClient` (Spring 6.1+ technically works, but the starter requires 7) |
| **Guice integration** | Guice **7.0+** (Jakarta packages) |
| **HikariCP integration** | HikariCP **7.0+** |
| **Dalesbred integration** | Dalesbred **1.3.6+** |

Jaregu Queries 2.x is fully on Jakarta EE — `jakarta.*` packages, no legacy `javax.*`.

For Java 11 + Spring Boot 3.x compatibility, stay on Jaregu Queries **1.4.x**.

---

## Quick-start — Spring Boot

Add the starter:

```groovy
dependencies {
    implementation 'com.jaregu:queries-spring-boot-starter:2.0.0'
    runtimeOnly 'org.hsqldb:hsqldb'   // or your favourite JDBC driver
}
```

Configure your DataSource in `application.properties` the usual way:

```properties
spring.datasource.url=jdbc:hsqldb:mem:demo
spring.datasource.username=SA
```

### 1. The entity

```java
@Table(name = "job")
@Data @NoArgsConstructor @AllArgsConstructor @Builder(toBuilder = true)
public class Job {

    // @Column is read in BOTH directions: by the entityFieldGenerator SQL
    // macro AND by the result-row mapper. So columns named
    // `usr_first_nm` ↔ `firstName` work out of the box — no convention
    // required between SQL and Java naming.
    @Column(name = "id")                private Integer id;
    @Column(name = "name")              private String name;
    @Column(name = "short_description") private String shortDescription;
    @Column(name = "version")           private Integer version;
    @Column(name = "created")           private Instant created;
    @Column(name = "modified")          private Instant modified;
}
```

Records work too — Jaregu Queries handles both Java 17 records and JavaBeans, and falls back to snake_case → camelCase for any column without an explicit `@Column`.

### 2. The SQL — `JobDAO.sql` (next to `JobDAO.java`)

```sql
-- create
CREATE TABLE job (
    id int NOT NULL PRIMARY KEY,
    name varchar(50) NOT NULL,
    short_description varchar(500),
    version integer NOT NULL,
    created timestamp NOT NULL,
    modified timestamp NOT NULL
);

-- insert
INSERT INTO job (/* entityFieldGenerator(template = 'column' entityClass = 'Job' excludeColumns = 'version, created, modified') */
    , version, created, modified)
VALUES (/* entityFieldGenerator(template = 'value' entityClass = 'Job' excludeColumns = 'version, created, modified') */
    , 1, NOW(), NOW());

-- update
UPDATE job SET
    -- entityFieldGenerator(template = 'columnAndValue' entityClass = 'Job' excludeColumns = 'id, version, created, modified')
    , version = version + 1
    , modified = NOW()
WHERE id = :id AND version = :version;

-- search
SELECT -- entityFieldGenerator(template = 'column' entityClass = 'Job' alias = 'j')
FROM job j
WHERE 1 = 1
    AND j.id = 1                                                    -- :id
    AND j.name LIKE '%' /* '%' + :name + '%'; :name != null && :name != '' */
```

### 3. The DAO interface

```java
@QueriesSourceClass
public interface JobDAO {

    @QueryRef("create") @ExecuteUpdate
    void createTable();

    @QueryRef("insert") @ExecuteUpdate(unique = true)
    void insert(Job job);

    @QueryRef("update") @ExecuteUpdate(unique = true)
    void update(Job job);

    @QueryRef(value = "search", toSorted = true, toPaged = true)
    @FindAll(Job.class)
    List<Job> search(JobsSearch search);

    @QueryRef(value = "search") @FindOptional(Job.class)
    Optional<Job> findOne(JobsSearch search);
}
```

### 4. Wire it

```java
@SpringBootApplication
@QueriesScan(basePackageClasses = JobDAO.class)
public class App {
    public static void main(String[] args) { SpringApplication.run(App.class, args); }
}
```

`@QueriesScan` accepts either:

- `basePackageClasses = { JobDAO.class }` — type-safe, refactor-resistant
- `basePackages = { "com.example.dao" }` — string-based package names
- both omitted → scans the package of the annotated class (and subpackages)

It scans for:

- **`@QueriesSourceClass` interfaces** → registered as proxy beans + their backing `QueriesSource`
- **`@Table` classes** → registered as `QueriesEntity` beans so the `entityFieldGenerator` SQL macro can find them

### 5. Use it

```java
@Service
@RequiredArgsConstructor
public class Jobs {

    private final JobDAO dao;

    @Transactional
    public void seed() {
        dao.insert(Job.builder().id(1).name("first").build());
        dao.insert(Job.builder().id(2).name("second").build());
    }

    public List<Job> findByName(String name, int offset, int limit) {
        return dao.search(JobsSearch.builder()
                .name(name)
                .offset(offset)
                .limit(limit)
                .orderBy(List.of("name asc"))
                .build());
    }
}
```

Spring's `@Transactional` works transparently — `JdbcClient` participates in the active transaction via Spring's `DataSourceUtils`.

---

## Pagination and sorting

Have your search class implement `OrderableSearch` and `PageableSearch`:

```java
@Value @Builder @With
public class JobsSearch implements OrderableSearch<JobsSearch>, PageableSearch<JobsSearch> {

    private Integer id;
    private String name;

    private Integer limit;
    private Integer offset;
    @Default private List<String> orderBy = List.of();
}
```

Then mark the DAO method with `toSorted = true` and/or `toPaged = true`:

```java
@QueryRef(value = "search", toSorted = true, toPaged = true)
@FindAll(Job.class)
List<Job> search(JobsSearch search);
```

The library wraps your SQL with the dialect's `ORDER BY ... LIMIT ? OFFSET ?` template. Default dialect emits `LIMIT ? OFFSET ?` (works in HSQL, MySQL, PostgreSQL). Pick a different dialect via a `QueriesConfigurator` bean — see [Customization](#customization).

---

## Tuning the builder

Drop a `QueriesConfigurator` bean for anything you'd normally set on `Queries.Builder`:

```java
@Bean QueriesConfigurator pgDialect() {
    return b -> b.dialect(Dialects.postgreSQL());
}

@Bean QueriesConfigurator cache() {
    return b -> b.cache(CaffeineCache.forSize(1000));
}
```

For entities with a non-default SQL alias:

```java
@Bean QueriesEntity jobEntity() {
    return QueriesEntity.of(Job.class, "j");
}
```

<a id="customization"></a>

---

## Beyond Spring Boot

The `queries` JAR works the same in any container. The starter is just convenient wiring.

### Plain Spring (non-Boot)

```java
@Configuration
@QueriesScan(basePackageClasses = JobDAO.class) // optional, or @Bean each DAO manually
public class QueriesConfig {

    @Bean JdbcClient jdbcClient(DataSource ds) { return JdbcClient.create(ds); }

    @Bean
    Queries queries(JdbcClient jdbc, List<QueriesSource> sources) {
        var b = Queries.builder();
        sources.forEach(b::source);
        return SpringQueriesMappers.register(b, jdbc).build();
    }
}
```

### Guice

```groovy
implementation 'com.jaregu:queries:2.0.0'
implementation 'com.google.inject:guice:7.+'
implementation 'org.dalesbred:dalesbred:1.3.+'   // or wire SpringQueriesMappers
implementation 'com.zaxxer:HikariCP:7.+'
```

```java
Injector injector = Guice.createInjector(
        QueriesModule.queriesModule(),
        DalesbredModule.create(),
        HikariModule.create(myConnectionPoolConfig),
        QueriesModule.proxyModule(JobDAO.class),
        QueriesModule.entityModule(Job.class));

JobDAO dao = injector.getInstance(JobDAO.class);
```

### No DI

```java
Queries queries = Queries.builder()
        .sourceOfClass(JobDAO.class)
        .entity(Job.class)
        .build();

JobDAO dao = queries.proxy(JobDAO.class);
```

You still need *something* to execute the resulting `Query` objects — call `Queries.builder().mapper(...)` to register your own factory, or use one of the bundled integrations (`SpringQueriesMappers`, `DalesbredModule`).

---

## SQL templating reference

### Conditional clauses

The full form is a two-part comment: `replacement; condition`. Replacement only happens when the condition is true. Otherwise the original `WHERE` term stays.

```sql
AND j.name LIKE '%foo%' /* '%' + :name + '%'; :name != null && :name != '' */
```

A single-line comment after a literal value rewrites that literal to a parameter:

```sql
AND j.id = 1   -- :id
```

### IN clauses (collection binding)

Bind a collection or array as a single parameter — the binder expands it:

```sql
AND j.status IN (:statuses)
```

### Entity field generation

Generate column lists from your `@Table` / `@Column`-annotated entity at compile time:

```sql
-- column list with optional table alias
SELECT /* entityFieldGenerator(template = 'column' entityClass = 'Job' alias = 'j') */
FROM job j

-- ':named, :params' for INSERT VALUES
VALUES (/* entityFieldGenerator(template = 'value' entityClass = 'Job') */)

-- 'col = :col, col2 = :col2' for UPDATE SET
SET -- entityFieldGenerator(template = 'columnAndValue' entityClass = 'Job' excludeColumns = 'id')
```

Templates: `column`, `value`, `columnAndValue`. Options: `entityClass`, `alias`, `excludeColumns`.

---

## Versioning

Versions come from git tags via [Reckon](https://github.com/ajoberstar/reckon). To cut release 2.1.0, tag and push:

```bash
git tag v2.1.0
git push origin v2.1.0
```

GitHub Actions builds, signs, and publishes to Maven Central automatically. Every push to `master` between releases publishes a `<next>-SNAPSHOT` to the snapshot repo at `https://central.sonatype.com/repository/maven-snapshots/`.

---

## License

[Apache License 2.0](LICENSE.md).
