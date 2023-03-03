![Metafacture](https://raw.github.com/wiki/metafacture/metafacture-core/img/metafacture.png)

[![Build](https://github.com/metafacture/metafacture-core/workflows/Build/badge.svg?branch=master)](https://github.com/metafacture/metafacture-core/actions?query=workflow%3ABuild)

Metafacture is a toolkit for processing semi-structured data with a focus on library metadata. It provides a versatile set of tools for reading, writing and transforming data. Metafacture can be used as a stand-alone application or as a Java library in other applications. The name Metafacture is a portmanteau of the words *meta*data and manu*facture*.

Metafacture includes a [large number of modules](https://github.com/metafacture/metafacture-documentation/blob/master/flux-commands.md) for operating on semi-structured data. These modules can be combined to build pipelines to perform complex metadata processing tasks. The pipelines can be constructed either in Java code or with the domain-specific language **Flux**. One of the core features of Metafacture is the **Metamorph** module. Metamorph is an XML-based language for specifying transformations of semi-structured data. It can be seamlessly integrated into Java code.

At its heart Metafacture is a framework for implementing modules for metadata processing. This makes Metafacture easily extendable with additional modules. The [plugins and tools page](https://github.com/metafacture/metafacture-core/wiki/Plugins-and-Tools) on the wiki shows supplementary packages and projects which extend Metafacture.

Originally, Metafacture was developed as part of the [Culturegraph](http://www.culturegraph.org) platform but it is developed independently now and used by others, too: [see who uses Metafacture](https://github.com/metafacture/metafacture-core/wiki/Who-uses-Metafacture).

# Getting started

You can either use Metafacture as a stand-alone application or include it as a Java library in your own projects.

## Metafacture as a stand-alone application

If you are only interested in running Flux scripts without doing any Java programming this is the way to go. The instructions assume that you are using a \*nix-like shell. [See more information in the wiki page about Flux](https://github.com/metafacture/metafacture-core/wiki/Flux-user-guide).

1. Download the latest distribution package from the [release page](https://github.com/metafacture/metafacture-core/releases). Make sure that you do download a distribution package and *not* a source code package (the file name should include `*-dist*`).

2. Extract the downloaded archive:
   ```bash
   $ tar xzf metafacture-core-$VERSION-dist.tar.gz
   ```
   This will create a new directory containing a ready-to-use Metafacture distribution.
3. Change into the newly created directory:
   ```bash
   $ cd metafacture-core-$VERSION
   ```
4. Run one of the example scripts:
   ```bash
   $ ./flux.sh examples/read/marc21/read-marc21.flux
   ```
   This example will print a number of MARC 21 records on standard output.

The `examples` folder contains many more examples which provide a good starting point for learning Metafacture. If you have any questions please join our [mailing list](http://lists.dnb.de/mailman/listinfo/metafacture) or use our issue-based discussion forum over at [metafacture-documentation](https://github.com/metafacture/metafacture-documentation).


## Using Metafacture as a Java library

If you want to use Metafacture in your own Java projects all you need is to add some dependencies to your project. As of Metafacture 5, the single metafacture-core package has been replaced with a number of domain-specific packages. You can find the list of packages on [Maven Central](https://search.maven.org/search?q=g:org.metafacture).

Alternatively, you can simply guess the package names from the top-level folders in the source code repository -- they are the same. For instance, if you want to use Metamorph in your project, simply add the following dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>org.metafacture</groupId>
    <artifactId>metamorph</artifactId>
    <version>$VERSION</version>
</dependency>
```

or if Gradle is your build tool of choice use:

```groovy
dependencies {
    implementation 'org.metafacture:metamorph:$VERSION'
}
```

Occasionally, we publish snapshot builds on [Sonatype OSS Repository](https://oss.sonatype.org/index.html#nexus-search;gav~org.metafacture~~~~). The version number is derived from the branch name. Snapshot builds from the master branch always have the version `master-SNAPSHOT`. We also provide sometimes pre releases as github packages.

<!--
TODO: Link to getting started tutorial
-->

# Building metafacture-core from source

Building metafacture-core from source is easy. All you need is git and JDK 8:

1. Clone the metafacture-core repository and change into the directory:

```bash
$ git clone https://github.com/metafacture/metafacture-core.git
$ cd metafacture-core
```

2. Invoke the Gradle wrapper to download Gradle and build metafacture-core (on Windows call `gradlew.bat install` instead):

```bash
$ ./gradlew install
```

Besides the resulting distribution in `metafacture-core/metafacture-runner/build/distributions/` this also provides builds in your local maven repository.


See [Code Quality and Style](https://github.com/metafacture/metafacture-core/wiki/Code-Quality-and-Style) on the wiki for further information on the sources.

<!--
TODO: Include a link to a page which explains how to write plugins
-->

# Stay updated

For support and discussion join the [mailing list](http://lists.dnb.de/mailman/listinfo/metafacture).
