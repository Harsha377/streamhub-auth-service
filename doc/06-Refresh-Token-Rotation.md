# 06 - Refresh Token Rotation

---

# 1. Introduction

Access Tokens are intentionally short-lived to reduce the impact of token theft.

However, asking users to log in every 15 minutes would create a poor user
experience.

To solve this problem, the Authentication Service uses **Refresh Token
Rotation**.

Instead of reusing the same Refresh Token forever, every successful refresh
request generates:

- A new Access Token
- A new Refresh Token

The previous Refresh Token immediately becomes invalid.

This approach significantly improves security while maintaining a seamless
user experience.

---

# 2. Why Refresh Tokens?

Suppose the Access Token expires after 15 minutes.

Without Refresh Tokens

User

↓

Login

↓

Access Token

↓

15 Minutes Later

↓

Expired

↓

Login Again

Poor User Experience

With Refresh Tokens

User

↓

Login

↓

Access Token

↓

Expires

↓

Refresh API

↓

New Access Token

↓

Continue Working

The user never notices the token renewal.

---

# 3. Why Token Rotation?

Many beginners implement refresh tokens like this.

Login

↓

Generate Refresh Token

↓

Store Forever

↓

Reuse Forever

Problem

If the Refresh Token is stolen,

the attacker can continue generating new Access Tokens until the token expires.

Instead we rotate it.

Login

↓

Refresh Token A

↓

Refresh

↓

Refresh Token B

↓

Refresh

↓

Refresh Token C

Token A

↓

Invalid

Token B

↓

Invalid

Only the latest Refresh Token remains valid.

---

# 4. Refresh Flow

Browser

↓

Access Token Expired

↓

POST /auth/refresh

↓

Read Refresh Cookie

↓

Hash Refresh Token

↓

Find Session

↓

Validate Session

↓

Generate New Access Token

↓

Generate New Refresh Token

↓

Hash New Refresh Token

↓

Update Session

↓

Replace Cookies

↓

Return Success

---

# 5. API

POST

```
/api/v1/auth/refresh
```

Request

No request body.

The browser automatically sends

HttpOnly Cookies.

Response

```
204 No Content
```

or

```
200 OK
```

depending on API design.

New cookies are returned automatically.

---

# 6. Internal Components

AuthenticationController

↓

AuthenticationService

↓

JwtService

↓

UserSessionRepository

↓

CookieUtil

---

# 7. Refresh Authentication Flow

AuthenticationService

↓

Read Refresh Cookie

↓

Hash Refresh Token

↓

Find UserSession

↓

Session Found?

↓

Validate Session

↓

Generate Access Token

↓

Generate Refresh Token

↓

Hash Refresh Token

↓

Update Database

↓

Replace Cookies

---

# 8. Why Hash Refresh Tokens?

Never store

```
ABCD123456XYZ
```

inside the database.

Instead

SHA-256

↓

3AF92D76...

Store only the hash.

Benefits

✓ Database leak protection

✓ Token cannot be recovered

✓ Same approach used for passwords

---

# 9. Why Find Session Using Hash?

Browser

↓

Raw Refresh Token

↓

SHA-256

↓

Database Lookup

↓

Matching Hash

This means the raw Refresh Token is never persisted.

---

# 10. Session Validation

After finding the session we verify

✓ Session Exists

✓ Not Revoked

✓ Not Expired

If any validation fails

↓

401 Unauthorized

---

# 11. Token Rotation

Old Refresh Token

↓

Generate New Refresh Token

↓

Hash

↓

Replace Existing Hash

↓

Save Session

The previous Refresh Token can never be used again.

---

# 12. Cookie Replacement

Old Cookies

ACCESS_TOKEN

REFRESH_TOKEN

↓

Response

↓

New Cookies

ACCESS_TOKEN

REFRESH_TOKEN

The browser replaces the old cookies automatically.

---

# 13. Database Changes

Before Refresh

Refresh Hash

↓

HASH_A

After Refresh

Refresh Hash

↓

HASH_B

HASH_A

↓

Invalid

---

# 14. Internal Sequence Diagram

Browser

↓

Refresh Cookie

↓

AuthenticationController

↓

AuthenticationService

↓

SHA-256

↓

UserSessionRepository

↓

Validate Session

↓

JwtService

↓

Generate Tokens

↓

Update Session

↓

CookieUtil

↓

Browser

---

# 15. Why Rotation Improves Security?

Without Rotation

Refresh Token

↓

Reuse Forever

↓

Attacker Can Use It

With Rotation

Refresh Token A

↓

Refresh

↓

Refresh Token B

↓

Refresh Token A

↓

Rejected

Only one Refresh Token remains valid.

---

# 16. Error Scenarios

Missing Refresh Cookie

↓

401 Unauthorized

Invalid Refresh Token

↓

401 Unauthorized

Session Revoked

↓

401 Unauthorized

Expired Session

↓

401 Unauthorized

Hash Not Found

↓

401 Unauthorized

---

# 17. Security Considerations

Implemented

✓ Refresh Token Rotation

✓ SHA-256 Hash

✓ Session Validation

✓ HttpOnly Cookies

✓ Secure Cookies

✓ Replace Old Token

Future

□ Replay Detection

□ Token Theft Detection

□ Security Events

□ Audit Logging

□ Key Rotation

---

# 18. Production Improvements

Future

Replay Protection

↓

Detect Reused Refresh Token

↓

Possible Token Theft

↓

Revoke Session

↓

Force Login

↓

Security Event

This is how Google, Microsoft, and Auth0 detect stolen Refresh Tokens.

---

# 19. Interview Questions

Q1. Why not use only Access Tokens?

Q2. Why use Refresh Tokens?

Q3. Why rotate Refresh Tokens?

Q4. Why hash Refresh Tokens?

Q5. Why validate the session during refresh?

Q6. What happens if the Refresh Token is stolen?

Q7. Why shouldn't Refresh Tokens be JWTs?

Q8. What is Refresh Token Replay Detection?

Q9. Why replace the Refresh Token after every refresh?

Q10. Why use HttpOnly cookies for Refresh Tokens?

---

# 20. Common Mistakes

❌ Reusing the same Refresh Token forever

❌ Storing raw Refresh Tokens

❌ Returning Refresh Tokens in JSON

❌ No session validation

❌ Long-lived Access Tokens

❌ No Refresh Token expiration

❌ Using Local Storage

---

# 21. Summary

Refresh Token Rotation enables long-lived authenticated sessions while
maintaining strong security.

Current Features

✓ Refresh Token Rotation

✓ SHA-256 Hashing

✓ Session Validation

✓ Cookie Replacement

✓ Automatic Token Renewal

✓ One Active Refresh Token Per Session

Future Features

- Replay Detection

- Token Theft Detection

- Security Events

- Audit Logs

- RS256 Key Rotation

- JWKS