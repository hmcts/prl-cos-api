# prl-cos-api

[![Build Status](https://travis-ci.org/hmcts/prl-cos-api.svg?branch=master)](https://travis-ci.org/hmcts/prl-cos-api)

## Notes

This is manage order changes base branch test.

Update with master - Respondent dynamic task list with stop representation.

Since Spring Boot 2.1 bean overriding is disabled. If you want to enable it you will need to set `spring.main.allow-bean-definition-overriding` to `true`.


JUnit 5 is now enabled by default in the project. Please refrain from using JUnit4 and use the next generation

## Building and deploying the application

### Building the application

The project uses [Gradle](https://gradle.org) as a build tool. It already contains
`./gradlew` wrapper script, so there's no need to install gradle.

To build the project execute the following command:

```bash
  ./gradlew build
```

### Running the application with RSE CFT Lib
[cftlib](https://github.com/hmcts/rse-cft-lib) is a Gradle plugin that provides a local CCD/ExUI environment
in which `prl-cos-api` can be developed and tested.

See [RSE CFT lib](docs/cftlib.md) for instructions on how to set up and run `prl-cos-api` with the plugin.

### Running the application

Create the image of the application by executing the following command:

```bash
  ./gradlew assemble
```

Create docker image:

```bash
  docker compose build
```

Run the distributions (created in `build/install/prl-cos-api` directory)
by executing the following command:

```bash
  docker compose up
```

This will start the API container exposing the application's port
(set to `4044` in this template app).

In order to test if the application is up, you can call its health endpoint:

```bash
  curl http://localhost:4044/health
```

You should get a response similar to this:

```
  {"status":"UP","diskSpace":{"status":"UP","total":249644974080,"free":137188298752,"threshold":10485760}}
```

### Alternative script to run application

To skip all the setting up and building, just execute the following command:

```bash
./bin/run-in-docker.sh
```

For more information:

```bash
./bin/run-in-docker.sh -h
```

Script includes bare minimum environment variables necessary to start api instance. Whenever any variable is changed or any other script regarding docker image/container build, the suggested way to ensure all is cleaned up properly is by this command:

```bash
docker-compose rm
```

It clears stopped containers correctly. Might consider removing clutter of images too, especially the ones fiddled with:

```bash
docker images

docker image rm <image-id>
```

## 🛡️ Quality & Coverage Tools
To maintain our 80% coverage standard and reduce CI/CD feedback loops, this project includes a local Quality Gate.
By default, local commits remain fast. You can "Opt-In" to the coverage gate so it runs automatically every time you hit commit.

Note on Workflow: By default, the Quality Gate is DISABLED to keep your local development fast. We recommend enabling it `gate-on` when working on feature-complete code to catch coverage gaps early and avoid CI failures.
### 📊 Local Coverage Reports
Run these commands to get instant, color-coded coverage feedback in your terminal:

Check Total Project: `./gradlew testWithCoverage`
Check Specific Class: `./gradlew testWithCoverage -Ptarget=MyClassName`
Check Multiple Classes: `./gradlew testWithCoverage -Ptarget=ClassA,ClassB`

> Note: The "Targeted" mode provides a line-by-line breakdown of missed instructions and branches.
>
🕹️ Control Aliases
To make toggling the gate easy, add these to your shell profile (~/.zshrc or ~/.bash_profile):

# Coverage Gate Controls
```
alias gate-on='git rev-parse --is-inside-work-tree >/dev/null 2>&1 && { touch "$(git rev-parse --show-toplevel)/.run-coverage-on-commit" && echo "🛡️  Coverage Gate: ENABLED"; } || echo "❌ Not in a git repo"'
alias gate-off='git rev-parse --is-inside-work-tree >/dev/null 2>&1 && { rm -f "$(git rev-parse --show-toplevel)/.run-coverage-on-commit" && echo "🔓  Coverage Gate: DISABLED"; } || echo "❌ Not in a git repo"'
alias gate-status='git rev-parse --is-inside-work-tree >/dev/null 2>&1 && { [ -f "$(git rev-parse --show-toplevel)/.run-coverage-on-commit" ] && echo "🛡️  ON" || echo "🔓  OFF"; } || echo "❌ Not in a git repo"'
```
`gate-on`: Enables the gate. Commits will be blocked if coverage is below 80%.
`gate-off`: Disables the gate for fast, unverified commits.
`gate-status`: Check if the guard is currently active.

Terminal Setup: After adding the aliases above, restart the IntelliJ terminal or run:

```bash
source ~/.zshrc
```

💻 IntelliJ Integration
The Quality Gate is fully compatible with the IntelliJ IDEA commit workflow.

The "Emergency" Skip: If the gate is ON but you need to bypass it for a single commit (e.g., a README typo), click the Gear Icon ⚙️ in the Commit window and uncheck "Run Git hooks".

UI Refresh: If you toggle the gate in the terminal and IntelliJ doesn't seem to notice, right-click the project root and select "Reload from Disk".

### ⚓ Git Pre-commit Hook (Opt-In)
A Git Pre-commit Hook is included to prevent "Red" builds in SonarQube. By default, local commits remain fast and skip the coverage check.

* **Installation:** Automatically installed/updated when you run `./gradlew build`.
* The "Emergency" Skip: If the gate is ON but you need to bypass it for a single commit (e.g., a README typo), click the Gear Icon ⚙️ in the Commit window and uncheck "Run Git hooks".

UI Refresh: If you toggle the gate and IntelliJ doesn't notice, right-click the project root and select "Reload from Disk".
* **How to Run:** To verify your coverage during a commit, set the `VERIFY_COVERAGE` flag:
  ```bash
  VERIFY_COVERAGE=true git commit -m "My verified feature"
There is no need to remove postgres and java or similar core images.

### Troubleshooting & Performance
First Run: The first time you run a verified commit, Gradle may take a moment to start the daemon. Subsequent runs will be faster.

JVM Crashes (Apple Silicon): If you encounter a SIGSEGV during the coverage task on a Mac, try running with:
`./gradlew build -Dorg.gradle.jvmargs="-XX:-TieredCompilation"`

Manual Refresh: If the hook isn't firing as expected, you can force a fresh installation of the logic with:
`./gradlew installGitHooks`

### Managing Preview environment PODs
Make sure you have added the label 'enable_keep_helm' while creating the PR. Otherwise, add the label and re-trigger the build.

## Testing with prl-ccd-definition

### If your ticket doesn’t require changes in the prl-ccd-definition
No changes are needed in this repository, and everything will be set automatically.

### If your ticket includes changes in the prl-ccd-definition
Create a new label in lines of `"pr-defs:pr-2878` # Replace `2878` with the PR number of the prl-ccd-definition update you want to test against this repo

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details
