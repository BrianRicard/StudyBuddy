# OWASP Dependency-Check in StudyBuddy

> What the "Dependency Vulnerability Scan (OWASP)" CI job does, how it works under the
> hood, and how it is configured in this repository.

## What it is

[OWASP Dependency-Check](https://owasp.org/www-project-dependency-check/) is a
**Software Composition Analysis (SCA)** tool. It does not scan *your* code — it scans
your **dependencies** (every JAR/AAR that Gradle pulls in) and answers one question:

> *"Does anything in my dependency tree have a publicly known vulnerability?"*

"Publicly known" means a **CVE** (Common Vulnerabilities and Exposures) entry in the
[National Vulnerability Database (NVD)](https://nvd.nist.gov/), the US government's
catalogue of published vulnerabilities. Each CVE carries a **CVSS score** from 0 to 10
rating its severity (7.0+ is generally "high", 9.0+ "critical").

This matters even for a local-first kids' app: a vulnerable JSON parser or image
decoder can be exploited through a crafted backup file or downloaded asset, and Play
Store reviews increasingly flag known-vulnerable libraries.

## How it works, step by step

1. **Evidence collection** — the tool walks every resolved dependency and extracts
   *evidence* of what the artifact actually is: Maven coordinates (`group:artifact:version`),
   JAR manifest entries, embedded `pom.xml` files, file names, and package structures.

2. **CPE matching** — that evidence is matched against **CPE** identifiers
   (Common Platform Enumeration), the naming scheme the NVD uses to say *which product*
   a CVE affects (e.g. `cpe:2.3:a:squareup:okhttp:4.9.0`). This fuzzy matching is why
   false positives happen: a library can share a name with an unrelated product.

3. **CVE lookup** — every matched CPE is looked up in a **local mirror of the NVD**
   that the tool downloads and keeps in a small database (H2). This is the expensive
   part: the first download fetches years of CVE history. That's what our CI cache and
   API key are for (see below).

4. **Reporting** — findings land in reports (we generate HTML for humans, JSON for
   tooling, SARIF for GitHub's Security tab). Each finding lists the dependency, the
   CVE(s), the CVSS score, and the evidence that led to the match.

5. **Gate** — if any finding's CVSS score is at or above `failBuildOnCVSS`, the Gradle
   task exits non-zero. In our workflow that marks the step red without hard-failing
   the whole pipeline (`continue-on-error: true`), so a new CVE published overnight
   can't block an unrelated fix — but it stays loudly visible.

## How it is set up in this repo

Three pieces: the Gradle plugin, its configuration, and the CI workflow.

### 1. The Gradle plugin

Declared in `gradle/libs.versions.toml`:

```toml
dependency-check = "12.1.0"
dependency-check = { id = "org.owasp.dependencycheck", version.ref = "dependency-check" }
```

and applied at the root, which gives every module the shared `dependencyCheckAnalyze`
task (it aggregates across all subprojects).

### 2. Configuration — root `build.gradle.kts`

```kotlin
dependencyCheck {
    formats = listOf("HTML", "JSON", "SARIF")
    failBuildOnCVSS = 7.0f
    suppressionFile = "$rootDir/owasp-suppressions.xml"

    // Persist the NVD cache to a stable, cacheable location across CI runs.
    data.directory = "$rootDir/.gradle/dependency-check-data"

    // Analyzers irrelevant to an Android/Kotlin project are disabled
    // (npm, NuGet, CocoaPods, Ruby gems, Go modules, …) to speed up scans
    // and cut false positives.
    analyzers.assemblyEnabled = false
    // … (see the file for the full list)

    nvd.apiKey = System.getenv("NVD_API_KEY") ?: ""
}
```

What each knob does:

| Setting | Effect |
|---|---|
| `failBuildOnCVSS = 7.0f` | Only High/Critical findings fail the task. Lower it to be stricter (e.g. `4.0f` includes Medium). |
| `suppressionFile` | Where false-positive suppressions live (see below). |
| `data.directory` | Where the local NVD mirror is stored — kept inside the repo dir so CI can cache it. |
| `analyzers.*Enabled = false` | Skips ecosystems we don't use; big speed win. |
| `nvd.apiKey` | Read from the environment; never hardcode it. |

### 3. CI — `.github/workflows/security.yml`

The `dependency-vulnerability-scan` job:

```yaml
- name: Cache NVD database            # actions/cache on .gradle/dependency-check-data
- name: Run OWASP Dependency-Check    # ./gradlew dependencyCheckAnalyze --continue
  continue-on-error: true
  env:
    NVD_API_KEY: ${{ secrets.NVD_API_KEY }}
- name: Upload OWASP Report           # HTML + JSON as build artifact, 30-day retention
- name: Upload SARIF to GitHub Security  # findings appear in the repo's Security tab
```

Key operational details (learned the hard way — see PR #103):

- **The NVD download is the bottleneck.** Cold and keyless it takes 40–60 min.
  Two mitigations, both active:
  - **Cache**: `actions/cache` persists the NVD database between runs. Caches created
    on `main` are visible to every PR branch (PR-branch caches are *not* shared with
    each other), so the runs on `main` are the ones that seed it. That's why the job
    timeout is 90 min and why `cancel-in-progress` is disabled for `main` — a seeding
    run must be allowed to finish once.
  - **API key**: with `NVD_API_KEY` set (repo → Settings → Secrets → Actions), NIST
    lifts its aggressive rate limit and even a cold download takes minutes. The key is
    free: <https://nvd.nist.gov/developers/request-an-api-key>.
- The cache key ends in `${{ github.run_id }}` with broader `restore-keys`, so every
  run restores the newest cache and saves a fresh one — a rolling cache that stays
  current without manual invalidation. The weekly scheduled run (Mondays 06:00 UTC)
  keeps it warm even during quiet weeks.

## Running it locally

```bash
# Optional but recommended: export your NVD key first
export NVD_API_KEY=xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx

./gradlew dependencyCheckAnalyze
```

First run downloads the NVD mirror into `.gradle/dependency-check-data/` (git-ignored);
later runs only fetch deltas. Reports land in `build/reports/`:

- `dependency-check-report.html` — start here; each finding shows the dependency, the
  CVEs, and the evidence used for the match.

## Handling findings

When the job flags something, there are only three honest outcomes:

1. **Upgrade the dependency** — the usual fix. Check the CVE entry for the first fixed
   version and bump it in `gradle/libs.versions.toml` (mind the compatibility rules in
   CLAUDE.md — minSdk 26, AGP/Kotlin version pins).

2. **Suppress a false positive** — if the CVE matched the wrong product, or applies to
   a component/mode we don't use, add a suppression to `owasp-suppressions.xml` **with
   a justification comment**:

   ```xml
   <suppress>
       <notes>False positive: CVE applies to the okhttp *server* MockWebServer,
       which we only use in tests</notes>
       <cve>CVE-2024-XXXXX</cve>
   </suppress>
   ```

   Suppressions are code-reviewed like anything else; an unexplained suppression is a
   bug report waiting to happen.

3. **Accept the risk temporarily** — no fixed version exists yet and the code path is
   unreachable for us. Suppress it with a note **and a follow-up issue** so it doesn't
   become permanent by accident.

Never fix a red scan by raising `failBuildOnCVSS` — that just turns the alarm off.

## Glossary

| Term | Meaning |
|---|---|
| **SCA** | Software Composition Analysis — scanning third-party components, not your code |
| **CVE** | A single published vulnerability record (e.g. CVE-2024-12345) |
| **CVSS** | 0–10 severity score attached to a CVE |
| **NVD** | The US national CVE database the tool mirrors locally |
| **CPE** | The product-naming scheme used to match libraries to CVEs |
| **SARIF** | A standard JSON format for static-analysis results; GitHub renders it in the Security tab |
| **Suppression** | A reviewed, documented decision to ignore one specific finding |
