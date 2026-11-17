# Mod diagnostics

## Build failures
Every GitHub Actions build stores a `*-build-diagnostics` artifact, including the full Gradle output with stack traces, Java/Gradle versions, runner OS, and commit SHA. Download that artifact from the failed Actions run. The workflow still fails normally and only uploads the mod JAR when the build and verification succeed.

## Minecraft runtime crashes (Linux / Chromebook Linux)
From the Minecraft instance directory, run:

```bash
bash tools/collect-mod-diagnostics.sh
```

For a custom instance path:

```bash
bash tools/collect-mod-diagnostics.sh "/path/to/instance"
```

The script creates an `error-extract.txt` index with exception/error lines and nearby stack-trace context, while preserving the complete `latest.log`, `debug.log`, five newest crash reports, JVM fatal-error logs, Java/OS details, and installed mod filenames in a `.tar.gz` archive. It does not copy JARs, worlds, or configuration files.

Send the generated archive when asking for a crash diagnosis. Check it before sharing: logs may contain your computer username, server addresses, or chat messages.

## Accuracy rules
- Report the exact exception, message, and complete relevant `Caused by` chain.
- Identify a mod as the cause only when the stack trace or controlled A/B test supports it.
- Separate confirmed facts from likely causes.
- A successful Gradle build is not proof that the mod works in Minecraft. Runtime testing is a separate check.
