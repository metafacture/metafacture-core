package org.culturegraph.mf.morph.functions.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.culturegraph.mf.exceptions.MorphException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by tgaengler on 30.09.16.
 */
public final class SQLUtils {

	private static final Logger LOG = LoggerFactory.getLogger(SQLUtils.class);

	private static final String JDBC_PREFIX_IDENTIFIER = "jdbc";
	private static final String COLON = ":";
	private static final String SLASH = "/";
	private static final String DEFAULT_OPTIONS = "?useUnicode=true&characterEncoding=UTF-8&autoReconnect=true&useSSL=false";

	public static Connection createSQLConnection(final String driver,
	                                             final String databaseType,
	                                             final String host,
	                                             final String port,
	                                             final String database,
	                                             final String login,
	                                             final String password) {

		try {

			Class.forName(driver).newInstance();

			final StringBuilder urlSB = new StringBuilder();
			urlSB.append(SQLUtils.JDBC_PREFIX_IDENTIFIER).append(SQLUtils.COLON)
					.append(databaseType).append(SQLUtils.COLON).append(SQLUtils.SLASH).append(SQLUtils.SLASH)
					.append(host).append(SQLUtils.COLON)
					.append(port).append(SQLUtils.SLASH)
					.append(database)
					.append(DEFAULT_OPTIONS);

			final String url = urlSB.toString();

			SQLUtils.LOG.debug("try to connection to database with connection string '{}'", url);

			return DriverManager.getConnection(url, login, password);
		} catch (final ClassNotFoundException | SQLException | InstantiationException | IllegalAccessException e) {

			throw new MorphException(e);
		}
	}
}
