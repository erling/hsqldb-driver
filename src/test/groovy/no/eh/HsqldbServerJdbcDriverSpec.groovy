package no.eh

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
        HsqldbServerJdbcDriver.stopServer();

        when:
        def connection = DriverManager.getConnection(connectionUrl);

        then:
        assert connection != null
        assert HsqldbServerJdbcDriver.getServer().getState() == ServerConstants.SERVER_STATE_ONLINE
    }


    def "Driver should tolerate url with database names containing : and /"() {
        given:
        def connectionUrl = "jdbc:hsqldb:mem:testdb::/"
        Class.forName(driverClassName);
        HsqldbServerJdbcDriver.stopServer();

        when:
        def connection = DriverManager.getConnection(connectionUrl);

        then:
        assert connection != null
    }


}
