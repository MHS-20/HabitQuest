## Git Hooks and Commit Strategy
The pre-commit hook is configured to execute the a custom gradle task `checkQuality` task. Depending on the module type, this automatically triggers:

- Java/Spring: Spotless formatting, Checkstyle, and PMD analysis.
- KMP/Android: Detekt static analysis and ktlint formatting

We enforce also enforce the Conventional Commits specification via the commitMsg hook.

## GitHub Protection Rules
1. Direct pushes to protected branches (main, develop) are prohibited. All code must be submitted via a pull request.
2. Pull Request Requirements: at least 1 approving review is required before a pull request can be merged, from a person different than the author.
3. Required Status Checks: all CI checks (build, test, lint) must pass before merging, through the `final-status` job in the build workflows.
4. Up-to-Date Branches: pull requests must be tested against the latest code from the base branch before merging.

## Branching Strategy
- `main`: production-ready code, always stable and deployable.
- `develop`: integration branch for features, always in a deployable state.
- `feature/*`: branches for new features or bug fixes, branched off from `develop` and merged back via pull request.
- `refactor/*`: branches for refactoring or non-functional changes, branched off from `develop` and merged back via pull request.

## Dependabot Configuration
The project employs **Dependabot** to automate security scanning and dependency updates, 
ensuring the software supply chain remains secure and current. 
Dependabot monitors two primary ecosystems on a **weekly schedule**: 
- the root **Gradle** build for backend and multiplatform logic
- the **npm** ecosystem. 

To maintain a clean and searchable Git history, all automated pull requests are prefixed with `chore`, 
adhering to our project's conventional commit standards.