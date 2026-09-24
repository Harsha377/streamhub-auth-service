# 17 - Production Best Practices & Interview Notes

---

# 1. Introduction

Building a project that works is one achievement.

Building a project that can run safely in production, scale to millions of
users, and be maintained by multiple developers is another.

This chapter summarizes the engineering practices, architectural decisions,
security considerations, and interview concepts learned while building the
StreamHub Authentication Service.

---

# 2. Production Mindset

Never ask

> "Does it work?"

Instead ask

✓ Is it secure?

✓ Is it scalable?

✓ Is it maintainable?

✓ Can another developer understand it?

✓ Can it handle failures?

✓ Can it be monitored?

✓ Can it be deployed safely?

A production engineer thinks beyond functionality.

---

# 3. Folder Structure

A clean project structure improves readability.

Example

```
src/main/java

config/

controller/

service/

repository/

entity/

dto/

mapper/

security/

exception/

scheduler/

util/

constants/

validation/
```

Each package should have one responsibility.

---

# 4. Layer Responsibilities

## Controller

Responsibilities

- Receive HTTP Requests
- Validate DTO
- Call Service
- Return Response

Never

❌ Business Logic

❌ Database Access

---

## Service

Responsibilities

- Business Logic
- Transaction Management
- Validation
- Security Rules

Never

❌ HTTP Response Building

❌ SQL Queries

---

## Repository

Responsibilities

- Database Access

Never

❌ Business Logic

---

## Entity

Responsibilities

- Database Mapping

Never

❌ Business Logic

---

## DTO

Responsibilities

Transfer Data

Never

Expose Entity directly.

---

# 5. Why DTO Instead of Entity?

Bad

```
Controller

↓

User Entity

↓

Frontend
```

Problems

- Exposes internal fields

- Tight coupling

- Security risk

Instead

```
Entity

↓

Mapper

↓

DTO

↓

Client
```

Benefits

✓ Secure

✓ Flexible

✓ Independent

---

# 6. Why Global Exception Handling?

Instead of

```
try

catch

try

catch

try

catch
```

Use

```
GlobalExceptionHandler
```

Benefits

✓ Consistent API

✓ Cleaner Controllers

✓ Easier Maintenance

---

# 7. Why JWT Instead of HTTP Session?

HTTP Session

```
Memory

↓

Not Scalable
```

JWT

```
Stateless

↓

Scalable

↓

Microservices
```

JWT is ideal for distributed systems.

---

# 8. Why Refresh Token Rotation?

Without Rotation

```
Refresh Token

↓

Reuse Forever
```

Risk

↓

Token Theft

With Rotation

```
Refresh

↓

New Refresh Token

↓

Old Token Invalid
```

Higher Security

---

# 9. Why Hash Refresh Tokens?

Never store

```
Raw Token
```

Instead

```
SHA-256

↓

Hash

↓

Database
```

Benefits

✓ Database Leak Protection

✓ Similar to Password Hashing

---

# 10. Why Session Management?

JWT alone cannot

✓ Logout Device

✓ Revoke Login

✓ Track Devices

✓ Activity Tracking

The

```
user_sessions
```

table solves all of these.

---

# 11. Why Device Detection?

Users expect

```
Google

↓

Your Devices
```

Device Detection enables

✓ Device List

✓ Login Notifications

✓ Security Monitoring

---

# 12. Why Atomic SQL Updates?

Instead of

```
find()

↓

modify()

↓

save()
```

Use

```
UPDATE

WHERE
```

Benefits

✓ Faster

✓ One Query

✓ No Race Conditions

✓ Better Performance

---

# 13. Why HttpOnly Cookies?

Instead of

```
Local Storage
```

Use

```
HttpOnly Cookies
```

Benefits

✓ JavaScript Cannot Read

✓ Better XSS Protection

✓ Browser Sends Automatically

---

# 14. Logging Best Practices

Log

✓ Login

✓ Logout

✓ OTP Sent

✓ Refresh

✓ Failed Authentication

Never Log

❌ Passwords

❌ OTP

❌ JWT

❌ Refresh Token

❌ Secrets

---

# 15. Security Best Practices

✓ Short Access Token

✓ Refresh Rotation

✓ Hash Refresh Token

✓ HTTPS

✓ HttpOnly Cookies

✓ Secure Cookies

✓ Session Validation

✓ Input Validation

✓ DTO

✓ Global Exception Handling

Future

□ MFA

□ Replay Detection

□ Trusted Devices

---

# 16. Performance Best Practices

✓ Atomic SQL

✓ Redis for OTP

✓ Session Activity Throttling

✓ Pagination

✓ Connection Pooling

✓ Indexes

✓ Background Cleanup

Future

- Redis Cluster

- Read Replicas

- Kafka

---

# 17. Database Best Practices

✓ UUID Session ID

✓ Foreign Keys

✓ Index Frequently Used Columns

✓ Flyway Migrations

✓ Soft Revocation

✓ Cleanup Scheduler

Avoid

❌ SELECT *

❌ Missing Indexes

❌ Raw SQL Everywhere

---

# 18. API Design Best Practices

Use

```
GET

POST

PUT

PATCH

DELETE
```

Properly.

Examples

```
GET

/auth/sessions
```

```
DELETE

/auth/sessions/{id}
```

Avoid

```
POST

/deleteSession
```

---

# 19. Spring Boot Best Practices

✓ Constructor Injection

✓ @ConfigurationProperties

✓ Validation

✓ Global Exception Handler

✓ Profiles

✓ Transaction Management

✓ Separate Config Classes

Avoid

❌ Field Injection

❌ Hardcoded Values

❌ Utility God Classes

---

# 20. Spring Security Best Practices

✓ Stateless Authentication

✓ JwtAuthenticationFilter

✓ SecurityContextHolder

✓ AuthenticationEntryPoint

✓ AccessDeniedHandler

✓ Method Security (Future)

---

# 21. Redis Best Practices

Store only

Temporary Data

Examples

OTP

Verification Flag

Rate Limits

Avoid

Permanent Business Data

---

# 22. PostgreSQL Best Practices

Store

✓ Users

✓ Sessions

✓ Audit

✓ Login History

Don't Store

OTP

Verification Cache

Session Cache

Those belong in Redis.

---

# 23. Production Checklist

Authentication

✓ JWT

✓ Refresh Rotation

✓ Session Validation

Security

✓ HTTPS

✓ HttpOnly

✓ Secure Cookie

✓ Validation

Performance

✓ Redis

✓ Atomic SQL

✓ Scheduler

Maintainability

✓ DTO

✓ Exception Handling

✓ Service Layer

✓ Repository Layer

---

# 24. Common Interview Questions

## Spring Boot

Q. Why constructor injection?

Q. Why DTO?

Q. Why Flyway?

Q. Why Profiles?

---

## Spring Security

Q. Explain Spring Security Flow.

Q. What is SecurityContextHolder?

Q. Difference between Authentication and Authorization?

Q. Difference between 401 and 403?

---

## JWT

Q. Why JWT?

Q. Why Refresh Token?

Q. Why Session ID?

Q. Why Hash Refresh Token?

Q. Why Rotate Refresh Token?

---

## Redis

Q. Why Redis?

Q. Why not PostgreSQL?

Q. Why TTL?

---

## Session Management

Q. Why user_sessions table?

Q. Why revoke instead of delete?

Q. Why one session per login?

Q. Why Activity Tracking?

---

## Database

Q. Why Atomic SQL?

Q. Why UUID?

Q. Why Indexes?

---

# 25. Common Mistakes

Authentication

❌ Returning JWT in JSON

❌ Local Storage

❌ Long Access Token

❌ No Refresh Rotation

Database

❌ No Indexes

❌ No Transactions

❌ Read-Modify-Write

Security

❌ No Validation

❌ No Exception Handler

❌ Logging Secrets

Code

❌ Fat Controllers

❌ Business Logic in Repository

❌ Exposing Entity

---

# 26. How Big Companies Build Authentication

Google

✓ Device Management

✓ Trusted Devices

✓ Session Tracking

✓ Login Notifications

✓ Risk Analysis

Microsoft

✓ MFA

✓ Azure AD

✓ Conditional Access

GitHub

✓ Fine-Grained Tokens

✓ Security Logs

✓ Device Verification

Netflix

✓ JWT

✓ API Gateway

✓ Microservices

Auth0 / Okta

✓ OAuth2

✓ OpenID Connect

✓ SAML

✓ Enterprise SSO

---

# 27. What We Have Built

Foundation

✅ Spring Boot

✅ PostgreSQL

✅ Redis

Authentication

✅ OTP

✅ Registration

✅ JWT

✅ Refresh Rotation

Security

✅ Spring Security

✅ HttpOnly Cookies

✅ Session Validation

Session Management

✅ Session Creation

✅ Device Detection

✅ Device Management

✅ Activity Tracking

✅ Cleanup Scheduler

Architecture

✅ Layered Architecture

✅ DTO

✅ Global Exception Handling

✅ Atomic SQL Updates

---

# 28. Future Learning Roadmap

Phase 1

Security Hardening

↓

Replay Detection

↓

Rate Limiting

↓

Account Locking

----------------------

Phase 2

Trusted Devices

↓

Login Notifications

↓

Session Limits

----------------------

Phase 3

OAuth2

↓

Google Login

↓

GitHub Login

↓

Microsoft Login

----------------------

Phase 4

MFA

↓

TOTP

↓

Backup Codes

----------------------

Phase 5

Enterprise

↓

SAML

↓

LDAP

↓

Azure AD

----------------------

Phase 6

Scalability

↓

API Gateway

↓

Kafka

↓

Redis Cluster

↓

Observability

---

# 29. Final Engineering Lessons

While building StreamHub Authentication Service, the most valuable lessons
were not about writing code—they were about making sound engineering decisions.

Key takeaways:

✓ Design before coding.

✓ Keep each class focused on a single responsibility.

✓ Never trust client-provided data.

✓ Build for scalability from the beginning.

✓ Prioritize security over convenience.

✓ Separate authentication, authorization, and business logic.

✓ Optimize database operations.

✓ Treat documentation as part of the product.

✓ Always think about how the system will evolve in the future.

---

# 30. Final Summary

The StreamHub Authentication Service has evolved from a simple OTP
verification system into a production-grade authentication platform.

Current Capabilities

✅ OTP Authentication

✅ Registration

✅ JWT Authentication

✅ Refresh Token Rotation

✅ Session Management

✅ Device Detection

✅ Device Management

✅ Spring Security

✅ Session Activity Tracking

✅ Scheduled Cleanup

✅ Global Exception Handling

✅ Layered Architecture

Planned Enhancements

□ Security Hardening

□ Trusted Devices

□ MFA

□ OAuth2 / OpenID Connect

□ Enterprise SSO

□ Audit Logging

□ Security Events

□ API Gateway Integration

□ Observability

This project demonstrates the complete lifecycle of designing, implementing,
and documenting a modern authentication service. The knowledge gained extends
beyond Spring Boot and JWT, covering architecture, security, scalability,
maintainability, and production engineering practices that are directly
applicable to real-world enterprise systems.