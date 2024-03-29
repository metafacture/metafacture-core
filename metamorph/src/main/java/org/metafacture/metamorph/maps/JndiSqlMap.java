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

package org.metafacture.metamorph.maps;

import org.metafacture.metamorph.api.MorphExecutionException;
import org.metafacture.metamorph.api.helpers.AbstractReadOnlyMap;

import java.io.Closeable;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

/**
 * A map which queries an sql database provided as jndi
 * resource.
 *
 * @author Daniel Schäfer
 *
 */
public final class JndiSqlMap extends AbstractReadOnlyMap<String, String> implements Closeable {

    private DataSource datasource;
    private String query;

    /**
     * Creates an instance of {@link JndiSqlMap}.
     */
    public JndiSqlMap() {
    }

    /**
     * Sets the {@link DataSource}.
     *
     * @see InitialContext
     *
     * @param name the name to be looked up in the InitialContext.
     */
    public void setDatasource(final String name) {
        try {
            datasource = (DataSource) new InitialContext().lookup(name);
        }
        catch (final NamingException e) {
            throw new MorphExecutionException("jndisqlmap: lookup of data source failed", e);
        }
    }

    /**
     * Sets the query.
     *
     * @param query the query
     */
    public void setQuery(final String query) {
        this.query = query;
    }

    @Override
    public String get(final Object key) {
        String resultString = null;
        try (
                Connection connection = datasource.getConnection();
                PreparedStatement statement = connection.prepareStatement(query)
        ) {
            statement.setString(1, key.toString());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.first()) {
                    resultString = resultSet.getString(1);
                }
            }
        }
        catch (final SQLException e) {
            throw new MorphExecutionException("jndisqlmap: execution of sql query failed", e);
        }
        return resultString;
    }

    @Override
    public void close() throws IOException {
        // Nothing to do
    }

}
