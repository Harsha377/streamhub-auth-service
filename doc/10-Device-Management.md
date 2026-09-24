# 10 - Device Management

---

# 1. Introduction

Device Management is responsible for managing every active login session of
an authenticated user.

Instead of treating all logins as one identity, the Authentication Service
tracks each login separately.

Each login creates an independent session.

Example

```
Laptop

↓

Session A

----------------

Android Phone

↓

Session B

----------------

MacBook

↓

Session C
```

Each session can be managed independently.

This enables:

- View Active Devices
- Logout Current Device
- Logout Specific Device
- Logout Other Devices
- Logout All Devices

---

# 2. Why Device Management?

Suppose a user logs in from

- Office Laptop
- Personal Laptop
- Mobile Phone

Now the mobile phone is lost.

Without Device Management

The user has only one option.

Logout everywhere.

This also logs out

✓ Office Laptop

✓ Personal Laptop

Not a good user experience.

With Device Management

The user can logout only

```
Mobile Phone
```

while keeping every other device logged in.

---

# 3. Device Management Architecture

```
Browser

↓

Authentication

↓

User Session

↓

user_sessions

↓

Device Management APIs

↓

Frontend
```

Everything is based on the

```
user_sessions
```

table.

---

# 4. Session Per Device

Every login creates one session.

Example

Harsha

↓

Windows Laptop

↓

Session A

↓

Android Phone

↓

Session B

↓

MacBook

↓

Session C

Three logins

↓

Three sessions

Each session contains

- Session ID
- Device Name
- Browser
- Operating System
- Login Time
- Last Activity
- Refresh Token Hash

---

# 5. APIs

Implemented APIs

```
GET

/api/v1/auth/sessions
```

```
DELETE

/api/v1/auth/sessions/{sessionId}
```

```
DELETE

/api/v1/auth/sessions
```

```
DELETE

/api/v1/auth/sessions/others
```

---

# 6. Get Active Sessions

Purpose

Return every active login.

Flow

```
Browser

↓

GET /sessions

↓

JwtAuthenticationFilter

↓

AuthenticationController

↓

AuthenticationService

↓

UserSessionRepository

↓

user_sessions

↓

SessionResponse

↓

Browser
```

---

# 7. SessionResponse

Each active session returns

```json
{
  "sessionId":"UUID",

  "deviceName":"Desktop",

  "browser":"Chrome",

  "operatingSystem":"Windows 11",

  "ipAddress":"192.168.1.12",

  "loginAt":"...",

  "lastActivityAt":"...",

  "currentDevice":true
}
```

Notice

We never return

```
refreshTokenHash

revoked

expiresAt
```

Internal information is hidden.

---

# 8. Current Device

The frontend needs to know

Which session belongs to

THIS device?

Current implementation

JWT

↓

Session ID

↓

CustomUserDetails

↓

Controller

↓

Compare

```
currentSessionId

==

session.sessionId
```

Result

```
currentDevice = true
```

---

# 9. Logout Current Device

Flow

```
Browser

↓

POST /logout

↓

Read Refresh Cookie

↓

Find Session

↓

Revoke Session

↓

Delete Cookies

↓

204
```

Only

Current Session

becomes revoked.

---

# 10. Logout Specific Device

API

```
DELETE

/api/v1/auth/sessions/{sessionId}
```

Example

```
Laptop

↓

Session A

Phone

↓

Session B

Mac

↓

Session C
```

User clicks

Logout Phone

↓

Session B

↓

revoked=true

↓

Done

Other sessions remain active.

---

# 11. Ownership Validation

A user must never revoke another user's session.

Current implementation

Repository

↓

Atomic SQL

```sql
UPDATE user_sessions

SET revoked=true

WHERE

session_id=?

AND user_id=?

AND revoked=false
```

If

Rows Updated

↓

1

Success

If

Rows Updated

↓

0

Session not found

Ownership validation happens directly inside SQL.

---

# 12. Logout Other Devices

Purpose

Keep

Current Device

↓

Logged In

Logout

Everything Else

Flow

```
DELETE

/sessions/others

↓

UPDATE

user_sessions

↓

WHERE

user_id=?

AND

session_id<>currentSessionId

↓

revoked=true
```

Example

Before

```
Laptop

Android

MacBook
```

After

```
Laptop

Active

Android

Revoked

MacBook

Revoked
```

---

# 13. Logout All Devices

Purpose

Logout

Everything

Flow

```
DELETE

/sessions

↓

UPDATE

user_sessions

↓

WHERE

user_id=?

↓

revoked=true
```

All sessions become invalid.

---

# 14. Why Revoke Instead of Delete?

Many beginners

DELETE

the session.

Problems

- Lose audit history

- Lose login history

- Harder debugging

Instead

```
revoked=true
```

Benefits

✓ Session History

✓ Security Audits

✓ Login Tracking

Future

Cleanup Scheduler

↓

Delete expired revoked sessions.

---

# 15. Device Management Flow

```
Login

↓

Session Created

↓

GET /sessions

↓

Display Devices

↓

User Clicks Logout

↓

Revoke Session

↓

Session Invalid
```

---

# 16. Internal Components

AuthenticationController

↓

AuthenticationService

↓

UserSessionRepository

↓

PostgreSQL

---

# 17. Database

user_sessions

Stores

```
sessionId

user

deviceName

browser

operatingSystem

ipAddress

loginAt

lastActivityAt

refreshTokenHash

revoked
```

Device Management reads from this table.

---

# 18. Security Benefits

Device Management provides

✓ Session Visibility

✓ Logout One Device

✓ Logout All Devices

✓ Device Tracking

✓ Active Session List

✓ Session Revocation

Without this module,

users cannot control where their accounts are logged in.

---

# 19. Production Optimizations

Implemented

✓ Atomic SQL Updates

✓ Ownership Validation

✓ DTO Responses

✓ Session Revocation

✓ Current Device Detection

Future

- Pagination

- Sorting

- Device Search

- Device Icons

- Location Detection

- Trusted Devices

- Last Login Location

- Login Notifications

---

# 20. Internal Sequence Diagram

```
Browser

↓

GET /sessions

↓

JwtAuthenticationFilter

↓

AuthenticationController

↓

AuthenticationService

↓

UserSessionRepository

↓

Database

↓

SessionResponse

↓

Browser
```

Logout

```
Browser

↓

DELETE /sessions/{id}

↓

AuthenticationService

↓

Atomic UPDATE

↓

Database

↓

204 No Content
```

---

# 21. Interview Questions

Q1. Why create one session per login?

Q2. Why use sessionId instead of userId?

Q3. Why revoke instead of delete?

Q4. Why use atomic SQL updates?

Q5. Why return SessionResponse instead of UserSession?

Q6. Why calculate currentDevice?

Q7. Why not expose refreshTokenHash?

Q8. How does Logout Other Devices work?

Q9. Why perform ownership validation?

Q10. How does Google implement device management?

---

# 22. Common Mistakes

❌ Returning UserSession entity

❌ Deleting sessions immediately

❌ No ownership validation

❌ Updating using find() then save()

❌ Returning refreshTokenHash

❌ No device information

❌ Using userId instead of sessionId

---

# 23. Summary

Device Management allows authenticated users to monitor and control every
active login session.

Current Features

✓ View Active Devices

✓ Logout Current Device

✓ Logout Specific Device

✓ Logout Other Devices

✓ Logout All Devices

✓ Session Revocation

✓ Ownership Validation

✓ Atomic SQL Updates

✓ Current Device Detection

Future Features

- Trusted Devices

- Login Notifications

- Geo-location

- Device Fingerprinting

- Risk-based Authentication

- Security Alerts

Device Management is built entirely on top of the **Session Management**
module. Every authenticated login creates a session, and this module gives
users complete control over those sessions without affecting other devices,
providing a secure and user-friendly experience similar to Google, GitHub,
and Microsoft account management.