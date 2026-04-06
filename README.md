# Playtech Bid Bot

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

- The bot communicates only via stdin/stdout as required by the assignment.
- Logging, if enabled, goes to stderr.
- The current category choice in the source is `Video Games`.