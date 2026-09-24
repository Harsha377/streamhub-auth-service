# 08 - Device Detection

---

# 1. Introduction

Device Detection is responsible for identifying the device from which a user
is accessing the application.

Whenever a user successfully authenticates, the Authentication Service
captures information about the client's device and stores it in the
`user_sessions` table.

Instead of storing

```
Unknown
```

the application stores meaningful information such as

- Desktop
- Laptop
- Samsung Galaxy S24 Ultra
- iPhone 15 Pro

Along with

- Browser
- Operating System
- IP Address

This information is later used by the Device Management module.

---

# 2. Why Device Detection?

Suppose a user opens

Google Account

↓

Security

↓

Your Devices

Google displays

```
Windows 11
Chrome
Bengaluru

Android 15
Chrome Mobile

MacBook Pro
Safari
```

How does Google know this?

Every login request contains information about the client's browser.

Our Authentication Service performs the same process.

---

# 3. Why Store Device Information?

Without Device Detection

```
Current Sessions

Unknown

Unknown

Unknown
```

The user cannot identify which device should be logged out.

With Device Detection

```
Current Sessions

Desktop
Chrome
Windows 11

Samsung Galaxy S24 Ultra
Chrome Mobile
Android 15

MacBook Pro
Safari
macOS
```

Now users can easily manage their devices.

---

# 4. Device Detection Flow

User Login

↓

Browser sends HTTP Request

↓

User-Agent Header

↓

DeviceInfoService

↓

User-Agent Parser

↓

Extract Device Information

↓

Store in user_sessions

↓

Device Management APIs

---

# 5. Where Does Device Information Come From?

The frontend does **not** send

```
deviceName

browser

operatingSystem
```

Instead,

every browser automatically includes

```
User-Agent
```

inside every HTTP request.

Example

```http
POST /api/v1/auth/verify-otp

User-Agent:

Mozilla/5.0 (Windows NT 10.0; Win64; x64)

AppleWebKit/537.36

Chrome/138.0.0.0

Safari/537.36
```

The browser adds this header automatically.

No frontend code is required.

---

# 6. Request Flow

Browser

↓

Automatically Adds

User-Agent Header

↓

Tomcat

↓

HttpServletRequest

↓

AuthenticationService

↓

DeviceInfoService

↓

User-Agent Parser

↓

DeviceInfo

↓

UserSession

↓

Database

---

# 7. Internal Components

AuthenticationService

↓

DeviceInfoService

↓

User-Agent Parser (YAUAA)

↓

DeviceInfo DTO

↓

UserSession

---

# 8. DeviceInfoService

Responsibilities

- Read User-Agent Header
- Parse Browser
- Parse Operating System
- Parse Device Name
- Extract Client IP
- Build DeviceInfo

The service hides all parsing logic from the AuthenticationService.

---

# 9. DeviceInfo DTO

Contains

- Device Name
- Browser
- Operating System
- IP Address

Example

```text
Device Name

Desktop

Browser

Chrome

Operating System

Windows 11

IP Address

192.168.1.15
```

The AuthenticationService simply stores these values.

---

# 10. Authentication Flow

OTP Verified

↓

AuthenticationService

↓

Generate Session

↓

DeviceInfoService

↓

Extract Device Information

↓

Create UserSession

↓

Store Device Information

↓

Generate Tokens

↓

Return Cookies

---

# 11. User-Agent Parsing

Example

Input

```
Mozilla/5.0

Windows NT 10.0

Chrome/138.0

Safari/537.36
```

Parser

↓

Recognizes

Operating System

↓

Windows 11

Browser

↓

Chrome

Device

↓

Desktop

---

# 12. IP Address Detection

The client IP is also stored.

Current Implementation

```
request.getRemoteAddr()
```

Production

If behind

- Nginx
- API Gateway
- Load Balancer

The application should first check

```
X-Forwarded-For
```

If unavailable

↓

Fallback

```
request.getRemoteAddr()
```

---

# 13. Database

user_sessions

Stores

```
device_name

browser

operating_system

ip_address
```

Example

| Session | Device | Browser | OS |
|----------|---------|----------|---------|
| A | Desktop | Chrome | Windows 11 |
| B | Galaxy S24 Ultra | Chrome Mobile | Android 15 |
| C | MacBook Pro | Safari | macOS |

---

# 14. Device Management

Later,

GET

```
/auth/sessions
```

reads

```
user_sessions
```

and returns

```json
[
  {
    "deviceName":"Desktop",
    "browser":"Chrome",
    "operatingSystem":"Windows 11",
    "currentDevice":true
  },
  {
    "deviceName":"Samsung Galaxy S24 Ultra",
    "browser":"Chrome Mobile",
    "operatingSystem":"Android 15",
    "currentDevice":false
  }
]
```

Without Device Detection,

this API would not be meaningful.

---

# 15. Why Separate DeviceInfoService?

Instead of

AuthenticationService

↓

1000 lines

↓

User-Agent Parsing

↓

Authentication

↓

Session Creation

↓

Cookies

We separate responsibilities.

AuthenticationService

↓

DeviceInfoService

↓

Returns DeviceInfo

Advantages

✓ Cleaner Code

✓ Easier Testing

✓ Single Responsibility Principle

✓ Reusable

---

# 16. Security Benefits

Device Detection enables

✓ Device Management

✓ Login History

✓ Security Notifications

✓ Unknown Device Detection

✓ Session Monitoring

Future

- Risk-based Authentication
- Device Fingerprinting
- Impossible Travel Detection
- Trusted Devices

---

# 17. Production Improvements

Future

Store

- Country
- City
- ISP
- Time Zone

Using

Geo-IP Service

Example

```
Chrome

Windows 11

Bengaluru

India
```

Email Notification

```
New login detected

Chrome

Windows 11

Bengaluru
```

---

# 18. Internal Sequence Diagram

Browser

↓

HTTP Request

↓

User-Agent Header

↓

Tomcat

↓

HttpServletRequest

↓

DeviceInfoService

↓

User-Agent Parser

↓

DeviceInfo

↓

AuthenticationService

↓

UserSession

↓

Database

---

# 19. Interview Questions

Q1. How does the backend know the user's browser?

Q2. What is the User-Agent header?

Q3. Why doesn't the frontend send device information?

Q4. Why create a separate DeviceInfoService?

Q5. Why store device information in user_sessions?

Q6. What is YAUAA?

Q7. Why use X-Forwarded-For?

Q8. Can User-Agent be spoofed?

Q9. Why is device information useful?

Q10. How would you implement Trusted Devices?

---

# 20. Common Mistakes

❌ Trusting client-provided device information

❌ Parsing User-Agent inside controllers

❌ Hardcoding browser names

❌ Ignoring proxy headers

❌ Mixing authentication logic with device parsing

❌ Returning internal parsing details to clients

---

# 21. Summary

Device Detection enriches every authenticated session with meaningful
information about the client's environment.

Current Features

✓ User-Agent Parsing

✓ Browser Detection

✓ Operating System Detection

✓ Device Detection

✓ Client IP Storage

✓ DeviceInfoService

✓ Integration with Session Management

Future Features

- Trusted Devices

- Device Fingerprinting

- Geo-IP Detection

- Login Notifications

- Risk-based Authentication

- Security Alerts

Device Detection is not responsible for authentication itself. Its role is to **collect and persist device metadata** so that other modules—such as Session Management, Device Management APIs, Security Auditing, and future Trusted Device features—can make informed security decisions.