# 01 - Project Overview

## Project Name

**StreamHub Authentication Service**

------------------------------------------------------------------------

# Objective

Build a production-grade Authentication and Authorization microservice
similar to the authentication systems used by Google, GitHub, Microsoft,
Netflix, and Amazon.

The service is responsible for:

-   User authentication
-   OTP verification
-   User registration
-   JWT-based authentication
-   Refresh token rotation
-   Device management
-   Session management
-   Spring Security integration

This service is designed as an independent microservice that can be
consumed by other services through secure JWT access tokens.

------------------------------------------------------------------------

# Technology Stack

## Backend

-   Java 21
-   Spring Boot
-   Spring Security
-   Spring Data JPA
-   Maven

## Database

-   PostgreSQL

## Cache

-   Redis

## Authentication

-   JWT (Access Token)
-   Refresh Token Rotation
-   HttpOnly Cookies

## Migration

-   Flyway

## API Testing

-   Postman / Swagger

------------------------------------------------------------------------

# High-Level Architecture

``` text
React / Mobile App
        │
        ▼
Authentication Service
        │
        ├── OTP Service
        ├── Registration
        ├── Authentication
        ├── Refresh Token
        ├── Device Detection
        ├── Session Management
        └── Spring Security
        │
        ▼
PostgreSQL + Redis
```

------------------------------------------------------------------------

# Core Responsibilities

-   Send OTP
-   Verify OTP
-   Register users
-   Authenticate users
-   Issue JWT access tokens
-   Rotate refresh tokens
-   Maintain user sessions
-   Detect client devices
-   Secure APIs using Spring Security

------------------------------------------------------------------------

# Authentication Flow

``` text
Send OTP
    │
    ▼
Verify OTP
    │
    ▼
Existing User?
 ┌──────┴──────┐
 │             │
No            Yes
 │             │
 ▼             ▼
Register   Authenticate
                │
                ▼
      Create Session
                │
                ▼
 Issue Access & Refresh Tokens
                │
                ▼
      Protected APIs
```

------------------------------------------------------------------------

# Main Components

-   OTP Module
-   Registration Module
-   Authentication Module
-   JWT Module
-   Refresh Token Module
-   Session Management Module
-   Device Detection Module
-   Spring Security Module

------------------------------------------------------------------------

# Database Overview

Current tables:

-   users
-   user_sessions

Future tables:

-   login_attempts
-   security_events
-   trusted_devices
-   audit_logs

------------------------------------------------------------------------

# Design Principles

-   Stateless authentication
-   HttpOnly cookies
-   Refresh token rotation
-   One session per login
-   Device-aware sessions
-   Secure-by-default design
-   Separation of concerns
-   Production-ready architecture

------------------------------------------------------------------------

# Current Status

Completed:

-   Foundation setup
-   OTP authentication
-   Registration
-   JWT authentication
-   Refresh token rotation
-   Session management
-   Device detection
-   Spring Security
-   Device management APIs
-   Session activity tracking
-   Session cleanup scheduler

Upcoming:

-   Security hardening
-   MFA
-   Enterprise SSO
-   Observability
-   API Gateway integration

------------------------------------------------------------------------

# Documentation Structure

1.  01-Project-Overview.md
2.  02-Architecture.md
3.  03-OTP-Authentication.md
4.  04-Registration.md
5.  05-JWT-Authentication.md
6.  06-Refresh-Token-Rotation.md
7.  07-Session-Management.md
8.  08-Device-Detection.md
9.  09-Spring-Security.md
10. 10-Device-Management.md
11. 11-Session-Activity-Tracking.md
12. 12-Security-Hardening.md
13. 13-Production-Deployment.md
