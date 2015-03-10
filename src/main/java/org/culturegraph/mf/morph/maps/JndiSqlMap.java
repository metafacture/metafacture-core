/*
 *  Copyright 2013, 2014 Deutsche Nationalbibliothek
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A map which queries an sql database provided as jndi
 * resource.
 *
 * @author Daniel Schäfer
 *
 */
public final class JndiSqlMap extends AbstractReadOnlyMap<String, String>
		implements Closeable {

	private static final Logger LOG = LoggerFactory.getLogger(JndiSqlMap.class);
	private DataSource datasource;
	private String query;

	protected DataSource getDatasource() {
		return datasource;
	}

	public void setDatasource(final String name) {
		try {
			this.datasource = (DataSource) new InitialContext().lookup(name);
		} catch (final NamingException e) {
			throw new MorphException(e);
		}
	}

	public void setQuery(final String query) {
		this.query = query;
	}

	@Override
	public String get(final Object key) {
		String resultString = null;
		final ResultSet resultSet;
		PreparedStatement stmt = null;
		Connection con = null;
		try {
			con = datasource.getConnection();
			stmt = con.prepareStatement(query);
			stmt.setString(1, key.toString());
			resultSet = stmt.executeQuery();
			if (resultSet.first()) {
				resultString = resultSet.getString(1);
			}
			resultSet.close();
		} catch (final SQLException e) {
			throw new MorphException(e);
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (final SQLException e) {
					LOG.error("Can't close SQL-Statement.", e);
				}
			}
			if (con != null) {
				try {
					con.close();
				} catch (final SQLException e) {
					LOG.error("Can't close Connection.", e);
				}
			}
		}
		return resultString;
	}

	@Override
	public void close() throws IOException {
		// Nothing to do
	}

}
