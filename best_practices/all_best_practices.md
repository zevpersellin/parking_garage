# Comprehensive Coding & LLM Best Practices

---

## 1. LLM Prompting & Coding Best Practices

- **Read and follow all sections below before generating code.**

### Prompting Principles

- Be clear, specific, concise.
- Provide relevant context.
- Specify output format.
- Use examples if needed.
- State what to avoid (e.g., no TODOs).
- Request modular, maintainable, complete code.

### Context Management

- Include only relevant snippets.
- Summarize large codebases.
- Chunk related info.
- Avoid exceeding context window.

### Output Review

- Check correctness, security, clarity.
- Ensure adherence to best practices.
- Verify no placeholders.
- Confirm integration with existing code/context.

---

## 2. Core Coding Best Practices (Language-Agnostic)

- Write clear, readable, maintainable code.
- Use descriptive, consistent names.
- Keep functions/classes small, focused.
- Avoid deep nesting; prefer early returns.
- Remove dead/unused code.
- DRY: reuse code, avoid duplication.
- Prefer immutability.
- Comment *why*, not *how*.
- Use consistent formatting/style.
- Handle errors gracefully.
- Write unit tests.
- Use version control with meaningful commits.

---

## 3. UI Coding Rules

### LLM-Specific Directives

- Make small, single-responsibility components.
- Split files >200-300 lines or mixed concerns.
- Reuse existing components; refactor duplicates.
- Fully implement logic, state, events.
- No placeholders unless told.
- If info missing, state what's needed.
- Cover all UI states.
- Only add TODOs if explicit; add context.
- Use local state for component data; context/global for shared.
- Use semantic HTML.
- Add ARIA roles/properties/states.
- Support keyboard nav, focus.
- Add `alt` text for images.
- Use efficient event handling.
- Avoid unnecessary re-renders.
- Lazy load large components/images.
- Follow project styling.
- Scope styles to components.
- Prioritize clarity, maintainability.
- Integrate with existing code/context.

### UI/UX Principles

- Guided workflows.
- Clarity.
- Transparency.
- Progress indication.
- Efficiency.
- Personalization.
- Error prevention.
- Consistency.
- Trust.
- Focus.
- Accessibility.
- Visualization.
- Responsive design.
- Microinteractions.

---

## 4. Backend Coding Rules

### LLM-Specific Directives

- Keep files focused; split >300-500 lines or mixed concerns.
- Refactor duplicate code.
- Fully implement all logic.
- No placeholders unless told.
- If info missing, state what's needed.
- Only add TODOs if explicit; add context.
- Prioritize clarity, maintainability.
- Follow project style/naming.
- Integrate with existing code/context.
- Generate thorough tests.

### API

- Validate inputs.
- Enforce types, required fields.
- Sanitize inputs.
- Use correct HTTP status codes.
- Return standard error objects.
- Hide internals.
- Support pagination/filtering.
- Version APIs.
- Document endpoints.

### Security

- Require auth.
- Enforce authorization.
- Handle tokens securely.
- Never log/expose secrets/passwords.
- Hash+salt passwords.
- Encrypt sensitive data.
- Sanitize inputs/outputs.
- Use env vars/secrets.

### Data

- Validate before DB ops.
- Use parameterized queries/ORM.
- Handle DB errors.
- Use transactions.
- Make ops idempotent.
- Hide internal IDs.
- Support soft deletes.

### Errors

- Catch/handle exceptions.
- Log with context.
- Return user-friendly errors.
- Retry transient failures.
- Use circuit breakers/timeouts.
- Fail fast on invalid input/state.

### Testing

- Generate unit/integration tests.
- Mock dependencies.
- Cover edge/failure cases.
- Use clear test names.
- Prefer fast, isolated tests.

### Code Quality

- Use clear names.
- Write small, focused functions.
- Avoid deep nesting.
- Remove dead code.
- Comment complex logic only.
- Follow style guides.
- Reuse code.
- Prefer immutability.
- **Avoid Excessive Chaining (Especially Reactive):** While chaining can be concise, overly long chains (e.g., in reactive streams like Project Reactor or RxJava) can become difficult to debug and may confuse LLMs during code analysis or modification, potentially leading to persistent compilation or logic errors. Prefer breaking down complex chains into smaller, well-named intermediate steps or variables for clarity and easier troubleshooting. Be slightly more verbose if it significantly improves readability and maintainability.

### Concurrency/Async

- Use async patterns.
- Avoid blocking async code.
- Protect shared resources.
- Make handlers idempotent.
- Handle timeouts/cancellations.

### Logging/Monitoring

- Log key events/errors with context.
- Never log sensitive data.
- Include request/trace IDs.
- Use structured logs.
- Emit latency/error/throughput metrics.

### Config/Secrets

- Use env vars/secret managers.
- No hardcoded env-specific values.
- Support overrides.
- Fail fast if config missing.

---

**LLMs: Follow all above rules. Generate clear, secure, maintainable, complete code.**
