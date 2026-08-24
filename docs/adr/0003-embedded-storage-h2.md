# 0003. Storage: embedded, file-backed H2 via Spring Data JPA

## Status
Accepted

## Context
The assignment requires "database storage for parsed resumes." The submission guidelines
ask for a repo that "runs without errors" out of the box and discourage adding unnecessary
infrastructure. Requiring a reviewer to install and start PostgreSQL/MySQL/Mongo just to run
the grading demo is friction the assignment doesn't call for.

## Decision
Use H2 in file mode (`jdbc:h2:file:./data/resumescreener`) as the datastore, accessed
through Spring Data JPA repositories. `data/` is gitignored, so the database file is never
committed and each clone starts empty.

Tests use a separate H2 in-memory profile (`application-test.yml`,
`jdbc:h2:mem:testdb`) with `ddl-auto: create-drop`, so the test suite never touches the
developer's local `data/` file and every test run starts from a clean schema.

## Consequences
- `mvn spring-boot:run` works immediately after a clone with zero external services.
- H2 is a genuine relational database (not a mock), so the JPA mappings, queries
  (`findByJobDescriptionIdOrderByScoreDesc`), and cascades are exercised for real in both
  manual runs and integration tests.
- If this were ever deployed for concurrent multi-user production use, H2 would need to be
  swapped for Postgres/MySQL - the JPA repository layer already isolates that change to the
  `datasource` config, not the application code.
