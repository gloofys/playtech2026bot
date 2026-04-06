# Playtech Bid Bot

Submission by Fred Brosman.

Java bot submission for the Playtech Summer Internship home assignment.

## Requirements

- Java 8 or newer
- Standard JDK only
- No third-party libraries

## Build

```bash
javac src/Main.java
```

## Run

```bash
java -cp src Main 10000000
```

The command line argument is the initial ebucks budget.

## Notes

- The bot follows the stdin/stdout protocol described in the assignment.
- Logging, if enabled, is written to stderr.
- The bot uses no additional threads and no external file or socket access.
- The selected advertising category is currently hardcoded as `Video Games`.
- This repository contains only the bot source code.
- Performance can be evaluated using the Playtech test harness.