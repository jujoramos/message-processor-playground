<!-- TOC -->
* [Overview](#overview)
* [Requirements](#requirements)
* [Solution](#solution)
  * [Building & Running](#building--running)
  * [Logging](#logging)
  * [Available Commands](#available-commands)
    * [process](#process)
<!-- TOC -->

# Overview

Playground project to demonstrate the implementation of a simple command line application that processes records from a
binary file.

# Requirements

Below is a summary of the requirements:

- Write an application that can a binary file given a pre-set format and extract all the messages including their
  sequence number.
- There are no incomplete messages, each message has the following packet structure
    - `PAYLOAD_HEADER (4 BYTES LE)`: first four bytes represent the payload header as an `Int32` (little endian), and
      its value denotes the size of the message.
    - `SEQUENCE_NUMBER (4 BYTES LE)`: next four bytes represent the message sequence number, it's also an `Int32` (
      little endian).
    - `MESSAGE (STRING)`: next `n` bytes represent the massage itself, it is a string and its size is defined in the
      aforementioned payload header.
- Presented in a git repository in any programming language.
- Solution well organized with relevant supporting documentation.

# Solution

The solution is provided as a command line application that relies on internal implementations to parse the input and
write the output.

The [buildSrc](buildSrc) module contains the common build logic, coding standards, static code analysis and dependencies
to be shared across the different modules.

The [app](app) module is the user interface, a simple command line utility based on [picocli](https://picocli.info/)
that allows users to specify the file location and what implementation to use for processing, reading and writing the
input records.

The [utilities](utilities) module contains the core services of the solution. There are multiple implementations of the
different interfaces, the user can choose which one to use when running the application and new implementations can be
added without breaking backward compatibility.

TLDR;

```shell
./gradlew clean test run --args="process --file $(pwd)/utilities/src/test/resources/logi.bin"
```

## Building & Running

All platforms require a Java installation with JDK 1.17 or a more recent version. The `JAVA_HOME` environment variable
can be set as below:

| Platform | Command                                             |
|:--------:|-----------------------------------------------------|
|   Unix   | `export JAVA_HOME=/usr/java/17.0.8`                 |
|   OSX    | `export JAVA_HOME=/usr/libexec/java_home -v 17.0.8` |
| Windows  | `set JAVA_HOME="C:\Program Files\Java\17.0.8"`      |

Clone the repository and, within the checkout directory with the source code, run gradle build:

```shell
# Build 
./gradlew clean build

# Unzip Distribution
pushd app/build/distributions
  unzip app-1.0.0.zip
popd
```

Once the solution has been built and the distribution installed, it can be executed by running the `app` bash script.

```shell
./app/build/distributions/app-1.0.0/bin/app
```

```shell
Usage:

app [-hV] [COMMAND]

Description:

Command Line Utilities for Performance Simulations.

Options:
  -h, --help      Show this help message and exit.
  -V, --version   Print version information and exit.

Available Commands:
  process  Process the given input <file> through the specified <processorName>.
```

## Logging

The application uses [SLF4J](http://www.slf4j.org/) as the logging facade
and [Log4j 2](https://logging.apache.org/log4j/2.x/) as the implementation.

By default, logs are written to the `logs/app.logs` file and the root level is set as `DEBUG`, this can be changed
through the [log4j2.xml](app/src/main/resources/log4j2.xml)

## Available Commands

### process

```shell
./app/build/distributions/app-1.0.0/bin/app process --help
```

```shell
Usage:

app process [-hV] -f=<file> [-p=<processorName>] [-r=<readerName>] [-w=<writerName>]

Description:

Process the given input <file> through the specified <processorName>.
Records are read and written using <readerName> and <writerName> respectively.

Options:
  -f, --file=<file>   Input File
  -p, --processor=<processorName>
                      Name of the Processor instance to use.
                      Default: 'StreamProcessor'.
                      Available: 'StreamProcessor'.
  -r, --reader=<readerName>
                      Name of the RecordReader instance to use.
                      Default: 'ErrorTolerantRecordReader'.
                      Available: 'DefaultRecordReader', 'ErrorTolerantRecordReader'.
  -w, --writer=<writerName>
                      Name of the RecordWriter instance to use.
                      Default: 'DefaultRecordWriter'.
                      Available: 'DefaultRecordWriter'.
  -h, --help          Show this help message and exit.
  -V, --version       Print version information and exit.
```

Below is the list of supported options for the different parameters, where the actual implementation can be found and a
summary of what does it do:

| Parameter         | Option                                                                                                                  | Description                                                                                                                         |
|-------------------|-------------------------------------------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------|
| `<readerName>`    | [DefaultRecordReader](utilities/src/main/kotlin/com/playground/service/impl/readers/DefaultRecordReader.kt)             | First reader implementation, without any error handling (errors parsing a record halt the entire process)                           | 
| `<readerName>`    | [ErrorTolerantRecordReader](utilities/src/main/kotlin/com/playground/service/impl/readers/ErrorTolerantRecordReader.kt) | The default reader implementation, will try to process the whole input even if there are errors while parsing a record              | 
| `<writerName>`    | [DefaultRecordWriter](utilities/src/main/kotlin/com/playground/service/impl/writers/DefaultRecordWriter.kt)             | The default writer implementation, it simply outputs the record (sequence number and message) to standard output                    | 
| `<processorName>` | [StreamProcessor](utilities/src/main/kotlin/com/playground/service/impl/StreamProcessor.kt)                             | The default processor implementation, it process the input file using a stream approach (read and write every record one at a time) | 

To execute the application against the original `logi.bin` file:

```shell
./app/build/distributions/app-1.0.0/bin/app process --file $(pwd)/utilities/src/test/resources/logi.bin
```