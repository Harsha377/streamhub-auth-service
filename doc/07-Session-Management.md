# 07 - Session Management

---

# 1. Introduction

Session Management is responsible for tracking every authenticated login
performed by a user.

Unlike traditional HTTP Sessions stored in server memory, StreamHub stores
session information inside the database.

Every successful authentication creates a unique session.

Each device has its own independent session.

This allows the application to provide:

- Logout Current Device
- Logout Specific Device
- Logout All Devices
- Device Management
- Session Activity Tracking
- Refresh Token Rotation
- Session Revocation

---

# 2. Why Session Management?

Many developers believe JWT is enough.

Actually it is not.

JWT only proves

"I was authenticated."

It cannot answer

• Is this login still active?

• Was this device logged out?

• Was the Refresh Token rotated?

• Has the account revoked this device?

For these reasons we maintain a user_sessions table.

---

# 3. Why Not Only JWT?

Without Session Management

Login

↓

JWT

↓

Valid for 15 Minutes

Problem

User clicks Logout

↓

JWT is still valid

↓

Attacker can continue using it

JWT cannot be revoked.

---

With Session Management

JWT

↓

Contains Session ID

↓

Database

↓

Session Exists?

↓

Revoked?

↓

Continue

Now logout immediately disables the session.

---

# 4. Session Lifecycle

Authentication

↓

Create Session

↓

Generate JWT

↓

Protected APIs

↓

Refresh Rotation

↓

Activity Tracking

↓

Logout

↓

Session Revoked

↓

Cleanup Scheduler

↓

Session Deleted

Every session follows this lifecycle.

---

# 5. Database Design

Table

user_sessions

Stores

- sessionId
- userId
- refreshTokenHash
- deviceName
- browser
- operatingSystem
- ipAddress
- loginAt
- lastActivityAt
- expiresAt
- revoked

Every login creates one row.

---

# 6. Why Session ID?

Every login receives a unique identifier.

Example

Laptop

↓

Session A

Android

↓

Session B

MacBook

↓

Session C

Each JWT stores its own Session ID.

Benefits

✓ Logout one device

✓ Track devices

✓ Refresh rotation

✓ Activity tracking

---

# 7. Session Creation

Authentication Success

↓

Generate UUID

↓

Create UserSession

↓

Store Refresh Hash

↓

Store Device Information

↓

Save Session

↓

Generate JWT

↓

Return Cookies

---

# 8. Session Validation

Every authenticated request performs

Read JWT

↓

Extract Session ID

↓

Find Session

↓

Session Exists?

↓

Revoked?

↓

Expired?

↓

Continue

Otherwise

↓

401 Unauthorized

---

# 9. Refresh Token Rotation

Refresh Cookie

↓

SHA-256

↓

Find Session

↓

Validate Session

↓

Generate New Refresh Token

↓

Replace Refresh Hash

↓

Save Session

↓

Return New Cookies

Only the newest Refresh Token remains valid.

---

# 10. Logout Current Device

Browser

↓

POST /logout

↓

Read Refresh Token

↓

Find Session

↓

Revoke Session

↓

Delete Cookies

↓

204 No Content

---

# 11. Logout Specific Device

DELETE

/auth/sessions/{sessionId}

↓

Atomic SQL Update

↓

revoked = true

↓

204 No Content

Only the selected device is logged out.

---

# 12. Logout All Devices

DELETE

/auth/sessions

↓

UPDATE

↓

All Sessions

↓

revoked = true

All active sessions become invalid.

---

# 13. Logout Other Devices

DELETE

/auth/sessions/others

↓

UPDATE

↓

Current Session

↓

Ignored

↓

All Others

↓

Revoked

Current device stays logged in.

---

# 14. Device Management

GET

/auth/sessions

Returns

- Device Name
- Browser
- Operating System
- Login Time
- Last Activity
- Current Device

This is similar to

Google

↓

Your Devices

GitHub

↓

Sessions

Microsoft

↓

Recent Activity

---

# 15. Session Activity Tracking

Problem

Updating database every request

↓

High Database Load

Solution

Update only every configured interval.

Flow

Authenticated Request

↓

JwtAuthenticationFilter

↓

Update Needed?

↓

Yes

↓

Update lastActivityAt

↓

No

↓

Continue

Benefits

✓ Lower DB Writes

✓ Accurate Last Active

✓ Better Performance

---

# 16. Cleanup Scheduler

Expired sessions should not remain forever.

Scheduler

↓

Find Expired Sessions

↓

Delete

↓

Finished

Benefits

✓ Smaller Table

✓ Faster Queries

✓ Less Storage

---

# 17. Internal Architecture

Authentication

↓

UserSessionRepository

↓

user_sessions

↓

JwtAuthenticationFilter

↓

Refresh Rotation

↓

Logout

↓

Device APIs

↓

Cleanup Scheduler

Everything uses the same session table.

---

# 18. Request Flow

Protected API

↓

Read Access Cookie

↓

Validate JWT

↓

Extract Session ID

↓

Find Session

↓

Validate Session

↓

Update Activity

↓

Controller

↓

Response

---

# 19. Why Database Sessions?

Advantages

✓ Immediate Logout

✓ Device Management

✓ Session Revocation

✓ Refresh Rotation

✓ Activity Tracking

✓ Audit Support

✓ Enterprise Security

---

# 20. Security Features

Implemented

✓ Session ID

✓ Refresh Hash

✓ Session Revocation

✓ Logout One Device

✓ Logout All Devices

✓ Activity Tracking

✓ Device Tracking

Future

□ Replay Detection

□ Concurrent Session Limits

□ Trusted Devices

□ Risk-based Authentication

□ Audit Events

---

# 21. Production Improvements

Future

Maximum Sessions

↓

Oldest Session Removed

Trusted Devices

↓

Skip OTP

New Device Detection

↓

Email Notification

Impossible Travel Detection

↓

Security Alert

Geo Location

↓

Country Tracking

Risk Score

↓

Adaptive Authentication

---

# 22. Interview Questions

Q1. Why is JWT alone not enough?

Q2. Why maintain user_sessions?

Q3. Why include sessionId inside JWT?

Q4. Why update lastActivityAt?

Q5. Why use atomic SQL updates?

Q6. Why hash Refresh Tokens?

Q7. Why revoke sessions instead of deleting immediately?

Q8. Why store device information?

Q9. How does Logout All Devices work?

Q10. What happens if a session is revoked?

---

# 23. Common Mistakes

❌ No session table

❌ Storing raw Refresh Tokens

❌ No activity tracking

❌ Updating activity every request

❌ Deleting session immediately on logout

❌ Returning session entity directly

❌ No device tracking

---

# 24. Session Lifecycle Diagram

User Login

↓

Session Created

↓

JWT Issued

↓

Protected APIs

↓

Refresh Rotation

↓

Device Management

↓

Activity Tracking

↓

Logout

↓

Session Revoked

↓

Cleanup Scheduler

↓

Session Deleted

---

# 25. Summary

Session Management is the foundation of the StreamHub Authentication Service.

It enables secure authentication beyond what JWT alone can provide.

Current Features

✓ Session Creation

✓ Session Validation

✓ Refresh Rotation

✓ Logout Current Device

✓ Logout Specific Device

✓ Logout All Devices

✓ Logout Other Devices

✓ Device Tracking

✓ Session Activity Tracking

✓ Cleanup Scheduler

Future Features

- Replay Detection

- Trusted Devices

- Concurrent Session Limits

- Risk-based Authentication

- Security Events

- Audit Logs

- Geo-location Tracking

- Adaptive Authentication

The `user_sessions` table is the central component that connects authentication, refresh token rotation, device management, activity tracking, and logout functionality into one cohesive session lifecycle.