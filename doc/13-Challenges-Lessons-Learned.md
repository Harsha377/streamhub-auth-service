# 13 - Challenges & Lessons Learned

---

# Introduction

Building a production-grade authentication service involves more than writing
business logic.

Throughout the implementation, several real-world challenges were encountered.

Each challenge provided valuable insights into Spring Boot, Spring Security,
Jackson, PostgreSQL, Redis, and production best practices.

This document records those challenges, their root causes, and the solutions.

---

# Challenge 1 - Jackson Library Version Issue

## Problem

During Redis configuration, JSON serialization failed because some Jackson
classes were missing or incompatible with the Spring Boot version.

Example

```
Cannot resolve symbol

JsonMapper

JavaTimeModule

GenericJacksonJsonRedisSerializer
```

Sometimes

```
NoSuchMethodError
```

or

```
ClassNotFoundException
```

also occurred.

---

## Root Cause

The project contained incompatible Jackson versions.

Example

```
Spring Boot

↓

Jackson 2.19

Dependency

↓

Jackson 2.16

Version Conflict
```

Different Jackson modules were using different versions.

---

## Solution

Allow Spring Boot to manage Jackson versions.

Instead of

```xml
<version>...</version>
```

use

```xml
<dependency>

<groupId>com.fasterxml.jackson.core</groupId>

<artifactId>jackson-databind</artifactId>

</dependency>
```

without specifying versions.

Spring Boot automatically imports compatible versions.

---

## Lesson Learned

Always let Spring Boot manage library versions unless there is a strong reason
to override them.

---

# Challenge 2 - Java Time Serialization

## Problem

Redis could not serialize

```
Instant

LocalDateTime

OffsetDateTime
```

Example

```
Java 8 date/time type

java.time.Instant

not supported
```

---

## Solution

Register

```
JavaTimeModule
```

Disable

```
WRITE_DATES_AS_TIMESTAMPS
```

Example

```java
ObjectMapper mapper = JsonMapper.builder()

.addModule(new JavaTimeModule())

.build();

mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
```

---

## Lesson Learned

Jackson does not automatically support Java Time types.

Always register

```
JavaTimeModule
```

---

# Challenge 3 - Redis Serialization

## Problem

Objects were stored as

```
\xAC\xED...
```

instead of readable JSON.

---

## Root Cause

Default Java Serialization.

---

## Solution

Configure

```
GenericJacksonJsonRedisSerializer
```

for values

and

```
StringRedisSerializer
```

for keys.

---

## Lesson Learned

Never use Java Serialization for Redis in production.

JSON serialization is portable, readable, and language independent.

---

# Challenge 4 - PostgreSQL Version Support

## Problem

Flyway failed with

```
Unsupported Database:

PostgreSQL 18.x
```

---

## Root Cause

Flyway version did not yet officially support PostgreSQL 18.

---

## Solution

Upgrade Flyway

or

Use a supported PostgreSQL version.

---

## Lesson Learned

Always verify compatibility between

- Spring Boot
- Flyway
- PostgreSQL

before upgrading.

---

# Challenge 5 - JWT Cookie Authentication

## Problem

JWT Authentication worked in Postman

but failed in the browser.

---

## Root Cause

Cookie configuration

```
SameSite

Secure

Domain

Path
```

were incorrect.

---

## Solution

Configure

```
HttpOnly

Secure

SameSite=None

Path=/
```

and enable CORS credentials.

---

## Lesson Learned

JWT authentication using cookies requires both backend and frontend
configuration.

---

# Challenge 6 - Spring Security Flow

## Problem

Initially it was difficult to understand

```
Which class executes first?

Why controller is not called immediately?

Where Authentication is created?
```

---

## Solution

Studied the complete

Spring Security Filter Chain

Request

↓

Filter Chain

↓

JwtAuthenticationFilter

↓

SecurityContextHolder

↓

Controller

---

## Lesson Learned

Understanding the Security Filter Chain is more important than memorizing
Spring Security annotations.

---

# Challenge 7 - Refresh Token Rotation

## Problem

Initially the same Refresh Token was reused.

This created a security weakness.

---

## Solution

Implemented

Refresh Token Rotation

Every refresh

↓

Generate New Refresh Token

↓

Replace Hash

↓

Invalidate Old Token

---

## Lesson Learned

Refresh Tokens should never be reused in production.

---

# Challenge 8 - Device Detection

## Problem

Initially

```
Unknown Device
```

was stored.

---

## Solution

Implemented

User-Agent Parsing

using

YAUAA

---

## Lesson Learned

Never rely on the frontend to provide device information.

Always parse the User-Agent header on the server.

---

# Challenge 9 - Session Activity Tracking

## Problem

Updating

```
last_activity_at
```

for every request created unnecessary database writes.

---

## Solution

Throttle updates.

Only update after a configured interval.

---

## Lesson Learned

Correct functionality is not enough.

Performance and scalability must also be considered.

---

# Challenge 10 - Atomic Database Updates

## Problem

Initially

```
find()

↓

modify()

↓

save()
```

was used.

This required two database operations.

---

## Solution

Replaced with

```
UPDATE

WHERE

session_id=?

AND

user_id=?
```

using a single atomic SQL statement.

---

## Lesson Learned

Whenever possible,

prefer atomic database updates over

read-modify-write operations.

---

# General Lessons Learned

✓ Let Spring Boot manage dependency versions.

✓ Read release notes before upgrading libraries.

✓ Understand Spring Security internals.

✓ Prefer Stateless Authentication.

✓ Store only hashed Refresh Tokens.

✓ Design for scalability from the beginning.

✓ Separate responsibilities into dedicated services.

✓ Never trust client-provided security data.

✓ Optimize database writes.

✓ Think about production, not just functionality.

---

# Conclusion

The implementation of the StreamHub Authentication Service involved solving
multiple real-world engineering challenges.

Each challenge improved the overall architecture, security, scalability,
and maintainability of the project.

Understanding these challenges is as valuable as understanding the final
implementation because they represent the practical experience gained while
building a production-ready authentication system.