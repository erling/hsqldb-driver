package no.eh

import javax.net.ServerSocketFactory

import org.hsqldb.server.ServerConstants
import spock.lang.Specification

import java.sql.DriverManager

import static java.sql.DriverManager.getDriver;

class HsqldbServerJdbcDriverSpec extends Specification {

    def driverClassName = "no.eh.HsqldbServerJdbcDriver"

    def "Loading the driver should register the correct driver"() {
        given:
        def expectedClass = HsqldbServerJdbcDriver.class

        when:
        Class.forName(driverClassName);

        then:
        assert expectedClass == getDriver("jdbc:hsqldb:").class
    }

    def "Specifying in-memory connection url should start server with default settings"() {
        given:
        def connectionUrl = "jdbc:hsqldb:mem:testdb"
        Class.forName(driverClassName);

        when:
        def connection = DriverManager.getConnection(connectionUrl);

        then:
        assert connection != null
        assert HsqldbServerJdbcDriver.getServer().getState() == ServerConstants.SERVER_STATE_ONLINE

        cleanup:
        HsqldbServerJdbcDriver.stopServer();
    }


    def "Driver should tolerate url with database names containing ?, : and /"() {
        given:
        def connectionUrl = "jdbc:hsqldb:mem:testdb::/?host=foo"
        Class.forName(driverClassName);

        when:
        def connection = DriverManager.getConnection(connectionUrl);

        then:
        assert connection != null

        cleanup:
        HsqldbServerJdbcDriver.stopServer();
    }


    def "Loading the driver two times should work" () {
        given:
        def connectionUrl = "jdbc:hsqldb:mem:testdb"
        Class.forName(driverClassName);
        DriverManager.getConnection(connectionUrl);

        when: "Loading the driver a second time"
        ClassLoader.getSystemClassLoader().loadClass(driverClassName)

        and: "requesting a connection"
        def connection = DriverManager.getConnection(connectionUrl);

        then:
        assert connection != null

        cleanup:
        HsqldbServerJdbcDriver.stopServer();
    }

    def "Driver should start server on next available port"() {
        given: "In-memory connection url"
        def connectionUrl = "jdbc:hsqldb:mem:testdb"
        Class.forName(driverClassName);

        and: "Default port in use"
        ServerSocketFactory.getDefault().createServerSocket(ServerConstants.SC_DEFAULT_HSQL_SERVER_PORT)

        when:
        DriverManager.getConnection(connectionUrl);

        then:
        assert HsqldbServerJdbcDriver.getServer().getPort() > ServerConstants.SC_DEFAULT_HSQL_SERVER_PORT

        cleanup:
        HsqldbServerJdbcDriver.stopServer();
    }

}
