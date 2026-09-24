# 05 - JWT Authentication

---

# 1. Introduction

JWT (JSON Web Token) Authentication is responsible for securely identifying
authenticated users without storing server-side HTTP sessions.

After successful authentication, the Authentication Service generates:

• Access Token
• Refresh Token

The Access Token is used to access protected APIs.

The Refresh Token is used to obtain a new Access Token when the current
Access Token expires.

Both tokens are stored in secure HttpOnly cookies.

---

# 2. Why JWT?

Traditional Authentication

User

↓

Login

↓

Server creates HTTP Session

↓

Session stored in Memory

↓

Every request checks server memory

Problems

• Server memory increases

• Doesn't scale well

• Sticky sessions required

• Difficult in Microservices

JWT Authentication

User

↓

Authenticate

↓

Generate JWT

↓

Store in Browser Cookie

↓

Every request sends JWT

↓

Server validates JWT

Advantages

✓ Stateless

✓ Scalable

✓ Works across Microservices

✓ No HTTP Session

---

# 3. Authentication Flow

User

↓

Verify OTP

↓

Authenticate

↓

Generate Session ID

↓

Generate Access Token

↓

Generate Refresh Token

↓

Hash Refresh Token

↓

Store Session

↓

Return HttpOnly Cookies

↓

Protected APIs

---

# 4. Token Types

The system generates two tokens.

## Access Token

Purpose

Authenticate every API request.

Characteristics

• JWT

• Short Expiry

• Contains User Information

• Digitally Signed

---

## Refresh Token

Purpose

Generate new Access Tokens.

Characteristics

• Random String

• Long Expiry

• Stored as SHA-256 Hash

• Never exposed from Database

---

# 5. Why Two Tokens?

Without Refresh Token

Access Token expires

↓

User logs in again

Poor User Experience

With Refresh Token

Access Token expires

↓

Refresh API

↓

New Access Token

↓

Continue Working

---

# 6. JWT Structure

JWT consists of three parts.

Header

↓

Payload

↓

Signature

Example

```
xxxxx.yyyyy.zzzzz
```

---

# 7. Header

Contains

```json
{
  "alg":"HS256",
  "typ":"JWT"
}
```

Meaning

alg

Signing Algorithm

typ

Token Type

---

# 8. Payload

Our Access Token contains

```json
{
   "sub":"harsha@gmail.com",
   "userId":1,
   "sessionId":"550e8400-e29b",
   "iat":1740000000,
   "exp":1740000900
}
```

Claims

sub

Email

userId

Database User ID

sessionId

Current Login Session

iat

Issued Time

exp

Expiration Time

---

# 9. Why Session ID Inside JWT?

Every login creates a new session.

Example

Laptop

↓

Session A

Phone

↓

Session B

Tablet

↓

Session C

Each JWT stores its own Session ID.

Benefits

✓ Logout One Device

✓ Logout All Devices

✓ Device Tracking

✓ Refresh Rotation

---

# 10. JWT Generation Flow

AuthenticationService

↓

Generate Session ID

↓

Generate JWT Claims

↓

JwtService

↓

Sign Token

↓

Return Access Token

---

# 11. JWT Validation Flow

Incoming Request

↓

Read Cookie

↓

Extract Access Token

↓

Validate Signature

↓

Validate Expiration

↓

Extract Claims

↓

Extract Session ID

↓

Find User Session

↓

Continue Authentication

---

# 12. Why HttpOnly Cookies?

Many tutorials return

```json
{
   "accessToken":"..."
}
```

Problems

JavaScript can read it.

XSS Attack

↓

Token Stolen

Instead

HttpOnly Cookie

↓

JavaScript Cannot Read

↓

Browser Sends Automatically

Safer

---

# 13. Why Secure Cookies?

Secure Cookie

↓

Only HTTPS

↓

Cannot travel over HTTP

Prevents token leakage.

---

# 14. Why SameSite?

SameSite protects against CSRF attacks.

Options

Strict

Lax

None

Production

SameSite=None

Secure=true

HTTPS

---

# 15. Token Expiry

Access Token

15 Minutes

Refresh Token

30 Days

Reason

Access Token

↓

Short Life

↓

Lower Risk

Refresh Token

↓

Long Life

↓

Better User Experience

---

# 16. Internal Components

AuthenticationService

↓

JwtService

↓

CookieUtil

↓

UserSessionRepository

---

# 17. Request Flow

Browser

↓

GET /profile

↓

Cookie

↓

Access Token

↓

JwtAuthenticationFilter

↓

JwtService

↓

Controller

---

# 18. Error Scenarios

Expired Token

↓

401 Unauthorized

Invalid Signature

↓

401 Unauthorized

Missing Cookie

↓

401 Unauthorized

Session Revoked

↓

401 Unauthorized

---

# 19. Security Considerations

Implemented

✓ Signed JWT

✓ Session ID

✓ Short-lived Access Token

✓ Refresh Rotation

✓ HttpOnly Cookies

✓ Secure Cookies

Future

□ Replay Detection

□ Key Rotation

□ RS256

□ Token Versioning

---

# 20. Production Improvements

Future

• Asymmetric Keys (RS256)

• JWKS Endpoint

• Key Rotation

• Distributed Verification

• Token Introspection

• OAuth2 Support

---

# 21. Sequence Diagram

Verify OTP

↓

AuthenticationService

↓

JwtService

↓

Generate JWT

↓

CookieUtil

↓

Browser

↓

Protected API

↓

JwtAuthenticationFilter

↓

JwtService

↓

Authentication

---

# 22. Interview Questions

Q1. What is JWT?

Q2. Why JWT over HTTP Sessions?

Q3. Why separate Access and Refresh Tokens?

Q4. Why store Session ID inside JWT?

Q5. Why store Refresh Token Hash?

Q6. Why use HttpOnly Cookies?

Q7. Why should Access Tokens expire quickly?

Q8. What happens when JWT expires?

Q9. Why validate both JWT and Session?

Q10. Why not store JWT in Local Storage?

---

# 23. Common Mistakes

❌ Long-lived Access Tokens

❌ Returning Refresh Token in JSON

❌ Storing Refresh Token in Database without hashing

❌ Using Local Storage

❌ No Session Validation

❌ No Refresh Rotation

❌ Using one token for everything

---

# 24. Summary

JWT Authentication provides stateless authentication for the entire
StreamHub platform.

Current Features

✓ Access Token

✓ Refresh Token

✓ JWT Claims

✓ Session ID

✓ HttpOnly Cookies

✓ Secure Cookies

✓ JWT Validation

✓ Session Validation

Future Features

- RS256

- Key Rotation

- Replay Protection

- OAuth2

- JWKS

- Token Versioning