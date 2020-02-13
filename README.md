BeanIO
======

A Java library for marshalling and unmarshalling bean objects from XML, CSV, delimited and fixed length stream formats.

**Note:**

This is a fork of the original BeanIO library. It combines :

* the legacy SVN codebase that was hosted at https://code.google.com/p/beanio/
* "the future BeanIO 3.x" that was started at https://github.com/kevinseim/beanio
* several fixes from other forks (see commit messages for more info)
 
The website for version 2.x is available at http://www.beanio.org.

# What's new in v3?

* Requires JDK 1.7 or higher
* Removed support for ASM class rewriting
* Removed Spring framework support
* Fixed IOUtil.getResource() to use ClassLoader argument (gc0109)
* Added support for validating marshalled fields (gc0096)
* BeanWriter now implements AutoCloseable
* BeanReader now implements Closeable
* Added SegmentBuilder.at(int) method
* Fixed a thread issue in `DateTypeHandlerSupport`
* Fixed a thread issue in `NumberTypeHandler`

