# CI/CD Pipeline with GitHub Actions

## Introduction

The Continuous Integration and Continuous Delivery system is entirely managed through **GitHub Actions** and is organized into five distinct workflows.
The workflows are not isolated: they communicate with each other via the **`repository_dispatch`** mechanism, forming an end-to-end event-driven pipeline that goes from code commit all the way to infrastructure deployment on AWS EKS.

## Workflow 1 — `build.yml`: Build and Package

This is the main workflow, the entry point of the entire pipeline. It is executed on every `push` and every `pull_request` on any branch.

### Validation
Before any operation on the code, two parallel jobs act as **quality gates**:

- **`commit-lint`**: verifies that every commit message complies with the **Conventional Commits** specification, a prerequisite for automatic semantic versioning calculation in the next workflow. The job is skipped for Dependabot commits.
- **`k8s-validate`**: installs `kustomize` and `kubeconform` and validates the Kubernetes manifests present in the repository. `kustomize build k8s` generates the final manifest (including overlays) and `kubeconform` verifies structural correctness in strict mode against the official Kubernetes schema. This prevents the deployment of malformed manifests.

### Modified Services Detection
One of the most significant aspects of the workflow is the **incremental approach**: not all microservices are recompiled on every push, but only those that have actually been modified.
The `detect-changes` job uses a path filter to analyze the files changed in the commit and produces the list of only the affected services. This output is passed to the subsequent jobs.

### Build and Test
The `build` job receives the list of modified services and launches a **build matrix**: a parallel job is executed for each modified service.
For each service:

1. JDK 21 is configured
2. The Gradle build is executed, which includes compilation, tests and other quality checks (lint, checkstyle, etc.)
3. The produced JAR is uploaded as a GitHub Actions **artifact**, uniquely identified by service name.

### Package, Scan and Publish
This job, also in matrix, handles containerization and image publishing.
It runs only if `build-aggregate` has succeeded.

For each service:

1. **Artifact download**: retrieves the JAR produced by the previous job.
2. **Authentication on GHCR**: accesses the GitHub Container Registry using the automatic `GITHUB_TOKEN`.
3. **Docker image build**: builds the image using the individual service's `Dockerfile`, tagging it with the commit SHA (`github.sha`), which guarantees uniqueness and traceability between commit and image.
4. **Vulnerability Scan with Grype**: before pushing, the image is scanned with **Grype** for known vulnerabilities. The report is produced in SARIF format and uploaded to GitHub Security, visible in the repository's *Security* tab.
5. **Image push**: the image is published on GHCR with two tags: the **commit SHA** (for traceability) and **`latest`** (for quick reference).
6. **Next workflow trigger**: if the push occurred on the `main` branch, a `repository_dispatch` event of type `microservice_semantic_release` is emitted, passing the service name and the image path on GHCR as payload.

The `final-status` job is an aggregator that runs `if: always()`, meaning it executes regardless of the success or failure of the previous jobs. 
It checks the status of the dependent jobs and fails only if at least one of them actually failed. This task is used to accept PRs. 

## Workflow 2 — `build_ui.yml`: Build and Package Multiplatform UI
This workflow is dedicated exclusively to the **HabitQuest UI** frontend, developed with **Kotlin Multiplatform (KMP)** and Compose Multiplatform.
It is triggered on `push` and `pull_request`, but only when changes affect:

- `habitquest-ui/**`
- `.github/workflows/build_ui.yml`

The workflow is organized into five jobs:

1. **`commit-lint`**: validates Conventional Commits (skipped for Dependabot).
2. **`static-scan`**: installs and updates **Grype**, scans the repository (`dir:${{ github.workspace }}`), generates a **SARIF** report, and uploads it to GitHub Security.
3. **`build`**: runs after quality gates and executes a **matrix build** across three platforms:
   - `web` on `ubuntu-22.04` (Node.js 18 + `:kotlinUpgradeYarnLock` + `:habitquest-ui:composeApp:jsBrowserProductionWebpack`)
   - `android` on `ubuntu-22.04` (`:habitquest-ui:composeApp:assembleDebug`)
   - `ios` on `macos-latest` (`:habitquest-ui:composeApp:linkDebugFrameworkIosSimulatorArm64`)

   Platform artifacts are uploaded separately for web, Android, and iOS.
4. **`semantic-release`**: runs only for **pushes on `main`** and emits `repository_dispatch` with event type `ui_semantic_release` and payload `{ "service": "habitquest-ui" }`.
5. **`ui-final-status`**: final aggregator job (`if: always()`) that fails only when one of the dependent jobs actually failed. This means PR builds are still valid even when `semantic-release` is skipped.


## Workflow 3 — `semantic-release.yml`: Semantic Release
This workflow is triggered exclusively by `repository_dispatch` events emitted by the build workflows at the end of a successful push on `main`.
It accepts both `microservice_semantic_release` and `ui_semantic_release` events.

### Semantic Versioning Calculation
The workflow uses **`semantic-release`** with the **`semantic-release-monorepo`** plugin, which adapts the standard semantic-release behavior to monorepo repositories.
Each service has its own independent versioning scope, with tags in the format `<service-name>-v<MAJOR>.<MINOR>.<PATCH>`.
When the payload identifies `habitquest-ui`, the workflow sets `RELEASE_DIR=habitquest-ui`; for backend services it uses `services/<service-name>`.

Versioning is calculated automatically by analyzing commit messages since the last tag:

- `feat:` → **MINOR** increment
- `fix:` → **PATCH** increment
- `!` → **MAJOR** increment

### Docker Image Retag
Afterwards, semantic-release takes care of creating a new tag on git and on the docker registry:

1. The `latest` image is pulled from GHCR.
2. The image is retagged with the calculated semantic version (e.g. `2.3.1`).
3. The new version is pushed to GHCR, alongside the already existing SHA and `latest` tags.
   If there are no relevant changes instead (e.g. only `chore:` or `docs:` commits), no tag is created and the workflow concludes without emitting subsequent events.

For the UI release path, no image payload is provided, so the Docker retag/push steps are skipped and only the git release/tag is produced.
For backend services, if a new version has been calculated, a second `repository_dispatch` of type `update_manifest` is emitted with a payload containing the service name, image path and semantic version.
Each image is tagged with the **commit SHA** that generated it, guaranteeing bidirectional traceability: from an image running in the cluster it is always possible to trace back to the exact commit that produced it.
The additional semantic tag instead provides human readability and allows versions to be communicated in a meaningful way.

## Workflow 4 — `update_manifest.yml`: Update Manifest
This workflow, triggered by `repository_dispatch`, partly draws on the **GitOps** principle:
the only source of truth for the state of the Kubernetes cluster is the Git repository.
Updating the manifests in Git is equivalent to declaring the intention to deploy.
However, fully automatic deployment has been disabled (commented out in the file),
to avoid incurring costs on AWS, but the infrastructure would be ready to release a new version on every push to `main`.

### Kustomize Manifest Update
The `update` job modifies the image tag in the `kustomization.yaml` file of the affected service.
Instead of committing directly to `main` (approach commented out in the file), the workflow opens a **Pull Request** on the `chore/auto-updates` branch.
This introduces a human review gate (or ArgoCD approval) before the change reaches the main branch and is applied to the cluster


## Overall Integration
| Workflow | Responsibility | Trigger |
|---|---|---|
| `build.yml` | CI: build, test, scan, publish | Push / PR |
| `build_ui.yml` | CI: frontend KMP and release trigger | Push / PR on `habitquest-ui/**` or `.github/workflows/build_ui.yml`; `main` push dispatches `ui_semantic_release` |
| `semantic-release.yml` | Automatic versioning | `repository_dispatch` from backend or UI builds |
| `update_manifest.yml` | GitOps: manifest update | `repository_dispatch` from semantic-release |
