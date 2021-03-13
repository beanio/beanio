# BeanIO [![Java CI](https://github.com/beanio/beanio/actions/workflows/gradle.yml/badge.svg)](https://github.com/beanio/beanio/actions/workflows/gradle.yml) [![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=beanio&metric=sqale_rating)](https://sonarcloud.io/dashboard?id=beanio) [![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=beanio&metric=security_rating)](https://sonarcloud.io/dashboard?id=beanio) [![Coverage](https://sonarcloud.io/api/project_badges/measure?project=beanio&metric=coverage)](https://sonarcloud.io/dashboard?id=beanio)

A Java library for marshalling and unmarshalling bean objects from XML, CSV, delimited and fixed length stream formats.

## Installation

Please note the new groupId `com.github.beanio`.

### Maven

To use snapshot versions, you have to configure the following repository:

```xml
<repositories>
    <repository>
        <id>ossrh</id>
        <url>https://s01.oss.sonatype.org/content/repositories/snapshots/</url>
        <snapshots>
            <enabled>true</enabled>
        </snapshots>
        <releases>
            <enabled>false</enabled>
        </releases>
    </repository>
</repositories>
```

Then add this dependency:

```xml
<dependency>
    <groupId>com.github.beanio</groupId>
    <artifactId>beanio</artifactId>
    <version>3.0.0-SNAPSHOT</version>
</dependency>
```

### Gradle

To use snapshot versions, you have to configure the following repository:

```groovy
repositories {
    maven {
        url 'https://s01.oss.sonatype.org/content/repositories/snapshots'
    }
}
```

Then add this dependency:

```groovy
implementation 'com.github.beanio:beanio:3.0.0-SNAPSHOT'
```

## What's new in v3?

See [changelog.txt](changelog.txt)

## Project status

This is a fork of the [original BeanIO library](https://github.com/kevinseim/beanio). It combines :

* the legacy SVN codebase that was hosted at https://code.google.com/p/beanio/
* "the future BeanIO 3.x" that was started at https://github.com/kevinseim/beanio
* several fixes from other forks (see commit messages for more info)
 
The website for version 2.x is available at http://www.beanio.org.

The website for version 3.x is available at https://beanio.github.io.
