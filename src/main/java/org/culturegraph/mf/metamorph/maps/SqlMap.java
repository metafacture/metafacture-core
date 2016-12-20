/*
 * Copyright 2013, 2014 Deutsche Nationalbibliothek
 *
 * Licensed under the Apache License, Version 2.0 the "License";
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.culturegraph.mf.metamorph.maps;

import java.io.Closeable;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.culturegraph.mf.metamorph.api.MorphExecutionException;
import org.culturegraph.mf.metamorph.api.helpers.AbstractReadOnlyMap;

/**
 * A map implementation that queries an sql database.
 *
 * @author Daniel Schäfer
 * @author Markus Michael Geipel
 *
 */
public final class SqlMap extends AbstractReadOnlyMap<String, String> implements
		Closeable {

	private boolean isUninitialized = true;

	private Connection conn;
	private String host;
	private String login;
	private String password;
	private String database;
	private String query;
	private String driver;

	private PreparedStatement preparedStatement;

	public void init() {

		try {
			preparedStatement = getMySqlConnection().prepareStatement(query);
		} catch (final SQLException e) {
			throw new MorphExecutionException(
					"sqlmap: could not create prepared statement for query", e);
		}
		isUninitialized = false;
	}

	@Override
	public void close() throws IOException {
		try {

			if (conn != null) {
				conn.close();
			}
		} catch (final SQLException e) {
			throw new MorphExecutionException("sqlmap: could not close db connection",
					e);
		}
	}

	private Connection getMySqlConnection() {
		try {
			Class.forName(driver);

			conn = DriverManager.getConnection("jdbc:mysql://" + host + "/"
					+ database + "?" + "user=" + login + "&" + "password="
					+ password);
		} catch (final ClassNotFoundException | SQLException e) {
			throw new MorphExecutionException("sqlmap: cannot create db connection",
					e);
		}
		return conn;
	}

	@Override
	public String get(final Object key) {
		if (isUninitialized) {
			init();
		}
		String resultString = null;
		final ResultSet resultSet;
		try {
			preparedStatement.setString(1, key.toString());
			resultSet = preparedStatement.executeQuery();
			if (resultSet.first()) {
				resultString = resultSet.getString(1);
			}
			resultSet.close();
		} catch (final SQLException e) {
			throw new MorphExecutionException(
					"sqlmap: execution of prepared statement failed", e);
		}
		return resultString;
	}

	public void setDriver(final String driver) {
		this.driver = driver;
	}

	public void setHost(final String host) {
		this.host = host;
	}

	public void setLogin(final String login) {
		this.login = login;
	}

	public void setPassword(final String password) {
		this.password = password;
	}

	public void setDatabase(final String database) {
		this.database = database;
	}

	public void setQuery(final String query) {
		this.query = query;
	}

}
