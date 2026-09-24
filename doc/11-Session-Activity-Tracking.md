# 11 - Session Activity Tracking

---

# 1. Introduction

Session Activity Tracking is responsible for recording the most recent time
a user actively interacted with the application.

Every authenticated request represents user activity.

Instead of updating the database on every request, the Authentication Service
updates the activity timestamp only after a configurable interval.

This provides:

- Last Active information
- Device Activity History
- Security Monitoring
- Lower Database Load
- Better Scalability

---

# 2. Why Session Activity Tracking?

Suppose a user logs into the application.

Without activity tracking

```
Login

↓

09:00 AM

↓

Works for 6 Hours

↓

Database

Last Active

09:00 AM
```

The application incorrectly assumes the user has been inactive since login.

With activity tracking

```
09:00 Login

↓

09:15 API Request

↓

09:45 API Request

↓

10:20 API Request

↓

Last Active

10:20 AM
```

The session always reflects the latest activity.

---

# 3. Why Not Update Every Request?

Imagine opening the dashboard.

React Application

↓

GET /profile

↓

GET /notifications

↓

GET /projects

↓

GET /permissions

↓

GET /settings

↓

GET /menu

One page load

↓

6 Requests

If every request performs

```
UPDATE user_sessions
```

Database writes increase dramatically.

Now imagine

10,000 Users

↓

30 Requests / Minute

↓

300,000 UPDATE statements every minute

Most of these updates are unnecessary.

---

# 4. Production Solution

Instead of updating on every request,

we throttle updates.

Flow

```
Request

↓

JwtAuthenticationFilter

↓

Current Time

↓

Compare

↓

Last Activity

↓

Difference >= Configured Interval ?

        │
   ┌────┴─────┐
   │          │
 No          Yes
   │          │
   ▼          ▼
Skip      Update Database
```

Only meaningful activity updates reach the database.

---

# 5. Current Implementation

Current interval

```
5 Minutes
```

Example

Last Activity

```
10:00
```

Request

```
10:02
```

↓

Skip

Request

```
10:07
```

↓

Update

Database now stores

```
10:07
```

---

# 6. Request Flow

Every authenticated request follows

```
Browser

↓

Protected API

↓

JwtAuthenticationFilter

↓

Validate JWT

↓

Validate Session

↓

Update Activity (If Needed)

↓

SecurityContext

↓

Controller

↓

Business Logic
```

Activity tracking occurs before the request reaches the controller.

---

# 7. Why Inside JwtAuthenticationFilter?

Every authenticated request already passes through

```
JwtAuthenticationFilter
```

If we update activity inside controllers,

every controller must remember to call

```
updateLastActivity()
```

Example

```
ProfileController

↓

Update Activity

↓

Business Logic

------------------

NotificationController

↓

Forgot to Update

↓

Business Logic
```

Now activity becomes inconsistent.

Instead

```
JwtAuthenticationFilter

↓

Update Activity

↓

Every Controller
```

One implementation.

Every endpoint benefits.

---

# 8. Repository Implementation

Instead of

```
SELECT

↓

Compare

↓

UPDATE
```

we use one atomic SQL statement.

Example

```sql
UPDATE user_sessions

SET last_activity_at = ?

WHERE session_id = ?

AND last_activity_at < ?
```

Benefits

✓ Single SQL Statement

✓ No Entity Loading

✓ Better Performance

✓ Atomic Update

---

# 9. Service Flow

AuthenticationService

↓

Current Time

↓

Calculate Threshold

↓

Repository

↓

Atomic UPDATE

↓

Return

The service never loads the entire session entity.

---

# 10. Database

Table

```
user_sessions
```

Column

```
last_activity_at
```

Example

| Session | Last Activity |
|----------|----------------|
| Laptop | 10:05 |
| Mobile | 09:42 |
| Tablet | Yesterday |

Device Management reads this value.

---

# 11. Device Management Integration

When the user opens

```
Your Devices
```

the application displays

```
Desktop

Chrome

Windows 11

Last Active

2 Minutes Ago
```

This information comes from

```
last_activity_at
```

Without activity tracking,

every device would show only

```
Login Time
```

which is not useful.

---

# 12. Authentication Flow

```
Request

↓

JwtAuthenticationFilter

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

Load User

↓

SecurityContextHolder

↓

Controller
```

Activity is updated only for valid sessions.

---

# 13. Performance Benefits

Without Optimization

100 Requests

↓

100 UPDATE statements

With Optimization

100 Requests

↓

1 UPDATE statement

Database writes are reduced significantly.

Advantages

✓ Better Throughput

✓ Lower Database Load

✓ Faster Response Time

✓ Better Scalability

---

# 14. Security Benefits

Activity tracking enables

✓ Last Active

✓ Device Monitoring

✓ Suspicious Activity Detection

✓ Login Analytics

Future

- Inactive Session Detection

- Automatic Logout

- Risk Analysis

- Security Alerts

---

# 15. Future Improvements

Future implementation

Make the interval configurable.

Example

application.yml

```yaml
session:

  activity:

    update-interval-minutes: 5
```

Instead of hardcoding

```
Duration.ofMinutes(5)
```

Configuration makes production tuning easier.

---

# 16. Internal Sequence Diagram

```
Browser

↓

GET /profile

↓

JwtAuthenticationFilter

↓

Validate JWT

↓

Update Activity

↓

Authentication

↓

Controller

↓

Response
```

---

# 17. Why Activity Tracking Matters?

Without it

```
Current Device

Last Active

3 Days Ago
```

Even though the user is actively browsing.

With it

```
Current Device

Last Active

1 Minute Ago
```

The information becomes meaningful.

---

# 18. Interview Questions

Q1. Why track session activity?

Q2. Why not update the database on every request?

Q3. Why place activity tracking inside JwtAuthenticationFilter?

Q4. Why use an atomic UPDATE instead of SELECT then UPDATE?

Q5. Why store last_activity_at separately from login_at?

Q6. How does Google show "Last Active"?

Q7. What happens if the update interval is too small?

Q8. What happens if it is too large?

Q9. How would you make the interval configurable?

Q10. How does activity tracking improve scalability?

---

# 19. Common Mistakes

❌ Updating the database on every request

❌ Updating activity inside every controller

❌ Loading the entity before every update

❌ Forgetting to validate the session first

❌ Hardcoding values everywhere

❌ Mixing authentication logic with business logic

---

# 20. Summary

Session Activity Tracking records the most recent interaction for every
authenticated session.

Current Features

✓ last_activity_at

✓ Atomic Database Update

✓ JwtAuthenticationFilter Integration

✓ Configurable Update Strategy

✓ Device Management Integration

✓ Performance Optimization

Future Features

- Configurable Update Interval

- Automatic Idle Logout

- Inactive Session Cleanup

- Risk Analysis

- Security Alerts

- Login Analytics

Session Activity Tracking is a lightweight but essential feature that
improves both user experience and security. By updating activity only after
a configurable interval, the system remains scalable while providing accurate
"Last Active" information for device management, monitoring, and future
security enhancements.