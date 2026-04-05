package onlineboard.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

public class SQLiteConnection {
    private static final Logger logger = LoggerFactory.getLogger(SQLiteConnection.class);

    private String dbFilePath;
    private Connection connection;

    public SQLiteConnection(String dbFilePath) {
        this.dbFilePath = dbFilePath;
    }

    //データベースの接続を確立
    public synchronized void connect() {
        if (connection == null) {
            String url = "jdbc:sqlite:" + dbFilePath;
            try {
                connection = DriverManager.getConnection(url);
                logger.info("Connected to SQLite database: {}", dbFilePath);
            } catch (SQLException e) {
                logger.error("Error connecting to SQLite database: {}", e.getMessage(), e);
                throw new RuntimeException("Error connecting to SQLite database", e);
            }
        }
    }

    //データベースの接続を切断
    public synchronized void disconnect() {
        if (connection != null) {
            try {
                connection.close();
                connection = null;
                logger.info("Disconnected from SQLite database");
            } catch (SQLException e) {
                logger.error("Error disconnecting from SQLite database: {}", e.getMessage(), e);
            }
        }
    }

    //SQL文を実行(SELECT)
    public synchronized ResultSet executeQuery(String query, Object... params) throws SQLException {
        logger.debug("Executing query: {}", query);
        PreparedStatement statement = connection.prepareStatement(query);
        setParameters(statement, params);
        return statement.executeQuery();
    }

    //SQL文を実行(CREATE, INSERT等SELECT以外)
    public synchronized int executeUpdate(String query, Object... params) throws SQLException {
        logger.debug("Executing update: {}", query);
        PreparedStatement statement = connection.prepareStatement(query);
        setParameters(statement, params);
        return statement.executeUpdate();
    }

    //クエリに値をセットする
    private synchronized void setParameters(PreparedStatement statement, Object[] params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            statement.setObject(i + 1, params[i]);
        }
    }
}