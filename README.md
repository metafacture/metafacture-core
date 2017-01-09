![Metafacture](https://raw.github.com/wiki/culturegraph/metafacture-core/img/metafacture.png)

Metafacture is a toolkit for processing semi-structured data with a focus on library metadata. It provides a versatile set of tools for reading, writing and transforming data. Metafacture can be used as a stand-alone application or as a java library in other applications. The name Metafacture is a portmanteau of the words *meta* data and manu*facture*.

Metafacture consists of a core library and a number of plugin packages and satellite projects which build on the core library and extend it with additional tools and features. This page describes the core package. Have a look at the [Plugins and Tools page](https://github.com/culturegraph/metafacture-core/wiki/Plugins-and-Tools) on the wiki for an overview of the supplementary packages and projects.

Originally, Metafacture was developed as part of the [Culturegraph](http://culturegraph.org) platform but its now used by others, too: [see who uses Metafacture](https://github.com/culturegraph/metafacture-core/wiki/Who-uses-Metafacture).

# Build

[![Build Status](https://secure.travis-ci.org/culturegraph/metafacture-core.svg?branch=master)](https://travis-ci.org/culturegraph/metafacture-core/) [![Dependency Status](https://www.versioneye.com/user/projects/5592673839656100200000d7/badge.svg)](https://www.versioneye.com/user/projects/5592673839656100200000d7) [![Maven Central](https://img.shields.io/maven-central/v/org.culturegraph/metafacture-core.svg)]() [![Javadocs](http://www.javadoc.io/badge/org.culturegraph/metafacture-core.svg?color=blue)](http://www.javadoc.io/doc/org.culturegraph/metafacture-core)

See the [.travis.yml](./.travis.yml) file for details on the CI config used by Travis.

# Getting started

You can use Metafacture either as a stand-alone application or as a Java library in your own projects.

## Metafacture as a stand-alone application
 
As of version 2.0.0 the stand-alone application is no longer part of the core library package. The application package can now be found at [culturegraph/metafacture-runner](https://github.com/culturegraph/metafacture-runner). If you are only interested in running Flux scripts without doing any Java programming you should head over there. 

## Using Metafacture as a Java libary

If you want use Metafacture in your own Java projects all you need to do is to include the metafacture-core package as a dependency in your project. metafacture-core is available from [Maven Central](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22org.culturegraph%22). To use it, add the following dependency declaration to your `pom.xml`:

    <dependency>
    	<groupId>org.culturegraph</groupId>
    	<artifactId>metafacture-core</artifactId>
    	<version>4.0.0</version>
    </dependency>

Our integration server automatically publishes successful builds of the master branch as snapshot versions on [Sonatype OSS Repository](https://oss.sonatype.org/index.html#nexus-search;quick~culturegraph).

<!--
TODO: Add

* Link to getting started tutorial
* Mention the application-archetype
-->

# Building metafacture-core from source

Building metafacture-core from source is easy. All you need is git and [maven](http://maven.apache.org/):

1. Clone the metafacture-core repository:

        $ git clone https://github.com/culturegraph/metafacture-core.git

2. Build and install in your local repository:

        $ mvn clean install

    It is important to perform this step *before* importing the project into your IDE because it generates the lexer and parser sources for Flux from antlr grammar definitions. Otherwise your IDE will fail to compile the sources if it has integrated background compilation (like Eclipse has, for instance).

See [Code Quality and Style](https://github.com/culturegraph/metafacture-core/wiki/Code-Quality-and-Style) on the wiki for further information on the sources.

<!--
TODO: Include a link to a page which explains how to write plugins
-->

# Stay updated

For support and discussion join the [mailing list](http://lists.dnb.de/mailman/listinfo/metafacture).
