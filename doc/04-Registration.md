# 04 - Registration

---

# 1. Introduction

Registration is the process of creating a new user account after the
user has successfully verified their email using OTP.

The Authentication Service never allows direct registration.

Every registration must first pass OTP verification.

This ensures that only users who own the email address can create an account.

---

# 2. Why Registration After OTP?

Many applications allow anyone to register with any email address.

Example

User

↓

Enter Email

↓

Register

↓

Fake Email

↓

Account Created

This creates problems:

• Fake accounts

• Spam

• Invalid email addresses

• Email verification later

Our flow is different.

User

↓

Verify Email

↓

Registration

Only verified users can register.

Advantages

✓ Valid email

✓ Better security

✓ No fake accounts

✓ Better user experience

---

# 3. Registration Flow

User

↓

Send OTP

↓

Verify OTP

↓

Verified Flag Stored (Redis)

↓

POST /auth/register

↓

Read Verified Flag

↓

Flag Exists?

        │
┌────┴─────┐
│          │
No          Yes
│          │
▼          ▼
Reject    Continue

↓

Email Already Exists?

        │
┌────┴──────┐
│           │
Yes          No
│           │
▼           ▼
409 Conflict  Create User

↓

Save User

↓

Authenticate

↓

Create Session

↓

Generate Tokens

↓

Return Cookies

---

# 4. API

POST

```
/api/v1/auth/register
```

Request

```json
{
    "fullName":"Harsha",
    "email":"harsha@gmail.com"
}
```

Response

```json
{
    "registered":true,
    "authenticated":true,
    "message":"Registration completed successfully."
}
```

---

# 5. Internal Components

RegistrationController

↓

RegistrationService

↓

UserRepository

↓

AuthenticationService

↓

UserSessionRepository

---

# 6. Internal Flow

Controller

↓

Validate DTO

↓

RegistrationService

↓

Check Redis Verified Flag

↓

Validate User Doesn't Exist

↓

Create User Entity

↓

Save User

↓

Delete Verified Flag

↓

Authenticate User

↓

Generate JWT

↓

Create Session

↓

Return Cookies

---

# 7. User Entity

Current Information

• User ID

• Full Name

• Email

• Created At

Future Fields

• Profile Picture

• Phone Number

• Status

• Last Login

• Email Verified

---

# 8. Why Redis Verified Flag?

OTP verification and registration are two separate requests.

Request 1

Verify OTP

↓

Success

↓

Store

verified:user@email.com

↓

10 Minutes TTL

Request 2

Register

↓

Read Verified Flag

↓

Allow Registration

Without this flag,

anyone could call the registration API directly.

---

# 9. Database Flow

PostgreSQL

users

↓

INSERT

↓

Authentication

↓

user_sessions

↓

INSERT

One registration creates

One User

One Session

---

# 10. Authentication After Registration

Instead of asking the user to login again,

Registration automatically authenticates them.

Flow

Register

↓

Create User

↓

AuthenticationService

↓

Generate Session

↓

JWT

↓

Cookies

↓

User Already Logged In

This provides a much better user experience.

---

# 11. Validation

Registration validates

✓ Verified Email

✓ Duplicate Email

✓ Valid Name

✓ Valid Email Format

✓ Required Fields

---

# 12. Error Scenarios

Email Not Verified

↓

403 Forbidden

Duplicate Email

↓

409 Conflict

Invalid Request

↓

400 Bad Request

Database Error

↓

500 Internal Server Error

---

# 13. Security Considerations

Implemented

✓ Verified email required

✓ Duplicate prevention

✓ Automatic authentication

✓ Secure cookies

Future

□ Disposable email detection

□ Domain blacklist

□ Invite-only registration

□ Registration audit logs

---

# 14. Production Improvements

Future

• Referral System

• Invite Codes

• Profile Completion

• Welcome Email

• User Preferences

• Organization Registration

---

# 15. Sequence Diagram

Client

↓

Verify OTP

↓

Redis

↓

Register

↓

RegistrationService

↓

UserRepository

↓

AuthenticationService

↓

JWT

↓

Cookies

↓

Client Logged In

---

# 16. Interview Questions

Q1. Why verify OTP before registration?

Q2. Why store a verified flag in Redis?

Q3. Why authenticate immediately after registration?

Q4. Why prevent duplicate emails?

Q5. Why use Redis instead of passing a boolean from the client?

Q6. What happens if Redis expires before registration?

---

# 17. Common Mistakes

❌ Register without OTP

❌ Returning JWT in response body

❌ Allow duplicate email

❌ Never deleting verified flag

❌ Trusting frontend verification

❌ Creating session before saving user

---

# 18. Summary

The Registration module creates new users only after successful OTP
verification.

Current Features

✓ OTP Verification Required

✓ Redis Verified Flag

✓ Duplicate Prevention

✓ User Creation

✓ Automatic Authentication

✓ JWT Generation

✓ Session Creation

Future Features

- Organization Registration

- Invite System

- Registration Analytics

- Audit Logging

- Domain Restrictions