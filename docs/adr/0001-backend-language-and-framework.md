# 0001. Backend language and framework: Java + Spring Boot

## Status
Accepted

## Context
The assignment lists Node.js, Python, or Java as acceptable backend choices. We needed a
stack that supports: a REST API, structured request validation, a persistence layer,
multipart file upload handling, and a mature testing story (unit + integration), while
staying within the submission guideline of keeping dependencies minimal.

## Decision
Use Java 17 with Spring Boot 3.3 (Maven build).

- Spring Web for the REST layer.
- Spring Data JPA for persistence (see ADR 0003 for the storage engine).
- Bean Validation (`spring-boot-starter-validation`) for request validation instead of
  hand-rolled checks.
- `spring-boot-starter-test` (JUnit 5, Mockito, AssertJ, MockMvc) covers all testing needs
  as a single dependency - no extra test libraries were added.
- Java records are used for all DTOs instead of adding Lombok, keeping the dependency list
  smaller and avoiding annotation-processor configuration.

## Consequences
- More boilerplate than a scripting-language equivalent (explicit getters/constructors on
  JPA entities, since records can't be entities), but the type system catches structural
  mistakes in the resume/JD/match-result flow at compile time.
- Spring Boot's auto-configuration means the dependency count in `pom.xml` stays small
  (6 runtime/compile dependencies) while still getting DI, validation, JSON mapping, and a
  test harness for free.
- Anyone reviewing the repo needs a JDK 17+ and Maven installed to build/run/test it.
