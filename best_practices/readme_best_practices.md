# README and Markdown Documentation Best Practices

## Purpose
This document outlines best practices for creating clear, consistent, and maintainable README files and other Markdown documentation within the AI-assisted structural design partner project. Adhering to these practices ensures documentation is easy to understand and navigate.

## General README Structure

Consider including the following sections where applicable, especially for repository-level READMEs:

1.  **Title:** Clear and descriptive (e.g., `# [Component/Repository Name] Summary/Documentation`).
2.  **Purpose:** Briefly explain the component's role and its place within the overall project.
3.  **Key Responsibilities (for Repositories):** Use bullet points to list the main functions or tasks handled by the code in the repository.
4.  **Technology Stack:** List the primary languages, frameworks, libraries, and tools used.
5.  **Setup/Installation/Usage (if applicable):** Provide instructions for developers or users.
6.  **Related Documents:** Link to other relevant documentation (e.g., architecture summaries, use cases, API definitions) using relative paths.

## Content Guidelines

*   **Clarity:** Write clearly and concisely. Avoid jargon where possible or explain it.
*   **Audience:** Consider who will be reading the document (e.g., developers, engineers, project managers).
*   **Consistency:** Use consistent formatting (headings, code blocks, lists).
*   **Maintenance:** Keep documentation up-to-date as the project evolves. Outdated documentation can be misleading.

## Mermaid Diagram Best Practices

Mermaid diagrams are encouraged for visualizing architecture, workflows, and relationships. Follow these guidelines for clarity and maintainability:

1.  **Code Block:** Always enclose Mermaid syntax within a fenced code block:
    ```markdown
    ```mermaid
    graph TD
        A --> B
    ```
    ```

2.  **No Comments Inside Diagram:** **Do not** use Mermaid comments (`%%`) within the `mermaid` code block. Explain the diagram using surrounding Markdown text before or after the code block.
    *   **Bad:**
        ```mermaid
        graph TD
            A --> B %% This connects A to B
        ```
    *   **Good:**
        This diagram shows the connection between component A and component B.
        ```mermaid
        graph TD
            A --> B
        ```

3.  **Readable Colors & Styling:** Use `classDef` (preferred for multiple nodes) or `style` directives to apply colors and styles.
    *   **Ensure High Contrast:** Choose background and text colors with sufficient contrast for readability. Test your choices. White (`#fff`) or black (`#000`) text often works well.
    *   **Example Good Contrast Styles (from project):**
        ```mermaid
        classDef core fill:#4682B4,stroke:#2F4F4F,stroke-width:2px,color:#fff;  %% Steel Blue fill, White text
        classDef analysis fill:#20B2AA,stroke:#008080,stroke-width:2px,color:#fff; %% Light Sea Green fill, White text
        classDef external fill:#9370DB,stroke:#483D8B,stroke-width:1px,color:#fff; %% Medium Purple fill, White text
        classDef action fill:#D6EAF8,stroke:#5DADE2,stroke-width:2px,color:#000; %% Light Blue fill, Black text
        classDef decision fill:#FCF3CF,stroke:#F4D03F,stroke-width:2px,color:#000; %% Light Yellow fill, Black text
        ```

4.  **Node Text Syntax:**
    *   Use quotes `"` for node text containing special characters or spaces: `A["Node Text"]`.
    *   Use `<br>` tags for line breaks within node text: `A["First Line<br>Second Line"]`.
    *   **Avoid Parentheses `()` Inside Brackets `[]`:** Do not put parentheses directly within the node's text brackets `[...]`. If you need to convey information often put in parentheses, integrate it into the text or use line breaks.
        *   **Bad:** `A[Node Text (Details)]`
        *   **Good:** `A["Node Text<br>Details"]` or `A["Node Text - Details"]`

5.  **Simplicity:** Keep diagrams focused on conveying specific information. Avoid excessive complexity in a single diagram. Create multiple diagrams if needed to illustrate different aspects.

By following these guidelines, we can maintain high-quality, understandable documentation throughout the project.
