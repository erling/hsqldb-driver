Hsqldb-driver for embedded server
================================

Hsqldb-driver that start an embedded server instead of in-process db. The server is lazy initialized on
the first connection request.

Usage
=====

Simply load (instead of <code>org.hsqldb.jdbcDriver</code>)

    no.eh.HsqldbServerJdbcDriver

and use connection-url

    jdbc:hsqldb:mem:[DBNAME]
    jdbc:hsqldb:file:[DBNAME]


external connections can be done by using

    jdbc:hsqldb:hsql://localhost:9001/[DBNAME]



