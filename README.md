# metafacture-runner #

Standalone commandline applications for working with Flux and Metamorph scripts

## Quickstart ##

1. Download the latest distribution package from [the metafacture-runner/releases](https://github.com/culturegraph/metafacutre-runner/releases) page. Make sure that you do download a distribution package and _not_ a source code package. The latest distribution package is [metafacture-runner 3.1.1](https://github.com/culturegraph/metafacture-runner/releases/download/metafacture-runner-3.1.1/metafacture-runner-3.1.1-dist.tar.gz).
2. Extract the downloaded archive:
   ```bash
   $ tar xzf metafacture-runner-3.1.1-dist.tar.gz
   ```
   This will create a new directory containing a ready-to-use metafacture distribution.
3. Change into the newly created directory:
   ```bash
   $ cd metafacture-runner-3.1.1
   ```
4. Run one of the example scripts:
   ```bash
   $ ./flux.sh examples/read/marc21/read-marc21.flux
   ```
   This example will print a number of marc21 records on standard out.

The _examples_ folder contains many more examples which provide a good starting point for learning metafacture. If you have any questions please join our [mailing list](http://lists.dnb.de/mailman/listinfo/metafacture) or use our issue-based discussion forum over at [metafacture-documentation](https://github.com/culturegraph/metafacture-documentation).
