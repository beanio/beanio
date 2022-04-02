# &nbsp;<img src="https://raw.githubusercontent.com/beanio/beanio.github.io/main/static/img/logo.svg" height="25"/> BeanIO ![](https://img.shields.io/maven-central/v/com.github.beanio/beanio) [![Java CI](https://github.com/beanio/beanio/actions/workflows/gradle.yml/badge.svg)](https://github.com/beanio/beanio/actions/workflows/gradle.yml) [![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=beanio&metric=sqale_rating)](https://sonarcloud.io/dashboard?id=beanio) [![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=beanio&metric=security_rating)](https://sonarcloud.io/dashboard?id=beanio) [![Coverage](https://sonarcloud.io/api/project_badges/measure?project=beanio&metric=coverage)](https://sonarcloud.io/dashboard?id=beanio)

A Java library for marshalling and unmarshalling bean objects from XML, CSV, delimited and fixed length stream formats.

## Installation

If you're coming from BeanIO 2.x, please note the new groupId `com.github.beanio`. Package names remain the same as
before (`org.beanio.*`).

### Maven

<details>
  <summary>To use snapshot versions, configure the following repository:</summary>

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
</details>

Add the following dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>com.github.beanio</groupId>
    <artifactId>beanio</artifactId>
    <version>3.0.0.M1</version>
</dependency>
```

### Gradle

<details>
  <summary>To use snapshot versions, configure the following repository:</summary>

```groovy
repositories {
    maven {
        url 'https://s01.oss.sonatype.org/content/repositories/snapshots'
    }
}
```
</details>

Add the following dependency to your `build.gradle`:

```groovy
implementation 'com.github.beanio:beanio:3.0.0.M1'
```

## What's new in v3?

See [changelog.txt](changelog.txt)

## Project status

This is a fork of the [original BeanIO library](https://github.com/kevinseim/beanio). It combines :

* the legacy SVN codebase for 2.x that was hosted at https://code.google.com/p/beanio/
* "the future BeanIO 3.x" that was started at https://github.com/kevinseim/beanio
* several fixes from other forks (see commit messages for more info)

The website for version 3.x is available at https://beanio.github.io.

The website for version 2.x is available at http://www.beanio.org.
