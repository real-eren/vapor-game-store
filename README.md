# vapor-game-store
This project requires [Maven](https://maven.apache.org/) and JDK 11 to build, JRE 11+ to run.

## How to build

### .class files
`mvn clean compile`

### Stand-alone jars
`mvn clean package`

## How to run

### CLI jar
```
java  -jar path/to/cli-jar
```
looks in root dir for `"credentials/cli.credentials"`  

or

```
java  -jar path/to/cli-jar  path/to/credentials/file
```
the first argument is used as the path to the credentials file

## Credentials
These files must be distributed outside the VCS
