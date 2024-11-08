package sddl.server.db;

import com.badlogic.gdx.Gdx;
import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import javax.sql.DataSource;

/**
 * Factory of MySqlDataSource object
 * that communicates with server database
 *
 * @author Pedro Sampaio
 * @since 1.4
 */
public class DataSourceFactory {

    public static DataSource getMySQLDataSource() {
        Properties props = new Properties();
        FileInputStream fis = null;
        MysqlDataSource mysqlDS = null;
        String path = "sql/db.properties";
        try {
            fis = new FileInputStream(path);
            props.load(fis);
            mysqlDS = new MysqlDataSource();
            mysqlDS.setURL(props.getProperty("MYSQL_DB_URL"));
            mysqlDS.setUser(props.getProperty("MYSQL_DB_USERNAME"));
            mysqlDS.setPassword(props.getProperty("MYSQL_DB_PASSWORD"));
        } catch (IOException e) {
            System.err.println("Could not find db properties file: "+path);
            e.printStackTrace();
        }
        return mysqlDS;
    }
}
