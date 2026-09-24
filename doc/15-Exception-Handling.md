# 15 - Exception Handling

---

# 1. Introduction

Exception Handling is responsible for converting application errors into
consistent and meaningful HTTP responses.

Instead of exposing Java stack traces or internal exceptions to clients,
the Authentication Service returns standardized JSON error responses.

This improves:

- API Consistency
- Client Experience
- Security
- Debugging
- Maintainability

---

# 2. Why Exception Handling?

Suppose a user calls

```
POST /api/v1/auth/register
```

Email already exists.

Without Exception Handling

```
500 Internal Server Error

NullPointerException

Stack Trace

Java Classes

File Names

Line Numbers
```

This exposes internal implementation details.

With Exception Handling

```json
{
    "timestamp":"2026-07-10T10:30:15Z",
    "status":409,
    "error":"Conflict",
    "message":"Email already registered.",
    "path":"/api/v1/auth/register"
}
```

The client receives a clean, consistent response.

---

# 3. Goals

Our Exception Handling should

✓ Hide internal implementation

✓ Return meaningful messages

✓ Use proper HTTP status codes

✓ Provide consistent JSON responses

✓ Make frontend integration easier

---

# 4. Architecture

```
Controller

↓

Service

↓

Repository

↓

Exception Thrown

↓

GlobalExceptionHandler

↓

ApiErrorResponse

↓

Client
```

All exceptions pass through one place.

---

# 5. Components

Our implementation consists of

```
GlobalExceptionHandler

↓

Custom Exceptions

↓

ApiErrorResponse

↓

Spring Boot
```

---

# 6. ApiErrorResponse

Every error response follows the same structure.

Example

```json
{
  "timestamp":"2026-07-10T10:30:15Z",
  "status":404,
  "error":"Not Found",
  "message":"User not found.",
  "path":"/api/v1/users/1"
}
```

Fields

timestamp

Time of error

status

HTTP status code

error

Status name

message

Human-readable message

path

API endpoint

---

# 7. GlobalExceptionHandler

Spring Boot provides

```
@RestControllerAdvice
```

Responsibilities

✓ Catch Exceptions

✓ Build Error Response

✓ Return HTTP Status

Instead of handling exceptions in every controller,
one centralized class handles all errors.

---

# 8. Custom Exceptions

Instead of throwing

```
RuntimeException
```

we create meaningful exceptions.

Examples

```
UserNotFoundException

EmailAlreadyExistsException

InvalidOtpException

SessionExpiredException

UnauthorizedException

ForbiddenException

ResourceNotFoundException
```

Each exception represents a business problem.

---

# 9. Validation Errors

Spring automatically validates DTOs.

Example

```java
@NotBlank

@Email
```

Invalid Request

↓

MethodArgumentNotValidException

↓

GlobalExceptionHandler

↓

400 Bad Request

Response

```json
{
    "status":400,
    "message":"Email must not be blank."
}
```

---

# 10. Authentication Exceptions

Example

Missing Access Token

↓

UnauthorizedException

↓

401 Unauthorized

Expired JWT

↓

UnauthorizedException

↓

401 Unauthorized

Revoked Session

↓

UnauthorizedException

↓

401 Unauthorized

---

# 11. Authorization Exceptions

User authenticated

↓

Access Admin API

↓

AccessDeniedException

↓

403 Forbidden

Authentication

↓

Who are you?

Authorization

↓

What are you allowed to do?

---

# 12. Resource Not Found

Example

```
GET /sessions/{id}
```

Session does not exist.

↓

ResourceNotFoundException

↓

404 Not Found

---

# 13. Business Exceptions

Examples

Duplicate Email

↓

409 Conflict

Invalid OTP

↓

400 Bad Request

Session Expired

↓

401 Unauthorized

OTP Expired

↓

410 Gone

Every business error has an appropriate HTTP status.

---

# 14. Unexpected Exceptions

Example

```
NullPointerException

SQLException

IOException
```

↓

Catch All

↓

500 Internal Server Error

Response

```json
{
   "status":500,
   "message":"Something went wrong."
}
```

The client never sees the stack trace.

---

# 15. Exception Flow

```
Browser

↓

Controller

↓

Service

↓

Repository

↓

Exception

↓

GlobalExceptionHandler

↓

ApiErrorResponse

↓

Client
```

---

# 16. HTTP Status Codes Used

| Status | Meaning | Example |
|----------|---------|---------|
| 200 | OK | Successful Request |
| 201 | Created | Registration |
| 204 | No Content | Logout |
| 400 | Bad Request | Invalid Input |
| 401 | Unauthorized | Invalid JWT |
| 403 | Forbidden | No Permission |
| 404 | Not Found | Session Missing |
| 409 | Conflict | Duplicate Email |
| 410 | Gone | OTP Expired |
| 422 | Unprocessable Content | Business Validation |
| 500 | Internal Server Error | Unexpected Error |

---

# 17. Security Benefits

Exception Handling prevents

✓ Stack Trace Leakage

✓ Package Name Exposure

✓ Internal Class Exposure

✓ SQL Error Exposure

Instead of

```
NullPointerException

at

AuthenticationService.java:120
```

the client receives

```
Something went wrong.
```

---

# 18. Logging Strategy

Client Response

↓

Simple Message

Server Logs

↓

Detailed Stack Trace

Example

Client

```
401 Unauthorized
```

Server

```
JWT expired

Session ID

User ID

Request URI

Stack Trace
```

Never expose internal logs to clients.

---

# 19. Best Practices

✓ Use custom exceptions

✓ Centralize exception handling

✓ Use meaningful HTTP status codes

✓ Keep responses consistent

✓ Log unexpected errors

✓ Never expose stack traces

✓ Separate business errors from system errors

---

# 20. Interview Questions

Q1. Why use @RestControllerAdvice?

Q2. Why create custom exceptions?

Q3. Difference between 401 and 403?

Q4. Why shouldn't stack traces be returned?

Q5. What is MethodArgumentNotValidException?

Q6. Why standardize error responses?

Q7. Difference between RuntimeException and Business Exception?

Q8. What should be logged?

Q9. Why use 409 Conflict?

Q10. Why use 410 Gone for expired OTP?

---

# 21. Common Mistakes

❌ Throwing RuntimeException everywhere

❌ Returning stack traces

❌ Different error formats

❌ Handling exceptions in every controller

❌ Returning 500 for business errors

❌ Logging sensitive information

❌ Returning SQL exceptions to clients

---

# 22. Summary

Exception Handling provides a centralized mechanism for converting
application errors into meaningful HTTP responses.

Current Features

✓ Global Exception Handler

✓ Standard Error Response

✓ Custom Exceptions

✓ Validation Errors

✓ Authentication Errors

✓ Authorization Errors

✓ Business Exceptions

✓ Unexpected Exception Handling

Future Features

- Error Codes

- Localization (i18n)

- Correlation IDs

- Structured Logging

- Distributed Tracing

- Security Audit Integration

A centralized exception handling strategy makes APIs predictable, secure,
and easier to consume while keeping business logic free from repetitive
try-catch blocks.