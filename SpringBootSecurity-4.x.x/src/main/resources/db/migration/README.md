# Flyway Migrations (Phase 1)

Flyway is enabled with `baseline-on-migrate=true` and `baseline-version=0`.

## For existing databases (already has schema from ddl-auto=update)
Flyway will automatically baseline at version 0 on first start. No migration needed.
Future schema changes go in `V2__xxx.sql`, `V3__xxx.sql`, etc.

## For fresh databases
Generate the baseline schema from a running dev instance:
1. Temporarily set `spring.jpa.hibernate.ddl-auto=create` in a dev profile.
2. Start the app against an empty PostgreSQL DB.
3. Run: `pg_dump --schema-only --no-owner <dbname> > V1__baseline.sql`
4. Place `V1__baseline.sql` in this directory.
5. Restore `ddl-auto=validate`.

## Naming convention
`V<version>__<description>.sql` — e.g., `V2__add_shipment_table.sql`
Never edit a migration after it has been applied to any environment.
