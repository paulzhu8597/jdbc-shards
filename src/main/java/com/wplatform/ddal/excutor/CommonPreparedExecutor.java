/*
 * Copyright 2014-2015 the original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the “License”);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an “AS IS” BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
// Created on 2015年4月12日
// $Id$

package com.wplatform.ddal.excutor;

import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

import com.wplatform.ddal.command.Prepared;
import com.wplatform.ddal.dbobject.schema.Schema;
import com.wplatform.ddal.dbobject.table.Table;
import com.wplatform.ddal.dbobject.table.TableMate;
import com.wplatform.ddal.dispatch.RoutingHandler;
import com.wplatform.ddal.engine.Database;
import com.wplatform.ddal.engine.Session;
import com.wplatform.ddal.message.DbException;
import com.wplatform.ddal.message.ErrorCode;
import com.wplatform.ddal.result.ResultInterface;

/**
 * @author <a href="mailto:jorgie.mail@gmail.com">jorgie li</a>
 */
public abstract class CommonPreparedExecutor<T extends Prepared> implements PreparedExecutor {

    protected T prepared;
    protected Session session;
    protected Database database;
    protected ThreadPoolExecutor sqlExecutor;
    protected RoutingHandler routingHandler;
    
    private Map<String, JdbcOperations> jdbcOperationMapping;


    /**
     * @param session
     * @param prepared
     */
    public CommonPreparedExecutor(Session session, T prepared) {
        super();
        this.prepared = prepared;
        this.session = session;
        this.database = session.getDatabase();
        database.getDataSourceRepository()
    }

    /**
     * Wrap a SQL exception that occurred while accessing a linked table.
     *
     * @param sql the SQL statement
     * @param ex the exception from the remote database
     * @return the wrapped exception
     */
    protected static DbException wrapException(String sql, Exception ex) {
        SQLException e = DbException.toSQLException(ex);
        return DbException.get(ErrorCode.ERROR_ACCESSING_DATABASE_TABLE_2, e, sql, e.toString());
    }

    /**
     * Execute the query.
     *
     * @param maxrows the maximum number of rows to return
     * @return the result set
     * @throws DbException if it is not a query
     */
    @Override
    public ResultInterface executeQuery(int maxrows) {
        throw DbException.get(ErrorCode.METHOD_ONLY_ALLOWED_FOR_QUERY);
    }

    /**
     * Execute the statement.
     *
     * @return the update count
     * @throws DbException if it is a query
     */
    @Override
    public int executeUpdate() {
        throw DbException.get(ErrorCode.METHOD_NOT_ALLOWED_FOR_QUERY);
    }

    /**
     * Check if this object is a query.
     *
     * @return true if it is
     */
    public boolean isQuery() {
        return false;
    }
    
    protected T getPrepared() {
        return this.prepared;
    }

    /**
     * @param tableName
     */
    public TableMate getTableMate(String tableName) {
        Table table = database.getSchema(session.getCurrentSchemaName()).findTableOrView(session, tableName);
        if (table == null) {
            String[] schemaNames = session.getSchemaSearchPath();
            if (schemaNames != null) {
                for (String name : schemaNames) {
                    Schema s = database.getSchema(name);
                    table = s.findTableOrView(session, tableName);
                    if (table != null) {
                        break;
                    }
                }
            }
        }
        if(table != null && table instanceof TableMate) {
            return (TableMate)table;
        }
        throw DbException.get(ErrorCode.TABLE_OR_VIEW_NOT_FOUND_1, tableName);
    }

}