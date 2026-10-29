# VeynaX Automotive ERP — Phase 0 Read-Only Assessment
**Repo:** https://github.com/Ajava007/SirCopy.git
**Branch assessed:** `main` (commit 5957be60 "first commit")
**Date:** 2026-10-29
**Mode:** Read-only. No files changed in this assessment. (A separate `angular-22-upgrade` branch contains an earlier UI upgrade experiment — it does not affect `main`.)

---

## 1. What the existing system demonstrably does

### 1.1 Confirmed stack (from source)
| Layer | Confirmed |
|---|---|
| Backend | Spring Boot **4.1.1**, Java 17, Maven |
| Database | PostgreSQL (dialect confirmed in application.properties), `ddl-auto=update` |
| Auth | JWT (jjwt), stateless, BCrypt passwords, 24h token, no refresh token |
| Roles | 7 roles: `SUPER_ADMIN, FACTORY_ADMIN, MANAGER, SUPERVISOR, OPERATOR, QUALITY_INSPECTOR, VIEWER` |
| ORM | Spring Data JPA, Hibernate, auditing (`@CreatedDate`/`@LastModifiedDate` via AuditingConfig) |
| API docs | springdoc OpenAPI (`/v3/api-docs`, Swagger UI) |
| Frontend | Angular 17.3 (standalone components, functional interceptors, lazy routes, esbuild builder), Bootstrap 5, Bootstrap Icons, RxJS 7.8, Karma/Jasmine, Playwright |
| Frontend pages | 20+ routes: dashboard, production, quality, maintenance, inventory, supply-chain, sales-payments, reports, master-data (orgs/factories/vehicle-models), engineering, workflow, scheduling, approval-queues, VIN traceability, vehicle-config, finished-vehicles, workforce, safety, audit-log, not-found |

### 1.2 Confirmed backend modules (23 controllers, ~40 entities, ~30 services)
| Blueprint module | Exists? | Evidence |
|---|---|---|
| A. Vehicle engineering / master data | **Partial** | `VehicleModelController`, `BomController` (BomRow), `RoutingController` (RoutingOperation), `PartController` (Part), `FactoryController`, `OrganizationController` |
| B. Dealer sales / customer portal | **Partial** | `SalesController`, `SalesOrder`, `SalesPayment`, `Customer`, `Dealer` entities. No quotation, price-list, dealer-auth-scoping confirmed. |
| C. Forecasting / production planning | **Partial** | `ProductionPlan` entity, `MrpService` (MRP run), `ProductionPlanService`, `ProductionController`. No forecast-combining logic confirmed. |
| D. Procurement / supplier | **Partial** | `SupplierController`, `PurchaseOrderController`, `PurchaseOrder` + `PurchaseOrderItem`, `Supplier` |
| E. Import & shipment tracking | **Missing** | No Shipment, Container, Vessel, Customs, LandedCost entity. No import controller. |
| F. Receiving / damage / inventory | **Partial** | `InventoryController`, `InventoryItem`, `StockTransaction` (reserve/issue/transfer/adjust). No damage-record, claim, quarantine, serial-number tracking. |
| G. Assembly / shop-floor | **Partial** | `WorkOrder` + `WorkOrderOperation`, `ProductionController`, `DowntimeService` (DowntimeEntry), `ShiftService` (ShiftAssignment). No workstation operator screen confirmed. |
| H. Quality management | **Partial** | `QualityController`, `NcrService` (NcrRecord), `InspectionService` (Inspection), `QualityGateService`, `Defect` entity. No rework loop, no cost-of-quality. |
| I. VIN & vehicle genealogy | **Missing** | No VIN entity, no genealogy table. Frontend has a `vin-traceability` page but no backend endpoint confirmed. |
| J. Finance & dealer payments | **Minimal** | `SalesPayment` only. No Invoice, CreditNote, Receipt, Refund, LandedCost, AccountBalance, AgingReport. |
| K. Delivery / warranty / after-sales | **Minimal** | `YardSlot` (yard map), finished-vehicle page. No Warranty, Claim, Delivery record. |
| L. HR / maintenance / safety / compliance | **Partial** | `WorkforceController` (Employee, Certification), `MaintenanceController` (Asset, MaintenanceWorkOrder), `ReportController` (GeneratedReport). No SafetyIncident, no LegalDocument register. |

### 1.3 Confirmed cross-module workflows (from service code)
1. **Work Order state machine** — `WorkOrderService` enforces status transitions (CREATED→RELEASED→IN_PROGRESS→COMPLETED/CLOSED). Throws `IllegalStateException` on invalid transitions.
2. **Purchase Order** — `PurchaseOrderService` with line items, status tracking.
3. **Sales Order → Payment** — `SalesOrder` linked to `SalesPayment`. Payment status tracked.
4. **Quality gate** — `QualityGateService` checks VIN against inspection results.
5. **NCR → Defect** — `NcrService` links nonconformance to defects.
6. **MRP** — `MrpService` runs material requirements planning (BOM explosion implied).
7. **Bootstrap data** — `BootstrapAdminInitializer` seeds 7 users (one per role) on first start. `OperationalDataInitializer`, `ProductDataInitializer`, `RoleDataMigration` seed demo data.

---

## 2. What is incomplete, broken, or unverified

### 2.1 Critical / blocking
| ID | Issue | Evidence | Impact |
|---|---|---|---|
| C1 | **Two `SecurityConfig` classes** — `config/SecurityConfig.java` references a non-existent `JwtAuthFilter` class (actual filter is `JwtAuthenticationFilter`). | `config/SecurityConfig.java` line: `private final JwtAuthFilter jwtAuthFilter;` — no such class in repo. | **Backend will not compile.** Must remove or fix. |
| C2 | **GlobalExceptionHandler maps only `NotFoundException`** — services throw `IllegalStateException` (invalid transitions), `IllegalArgumentException` (duplicate username/email), `EntityNotFoundException`. All become 500 with no detail. | `util/GlobalExceptionHandler.java` — single `@ExceptionHandler`. | UI cannot show "invalid state transition" or "duplicate PO number". |
| C3 | **No tests** — no `src/test` directory in repo. | `find src/test` → empty. | No regression safety net for any change. |
| C4 | **No database migrations** — `ddl-auto=update` only. No Flyway/Liquibase. | application.properties. | Schema drift, no rollback, destructive changes possible. |

### 2.2 High severity
| ID | Issue | Impact |
|---|---|---|
| H1 | Auth returns inconsistent shapes — `/api/auth/signin` returns `JwtResponse` (token + user DTO), `/api/auth/signup` returns plain text string. | Frontend must special-case auth responses. |
| H2 | Mixed response envelopes — `/api/v1/**` uses `ApiResponse<T>`; `/api/users`, `/api/admin`, `/api/products`, `/api/dashboard` return raw DTOs; some endpoints return raw JPA entities (maintenance assets, workforce employees, report history). | No stable contract; type generation impossible. |
| H3 | JWT carries username only — no roles claim. Roles come from signin response. Expired token → anonymous → reads still succeed (no `permitAll("/**")` found, but expired-token handling not verified). | Reload loses role context unless re-fetched. |
| H4 | `AuthController` maps only `BadCredentialsException`→401. Disabled/locked accounts → 500 "Login failed". | Misleading errors. |
| H5 | Bootstrap passwords are weak defaults (`11111111`...`77777777`) with env-var overrides. JWT secret has hardcoded base64 fallback. | Dev-only; must be removed before production. |
| H6 | CORS hardcoded to `http://localhost:4200` (ignores README-documented `CORS_ALLOWED_ORIGINS`). | Deployment requires code change. |
| H7 | `StudentModel` entity exists in factory domain — leftover from a tutorial/scaffold. | Dead code, indicates copy-paste origin. |
| H8 | Two `LoginRequest` and two `RegisterRequest` classes (one in `entity/`, one in `dtos/`). | Confusing duplication; which is used? |
| H9 | `ProductController` (legacy product CRUD) coexists with factory domain — leftover from original template. | Dead/confusing module. |

### 2.3 Unverified (cannot confirm without running server)
- Whether `hasRole()` checks actually pass (authorities include `ROLE_` prefix + legacy aliases — should work, but not executed).
- Actual `Page<T>` JSON shape (no `@JsonSerialize` config found — expect default `PageImpl`).
- Whether the two SecurityConfigs cause a bean conflict at runtime (Spring may fail fast on duplicate `SecurityFilterChain`).
- End-to-end flow: signin → token → authenticated request → 200.
- Database schema generated by `ddl-auto=update` against a real PostgreSQL.

---

## 3. Gaps between existing system and the business blueprint

The existing system is a **factory operations skeleton** — it has the core nouns (work order, BOM, part, supplier, PO, inventory, quality, sales) but lacks the **connected lifecycle** the blueprint requires. Major gaps by module:

| Blueprint requirement | Gap |
|---|---|
| **E. Import & shipment** | Entire module missing: shipment, container, vessel, voyage, customs declaration, duties/freight/insurance, landed-cost allocation, cost reconciliation. |
| **I. VIN & genealogy** | No VIN as a first-class entity, no component-to-vehicle linkage, no supplier/PO/container traceability on installed parts. Frontend has a VIN page but no backend. |
| **B. Dealer portal** | No dealer-scoped authentication, no quotation workflow, no dealer-specific price lists, no advance-payment-percentage config, no customer-facing order progress. |
| **C. Forecasting** | No forecast entity, no historical-demand + dealer-forecast + manager-adjustment combination, no approval history on adjustments. |
| **F. Damage & claims** | No damage record, no auto-created supplier/carrier claim, no quarantine stock state, no replacement/credit tracking, no serial numbers on critical components. |
| **H. Quality closure** | No rework loop, no reinspection-after-rework, no quality-hold release authorization, no cost-of-quality tracking. |
| **J. Finance** | No invoice, credit note, receipt, refund, account balance, aging report, landed cost, production cost, profitability. |
| **K. Warranty** | No warranty policy, no warranty claim, no link from claim to VIN/genealogy. |
| **L. Compliance** | No safety incident record, no legal/customs document register, no insurance record/claim. |
| **Cross-cutting** | No audit trail with before/after values, no field-level permissions, no segregation-of-duties enforcement, no configurable checklists. |

**Estimated coverage: ~35–40%** of the blueprint's functional scope exists in some form. The remaining 60% is net-new development.

---

## 4. Confirmed cross-module workflows and dependencies

```
VehicleModel (A) ──► BOM (A) ──► Part (A) ──► Supplier (D) ──► PurchaseOrder (D)
     │                                        │
     ▼                                        ▼
ProductionPlan (C) ──► WorkOrder (G) ──► StockTransaction (F) ──► InventoryItem (F)
     │                   │
     │                   ▼
     │            Inspection (H) ──► NcrRecord (H) ──► Defect (H)
     │                   │
     ▼                   ▼
SalesOrder (B) ◄── Finished Vehicle (implied) ──► YardSlot (K)
     │
     ▼
SalesPayment (J)
```

**Hard dependencies (Phase order constrained by these):**
1. **A (master data/BOM) must precede C (planning) and F (inventory/MRP)** — MRP explodes BOM.
2. **D (procurement) must precede E (import/shipment)** — shipments reference POs.
3. **E (shipment) must precede F (receiving/landed cost)** — receipts reference shipments; landed cost needs shipment expenses.
4. **F (inventory) must precede G (assembly)** — kitting/consumption needs stock.
5. **G (assembly) + H (quality) must precede I (VIN genealogy)** — genealogy records what was installed and inspected.
6. **B (dealer orders) must precede J (finance)** — invoices reference orders.
7. **I (VIN) must precede K (warranty)** — warranty claims reference VIN.

---

## 5. Proposed data & API changes (backward-compatibility risks)

### 5.1 Additive only (low risk, backward compatible)
- New entities: `Shipment`, `Container`, `Vessel`, `CustomsDeclaration`, `LandedCost`, `DamageRecord`, `SupplierClaim`, `QuarantineLot`, `SerialNumber`, `VinRecord`, `VinGenealogy`, `Quotation`, `Invoice`, `CreditNote`, `Receipt`, `Refund`, `DealerPriceList`, `Forecast`, `ForecastAdjustment`, `WarrantyPolicy`, `WarrantyClaim`, `SafetyIncident`, `LegalDocument`, `AuditLog` (with before/after JSON).
- New endpoints under `/api/v1/<module>/` following the existing `ApiResponse<T>` envelope.
- New roles or fine-grained permissions (additive to existing 7 roles).

### 5.2 Breaking changes (require migration, high risk)
| Change | Risk | Mitigation |
|---|---|---|
| Standardize all endpoints to `ApiResponse<T>` envelope | Existing frontend pages that consume raw DTOs break | Add a versioned `/api/v2/` prefix; keep v1 as-is; migrate pages incrementally. |
| Replace `ddl-auto=update` with Flyway migrations | First migration must capture current schema | Generate baseline migration from a running dev DB. |
| Add refresh-token + short-lived access token | Changes auth flow; frontend must handle refresh | Backward-compatible: keep 24h token working; add `/api/auth/refresh` alongside. |
| Rename/move `ProductController`, `StudentModel`, duplicate `LoginRequest`/`RegisterRequest` | Dead-code removal | Delete in a dedicated cleanup commit; verify no references. |
| Add `ROLE_` prefix consistency + remove legacy aliases | Any `hasRole("ADMIN")` checks break | Keep legacy aliases permanently (already in `CustomUserDetailsService`). |

---

## 6. Recommended UX & technology direction

### 6.1 Frontend
- **Keep Angular, upgrade to 22 LTS** (already proven to build on this codebase — standalone components, signals, zoneless optional). The existing 20+ pages are a strong foundation; do **not** rewrite from scratch.
- **Design system:** adopt a premium dark-first glassmorphic theme (Linear/Vercel-style) with CSS variables, light/dark toggle, Inter font. An experimental upgrade on the `angular-22-upgrade` branch already compiles and can be reviewed.
- **Component architecture:** `core/` (auth, http, permissions, errors) + `shared/ui/` (design-system adapters — the only place importing a UI lib) + `features/<domain>/` (pages + data-access + dumb components). Enforce with ESLint `no-restricted-imports`.
- **API types:** generate from `/v3/api-docs` via `openapi-typescript` once the envelope is standardized.
- **Responsive targets:** desktop (office), tablet (shop-floor/quality), mobile (manager approvals), mobile-friendly dealer portal.

### 6.2 Backend
- **Keep Spring Boot 4.1.1 / Java 17 / PostgreSQL.** Do not change the stack.
- **Add Flyway** for migrations; set `ddl-auto=validate`.
- **Unify error envelope** — one `ErrorResponse` shape with `status_code`, `status`, `reason`, `fieldErrors`. Map `IllegalStateException`→409, `IllegalArgumentException`→400, `EntityNotFoundException`→404, validation→400.
- **Add audit trail** via Hibernate Envers or a custom `@Audited` aspect with before/after JSON.
- **Add module package structure** — the current flat package (`controller/`, `service/`, `entity/`) will not scale to 12 modules. Refactor to `module/<domain>/` incrementally, one module at a time.

### 6.3 What NOT to do
- Do not rewrite the frontend in another framework. Angular 22 is current and the codebase is already standalone.
- Do not introduce a microservices architecture. One factory = one modular monolith.
- Do not delete the legacy `ProductController`/`StudentModel` until references are verified.
- Do not enable `permitAll` anywhere. Do not disable security for "convenience".

---

## 7. Prioritized implementation roadmap (with acceptance tests)

Following the blueprint's Phase 0–12, reordered by hard dependencies. **Each phase ends at a review checkpoint.**

| Phase | Scope | Key acceptance tests | Est. |
|---|---|---|---|
| **0** | ✅ This assessment | n/a | Done |
| **1** | Foundation: fix C1 (compile), C2 (errors), add Flyway baseline, add audit trail, unify error envelope, cleanup dead code (H7/H8/H9), add test harness | Backend compiles; `./mvnw test` green; every exception returns `ErrorResponse`; audit log records before/after | 1–2 wks |
| **2** | Module A (master data): VehicleModel variants/colors/options, BOM revisions/effectivity, routings, substitutes, model-to-line assignment | BOM explosion returns exact part qty per config; BOM revision history; obsolete part blocked from new BOM | 2 wks |
| **3** | Module D + E (procurement + import): suppliers, PR/PO/amendments, shipment/container/vessel, customs, landed cost, cost reconciliation | PO→shipment→container→receipt chain; landed cost allocated per part; damage auto-creates claim | 3 wks |
| **4** | Module F (inventory + receiving): damage/shortage/reject, quarantine, serial numbers, stock states (usable/reserved/quarantined), no-negative-stock, transaction history | Damaged item never becomes usable; negative stock blocked; reservation prevents double-allocation | 2 wks |
| **5** | Module C (forecasting + planning): forecast combine (history + dealer + manager + approval), daily targets, MRP, shortage alerts, plan-vs-actual | Forecast shows source breakdown; manager adjustment requires reason + approval; shortage list is accurate | 2 wks |
| **6** | Module G (assembly): work orders, kitting/consumption, workstation tablet UI, downtime/escalation, actual-vs-plan | WO transition rules enforced; consumption decrements stock; downtime logged with reason | 3 wks |
| **7** | Module H (quality): incoming/in-process/final inspection, NCR, rework loop, reinspection, quality hold, cost of quality | Quality hold blocks delivery; rework requires reinspection; defect trends by supplier/part | 2 wks |
| **8** | Module I (VIN genealogy): VIN as entity, installed-component traceability, supplier/PO/container link, VIN history view | VIN query returns full genealogy; every serialized part traces to shipment | 2 wks |
| **9** | Module B + J (dealer + finance): dealer-scoped portal, quotations, price lists, order confirmation, advance payment, invoices, credit notes, receipts, refunds, aging, landed cost + profitability | Dealer sees only own data; invoice marked paid only when payments+credits reconcile; duplicate refund blocked | 3 wks |
| **10** | Module K + L (delivery/warranty + HR/maintenance/safety/compliance): finished-vehicle allocation, delivery, warranty claims, safety incidents, legal document register, maintenance, HR | Warranty claim links to VIN; expired documents trigger alerts; maintenance downtime links to production | 2 wks |
| **11** | Dashboards + UX polish: 11 department dashboards, responsive tablet/mobile, field-level permissions, segregation of duties | Each dashboard has source-backed metrics; role X cannot see field Y; SoD enforced | 2 wks |
| **12** | E2E tests, security review, performance, docs, deployment readiness | Critical failure-recovery scenarios pass (damaged receipt, shortage block, quality hold, failed payment, duplicate refund, overdue invoice, VIN query) | 2 wks |

**Total estimate: ~25–30 weeks** for a team of 2–3 engineers. Can be parallelized: backend (Phases 1–8) and frontend (pages + design system) tracks run concurrently after Phase 1.

---

## 8. Immediate recommendation

**Approve Phase 1 (foundation) first.** It is the highest-leverage work: it fixes the compile blocker, unifies the API contract (enabling type generation and frontend integration), adds migrations and audit (required by every later phase), and establishes the test harness. Without Phase 1, every subsequent phase accumulates technical debt.

Do **not** start building new business modules (shipment, VIN, finance) until Phase 1 lands — they would inherit the broken error handling, missing audit, and unversioned schema.

---

*Assessment based on source-code reading only. No server was executed. Items marked "unverified" require a running backend + PostgreSQL to confirm.*
