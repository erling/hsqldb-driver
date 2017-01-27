package no.eh;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.ServerSocket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.net.ServerSocketFactory;

import org.hsqldb.DatabaseURL;
import org.hsqldb.persist.HsqlProperties;
import org.hsqldb.server.Server;
import org.hsqldb.server.ServerAcl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HsqldbServerJdbcDriver extends org.hsqldb.jdbcDriver {

    private static final Logger LOG = LoggerFactory.getLogger(HsqldbServerJdbcDriver.class);

    // Register this as the hsqldb driver!
    static {
        try {
            if (driverInstance != null) {
                DriverManager.deregisterDriver(driverInstance);
            }
            DriverManager.registerDriver(new HsqldbServerJdbcDriver());
        } catch (SQLException e) {
            // Ignored
        }
    }

    private static Server server = null;

    private static Pattern allowedChars = Pattern.compile("^(\\w+).*$");

    private static String connectionUrl;

    private static class NullWriter extends Writer {

        @Override
        public void write(final char[] cbuf, final int off, final int len) throws IOException {
        }

        @Override
        public void flush() throws IOException {
        }

        @Override
        public void close() throws IOException {
        }
    }

    @Override
    public Connection connect(String url, Properties info) throws SQLException {
        if (server == null) {
            try {
                server = createAndInitServer(url);
                Runtime.getRuntime().addShutdownHook(
                        new Thread() {

                            @Override
                            public void run() {
                                stopServer();
                            }
                        }
                );
                connectionUrl = "jdbc:hsqldb:hsql://localhost:" + server.getPort() + "/" + server.getDatabaseName(0, true);
                LOG.info("Started HSQLDB. Connection url: " + connectionUrl);
            } catch (IOException | ServerAcl.AclFormatException e) {
                throw new SQLException(e);
            }
        }
        return super.connect(connectionUrl, info);
    }

    public static Server getServer() {
        return server;
    }

    public static void stopServer() {
        if (server != null) {
            server.stop();
            server = null;
        }
    }

    private static Server createAndInitServer(final String url) throws IOException, ServerAcl.AclFormatException {
        Server server = new Server();
        server.setLogWriter(new PrintWriter(new NullWriter()));
        HsqlProperties props = DatabaseURL.parseURL(url, url.startsWith(DatabaseURL.S_URL_PREFIX), false);
        server.setProperties(props);
        String database = cleanDatabaseName(props.getProperty("database"));
        server.setDatabasePath(0, props.getProperty("connection_type") + database);
        server.setDatabaseName(0, database);
        server.setPort(findAvailablePort(server.getPort()));
        server.start();
        return server;
    }

    private static String cleanDatabaseName(final String dirtyDatabaseName) {
        Matcher matcher = allowedChars.matcher(dirtyDatabaseName);
        if (!matcher.find()) {
            throw new RuntimeException("Invalid database name: " + dirtyDatabaseName);
        }
        return matcher.group(1);
    }

    private static int findAvailablePort(int startPort) {
        int availablePort = startPort;
        while(!isPortAvailable(availablePort)) {
            availablePort++;
        }
        return availablePort;
    }

    private static boolean isPortAvailable(int port) {
        try {
            ServerSocket serverSocket = ServerSocketFactory.getDefault().createServerSocket(port);
            serverSocket.close();
            return true;
        } catch (Throwable e) {
            return false;
        }
    }

}
