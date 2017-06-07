# ISAcreator

Here are some instructions to compile, package and run ISAcreator desktop application based on [ISAcreator](https://github.com/ISA-tools/ISAcreator).

### Prerequisites

Note: all sample bash scripts given below are to be run on [ubuntu](https://www.ubuntu.com/) system distributions.

#### Java

Check that Java is installed on your system:

```sh
java -version
```

Install [Java](http://www.oracle.com/technetwork/java/index.html):

```sh
sudo apt-add-repository ppa:webupd8team/java
sudo apt-get update
sudo apt-get install oracle-java7-installer

```

Update Java7 environment variables (JAVA_HOME and PATH) if required:

```sh
sudo apt-get install oracle-java7-set-default
```

#### Maven

Check that Maven is installed on your system:

```sh
mvn -version
```

Install [Maven](https://maven.apache.org):

```sh
sudo apt-get install maven
```

Update maven's settings:

```sh
nano ~/.m2/settings.xml
```

Add mirror for the codehaus repository:

```xml
<mirror>
  <id>codehaus-repo</id>
  <mirrorOf>repository.codehaus</mirrorOf>
  <name>codehaus.org repo mirror</name>
  <url>http://repo.maven.apache.org/maven2</url>
</mirror>
```

If the file does not exist yet, please create new with the following sample content

```xml
<?xml version="1.0" encoding="UTF-8"?>
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 http://maven.apache.org/xsd/settings-1.0.0.xsd">
  <pluginGroups/>
  <proxies/>
  <servers/>
  <mirrors>
    <mirror>
      <id>codehaus-repo</id>
      <mirrorOf>repository.codehaus</mirrorOf>
      <name>codehaus.org repo mirror</name>
      <url>http://repo.maven.apache.org/maven2</url>
    </mirror>
  </mirrors>
  <profiles/>
</settings>
```

### ISAcreator

#### Repositories

Clone the `ISAcreator` repository (`scigraph` branch).

#### Update compiler plugin settings

It is recommended to update all `source`, `target` and `executable` parameters for `maven-compiler-plugin` (see `pom.xml` file)

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <version>2.3.2</version>
    <configuration>
        <source>1.7</source>
        <target>1.7</target>
        <executable>$JAVA_HOME/bin/javac</executable>
        <fork>true</fork>
        <debug>true</debug>
        <meminitial>128m</meminitial>
        <maxmem>512m</maxmem>
    </configuration>
</plugin>
```

Eg.: set `/usr/bin/javac` as `executable` if you have installed Java as described in prerequisites section.

#### Set scigraph default URL (if required)

Normally, the scigraph URL is set within `Settings/settings.properties` file:

```properties
scigraph.host=yourcompany.com
```

However, this URL could be also set in code (just in case users forget to configure it in advance) - simply update `SCIGRAPH_HOST_DEFAULT` constant within `SciGraphSearchResultHandler.java` file:

Eg.:

```sh
vi ./src/main/java/org/isatools/isacreator/ontologymanager/scigraph/jsonresulthandlers/SciGraphSearchResultHandler.java
```

```java
public class SciGraphSearchResultHandler {

    public static final String SCIGRAPH_HTTP_DEFAULT = "https";
    public static final String SCIGRAPH_HOST_DEFAULT = "yourcompany.net";
    public static final String SCIGRAPH_PORT_DEFAULT = "9000";

```

Note: both protocol and port for scigraph service could be also updated there.


#### Build (via bash script)

Note: it is strongly recommended to build the project via bash scripts for the first time in order to download additional configuration files.

Execute `compile` script in terminal. It will automatically download required files, resolve dependencies and build the project

```sh
./compile.sh
```

#### Build (via Idea IDE)

Create new project from existing sources (`File - New - Project from existing sources`) and select the `pom.xml` file in local repository. Use default settings to create the project and wait until all dependencies are successfull resolved. Open Maven settings in Idea (`File - Settings - Build, Execution, Deployment - Build Tools - Maven - Runner`) and check `Skip tests` option.

Now, open `Maven Projects` window (`View - Tool Windows - Maven Projects`) to build all `clean`, `compile` and `package` goals (see `ISAcreator - Lifecycle - [goal]`).

#### Run

Execute `run` script to start desktop application:

```sh
./run.sh
```

or (for this particular version)

```sh
java -cp target/ISAcreator-1.7.9.jar org.isatools.isacreator.launch.ISAcreatorApplication
```

Have fun :)
