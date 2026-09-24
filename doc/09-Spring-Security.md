# 09 - Spring Security

---

# 1. Introduction

Spring Security is the security framework responsible for protecting every
API inside the StreamHub Authentication Service.

It ensures that only authenticated and authorized users can access protected
resources.

Unlike traditional applications that rely on HTTP Sessions, our application
uses Stateless JWT Authentication.

Spring Security validates every incoming request before it reaches any
controller.

---

# 2. Why Spring Security?

Suppose we expose

```
GET /api/v1/users/profile
```

Without Spring Security

Anyone can access

```
GET /users/profile

↓

Controller

↓

Database

↓

Response
```

There is no authentication.

With Spring Security

```
GET /users/profile

↓

Spring Security

↓

JWT Validation

↓

Authentication

↓

Controller
```

Only authenticated users can access the endpoint.

---

# 3. Why Use Spring Security?

Instead of writing authentication logic inside every controller,

Spring Security centralizes authentication.

Without Spring Security

```
Controller A

↓

Validate JWT

↓

Business Logic

----------------

Controller B

↓

Validate JWT

↓

Business Logic

----------------

Controller C

↓

Validate JWT

↓

Business Logic
```

The same authentication code is duplicated.

With Spring Security

```
Request

↓

Spring Security

↓

Validate Once

↓

Controller
```

Authentication happens once for every request.

---

# 4. High-Level Architecture

```
Browser

↓

HTTP Request

↓

Spring Security Filter Chain

↓

JwtAuthenticationFilter

↓

AuthenticationManager (Future)

↓

SecurityContextHolder

↓

Controller

↓

Service

↓

Database
```

---

# 5. Security Flow

Every request follows this flow.

```
Browser

↓

Protected API

↓

Spring Security

↓

JwtAuthenticationFilter

↓

Validate JWT

↓

Create Authentication

↓

SecurityContextHolder

↓

Controller

↓

Service

↓

Response
```

---

# 6. Core Components

Our Spring Security module consists of

```
SecurityConfig

↓

JwtAuthenticationFilter

↓

CustomUserDetails

↓

CustomUserDetailsService

↓

AuthenticationEntryPoint

↓

AccessDeniedHandler

↓

SecurityContextHolder
```

Each class has a single responsibility.

---

# 7. SecurityConfig

SecurityConfig configures the entire Spring Security framework.

Responsibilities

✓ Public APIs

✓ Protected APIs

✓ Stateless Authentication

✓ Filter Registration

✓ Exception Handling

✓ CORS

Current configuration

Public APIs

```
POST /auth/send-otp

POST /auth/verify-otp

POST /auth/register

POST /auth/refresh
```

Protected APIs

Everything else.

---

# 8. Stateless Authentication

Traditional Spring Security

```
Login

↓

HTTP Session

↓

Memory

↓

Every Request
```

Our Application

```
Login

↓

JWT

↓

HttpOnly Cookie

↓

Every Request

↓

Validate JWT
```

No server-side HTTP Session exists.

Everything is validated using JWT.

---

# 9. Security Filter Chain

Spring Security works using filters.

```
Incoming Request

↓

CorsFilter

↓

JwtAuthenticationFilter

↓

AuthorizationFilter

↓

ExceptionTranslationFilter

↓

Controller
```

Every request passes through the filter chain.

---

# 10. JwtAuthenticationFilter

This is the most important filter.

Responsibilities

✓ Read Cookie

✓ Extract JWT

✓ Validate JWT

✓ Extract Session ID

✓ Load User Session

✓ Validate Session

✓ Create Authentication

✓ Store Authentication

It executes before every protected controller.

---

# 11. SecurityContextHolder

After authentication,

Spring Security stores the authenticated user inside

```
SecurityContextHolder
```

Structure

```
SecurityContextHolder

↓

SecurityContext

↓

Authentication

↓

CustomUserDetails
```

Controllers later access

```
@AuthenticationPrincipal
```

instead of parsing JWT again.

---

# 12. CustomUserDetails

Represents the authenticated user.

Contains

```
User

Session ID

Authorities
```

Instead of exposing the User entity directly,

Spring Security uses this object.

Advantages

✓ Secure

✓ Extendable

✓ Centralized

---

# 13. CustomUserDetailsService

Responsibilities

Load authenticated user.

Flow

```
Email

↓

UserRepository

↓

User

↓

CustomUserDetails
```

This service acts as the bridge between Spring Security and the database.

---

# 14. AuthenticationEntryPoint

Executed when

Authentication fails.

Examples

Missing JWT

Expired JWT

Invalid JWT

Revoked Session

Returns

```
401 Unauthorized
```

instead of exposing internal exceptions.

---

# 15. AccessDeniedHandler

Executed when

User is authenticated,

but not authorized.

Example

```
ROLE_USER

↓

Access Admin API

↓

403 Forbidden
```

Difference

AuthenticationEntryPoint

↓

Authentication Failure

AccessDeniedHandler

↓

Authorization Failure

---

# 16. Authentication Object

After successful authentication

Spring creates

```
UsernamePasswordAuthenticationToken
```

Contains

```
Principal

Authorities

Credentials (null)
```

This object is stored inside SecurityContextHolder.

---

# 17. Request Lifecycle

```
Browser

↓

GET /profile

↓

Spring Security

↓

JwtAuthenticationFilter

↓

Validate JWT

↓

Load Session

↓

Load User

↓

Authentication

↓

SecurityContextHolder

↓

ProfileController

↓

Response
```

---

# 18. Authorization

Authentication

↓

Who are you?

Authorization

↓

What are you allowed to do?

Current Roles

```
ROLE_USER
```

Future

```
ROLE_ADMIN

ROLE_MANAGER

ROLE_WORKER

ROLE_CONTRACTOR
```

---

# 19. Exception Flow

Authentication Failure

↓

AuthenticationEntryPoint

↓

401

Authorization Failure

↓

AccessDeniedHandler

↓

403

The client receives consistent JSON error responses.

---

# 20. Why Controllers Don't Validate JWT?

Controllers focus only on business logic.

Without Spring Security

```
Controller

↓

Read Cookie

↓

Validate JWT

↓

Load User

↓

Business Logic
```

With Spring Security

```
Controller

↓

Business Logic Only
```

Authentication has already been completed.

---

# 21. Security Architecture

```
Browser

↓

Spring Security Filter Chain

↓

JwtAuthenticationFilter

↓

SecurityContextHolder

↓

Controller

↓

Service

↓

Repository

↓

Database
```

---

# 22. Security Features Implemented

✓ Stateless Authentication

✓ JWT Authentication

✓ HttpOnly Cookies

✓ Session Validation

✓ Refresh Rotation

✓ SecurityContext

✓ Global Authentication

✓ Protected APIs

✓ Custom Entry Point

✓ Custom Access Denied Handler

---

# 23. Future Enhancements

Spring Security can later support

• RBAC

• Method Security

• OAuth2 Login

• Google Login

• GitHub Login

• Microsoft Login

• SAML

• LDAP

• MFA

• API Gateway Authentication

---

# 24. Interview Questions

Q1. Why use Spring Security?

Q2. What is SecurityContextHolder?

Q3. What is Authentication?

Q4. What is UserDetails?

Q5. What is UserDetailsService?

Q6. What is the Security Filter Chain?

Q7. Why Stateless Authentication?

Q8. Difference between Authentication and Authorization?

Q9. Difference between 401 and 403?

Q10. Why use JwtAuthenticationFilter?

---

# 25. Common Mistakes

❌ Parsing JWT inside every controller

❌ Returning User entity directly

❌ Using HTTP Sessions with JWT

❌ Disabling Spring Security completely

❌ Mixing business logic with authentication

❌ Not clearing SecurityContext

❌ Using Local Storage for JWT

---

# 26. Summary

Spring Security acts as the security gateway for the entire application.

Every incoming request is authenticated before it reaches the business layer.

Current Features

✓ Stateless Authentication

✓ JWT Validation

✓ Security Filter Chain

✓ SecurityContextHolder

✓ AuthenticationEntryPoint

✓ AccessDeniedHandler

✓ CustomUserDetails

✓ CustomUserDetailsService

✓ Protected APIs

Future Features

- Role-Based Access Control (RBAC)

- Method-Level Security

- OAuth2 Login

- Multi-Factor Authentication

- API Gateway Integration

- Enterprise Identity Providers

Spring Security is the foundation of request authentication. It centralizes authentication, keeps controllers free from security logic, and ensures that every protected request follows the same secure authentication process before reaching the application's business layer.