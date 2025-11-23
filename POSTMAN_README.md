# Postman Testing Guide - Identity Service

This Postman collection and environment will help you test all the features of the Identity Service (IAM enhancements): roles, permissions, user-role associations, token generation, introspection, and more.

## Files included
- `IdentityService.postman_collection.json` - Main Postman collection with folders and requests
- `IdentityService.postman_environment.json` - Environment variables (baseUrl, tokens, IDs, credentials)

## Quick Setup
1. Import the environment file into Postman:
   - Open Postman -> Environments -> Import -> choose `IdentityService.postman_environment.json`.
2. Import the collection file into Postman:
   - Collections -> Import -> choose `IdentityService.postman_collection.json` (this collection contains built-in collection-level variables so it can run standalone).
3. Configure your `baseUrl` if the service runs on a different host/port.
4. Start the collection runner or use the Postman UI to run requests in sequence.

## Suggested Run Order
Tip: Before running the collection, run the following requests in this order to populate useful environment variables: `Admin Login (seeded admin)` -> `Admin - Utilities / Get Admin User & Role IDs (Auto detect)` -> `Auth / Login Registered User`.
1. `Auth / Register (Self) - Happy Path` -> Captures `userId` for further tests
2. `Auth / Admin Login` -> Captures `adminToken`
3. `Auth / Login Registered User` -> Captures `userToken`
4. `Admin - Roles` -> Create, get, update, delete test
5. `Admin - Permissions` -> Create, get, delete test
6. `Role/Permission Association` -> Assign and remove
7. `Admin - User Role Management` -> Create admin-created user, assign roles, remove
8. `OAuth & JWT Tests` -> Validate JWT structure and introspection
9. `Security/Edge Cases` -> Attempt unauthorized operations and protected constraints

## Notes
 - The collection includes collection variables that store `adminToken`, `userToken`, `roleId`, `permissionId`, `userId`, `createdUserId` so you do not need to import the environment file if you prefer a single-file usage.
- The default seeded admin is included in the migration: `admin` / `admin123`. If your seed differs, update the environment file.
- Some endpoints may return 200/201/400/403/404 depending on system state. The tests assert a range of valid response codes.
- Adjust or add test scripts if the service behavior or error mapping is different.

## Adding New Scenarios
- Add new request to the appropriate folder. Use `{{adminToken}}` for admin auth and `{{userToken}}` for regular user auth.
 - Use `pm.collectionVariables.set` to save values returned by the API to use in later requests when using the single collection; `pm.environment.set` still works with environment files.

## Running In Postman Collection Runner
- Choose the environment and collection, optionally set iteration counts and data files.
- Click `Run` and observe results for each request.

## Run with Newman
You can run the collection via Newman in CI or locally after installing Newman:

```
npm install -g newman
newman run IdentityService.postman_collection.json -e IdentityService.postman_environment.json --delay-request 200
```

## Exporting Test Results
- Postman Runner provides an Export option for results which can be used for CI or reporting.

## Want a Helmet? – Best Practices
- Use a test-specific DB and run in isolation.
- Reset DB between runs (e.g., run migration + seed or run in a disposable container).
- Use unique names (with a timestamp) for test-created roles/users to avoid collisions.

---

If you want, I can also generate a ready-to-run Newman command and a sample `.env` for CI pipeline integration.