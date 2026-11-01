# Spring Boot role based security API

See [API.md](API.md) for the complete endpoint reference, role matrix, DTO shapes, and Angular 17 integration examples.

This API uses stateless JWT authentication and a fixed role set stored as enum values. New accounts created through `/api/auth/signup` receive `ROLE_USER`; only an administrator can assign or change roles.

## Configuration

Set these environment variables before starting the application:

- `JWT_SECRET`: Base64 encoded random secret of at least 32 bytes. Use a new secret in each environment and rotate any secret previously committed to source control.
- `DB_URL`, `DB_USER`, `DB_PASS`: PostgreSQL connection settings. URL and username default to local PostgreSQL; `DB_PASS` is required.
- `APP_BOOTSTRAP_ENABLED`: Defaults to `false`. Set to `true` only for an intentional first-account bootstrap, together with the `BOOTSTRAP_SUPER_ADMIN_PASSWORD` value below. Bootstrap users are never created implicitly.
- `BOOTSTRAP_SUPER_ADMIN_PASSWORD`: Required only when bootstrap is explicitly enabled; use a unique password of at least 12 characters. Other optional seed roles use the corresponding `BOOTSTRAP_FACTORY_ADMIN_PASSWORD`, `BOOTSTRAP_MANAGER_PASSWORD`, `BOOTSTRAP_SUPERVISOR_PASSWORD`, `BOOTSTRAP_OPERATOR_PASSWORD`, `BOOTSTRAP_QUALITY_INSPECTOR_PASSWORD`, or `BOOTSTRAP_VIEWER_PASSWORD` variables.
- `CORS_ALLOWED_ORIGIN_PATTERNS`: Comma separated allowed origin patterns. Defaults to `http://localhost:4200`.
- `APP_DEMO_DATA_ENABLED`: Defaults to `false`; set to `true` only in a disposable local development database to insert synthetic sample rows into empty tables.

For local development, copy `.env.example` to the ignored project-root `.env`, then set a newly generated Base64 `JWT_SECRET` (at least 32 random bytes) and `DB_PASS`. Keep `.env` out of source control. Existing database rows are preserved; demo rows are opt-in. The current Hibernate `ddl-auto=update` setting remains until the deployed schema has been inventoried and a reviewed migration baseline is prepared.

The available roles are defined in `ERole`; the app does not create role rows or allow runtime role creation. On startup, existing assignments from the old `userrole` table are copied to `user_roles`; the old table is left untouched. Custom database roles that are not in `ERole` are skipped. It does not create a default admin account or include a default JWT signing secret. Docker files and the Docker based Render manifest have been removed; run the application with the Maven wrapper or your IDE.

## Endpoints

- `POST /api/auth/signup` and `POST /api/auth/signin` are public.
- `GET /api/products` is public. Reading a product by ID requires authentication; creating or updating products requires `ROLE_MODERATOR` or `ROLE_ADMIN`; deleting requires `ROLE_ADMIN`.
- `/api/admin/**` requires `ROLE_ADMIN`.
- `GET /api/admin/roles` lists the fixed enum roles. `PUT /api/admin/users/{username}/roles` assigns one or more roles using their enum names.
- The products API stores data in PostgreSQL. It seeds three examples on an empty database. `GET /api/products` is public; `GET /api/products/premium` requires `ROLE_PREMIUM_USER`; create/update requires `ROLE_MODERATOR` or `ROLE_ADMIN`; delete requires `ROLE_ADMIN`.
- Dashboards are API endpoints, not frontend pages: `/api/dashboard/user` is available to all signed-in roles; `/moderator`, `/admin`, and `/premium` require `ROLE_MODERATOR`, `ROLE_ADMIN`, and `ROLE_PREMIUM_USER`, respectively. Each returns role-appropriate metrics and a limited product list.

Example product create request (sign in as a moderator or admin and send the JWT as a bearer token):

```json
{
  "name": "Wireless Mouse",
  "category": "Accessories",
  "price": 24.99,
  "description": "Wireless optical mouse",
  "stock": 25,
  "active": true,
  "premium": false,
  "imageUrl": "https://example.com/mouse.jpg"
}
```

To promote a test account to moderator, an administrator can send `PUT /api/admin/users/{username}/roles` with `{"roles":["ROLE_MODERATOR"]}`. The admin can also use `ROLE_PREMIUM_USER` to test the premium catalog.
- `/api/users/**` requires authentication. Users can read or update their own profile; administrator only actions are annotated in the controller.
- All other endpoints require authentication, with their role checks applied by controller method security.

Send the login token on protected requests as `Authorization: Bearer <token>`.
