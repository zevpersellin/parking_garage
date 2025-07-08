# AI-Native Development Workflow Guide
*Version 2.0 | Status Draft | Last Updated July 8 2025*

**Owner:** Lead AI Software Engineer (you)  
**Applies to:** Parking-Garage Demo & all future prototypes

---

## 1  Purpose
Shift the developer’s role from *primary code author* to *director of autonomous agents*.  
Instead of “letting AI write a few functions,” we orchestrate a continuous loop of **Prompt → Generate → Critique → Refine** that touches every stage of the SDLC. :contentReference[oaicite:0]{index=0}

---

## 2  Guiding Principles
| Principle | What It Means in Practice |
|-----------|--------------------------|
| **Human-in-the-Loop** | Every AI artifact (code, tests, docs) passes a human review gate before merge. |
| **Spec-First** | Feed the AI the PRD, architecture spec, and test matrix *before* asking for code. |
| **Small Batches** | Request work in bite-size units (file, class, or test) to simplify review & rollback. |
| **Critique Loop** | Alternate between *generation* and *review* until quality gates pass. |
| **Traceability** | Link every commit to a requirement ID from the PRD / checklist. |

---

## 3  Roles & Agents

| Human Role | AI Agent Persona | Primary Prompts |
|------------|-----------------|-----------------|
| **Architect** | *“Senior Software Architect”* | “Propose three architectures… Compare trade-offs…” |
| **Developer** | *“Spring Boot Engineer”* | “Generate `ParkingSpot` entity per spec…” |
| **QA Lead** | *“Senior QA Engineer”* | “List positive & negative test cases for `checkInCar`…” |
| **Release Mgr.** | *“DevOps Coach”* | “Create GitHub Actions workflow for mvn test & OpenAPI diff…” |

> *Tip — Keep the same chat thread per persona to preserve context.*

---

## 4  Golden Path Workflow

### 4.1 Ideate & Architect (Architect Agent)
1. **Prompt:** Feed full PRD + non-functional goals.
2. **Output:** Candidate architectures, class diagram, risk list.
3. **Gate:** Human selects architecture; logs rationale in `/docs/architecture.md`.

### 4.2 Scaffold & Implement (Developer Agent)
1. Generate models → controllers → services in that order.
2. After each file, run *static analysis* and *formatting*.
3. **Gate:** Pull request with mandatory code review & green unit tests.

### 4.3 Test & Harden (QA Agent)
1. AI enumerates edge-case matrix.
2. AI generates JUnit 5 + Mockito tests; human tweaks assertions.
3. **Gate:** CI fails if coverage < 85 % or mutation score < 60 %.

### 4.4 Review & Ship (Release Agent)
1. AI drafts release notes from commit messages.
2. AI updates OpenAPI spec & runs contract tests.
3. **Gate:** Tag-and-release only when all previous gates are green.

*(Phases 1-3 mirror the original document but add explicit gates and CI hooks.)*

---

## 5  Prompting Playbook

1. **Context Block** – surround long inputs with ```text``` fences.
2. **Role Instruction** – “Act as a …”.
3. **Task** – one clear ask, e.g., “Generate class diagram as Mermaid.”
4. **Constraints** – language, framework, coding style.
5. **Critique Request** – “Identify three performance risks.”
6. **Output Format** – code block, table, or JSON.

> **Example:**
> ```
> Act as a senior QA engineer.  
> Using the spec below, list 10 edge-case tests as a Markdown table.  
> ```

---

## 6  Quality Gates

| Stage | Gate | Tooling |
|-------|------|---------|
| Pre-commit | Lint & format | Spotless / Checkstyle |
| Build | Unit tests pass | `mvn test` |
| CI | Coverage ≥ 85 % | JaCoCo |
| PR Merge | Human review | GitHub CODEOWNERS |
| Release | OpenAPI diff = 0 | swagger-diff |

---

## 7  Anti-Patterns to Avoid
* **“Big-Bang” Prompting** – requesting the entire codebase in one go.
* **Blind Trust** – merging AI code without line-by-line review.
* **Spec Drift** – changing prompts without updating the PRD/architecture doc.
* **Context Overflow** – pasting huge logs instead of distilled requirements.

---

## 8  Appendix A — Prompt Templates

<details>
<summary>Architect Template</summary>

```text
Act as a senior software architect.  
Context: <insert PRD link>  
Task: Propose 3 architectures…  
Output: Markdown table with pros/cons & recommended pick.  
