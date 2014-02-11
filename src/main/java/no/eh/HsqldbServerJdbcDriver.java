package no.eh;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import org.hsqldb.DatabaseURL;
import org.hsqldb.Server;
import org.hsqldb.persist.HsqlProperties;
import org.hsqldb.server.ServerAcl;

public class HsqldbServerJdbcDriver extends org.hsqldb.jdbcDriver {

    // Register this as the hsqldb driver!
    static {
        try {
            if(driverInstance != null) {
                DriverManager.deregisterDriver(driverInstance);
            }
            driverInstance = new HsqldbServerJdbcDriver();
            DriverManager.registerDriver(driverInstance);
        } catch (SQLException e) {
            // Ignored
        }
    }

    private static Server server = null;

    @Override
    public Connection connect(String url, Properties info) throws SQLException {
        if(server == null) {
            try {
                server = createAndInitServer(url);
            } catch (IOException | ServerAcl.AclFormatException e) {
                throw new SQLException(e);
            }
        }
        return super.connect("jdbc:hsqldb:hsql://localhost/" + server.getDatabaseName(0, true), info);
    }

    public static Server getServer() {
        return server;
    }

    public static void stopServer() {
        if(server != null) {
            server.stop();
            server = null;
        }
    }

    private static Server createAndInitServer(String url) throws IOException, ServerAcl.AclFormatException {
        Server server = new Server();
        HsqlProperties props = DatabaseURL.parseURL(url, url.startsWith(DatabaseURL.S_URL_PREFIX), false);
        server.setProperties(props);
        String database = props.getProperty("database").replaceAll("[/:]", "");
        server.setDatabasePath(0, props.getProperty("connection_type") + database);
        server.setDatabaseName(0, database);
        server.setSilent(true);
        server.start();
        return server;
    }
}
