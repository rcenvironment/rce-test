/*
 * Copyright (C) 2006-2010 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
package de.rcenvironment.rce.components.sql;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.rcenvironment.commons.channel.VariantArray;
import de.rcenvironment.commons.variables.TypedValue;
import de.rcenvironment.rce.component.ComponentException;
import de.rcenvironment.rce.component.ComponentInstanceInformation;
import de.rcenvironment.rce.component.endpoint.Input;
import de.rcenvironment.rce.component.endpoint.Output;
import de.rcenvironment.rce.components.sql.ResultValueConverter.ResultSetMetaData;
import de.rcenvironment.rce.components.sql.commons.JDBCProfile;
import de.rcenvironment.rce.components.sql.commons.JDBCService;
import de.rcenvironment.rce.components.sql.commons.SqlComponentConstants;

/**
 * SQL Database implementation of {@link de.rcenvironment.rce.component.Component}.
 * 
 * @author Markus Kunde
 * @author Christian Weiss
 */
public abstract class AbstractSQLComponent extends AbstractComponent {

    protected static final String META_OUTPUT_PREFIX = "meta.";

    private static JDBCService jdbcService;

    private Connection connection;

    private Boolean hasDataInputs;

    private Boolean hasDataOutputs;

    private Map<String, Class<? extends Serializable>> dataOutputs;

    private Boolean hasFullDataOutputs;

    private Map<String, Class<? extends VariantArray>> fullDataOutputs;

    private Boolean hasFullDataInputs;

    private Map<String, Class<? extends VariantArray>> fullDataInputs;

    private Map<String, Class<? extends Serializable>> metaOutputs;

    public AbstractSQLComponent() {
        super(new CyclicInputConsumptionStrategy(), true);
    }

    protected void bindJdbcService(final JDBCService newJdbcService) {
        jdbcService = newJdbcService;
    }

    protected void unbindJdbcService(final JDBCService oldJdbcService) {
        /*
         * nothing to do here, this unbind method is only needed, because DS is throwing an
         * exception when disposing otherwise. probably a bug
         */
    }

    @Override
    public void onPrepare(ComponentInstanceInformation incInstInformation) throws ComponentException {
        super.onPrepare(incInstInformation);
        final ComponentInstanceInformation instInformation = getInstanceInformation();
        // set static replacement values
        final String dash = "-";
        defaultContext.set(CONTEXT_STATIC, "componentSqlId", instInformation.getIdentifier().replace(dash, ""));
        defaultContext.set(CONTEXT_STATIC, "workflowSqlId", instInformation.getComponentContextIdentifier().replace(dash, ""));
        if (hasProperty(SqlComponentConstants.METADATA_TABLE_NAME_PROPERTY)) {
            defaultContext.set(CONTEXT_STATIC, "tableName", getTableName());
        }
        validate();
        logger.debug("SQL component prepared");
    }

    protected abstract void validate();

    protected boolean hasDataInputs() {
        if (hasDataInputs == null) {
            hasDataInputs = hasDataInputs(null);
        }
        return hasDataInputs;
    }

    protected boolean hasDataInputs(final Class<? extends Serializable> type) {
        boolean result = false;
        for (final String inputName : getInputs(type).keySet()) {
            if (!inputName.startsWith(META_OUTPUT_PREFIX)) {
                result = true;
                break;
            }
        }
        return result;
    }

    protected boolean hasFullDataInputs() {
        if (hasFullDataInputs == null) {
            hasFullDataInputs = hasDataInputs(VariantArray.class);
        }
        return hasFullDataInputs;
    }

    protected <T extends Serializable> Map<String, Class<? extends T>> getDataInputs(final Class<T> type) {
        final Map<String, Class<? extends T>> result = new HashMap<String, Class<? extends T>>();
        for (final Map.Entry<String, Class<? extends T>> entry : getInputs(type).entrySet()) {
            final String inputName = entry.getKey();
            if (!inputName.startsWith(META_OUTPUT_PREFIX)) {
                result.put(inputName, entry.getValue());
            }
        }
        return result;
    }

    protected Map<String, Class<? extends VariantArray>> getFullDataInputs() {
        if (fullDataInputs == null) {
            fullDataInputs = new HashMap<String, Class<? extends VariantArray>>();
            fullDataInputs.putAll(getDataInputs(VariantArray.class));
        }
        return fullDataInputs;
    }

    protected boolean hasDataOutputs() {
        if (hasDataOutputs == null) {
            hasDataOutputs = hasDataOutputs(null);
        }
        return hasDataOutputs;
    }

    protected boolean hasDataOutputs(final Class<? extends Serializable> type) {
        boolean result = false;
        for (final String outputName : getOutputs(type).keySet()) {
            if (!outputName.startsWith(META_OUTPUT_PREFIX)) {
                result = true;
                break;
            }
        }
        return result;
    }

    protected boolean hasFullDataOutputs() {
        if (hasFullDataOutputs == null) {
            hasFullDataOutputs = hasDataOutputs(VariantArray.class);
        }
        return hasFullDataOutputs;
    }

    protected <T extends Serializable> Map<String, Class<? extends T>> getDataOutputs(final Class<T> type) {
        final Map<String, Class<? extends T>> result = new HashMap<String, Class<? extends T>>();
        for (final Map.Entry<String, Class<? extends T>> entry : getOutputs(type).entrySet()) {
            final String outputName = entry.getKey();
            if (!outputName.startsWith(META_OUTPUT_PREFIX)) {
                result.put(outputName, entry.getValue());
            }
        }
        return result;
    }

    protected Map<String, Class<? extends VariantArray>> getFullDataOutputs() {
        if (fullDataOutputs == null) {
            fullDataOutputs = new HashMap<String, Class<? extends VariantArray>>();
            fullDataOutputs.putAll(getDataOutputs(VariantArray.class));
        }
        return fullDataOutputs;
    }

    protected Map<String, Class<? extends Serializable>> getDataOutputs() {
        if (dataOutputs == null) {
            dataOutputs = new HashMap<String, Class<? extends Serializable>>();
            for (Map.Entry<String, Class<? extends Serializable>> entry : getOutputs().entrySet()) {
                final String name = entry.getKey();
                if (name.startsWith(META_OUTPUT_PREFIX)) {
                    continue;
                }
                final Class<? extends Serializable> value = entry.getValue();
                if (value != VariantArray.class) {
                    dataOutputs.put(name, value);
                }
            }
        }
        return dataOutputs;
    }

    protected Map<String, Class<? extends Serializable>> getMetaOutputs() {
        if (metaOutputs == null) {
            metaOutputs = new HashMap<String, Class<? extends Serializable>>();
            for (Map.Entry<String, Class<? extends Serializable>> entry : getOutputs().entrySet()) {
                final String name = entry.getKey();
                if (!name.startsWith(META_OUTPUT_PREFIX)) {
                    continue;
                }
                final String variableName = name.substring(META_OUTPUT_PREFIX.length());
                final Class<? extends Serializable> value = entry.getValue();
                if (value != VariantArray.class) {
                    metaOutputs.put(variableName, value);
                }
            }
        }
        return metaOutputs;
    }

    protected JDBCProfile getJdbcProfile() {
        final String profileLabel = getProperty(SqlComponentConstants.METADATA_JDBC_PROFILE_PROPERTY, String.class, "");
        if (profileLabel.trim().isEmpty()) {
            throw new RuntimeException("No JDBC profile label configured.");
        }
        final JDBCProfile profile = jdbcService.getProfileByLabel(profileLabel);
        if (profile == null) {
            throw new RuntimeException(String.format("JDBC profile with the label '%s' does not exist.", profileLabel));
        }
        return profile;
    }

    protected Connection getConnection() throws SQLException {
        if (connection == null) {
            final JDBCProfile profile = getJdbcProfile();
            connection = jdbcService.getConnection(profile);
            if (connection == null) {
                throw new RuntimeException("Connection could not be retrieved.");
            }
        }
        return connection;
    }

    protected void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                logger.error("Database connection could not be closed.", e);
            }
            connection = null;
        }
    }

    @Override
    public boolean runInitialInComponent(boolean inputsConnected) {
        logger.debug("Database component started");
        sqlSetup();
        if (isSqlInitQueryEnabled()) {
            final String sql = getSqlInitQuery();
            runSqlSafe(sql);
        }
        return true;
    }

    @Override
    protected boolean runStepInComponent(Map<String, List<Input>> inputValues) throws ComponentException {
        // get the sql query string (variables are already substituted)
        final String sql = getSqlQuery();
        runSqlSafe(sql);
        return true;
    }

    private void runSqlSafe(final String sql) {
        Integer success = null;
        try {
            runSql(sql);
            success = 1;
        } catch (final RuntimeException e) {
            success = 0;
            logger.warn("Failed to execute SQL statement:", e);
            throw e;
        } finally {
            getContext().set(CONTEXT_RUN, "sqlSuccess", success);
            distributeMetas();
        }
    }
        
    protected abstract void runSql(final String sql);


    protected void sqlSetup() {
        final String sqlStatementsString =
            getProperty(SqlComponentConstants.METADATA_SQL_SETUP_PROPERTY, String.class);
        executeSqlStatements(sqlStatementsString);
    }

    protected void sqlCleanup() {
        final String sqlStatementsString =
            getProperty(SqlComponentConstants.METADATA_SQL_CLEANUP_PROPERTY, String.class);
        executeSqlStatements(sqlStatementsString);
    }

    protected void sqlDispose() {
        final String sqlStatementsString =
            getProperty(SqlComponentConstants.METADATA_SQL_DISPOSE_PROPERTY, String.class);
        executeSqlStatements(sqlStatementsString);
    }

    protected void executeSqlStatements(String sqlStatementsString) {
        if (sqlStatementsString == null || sqlStatementsString.isEmpty()) {
            return;
        }
        sqlStatementsString = replace(sqlStatementsString);
        final String[] sqlStatements = sqlStatementsString.split(";");
        // declare the jdbc assets to be able to close them in the finally-clause
        Connection jdbcConnection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        // exectue the jdbc stuff
        try {
            jdbcConnection = getConnection();
            final boolean autoCommit = jdbcConnection.getAutoCommit();
            jdbcConnection.setAutoCommit(false);
            boolean commited = false;
            try {
                statement = jdbcConnection.createStatement();
                for (final String sql : sqlStatements) {
                    final String sqlClean = sql.trim();
                    if (!sqlClean.isEmpty()) {
                        statement.addBatch(sqlClean);
                    }
                }
                statement.executeBatch();
                jdbcConnection.commit();
                commited = true;
            } finally {
                if (!commited) {
                    jdbcConnection.rollback();
                }
                jdbcConnection.setAutoCommit(autoCommit);
            }
        } catch (SQLException e) {
            logger.error("SQL Exception occured.", e);
            throw new RuntimeException(e);
        } finally {
            try {
                if (resultSet != null) {
                    resultSet.close();
                }
                if (statement != null) {
                    statement.close();
                }
            } catch (SQLException e) {
                logger.error("ResultSet or Statement could not be closed properly.", e);
            }
        }
    }

    protected String getSqlQuery() {
        return getSqlQuery(true);
    }
    
    protected String getSqlQuery(final boolean replace) {
        String result;
        result = getProperty(SqlComponentConstants.METADATA_SQL_PROPERTY, String.class);
        if (result != null && replace) {
            result = replace(result);
        }
        return result;
    }
    
    protected boolean isSqlInitQueryEnabled() {
        final boolean result = getProperty(SqlComponentConstants.METADATA_DO_SQL_INIT_PROPERTY, Boolean.class, false);
        return result;
    }

    protected String getSqlInitQuery() {
        return getSqlInitQuery(true);
    }

    protected String getSqlInitQuery(final boolean replace) {
        String result;
        result = getProperty(SqlComponentConstants.METADATA_SQL_INIT_PROPERTY, String.class);
        if (result != null && replace) {
            result = replace(result);
        }
        return result;
    }

    protected String getTableName() {
        return getTableName(true);
    }

    protected String getTableName(final boolean replace) {
        String result;
        result = getProperty(SqlComponentConstants.METADATA_TABLE_NAME_PROPERTY, String.class);
        if (result != null && replace) {
            result = replace(result);
        }
        return result;
    }

    @Override
    public void onDispose() {
        sqlDispose();
        closeConnection();
        logger.debug("Database component disposed");
    }

    @Override
    public void onCancel() {
        sqlCleanup();
        logger.debug("Database component canceled");
    }

    @Override
    public void onFinish() {
        sqlCleanup();
        closeConnection();
    }

    protected void distributeResults(ResultSet resultSet) throws SQLException {
        final ComponentInstanceInformation instanceInformation = getInstanceInformation();
        if (!hasDataOutputs() && !hasFullDataOutputs()) {
            return;
        }
        final Map<String, Class<? extends Serializable>> metaOuts = getMetaOutputs();
        final ResultSetMetaData metaData = ResultSetMetaData.parse(resultSet);
        /*
         * Convert the WHOLE ResultSet to a list of TypedValue arrays.
         * This needs to be changed if the primary use case is iteration over the ResultSet
         */
        final Iterable<TypedValue[]> rows = ResultValueConverter.convertToTypedValueListIterator(resultSet);
        /*
         * If full data outputs exist, the whole result set has to be stored in a collection.
         */
        final boolean storeFullRowList = hasFullDataOutputs();
        List<TypedValue[]> fullRowList = null;
        if (storeFullRowList) {
            fullRowList = new LinkedList<TypedValue[]>();
        }
        // determine the meta outputs
        if (hasDataOutputs()) {
            /*
             * Iterate over each row and write the values to the output.
             */
            final Map<String, Class<? extends Serializable>> dataOuts = getDataOutputs();
            // determine the relevant columns which are those who are mapped to an output with equal
            // name
            final Map<String, Integer> relevantColumns = new HashMap<String, Integer>();
            for (int columnIndex = 0; columnIndex < metaData.columnCount; ++columnIndex) {
                final String columnName = metaData.columnLabels.get(columnIndex);
                // output with equal name indicates a relevant column
                if (dataOuts.containsKey(columnName)) {
                    relevantColumns.put(columnName, columnIndex);
                }
            }
            // only iterate over the rows if relevant columns exist or meta outputs exist
            if (!relevantColumns.isEmpty() || !metaOuts.isEmpty()) {
                // iterate over the rows
                for (final TypedValue[] row : rows) {
                    // write the value of each relevant column into the output with equal name
                    for (final Map.Entry<String, Integer> relevantColumn : relevantColumns.entrySet()) {
                        final String columnName = relevantColumn.getKey();
                        final int columnIndex = relevantColumn.getValue();
                        final Serializable value = row[columnIndex].getValue();
                        final Output output = instanceInformation.getOutput(columnName);
                        output.write(value);
                    }
                    // store the row in the full row list if necessary
                    if (storeFullRowList) {
                        fullRowList.add(row);
                    }
                }
            }
        } else if (storeFullRowList) {
            // store the row in the full row list if necessary
            // iterate over the rows
            for (final TypedValue[] row : rows) {
                fullRowList.add(row);
            }
        }
        if (hasFullDataOutputs()) {
            final VariantArray value = ResultValueConverter.convertToVariantArray("VariantArray", fullRowList, metaData.columnCount);
            for (final Map.Entry<String, Class<? extends VariantArray>> output : getFullDataOutputs().entrySet()) {
                final String outputName = output.getKey();
                instanceInformation.getOutput(outputName).write(value);
            }
        }
    }

    protected void distributeMetas() {
        final Map<String, Class<? extends Serializable>> metaOuts = getMetaOutputs();
        for (Map.Entry<String, Class<? extends Serializable>> entry : metaOuts.entrySet()) {
            final String variableName = entry.getKey();
            final Serializable variableValue;
            variableValue = getVariableValue(variableName, Serializable.class);
            final Output output = getOutput(META_OUTPUT_PREFIX + variableName);
            output.write(variableValue);
        }
    }

}
