# 16 - Complete Application Flow & Security Flow

---

# 1. Introduction

This chapter explains how every component of the StreamHub Authentication
Service works together.

Instead of explaining individual modules separately, this document shows the
complete request lifecycle from the moment a user opens the application until
the request reaches the business logic.

By the end of this chapter, you should understand

✓ Complete Authentication Flow

✓ Spring Security Flow

✓ Session Management Flow

✓ Refresh Token Flow

✓ Device Management Flow

✓ Logout Flow

✓ Request Lifecycle

---

# 2. Complete Application Architecture

                           React / Mobile App
                                    │
                                    │ HTTPS
                                    ▼
                     Authentication Service
                                    │
         ┌──────────────┬───────────────┬──────────────┐
         ▼              ▼               ▼              ▼
     OTP Module   Registration    Authentication   Device Detection
         │              │               │
         └──────────────┴───────────────┘
                        │
                        ▼
                 Session Management
                        │
            ┌───────────┴─────────────┐
            ▼                         ▼
        PostgreSQL                 Redis

---

# 3. Complete Authentication Flow

User

↓

Enter Email

↓

POST /auth/send-otp

↓

Generate OTP

↓

Store OTP in Redis

↓

Send Email

↓

User Enters OTP

↓

POST /auth/verify-otp

↓

Verify OTP

↓

Existing User?

        │
┌────┴───────┐
│            │
No            Yes
│            │
▼            ▼
Register   Authenticate

↓

Generate Session

↓

Generate Access Token

↓

Generate Refresh Token

↓

Hash Refresh Token

↓

Save Session

↓

Return Cookies

↓

Authentication Completed

---

# 4. Registration Flow

Verify OTP

↓

Verified Flag

↓

POST /register

↓

Validate Verified Flag

↓

Email Exists?

↓

Create User

↓

Save User

↓

Authenticate

↓

Create Session

↓

Return Cookies

---

# 5. Refresh Token Flow

Browser

↓

Access Token Expired

↓

POST /refresh

↓

Read Refresh Cookie

↓

Hash Refresh Token

↓

Find Session

↓

Validate Session

↓

Generate Access Token

↓

Generate Refresh Token

↓

Replace Refresh Hash

↓

Update Session

↓

Replace Cookies

↓

Continue Working

---

# 6. Logout Flow

Current Device

↓

Read Refresh Cookie

↓

Hash Refresh Token

↓

Find Session

↓

Revoke Session

↓

Delete Cookies

↓

204 No Content

---

# 7. Logout Specific Device

Frontend

↓

DELETE /sessions/{id}

↓

AuthenticationService

↓

Atomic SQL Update

↓

revoked=true

↓

Done

---

# 8. Logout Other Devices

DELETE /sessions/others

↓

Current Session

↓

Ignored

↓

Everything Else

↓

revoked=true

---

# 9. Logout All Devices

DELETE /sessions

↓

UPDATE user_sessions

↓

All Sessions

↓

revoked=true

---

# 10. Device Detection Flow

Browser

↓

HTTP Request

↓

User-Agent Header

↓

DeviceInfoService

↓

YAUAA Parser

↓

DeviceInfo

↓

Store UserSession

---

# 11. Session Activity Flow

Authenticated Request

↓

JwtAuthenticationFilter

↓

Validate Session

↓

Update Needed?

↓

Yes

↓

UPDATE last_activity_at

↓

Continue

---

# 12. Session Cleanup Flow

Scheduler

↓

Find Expired Sessions

↓

Delete Sessions

↓

Completed

---

# 13. Spring Security Flow

Incoming Request

↓

Spring Security Filter Chain

↓

JwtAuthenticationFilter

↓

Extract Cookie

↓

Read Access Token

↓

JWT Valid?

↓

Extract Session ID

↓

Find Session

↓

Session Valid?

↓

Load User

↓

Create Authentication

↓

SecurityContextHolder

↓

Controller

↓

Business Logic

↓

Response

---

# 14. JwtAuthenticationFilter Internal Flow

Request

↓

Read ACCESS_TOKEN Cookie

↓

Cookie Exists?

↓

Extract JWT

↓

Validate Signature

↓

Expired?

↓

Extract Claims

↓

Extract Session ID

↓

Find User Session

↓

Session Revoked?

↓

Load User

↓

Create UsernamePasswordAuthenticationToken

↓

SecurityContextHolder

↓

FilterChain.doFilter()

↓

Controller

---

# 15. SecurityContext Flow

JwtAuthenticationFilter

↓

UsernamePasswordAuthenticationToken

↓

SecurityContext

↓

SecurityContextHolder

↓

Controller

↓

@AuthenticationPrincipal

↓

Current User

---

# 16. Complete Request Lifecycle

Browser

↓

HTTP Request

↓

Tomcat

↓

Spring Security

↓

JwtAuthenticationFilter

↓

JWT Validation

↓

Session Validation

↓

Update Activity

↓

SecurityContextHolder

↓

Controller

↓

Service

↓

Repository

↓

PostgreSQL

↓

Service

↓

Controller

↓

HTTP Response

---

# 17. Database Interaction Flow

Authentication

↓

users

↓

user_sessions

↓

Refresh

↓

user_sessions

↓

Device Management

↓

user_sessions

↓

Logout

↓

user_sessions

↓

Cleanup Scheduler

↓

user_sessions

Redis

↓

OTP

↓

Verified Flag

---

# 18. Complete Security Flow

Client

↓

HTTPS Request

↓

Spring Security

↓

JwtAuthenticationFilter

↓

Validate JWT

↓

Validate Session

↓

Validate Revocation

↓

Load User

↓

Create Authentication

↓

Store SecurityContext

↓

Authorization

↓

Controller

↓

Business Logic

↓

Response

Every protected request follows this exact path.

---

# 19. Internal Class Flow

AuthenticationController

↓

AuthenticationService

↓

JwtService

↓

DeviceInfoService

↓

CookieUtil

↓

UserSessionRepository

↓

PostgreSQL

---

Protected Request

↓

JwtAuthenticationFilter

↓

JwtService

↓

UserSessionRepository

↓

CustomUserDetailsService

↓

SecurityContextHolder

↓

Controller

---

# 20. Redis Flow

Send OTP

↓

Redis

↓

Verify OTP

↓

Delete OTP

↓

Store Verified Flag

↓

Register

↓

Delete Verified Flag

Redis is only used for temporary authentication data.

---

# 21. PostgreSQL Flow

Registration

↓

users

Authentication

↓

user_sessions

Refresh

↓

user_sessions

Device Management

↓

user_sessions

Logout

↓

user_sessions

---

# 22. Overall Component Interaction

Frontend

↓

AuthenticationController

↓

AuthenticationService

↓

JwtService

↓

DeviceInfoService

↓

CookieUtil

↓

Repositories

↓

Database

↓

Response

---

# 23. Summary

The complete authentication lifecycle is

Send OTP

↓

Verify OTP

↓

Registration / Authentication

↓

Session Creation

↓

JWT Generation

↓

Cookie Creation

↓

Protected APIs

↓

JWT Validation

↓

Session Validation

↓

Business Logic

↓

Refresh Rotation

↓

Device Management

↓

Logout

↓

Cleanup Scheduler

The StreamHub Authentication Service now provides

✓ OTP Authentication

✓ JWT Authentication

✓ Refresh Token Rotation

✓ Session Management

✓ Device Detection

✓ Device Management

✓ Spring Security

✓ Activity Tracking

✓ Cleanup Scheduler

✓ Production-ready Authentication Architecture

Future work will enhance this foundation with

- Replay Protection
- Account Locking
- Rate Limiting
- Trusted Devices
- MFA
- OAuth2
- Security Auditing
- Enterprise Authentication