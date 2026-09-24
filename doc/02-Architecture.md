# 02 - Architecture

---

# 1. Introduction

## What is the Authentication Service?

The Authentication Service is an independent microservice responsible for
verifying user identity and issuing secure access to other microservices.

Instead of every microservice implementing authentication separately,
a centralized authentication service is responsible for:

- User Authentication
- User Registration
- JWT Generation
- Refresh Token Rotation
- Session Management
- Device Management
- Security Policies

Once authentication is successful, this service issues JWT Access Tokens
that are trusted by all other microservices.

---

# 2. Why Separate Authentication?

In a monolithic application, authentication is usually implemented inside
the same application.

Example

Frontend
│
▼
Application
│
├── Authentication
├── Orders
├── Payments
├── Products
└── Users

This works for small systems.

As the application grows, authentication becomes shared by many services.

Example

                Frontend
                    │
                    ▼
             API Gateway
                    │
    ┌───────────────┼────────────────┐
    ▼               ▼                ▼
Auth Service    Product Service   Order Service
│               │                │
▼               ▼                ▼
Notification     Payment        Inventory

Only the Auth Service performs authentication.

Every other service trusts JWTs issued by the Auth Service.

Advantages

- Single authentication system
- Easier maintenance
- Better scalability
- Consistent security
- Independent deployment

---

# 3. High-Level Architecture

                    React / Mobile App
                              │
                              ▼
                     API Gateway (Future)
                              │
                              ▼
                Authentication Service
                              │
        ┌─────────────┬───────────────┬──────────────┐
        ▼             ▼               ▼              ▼
      OTP         Registration   Authentication   Refresh
        │                             │
        ▼                             ▼
      Redis                     JWT Generation
                                      │
                                      ▼
                             Session Management
                                      │
                                      ▼
                             Device Detection
                                      │
                                      ▼
                               PostgreSQL

---

# 4. Internal Modules

The Authentication Service is divided into multiple independent modules.

## OTP Module

Responsibilities

- Generate OTP
- Store OTP in Redis
- Validate OTP
- Expire OTP

---

## Registration Module

Responsibilities

- Create new users
- Validate verified email
- Store user information

---

## Authentication Module

Responsibilities

- Authenticate existing users
- Generate Session ID
- Generate JWT
- Generate Refresh Token

---

## JWT Module

Responsibilities

- Generate Access Tokens
- Validate Tokens
- Extract Claims

---

## Refresh Module

Responsibilities

- Rotate Refresh Tokens
- Replace old Refresh Tokens
- Prevent stale sessions

---

## Session Module

Responsibilities

- Create Sessions
- Update Activity
- Logout Devices
- Cleanup Expired Sessions

---

## Device Module

Responsibilities

- Detect Browser
- Detect Operating System
- Detect Device
- Store Client Information

---

## Security Module

Responsibilities

- JwtAuthenticationFilter
- SecurityConfig
- AuthenticationEntryPoint
- AccessDeniedHandler

---

# 5. Authentication Request Flow

User

↓

Send OTP

↓

Redis

↓

Verify OTP

↓

Existing User?

↓

Generate Session

↓

Generate JWT

↓

Store Session

↓

Return Cookies

↓

Protected APIs

---

# 6. Database Architecture

PostgreSQL

users

Stores

- User Information

user_sessions

Stores

- Session Information

Redis

Stores

- OTP
- Verified User Flag

---

# 7. Design Principles

This project follows the following software engineering principles.

## Single Responsibility Principle

Every class performs one responsibility.

Examples

JwtService

Only handles JWT.

AuthenticationService

Only handles authentication.

DeviceInfoService

Only detects client devices.

---

## Stateless Authentication

No server-side HTTP sessions are used.

Authentication relies entirely on:

- JWT Access Token
- Refresh Token
- User Session table

---

## Session-Based Security

Every login creates a unique session.

Benefits

- Logout one device
- Logout all devices
- Track active devices
- Session activity tracking

---

## Refresh Token Rotation

Refresh Tokens are never reused.

Every refresh generates:

- New Access Token
- New Refresh Token

---

## Device Awareness

Every session stores:

- Device
- Browser
- Operating System
- IP Address

---

# 8. Current Architecture Status

Completed

✅ OTP

✅ Registration

✅ JWT Authentication

✅ Refresh Rotation

✅ Session Management

✅ Device Detection

✅ Device Management

✅ Spring Security

✅ Session Tracking

✅ Cleanup Scheduler

---

# 9. Future Architecture

Security Hardening

↓

Replay Protection

↓

Audit Logs

↓

Security Events

↓

Trusted Devices

↓

MFA

↓

OAuth2

↓

API Gateway

↓

Observability

---

# 10. Summary

The Authentication Service now provides:

- OTP Authentication
- JWT Authentication
- Refresh Token Rotation
- Session Management
- Device Management
- Spring Security
- Production-ready architecture

The remaining work focuses on enterprise security features,
scalability, monitoring, and advanced authentication mechanisms.