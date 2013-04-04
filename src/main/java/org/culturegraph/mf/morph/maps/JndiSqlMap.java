/*
 *  Copyright 2013 Deutsche Nationalbibliothek
 *
 *  Licensed under the Apache License, Version 2.0 the "License";
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.culturegraph.mf.morph.maps;

import java.io.Closeable;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.culturegraph.mf.exceptions.MorphException;


/**
 * @author Daniel
 * 
 */
public final class JndiSqlMap extends AbstractReadOnlyMap<String, String> implements Closeable {

	private boolean isUninitialized = true;
	private DataSource datasource;
	private String query;
	private Connection connection;

	private PreparedStatement preparedStatement;

	public void init() {

		try {
			connection = datasource.getConnection();
			this.preparedStatement = connection.prepareStatement(query);
		} catch (SQLException e) {
			throw new MorphException(e);
		}
		isUninitialized = false;
	}

	@Override
	public void close() throws IOException {
		try {
			if (connection != null) {
				connection.close();
			}
		} catch (SQLException e) {
			throw new MorphException(e);
		}
	}

	protected DataSource getDatasource() {
		return datasource;
	}

	public void setDatasource(final String name) {
		try {
			this.datasource = (DataSource) new InitialContext().lookup(name);
		} catch (NamingException e) {
			throw new MorphException(e);
		}
	}

	public void setQuery(final String query) {
		this.query = query;
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
		} catch (SQLException e) {
			throw new MorphException(e);
		}
		return resultString;
	}

}
