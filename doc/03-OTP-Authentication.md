# 03 - OTP Authentication

---

# 1. Introduction

OTP (One-Time Password) Authentication is the first security layer of the
StreamHub Authentication Service.

Instead of asking users to remember passwords, the application verifies
ownership of an email address by sending a temporary verification code.

Only users who successfully verify the OTP are allowed to continue with
registration or login.

---

# 2. Why OTP Authentication?

Traditional systems require users to remember passwords.

Problems

• Weak passwords

• Password reuse

• Forgotten passwords

• Password reset complexity

• Credential leaks

OTP Authentication removes these problems by verifying that the user owns
the email address.

Advantages

- Simple user experience
- No password management
- Secure verification
- Short-lived credentials

---

# 3. Authentication Flow

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

User receives OTP

↓

POST /auth/verify-otp

↓

Validate OTP

↓

OTP Correct?

        │
┌────┴─────┐
│          │
No          Yes
│          │
▼          ▼
Return Error Continue

↓

Existing User?

        │
┌────┴──────┐
│           │
No           Yes
│           │
▼           ▼
Register   Authenticate

---

# 4. APIs

## Send OTP

POST

```
/api/v1/auth/send-otp
```

Request

```json
{
    "email":"harsha@gmail.com"
}
```

Response

```json
{
    "message":"OTP sent successfully."
}
```

---

## Verify OTP

POST

```
/api/v1/auth/verify-otp
```

Request

```json
{
    "email":"harsha@gmail.com",
    "otp":"123456"
}
```

Possible Results

New User

↓

OTP Verified

↓

Registration Required

Existing User

↓

OTP Verified

↓

Authenticated

↓

Access Token Cookie

↓

Refresh Token Cookie

---

# 5. Internal Components

OTP Module consists of

OtpController

↓

OtpService

↓

OtpGenerator

↓

Redis

↓

EmailService

---

# 6. Send OTP Internal Flow

Controller

↓

Validate Request

↓

OtpService.sendOtp()

↓

Generate OTP

↓

Generate Redis Key

↓

Store OTP

↓

Send Email

↓

Return Success

---

# 7. Verify OTP Internal Flow

Controller

↓

OtpService.verifyOtp()

↓

Read OTP from Redis

↓

OTP Exists?

↓

Compare OTP

↓

Delete OTP

↓

Store VERIFIED Flag

↓

Find User

↓

Existing User?

↓

Authenticate

OR

Registration Required

---

# 8. Redis Usage

Redis stores temporary authentication data.

Keys

otp:user@email.com

verified:user@email.com

OTP TTL

5 Minutes

Verified Flag TTL

10 Minutes

Redis automatically removes expired keys.

---

# 9. Why Redis?

Instead of PostgreSQL.

Problems with Database

Every OTP

↓

INSERT

↓

UPDATE

↓

DELETE

Large number of writes.

Redis

Memory

↓

Fast

↓

Automatic Expiration

↓

No Cleanup Required

Advantages

- Very Fast
- Built-in TTL
- Low Latency
- Perfect for temporary data

---

# 10. OTP Generation

OTP Generator

↓

SecureRandom

↓

Numeric OTP

↓

Length configurable

Example

```
483921
```

Characteristics

- Random
- Unpredictable
- Short-lived

---

# 11. Email Delivery

OTP

↓

EmailService

↓

SMTP

↓

Mail Provider

↓

Inbox

The OTP is never returned in the API response.

---

# 12. Error Scenarios

OTP Expired

↓

410 Gone

Invalid OTP

↓

400 Bad Request

User Not Registered

↓

Registration Required

Email Sending Failed

↓

500 Internal Server Error

---

# 13. Security Considerations

Implemented

✓ Redis TTL

✓ OTP deleted after verification

✓ One-time usage

✓ Email ownership verification

Future

□ Rate Limiting

□ Resend Cooldown

□ Maximum Attempts

□ OTP Audit Logs

□ CAPTCHA

---

# 14. Production Improvements

Future enhancements

• OTP resend limit

• Device-aware OTP

• Country restrictions

• Disposable email detection

• Fraud detection

• IP-based throttling

---

# 15. Sequence Diagram

Client

↓

Send OTP

↓

OtpController

↓

OtpService

↓

Redis

↓

EmailService

↓

SMTP

↓

User

↓

Verify OTP

↓

Redis

↓

User Lookup

↓

Authentication

↓

Registration

---

# 16. Interview Questions

Q1. Why Redis instead of PostgreSQL?

Q2. Why delete OTP after verification?

Q3. Why should OTP have an expiry?

Q4. Why shouldn't OTP be returned in the response?

Q5. What happens if Redis goes down?

Q6. How would you implement OTP rate limiting?

Q7. How would you prevent OTP brute-force attacks?

---

# 17. Common Mistakes

❌ Storing OTP permanently

❌ Long OTP expiry

❌ Reusing OTP

❌ Logging OTP

❌ Returning OTP in API response

❌ Unlimited resend

---

# 18. Summary

The OTP Authentication module verifies ownership of an email address before
allowing registration or authentication.

Current Features

✓ OTP Generation

✓ Redis Storage

✓ Email Delivery

✓ Verification

✓ Automatic Expiration

✓ One-Time Usage

Future Features

- Rate Limiting

- Retry Limits

- Fraud Detection

- Security Auditing

- CAPTCHA

- Risk-based Verification