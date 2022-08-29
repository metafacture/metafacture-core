Place your JNDI properties files in this directory. By default
metafacture-runner uses [simpleJNDI](http://code.google.com/p/osjava/wiki/SimpleJNDI)
as JNDI provider.

# Examples

One of the most common use cases of JNDI is to provide data sources for
JDBC. However, JNDI can be used to pass other variables into a program
as well. The following to examples demonstrate how to define data sources
and other values.

More elaborate examples can be found in the simpleJDNI documentation which
is part of the [binary download package](http://code.google.com/p/osjava/downloads/detail?name=simple-jndi-0.11.4.1.zip&can=2&q=)
of simpleJNDI.

## Data sources

To register a data source with the name `MySqlDB`, create a property
file named `MySqlDB.properties` with the following contents:

```
type=javax.sql.DataSource
driver=com.mysql.jdbc.Driver
url=jdbc:mysql://localhost:3306/DBNAME
user=USER
password=PASSWORD
```

## Other values

Other values can be made available via JNDI by creating property files
such as the following:

```
enabled = true
enabled.type = java.lang.Boolean
```

If this file is saved as `debug.properties`, the value of enabled can
be accessed as follows: `debug.enabled`.
