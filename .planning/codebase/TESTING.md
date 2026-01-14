# Testing Patterns

**Analysis Date:** 2026-01-13

## Test Framework

**Runner:**
- None detected - No formal test framework in either project

**Assertion Library:**
- Not applicable

**Run Commands:**
```bash
# Dashboard - no test command configured
npm run lint                      # ESLint validation only

# Scripts - no test infrastructure
osmb build TidalsGemCutter        # Build only
```

## Test File Organization

**Location:**
- No test files detected
- No `*.test.ts`, `*.spec.ts`, `__tests__/` directories
- No Java test directories (`src/test/java/`)

**Naming:**
- Not applicable

**Structure:**
```
# Current (no tests)
src/
  main/java/...   # Source only

# Recommended
src/
  main/java/...   # Source
  test/java/...   # Tests (to be added)
```

## Test Structure

**Suite Organization:**
- Not applicable - no tests exist

**Patterns:**
- Manual testing in OSMB client environment
- Visual verification of script behavior

## Mocking

**Framework:**
- Not applicable

**What Would Need Mocking (if tests added):**
- OSMB API interactions (Script, Inventory, Bank, etc.)
- HTTP calls for stats reporting
- File system for Prisma

## Fixtures and Factories

**Test Data:**
- Demo data exists in dashboard for display purposes (`script-dashboard/src/app/stats/page.tsx`)
- Not for testing, just fallback when no real data

## Coverage

**Requirements:**
- No coverage requirements
- No coverage tooling configured

**Current State:**
- 0% automated test coverage
- Quality relies on:
  - TypeScript type checking (strict mode)
  - ESLint validation
  - Manual testing
  - Code review

## Test Types

**Unit Tests:**
- None

**Integration Tests:**
- None

**E2E Tests:**
- None
- Manual testing in OSMB client serves this purpose for scripts

## Quality Assurance Practices

**Type Safety:**
- TypeScript strict mode enabled (`script-dashboard/tsconfig.json`)
- Java compile-time type checking (JDK 17)

**Linting:**
- ESLint with Next.js config for dashboard
- No Java linting (IDE-based only)

**Code Review:**
- Git history shows iterative development
- No formal PR review process visible

**Manual Testing:**
- Scripts tested in OSMB client
- Dashboard tested via browser

## Validation Patterns (Substitute for Tests)

**Input Validation (Dashboard):**
```typescript
// script-dashboard/src/app/api/stats/route.ts
function sanitizeString(str: string, maxLength: number): string {
  if (typeof str !== 'string') return ''
  return str.replace(/\0/g, '').trim().slice(0, maxLength)
}
```

**Null Safety (Scripts):**
```java
// Consistent pattern across utilities
if (inv == null) return false;
if (bank == null) { script.log(getClass(), "bank not found"); return null; }
```

**Rate Limiting:**
```typescript
// Timing-based protection
const RATE_LIMIT_WINDOW = 60_000
const RATE_LIMIT_MAX = 30
```

## Recommendations for Future Testing

**Dashboard (Priority: Medium):**
1. Add Vitest for unit tests
2. Test API route handlers with mock Prisma
3. Test utility functions (formatRuntime, sanitizeString)

**Scripts (Priority: Low):**
1. Extract pure logic to testable modules
2. Mock OSMB API for unit tests
3. Focus on RetryUtils, BankingUtils

**Quick Wins:**
- Add `npm test` script to package.json
- Configure Vitest with `vitest.config.ts`
- Test sanitization and validation functions first

---

*Testing analysis: 2026-01-13*
*Update when test patterns change*
