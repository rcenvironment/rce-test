/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.datamanagement.backend.catalog.derby.internal;

import static de.rcenvironment.rce.datamanagement.commons.CatalogConstants.ALL_METADATA;
import static de.rcenvironment.rce.datamanagement.commons.CatalogConstants.DATAREFERENCES;
import static de.rcenvironment.rce.datamanagement.commons.CatalogConstants.DATAREFERENCE_GUID;
import static de.rcenvironment.rce.datamanagement.commons.CatalogConstants.GUID;
import static de.rcenvironment.rce.datamanagement.commons.CatalogConstants.HEAD_REVISION;
import static de.rcenvironment.rce.datamanagement.commons.CatalogConstants.KEY;
import static de.rcenvironment.rce.datamanagement.commons.CatalogConstants.LOCATION;
import static de.rcenvironment.rce.datamanagement.commons.CatalogConstants.METADATA;
import static de.rcenvironment.rce.datamanagement.commons.CatalogConstants.NUMBER;
import static de.rcenvironment.rce.datamanagement.commons.CatalogConstants.PARENT_EXT_HOST;
import static de.rcenvironment.rce.datamanagement.commons.CatalogConstants.PARENT_EXT_INSTANCE_ID;
import static de.rcenvironment.rce.datamanagement.commons.CatalogConstants.PARENT_GUID;
import static de.rcenvironment.rce.datamanagement.commons.CatalogConstants.PARENT_REVISION;
import static de.rcenvironment.rce.datamanagement.commons.CatalogConstants.READ_ONLY;
import static de.rcenvironment.rce.datamanagement.commons.CatalogConstants.REFERENCE_METADATA;
import static de.rcenvironment.rce.datamanagement.commons.CatalogConstants.REVISIONS;
import static de.rcenvironment.rce.datamanagement.commons.CatalogConstants.REVISION_NUMBER;
import static de.rcenvironment.rce.datamanagement.commons.CatalogConstants.TYPE;
import static de.rcenvironment.rce.datamanagement.commons.CatalogConstants.VALUE;

import java.io.File;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLNonTransientConnectionException;
import java.sql.SQLTransientConnectionException;
import java.sql.Statement;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.dbcp.datasources.SharedPoolDataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.derby.jdbc.EmbeddedConnectionPoolDataSource;
import org.osgi.framework.BundleContext;

import de.rcenvironment.commons.Assertions;
import de.rcenvironment.rce.authentication.User;
import de.rcenvironment.rce.communication.PlatformIdentifier;
import de.rcenvironment.rce.communication.PlatformIdentifierFactory;
import de.rcenvironment.rce.communication.PlatformService;
import de.rcenvironment.rce.configuration.ConfigurationService;
import de.rcenvironment.rce.datamanagement.backend.CatalogBackend;
import de.rcenvironment.rce.datamanagement.commons.CatalogConstants;
import de.rcenvironment.rce.datamanagement.commons.DMQLQuery;
import de.rcenvironment.rce.datamanagement.commons.DataReference;
import de.rcenvironment.rce.datamanagement.commons.DataReference.DataReferenceType;
import de.rcenvironment.rce.datamanagement.commons.MetaData;
import de.rcenvironment.rce.datamanagement.commons.MetaDataResult;
import de.rcenvironment.rce.datamanagement.commons.MetaDataResultList;
import de.rcenvironment.rce.datamanagement.commons.MetaDataSet;
import de.rcenvironment.rce.datamanagement.commons.ParentRevision;
import de.rcenvironment.rce.datamanagement.commons.Query;
import de.rcenvironment.rce.datamanagement.commons.Revision;

/**
 * Derby implementation of {@link CatalogBackend}.
 * 
 * @author Dirk Rossow
 * @author Juergen Klein
 */
public class DerbyCatalogBackend implements CatalogBackend {

    private static final int TIME_TO_WAIT_FOR_RETRY = 10000;

    private static final String INITIALIZATION_TIMEOUT_ERROR_MESSAGE = "Initialization not completed in a timely fashion.";

    private static final int INITIALIZATION_TIMEOUT = 10;

    private static final String DOT = ".";

    private static final int MAX_RETRIES = 3;
    
    private static final String PLEASE_PROVIDE_A_VALID_PROXY_CERTIFICATE = "Please provide a valid ProxyCertificate.";

    private static final Log LOGGER = LogFactory.getLog(DerbyCatalogBackend.class);

    private static final String WHERE = " WHERE ";

    private static final String EQUAL = " = ";

    private static final String APO = "'";

    private static final String COMMA = " , ";

    private static final String US = "_";

    private static final String ON = " ON ";

    private static final String AND = " AND ";

    private static final String SELECT_STAR_FROM = " SELECT * FROM ";

    private static final String SELECT = " SELECT ";

    private static final String FROM = " FROM ";

    private static final String DELETE_FROM = " DELETE FROM ";

    private static final String CREATE_TABLE = " CREATE TABLE ";

    private static final String CREATE_VIEW = " CREATE VIEW ";

    private static final String AS = " AS ";

    private static final String CREATE_INDEX = " CREATE INDEX ";

    private static final String PRIMARY_KEY = "PRIMARY KEY";

    private static final String FOREIGN_KEY = " FOREIGN KEY ";

    private static final String REFERENCES = " REFERENCES ";

    private static final String CHAR_5_NOT_NULL = " CHAR(5) NOT NULL ";
    
    private static final String CHAR_36_NOT_NULL = " CHAR(36) NOT NULL ";

    private static final String ON_DELETE_CASCADE = " ON DELETE CASCADE ";

    private static final String INSERT_INTO = " INSERT INTO ";

    private static final String VARCHAR = " VARCHAR";
    
    private static final String NOT_NULL = "NOT NULL";

    private static final String TYPE_VARCHAR_MAX_NOT_NULL = VARCHAR + "(" + MetaData.MAX_VALUE_LENGTH + ")" + " " + NOT_NULL;

    private static final int MAX_QUERY_FETCH_SIZE = 1000;

    private static final String CATALOG_DB_NAME = "catalog";
    
    private final CountDownLatch initializationLatch = new CountDownLatch(1);
    
    private SharedPoolDataSource connectionPool;

    private EmbeddedConnectionPoolDataSource connectionPoolDatasource;

    private ConcurrentMap<UUID, User> lockedDataReferences;

    private DerbyCatalogBackendConfiguration configuration;

    private ConfigurationService configService;

    private PlatformService platformService;
    
    private final ThreadLocal<PooledConnection> connections = new ThreadLocal<PooledConnection>();

    protected void activate(BundleContext context) {
        System.setProperty("derby.stream.error.file",
                           configService.getConfigurationArea() + File.separator + "derby.log");
        System.setProperty("derby.locks.waitTimeout", "90");
        System.setProperty("derby.locks.deadlockTimeout", "60");

        configuration = configService.getConfiguration(context.getBundle().getSymbolicName(), DerbyCatalogBackendConfiguration.class);
        if (configuration.getDatabaseURL().equals("")) {
            configuration.setDatabaseUrl(configService.getPlatformHome() + File.separator + CATALOG_DB_NAME);
        }
        lockedDataReferences = new ConcurrentHashMap<UUID, User>();
        new Thread() {
            @Override
            public void run() {
                initialize();
            };
        }.run(); // FIXME start() fails with NullPointerExceptions
    }
    
    protected void deactivate() {
        shutDown();
    }
    
    protected void bindConfigurationService(ConfigurationService newConfigurationService) {
        configService = newConfigurationService;
    }
    
    protected void bindPlatformService(PlatformService newPlatformServiceService) {
        platformService = newPlatformServiceService;
    }
    
    protected Connection getConnection() {
        PooledConnection result = connections.get();
        try {
            if (result != null && result.isClosed()) {
                result = null;
            }
        } catch (SQLException e) {
            result = null;
        }
        if (result == null) {
            try {
                final Connection connection = connectionPool.getConnection();
                final PooledConnectionInvocationHandler handler = new PooledConnectionInvocationHandler(connection);
                final PooledConnection pooledConnection = (PooledConnection) Proxy.newProxyInstance(getClass().getClassLoader(),
                        new Class<?>[] { PooledConnection.class }, handler);
                connections.set(pooledConnection);
                result = pooledConnection;
            } catch (SQLException e) {
                throw new RuntimeException("Failed to retrieve connection from connection pool:", e);
            }
        }
        result.increment();
        return result;
    }
    
    /**
     * 
     * A pooled Connection.
     *
     * @author Christian Weiss
     */
    private interface PooledConnection extends Connection {
        
        void increment();
        
        void decrement();
        
    }
    
    /**
     * InvocationHandler for pooled connections.
     *
     * @author Christian Weiss
     */
    private final class PooledConnectionInvocationHandler implements InvocationHandler {

        private final Connection connection;
        
        private int count = 0;
        
        private PooledConnectionInvocationHandler(final Connection connection) {
            this.connection = connection;
        }
        
        @Override
        public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
            boolean execute = true;
            if (method.getName().equals("increment")) {
                count++;
                execute = false;
            } else if (method.getName().equals("decrement")) {
                count--;
                execute = false;
            } else if (method.getName().equals("close")) {
                count--;
                if (count > 0) {
                    execute = false;
                }
            }
            if (count == 0) {
                connection.close();
            }
            if (execute) {
                // not sure, if this is semantically duplicate code, but I want to double check for
                // now if retrying is done, because exception was thrown in Chameleon
                int attempts = 0;
                Object result = null;
                do {
                    try {
                        result = method.invoke(connection, args);
                        break;
                    } catch (InvocationTargetException e) {
                        // from spec:
                        // "The subclass of SQLException for the SQLState class value '08',
                        // representing that the connection operation that failed might be able to succeed when the
                        // operation is retried without any application-level changes."
                        if (e.getCause() instanceof SQLNonTransientConnectionException) {
                            if (attempts <= MAX_RETRIES) {
                                attempts++;
                                waitForRetry();
                            } else {
                                throw e;
                            }
                        } else {
                            throw e;
                        }
                    }
                } while (attempts <= MAX_RETRIES);
                return result;
            } else {
                return null;
            }
        }

    }
    
    /**
     * Template for safe executions.
     *
     * @author Christian Weiss
     */
    protected abstract class SafeExecution<T> implements Callable<T> {
        
        @Override
        public final T call() {
            T result = null;
            final Connection connection = getConnection();
            try {
                connection.setAutoCommit(false);
                initializationLatch.await(INITIALIZATION_TIMEOUT, TimeUnit.SECONDS);
                int count = 0;
                do {
                    try {
                        result = protectedCall(connection, false);
                        break;
                    } catch (SQLTransientConnectionException e) {
                        // from spec:
                        // "The subclass of SQLException for the SQLState class value '08', representing that
                        // the connection operation that failed might be able to succeed when the operation
                        // is retried without any application-level changes."
                        waitForRetry();
                    }
                    count++;
                } while(count <= MAX_RETRIES);
                connection.commit();
            } catch (InterruptedException e) {
                LOGGER.error(INITIALIZATION_TIMEOUT_ERROR_MESSAGE, e);
                throw new RuntimeException(INITIALIZATION_TIMEOUT_ERROR_MESSAGE, e);
            } catch (SQLException e) {
                try {
                    connection.rollback();
                } catch (SQLException e1) {
                    e1 = null;
                }
                final String message = "Failed to safely execute:";
                LOGGER.error(message, e);
                throw new RuntimeException(message, e);
            } finally {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e = null;
                } catch (RuntimeException e) {
                    e = null;
                }
            }
            return result;
        }
        
        protected abstract T protectedCall(Connection connection, boolean isRetry) throws SQLException;
        
    }

    @Override
    public Collection<DataReference> executeQuery(final Query query, final int maxResults) {
        final SafeExecution<Collection<DataReference>> execution = new SafeExecution<Collection<DataReference>>() {

            @Override
            protected Collection<DataReference> protectedCall(final Connection connection, final boolean isRetry) throws SQLException {
                return executeQuery(query, maxResults, connection, isRetry);
            }

        };
        return execution.call();
    }

    private Collection<DataReference> executeQuery(final Query query, final int maxResults, final Connection connection,
            final boolean retried) throws SQLException {

        Set<DataReference> dataReferences = new HashSet<DataReference>();
        final Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        stmt.setFetchSize(MAX_QUERY_FETCH_SIZE);
        String sqlQueryWherePart = query.getQuery();
        String sqlQuery = "SELECT d.guid, d.type, d.parent_guid, d.parent_revision, d.parent_ext_host, d.parent_ext_instance_id, " //
                + "r.number, r.location " //
                + "FROM DATAREFERENCES AS d LEFT OUTER JOIN REVISIONS AS r ON d.GUID = r.DATAREFERENCE_GUID";
        if (sqlQueryWherePart.length() != 0) {
            sqlQuery += WHERE + sqlQueryWherePart;
        }
        sqlQuery += " ORDER BY d.GUID, r.number";
        // FIXME - pagination!
        stmt.setMaxRows(maxResults);
        final ResultSet resultSet = stmt.executeQuery(sqlQuery);
        
        String lastGuidValue = null;
        DataReference lastDataReference = null;
        while (resultSet.next()) {
            final String guidValue = resultSet.getString(1);
            // check, if the current row is a new DataReference or a revision for the previous one
            if (!guidValue.equals(lastGuidValue)) {
                lastGuidValue = guidValue;
                // extract the data for the new DataReference
                final String typeValue = resultSet.getString(2);
                final String parentGuidValue = resultSet.getString(3);
                final int parentRevisionValue = resultSet.getInt(4);
                final String parentExtHostValue = resultSet.getString(5);
                // parse the data for the new DataReference
                final DataReferenceType type = DataReference.DataReferenceType.valueOf(typeValue);
                final UUID guid = UUID.fromString(guidValue);
                final DataReference dataReference;
                // create the DataReference either with a parent set or without
                if (parentGuidValue != null) {
                    final UUID parentGuid = UUID.fromString(parentGuidValue);
                    PlatformIdentifier parentPlatformIdentifier = PlatformIdentifierFactory.fromNodeId(parentExtHostValue);
                    ParentRevision parentRevision = new ParentRevision(parentGuid, parentPlatformIdentifier, parentRevisionValue);
                    dataReference = new DataReference(type, guid, platformService.getPlatformIdentifier(), parentRevision);
                } else {
                    dataReference = new DataReference(type, guid, platformService.getPlatformIdentifier());
                }
                // add the DataReference to the result set
                dataReferences.add(dataReference);
                // use the newly created DataReference as last DataReference
                lastDataReference = dataReference;
            }
            // add revision information to the last DataReference (might be the one in the current row)
            final int revisionNumber = resultSet.getInt(7);
            final String locationValue = resultSet.getString(8);
            if (revisionNumber != 0 && locationValue != null) {
                final URI location = URI.create(locationValue);
                lastDataReference.addRevision(revisionNumber, location);
            }
        }
        resultSet.close();
        stmt.close();
        return dataReferences;
    }

    @Override
    public MetaDataResultList executeMetaDataQuery(final Query query, final Integer maxResults) {
        final SafeExecution<MetaDataResultList> execution = new SafeExecution<MetaDataResultList>() {

            @Override
            protected MetaDataResultList protectedCall(final Connection connection, final boolean isRetry) throws SQLException {
                if (query instanceof DMQLQuery) {
                    return executeMetaDataQuery((DMQLQuery) query, maxResults, connection, isRetry);
                } else {
                    return executeMetaDataQuery(query, maxResults, connection, isRetry);
                }
            }
            
        };
        return execution.call();
    }

    private MetaDataResultList executeMetaDataQuery(final Query query, final Integer maxResults, final Connection connection,
            final boolean retried) throws SQLException {
        final MetaDataResultList result = new MetaDataResultList();
        final Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        stmt.setFetchSize(MAX_QUERY_FETCH_SIZE);
        String sqlQueryWherePart = query.getQuery();
        String sqlQuery = String.format(SELECT + DATAREFERENCE_GUID + COMMA + KEY + COMMA + VALUE + COMMA + READ_ONLY + COMMA
                + REVISION_NUMBER + " = 0 AS revision_independent " //
                + FROM + " %s ", //
                                        CatalogConstants.ALL_METADATA);
        if (sqlQueryWherePart.length() != 0) {
            sqlQuery += WHERE + DATAREFERENCE_GUID + " IN (" + sqlQueryWherePart + ")";
        }
        sqlQuery += " ORDER BY " + DATAREFERENCE_GUID + COMMA + REVISION_NUMBER + " ASC";
        // FIXME - pagination!
        stmt.setMaxRows(maxResults);
        final ResultSet resultSet = stmt.executeQuery(sqlQuery);

        String lastGuidValue = null;
        MetaDataSet currentMetaDataSet = null;
        while (resultSet.next()) {
            final String guidValue = resultSet.getString(1);
            // check, if the current row is a new DataReference or a revision for the previous
            // one
            if (!guidValue.equals(lastGuidValue)) {
                lastGuidValue = guidValue;
                // extract the data for the new DataReference
                // parse the data for the new DataReference
                final UUID guid = UUID.fromString(guidValue);
                // create a new MetaDataSet to store the metadata
                currentMetaDataSet = new MetaDataSet();
                // add the metadata information to the result set
                result.add(new MetaDataResult(guid, currentMetaDataSet));
            }
            // add metadata information to current MetaDataSet
            final String keyValue = resultSet.getString(2);
            final String valueValue = resultSet.getString(3);
            final boolean readOnlyValue = resultSet.getBoolean(4);
            final boolean revisionIndependentValue = resultSet.getBoolean(5);
            currentMetaDataSet.setValue(new MetaData(keyValue, revisionIndependentValue, readOnlyValue), valueValue);
        }
        resultSet.close();
        stmt.close();
        return result;
    }

    protected MetaDataResultList executeMetaDataQuery(final DMQLQuery query, final Integer maxResults,
            final Connection connection, final boolean retried) throws SQLException {
        final MetaDataResultList result = new MetaDataResultList();
        final DMQLQueryConverter converter = new DMQLQueryConverter(query);
        final String sqlQuery = converter.getSqlQuery();
        final List<String> metaDataKeys = converter.getPropertyKeys();
        final MetaData[] metaDatas = new MetaData[metaDataKeys.size()];
        for (int index = 0; index < metaDataKeys.size(); ++index) {
            final String metaDataKey = metaDataKeys.get(index);
            metaDatas[index] = new MetaData(metaDataKey, true, true);
        }
        Statement stmt = null;
        ResultSet resultSet = null;
        try {
            stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            stmt.setFetchSize(MAX_QUERY_FETCH_SIZE);
            // FIXME - pagination!
            stmt.setMaxRows(maxResults);
            resultSet = stmt.executeQuery(sqlQuery);
            
            final int columnCount = resultSet.getMetaData().getColumnCount();
            if (columnCount != metaDataKeys.size()) {
                throw new RuntimeException();
            }
            
            /*
             * Every row is a MetaDataSet.
             */
            
            while (resultSet.next()) {
                // create a new MetaDataSet to store the metadata
                final MetaDataSet metaDataSet = new MetaDataSet();
                for (int column = 1; column <= columnCount; ++column) {
                    final MetaData metaData = metaDatas[column - 1];
                    final String value = resultSet.getString(column);
                    metaDataSet.setValue(metaData, value);
                }
                final MetaDataResult metaDataResult = new MetaDataResult(null, metaDataSet);
                result.add(metaDataResult);
            }
        } finally {
            try {
                if (resultSet != null) {
                    resultSet.close();
                }
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException e) {
                e = null; // ignore
            }
        }
        return result;
    }

    @Override
    public void storeReference(final DataReference dataReference) {
        final SafeExecution<Void> execution = new SafeExecution<Void>() {

            @Override
            protected Void protectedCall(final Connection connection, final boolean isRetry) throws SQLException {
                storeReference(dataReference, connection, isRetry);
                return null;
            }
            
        };
        execution.call();
    }
    
    private void storeReference(final DataReference dataReference, final Connection connection, final boolean retried) throws SQLException {
        
        ParentRevision parentRevision = dataReference.getParentRevision();
        String parentId = null;
        int parentRevNr = DataReference.HEAD_REVISION;
        String parentHost = null;
        int parentPlatformNumber = 0;
        if (parentRevision != null) {
            parentId = parentRevision.getIdentifier().toString();
            parentRevNr = parentRevision.getRevisionNumber();
            parentHost = parentRevision.getPlatformIdentifier().getNodeId();
        }

        PreparedStatement insertRefStmt = connection.prepareStatement(INSERT_INTO + DATAREFERENCES + "("
            + GUID + ", " + TYPE + ", " + PARENT_GUID + ", " + PARENT_REVISION + ", " + PARENT_EXT_HOST + ", "
            + PARENT_EXT_INSTANCE_ID + " ) VALUES ( ?, ?, ?, ?, ?, ? )");

        insertRefStmt.setString(1, dataReference.getIdentifier().toString());
        insertRefStmt.setString(2, dataReference.getDataType().toString());
        insertRefStmt.setString(3, parentId);
        insertRefStmt.setInt(4, parentRevNr);
        insertRefStmt.setString(5, parentHost);
        insertRefStmt.setInt(6, parentPlatformNumber);

        insertRefStmt.executeUpdate();
        insertRefStmt.close();
        updateRevisions(dataReference, connection, retried);
    }

    @Override
    public boolean deleteReference(final UUID dataReferenceId) {
        final SafeExecution<Boolean> execution = new SafeExecution<Boolean>() {

            @Override
            protected Boolean protectedCall(final Connection connection, final boolean isRetry) throws SQLException {
                return deleteReference(dataReferenceId, connection, isRetry);
            }
            
        };
        return execution.call();
    }
    
    private boolean deleteReference(final UUID dataReferenceId, final Connection connection, final boolean retried) throws SQLException {
        boolean deleted = false;
        String guidString = dataReferenceId.toString();
        // try to delete metadata first to avoid inconsistencies
        if (!deleteMetadata(dataReferenceId, connection, false) // delete metadata
                || !deleteRevisions(dataReferenceId, connection, false)) { // delete revisions
            return false;
        }
        String deleteSql = String.format("DELETE  FROM %s WHERE %s = ?", //
                                         DATAREFERENCES, // TABLE
                                                 GUID // ID COLUMN
        );
        PreparedStatement deleteRefStmt = connection.prepareStatement(deleteSql);
        deleteRefStmt.setString(1, guidString);
        int affectedRows = deleteRefStmt.executeUpdate();
        if (affectedRows > 0) {
            deleted = true;
        }
        deleteRefStmt.close();
        return deleted;
    }

    // FIXME: Transaction required ... but only one connection!
    private boolean deleteMetadata(final UUID dataReferenceId, final Connection connection, final boolean retried) {
        String guidString = dataReferenceId.toString();
        PreparedStatement preparedStatement;
        String sql;
        try {
            final String deletePattern = "DELETE FROM %s WHERE %s = ?";
            // prepare revision independent metadata delete statement
            // delete revision independent metadata
            sql = String.format(deletePattern, //
                                ALL_METADATA, // TABLE
                                DATAREFERENCE_GUID // ID COLUMN
            );
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, guidString);
            preparedStatement.close();
        } catch (SQLException e) {
            if (e instanceof SQLTransientConnectionException) {
                waitForRetry();
                return deleteMetadata(dataReferenceId, connection, true);
            } else {
                LOGGER.error("Failed to delete metadata from database", e);
                return false;
            }
        }
        return true;
    }

    // FIXME: Transaction required ... but only one connection!
    private boolean deleteRevisions(final UUID dataReferenceId, final Connection connection, final boolean retried) {
        String guidString = dataReferenceId.toString();
        try {
            String deleteRevisionsSql = String.format("DELETE FROM %s WHERE %s = ?", //
                                                      REVISIONS, // TABLE
                                                      DATAREFERENCE_GUID // ID COLUMN
            );
            final PreparedStatement deleteRevisionsStatement = connection.prepareStatement(deleteRevisionsSql);
            deleteRevisionsStatement.setString(1, guidString);
            deleteRevisionsStatement.executeUpdate();
            deleteRevisionsStatement.close();
        } catch (SQLException e) {
            if (e instanceof SQLTransientConnectionException && !retried) {
                waitForRetry();
                return deleteRevisions(dataReferenceId, connection, true);
            } else {
                LOGGER.error("Failed to delete revisions from database:", e);
                return false;
            }
        }
        return true;
    }

    @Override
    public DataReference getReference(final UUID dataReferenceId) {
        final SafeExecution<DataReference> execution = new SafeExecution<DataReference>() {

            @Override
            protected DataReference protectedCall(final Connection connection, final boolean isRetry) throws SQLException {
                return getReference(dataReferenceId, connection, false);
            }
            
        };
        return execution.call();
    }
    
    private DataReference getReference(final UUID dataReferenceId, final Connection connection, final boolean retried) throws SQLException {
        DataReference dr = null;
        Statement getRefByGUIDStmt = connection.createStatement();
        ResultSet rs = getRefByGUIDStmt.executeQuery(SELECT_STAR_FROM + DATAREFERENCES + WHERE + GUID + EQUAL + APO
                                                     + dataReferenceId.toString() + APO);
        if (rs.next()) {
            dr = restoreReferenceFromResultSet(rs, connection, false);
        }
        rs.close();
        getRefByGUIDStmt.close();
        if (dr != null) {
            loadRevisions(dr, connection, false);
        }
        return dr;
    }
    
    @Override
    public void updateRevisions(final DataReference dataReference) {
        final SafeExecution<Void> execution = new SafeExecution<Void>() {

            @Override
            protected Void protectedCall(final Connection connection, final boolean isRetry) throws SQLException {
                updateRevisions(dataReference, connection, isRetry);
                return null;
            }
            
        };
        execution.call();
    }

    private void updateRevisions(final DataReference dataReference, final Connection connection,
            final boolean retried) throws SQLException {
        final Set<Integer> revisionsToStoreInDB = new HashSet<Integer>();
        for (final int revisionNumber : dataReference.getRevisionNumbers()) {
            revisionsToStoreInDB.add(revisionNumber);
        }
        int headRevision = dataReference.getHighestRevisionNumber();

        String drId = dataReference.getIdentifier().toString();
        Statement getAllReferenceRevisionsStmt = null;
        getAllReferenceRevisionsStmt = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
        ResultSet dbRevisions = getAllReferenceRevisionsStmt.executeQuery(SELECT_STAR_FROM + REVISIONS + WHERE + DATAREFERENCE_GUID
            + EQUAL + APO + drId + APO);
        boolean hasRevisionIndependantRevision = false;
        // remove revisions already contained in db from revisionMap
        // and delete revions from db which are not contained in the map
        while (dbRevisions.next()) {
            int revisionNumber = dbRevisions.getInt(NUMBER);
            revisionsToStoreInDB.remove(new Integer(revisionNumber));
            if (revisionNumber == DataReference.REVISION_INDEPENDENT_REVISION) {
                hasRevisionIndependantRevision = true;
                continue;
            }
            // delete revisions stored in the database but not in the DataReference
            if (!revisionsToStoreInDB.contains(revisionNumber)) {
                dataReference.removeRevision(revisionNumber);
                dbRevisions.deleteRow();
            }
        }
        final Statement statement = connection.createStatement();
        // add revision independant revision if not exists
        if (!hasRevisionIndependantRevision) {
            final String insertRevisionIdependantSql = INSERT_INTO + REVISIONS + " VALUES " 
                    + "(" + APO + drId + APO + ", " + DataReference.REVISION_INDEPENDENT_REVISION + ", '')";
            statement.execute(insertRevisionIdependantSql);
        }
        // add remaining revisions to db
        if (!revisionsToStoreInDB.isEmpty()) {
            dbRevisions.moveToInsertRow(); // moves cursor to the insert row
            for (final Integer revisionNumber : revisionsToStoreInDB) {
                final Revision revision = dataReference.getRevision(revisionNumber);
                dbRevisions.updateString(DATAREFERENCE_GUID, drId);
                dbRevisions.updateInt(NUMBER, revision.getRevisionNumber());
                dbRevisions.updateString(LOCATION, revision.getLocation().toString());
                dbRevisions.insertRow();
            }
        }
        dbRevisions.close();
        getAllReferenceRevisionsStmt.close();
        if (headRevision != DataReference.REVISION_INDEPENDENT_REVISION) {
            String sql;
            sql = DELETE_FROM + HEAD_REVISION + WHERE + DATAREFERENCE_GUID + EQUAL + APO + drId + APO;
            statement.execute(sql);
            sql = INSERT_INTO + HEAD_REVISION + " VALUES (" + APO + drId + APO + COMMA + headRevision + ")";
            statement.execute(sql);
        }
        statement.close();
    }
    
    @Override
    public MetaDataSet getMetaDataSet(final UUID dataReferenceId, final int revisionNr) {
        final SafeExecution<MetaDataSet> execution = new SafeExecution<MetaDataSet>() {

            @Override
            protected MetaDataSet protectedCall(final Connection connection, final boolean isRetry) throws SQLException {
                return getMetaDataSet(dataReferenceId, revisionNr, connection, isRetry);
            }
            
        };
        return execution.call();
    }

    private MetaDataSet getMetaDataSet(final UUID dataReferenceId, final int revisionNr, final Connection connection,
            final boolean retried) throws SQLException {
        final String dataReferenceIdString = dataReferenceId.toString();
        final String revisionNumberSql;
        if (revisionNr >= 0) {
            revisionNumberSql = Integer.toString(revisionNr);
        } else {
            revisionNumberSql = String.format("(SELECT number FROM REVISIONS WHERE DATAREFERENCE_GUID = 's')", dataReferenceIdString);
        }
        String sql = String.format("SELECT KEY_, VALUE, READ_ONLY, 1 AS REVISION_DEPENDENT FROM REFERENCE_METADATA " //
                + "WHERE DATAREFERENCE_GUID = '%s'" //
                + " UNION " //
                + "SELECT KEY_, VALUE, READ_ONLY, 0 AS REVISION_DEPENDENT FROM METADATA AS m " //
                + "WHERE DATAREFERENCE_GUID = '%s' " //
                + "AND m.REVISION_NUMBER = %s", //
                                   dataReferenceIdString, dataReferenceIdString, revisionNumberSql);
        MetaDataSet metaDataSet = null;
        metaDataSet = new MetaDataSet();
        Statement stmt = connection.createStatement();
        stmt.setFetchSize(MAX_QUERY_FETCH_SIZE);
        ResultSet rs = stmt.executeQuery(sql);
        while (rs.next()) {
            String key = rs.getString(1);
            String value = rs.getString(2);
            Boolean isReadOnly = rs.getBoolean(3);
            Boolean isRevisionDependent = rs.getBoolean(4);
            metaDataSet.setValue(new MetaData(key, isRevisionDependent, isReadOnly), value);
        }
        rs.close();
        stmt.close();

        if (metaDataSet.isEmpty()) {
            LOGGER.error("Meta data of required revision does not exist in data management catalog db: " + dataReferenceId + US
                    + revisionNr);
            metaDataSet = null;
        }
        return metaDataSet;
    }

    @Override
    public void storeMetaDataSet(final UUID dataReferenceId, final int revisionNr, final MetaDataSet metaDataSet,
            final boolean includeRevisionIndependent) {
        final SafeExecution<MetaDataSet> execution = new SafeExecution<MetaDataSet>() {

            @Override
            protected MetaDataSet protectedCall(final Connection connection, final boolean isRetry) throws SQLException {
                storeMetaDataSet(dataReferenceId, revisionNr, metaDataSet, includeRevisionIndependent, connection, isRetry);
                return null;
            }
            
        };
        execution.call();
    }
    
    private void storeMetaDataSet(final UUID dataReferenceId, final int revisionNr, final MetaDataSet metaDataSet,
            final boolean includeRevisionIndependent, final Connection connection, final boolean retried) throws SQLException {
        String guidString = dataReferenceId.toString();
        // delete existing revision dependent meta data
        String sql;
        int revisionNr2 = revisionNr;
        if (revisionNr == DataReference.HEAD_REVISION) {
            final DataReference reference = getReference(dataReferenceId);
            revisionNr2 = reference.getHighestRevisionNumber();
        }
        boolean includeRevisionDependent = revisionNr2 != DataReference.REVISION_INDEPENDENT_REVISION;
        Statement deleteMetaDataStmt = connection.createStatement();
        if (includeRevisionDependent) {
            sql = DELETE_FROM + ALL_METADATA + WHERE //
                    + DATAREFERENCE_GUID + EQUAL + APO + guidString + APO + //
                    AND + REVISION_NUMBER + EQUAL + revisionNr2;
            deleteMetaDataStmt.executeUpdate(sql);
        }
        if (includeRevisionIndependent) {
            sql = DELETE_FROM + ALL_METADATA + WHERE //
                    + DATAREFERENCE_GUID + EQUAL + APO + guidString + APO //
                    + AND + REVISION_NUMBER + EQUAL + DataReference.REVISION_INDEPENDENT_REVISION;
            deleteMetaDataStmt.executeUpdate(sql);
        }
        deleteMetaDataStmt.close();

        final String insertSql = INSERT_INTO + " " + ALL_METADATA + "("
                + DATAREFERENCE_GUID + COMMA + REVISION_NUMBER + COMMA + KEY + COMMA + VALUE + COMMA + READ_ONLY
                + ") VALUES (?,?,?,?,?)";
        PreparedStatement insertAllMetaDataStmt = connection.prepareStatement(insertSql);

        for (Iterator<MetaData> iterator = metaDataSet.iterator(); iterator.hasNext();) {
            MetaData metaData = iterator.next();
            String key = metaData.getKey();
            String value = metaDataSet.getValue(metaData);
            insertAllMetaDataStmt.setString(1, guidString);
            if (!metaData.isRevisionIndependent()) {
                if (!includeRevisionDependent) {
                    continue;
                }
                insertAllMetaDataStmt.setInt(2, revisionNr2);
            } else {
                insertAllMetaDataStmt.setInt(2, DataReference.REVISION_INDEPENDENT_REVISION);
            }
            insertAllMetaDataStmt.setString(3, key);
            insertAllMetaDataStmt.setString(4, value);
            insertAllMetaDataStmt.setString(5, String.valueOf(metaData.isReadOnly()));
            insertAllMetaDataStmt.executeUpdate();
        }
        insertAllMetaDataStmt.close();
    }

    @Override
    public boolean isLockedDataReference(UUID dataReferenceId) {
        try {
            initializationLatch.await(INITIALIZATION_TIMEOUT, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            LOGGER.error(INITIALIZATION_TIMEOUT_ERROR_MESSAGE, e);
        }
        if (lockedDataReferences.containsKey(dataReferenceId)) {
            return true;
        }
        return false;
    }

    @Override
    public void lockDataReference(UUID dataReferenceId, User proxyCertificate) {
        try {
            initializationLatch.await(INITIALIZATION_TIMEOUT, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            LOGGER.error(INITIALIZATION_TIMEOUT_ERROR_MESSAGE, e);
        }
        Assertions.isDefined(proxyCertificate, PLEASE_PROVIDE_A_VALID_PROXY_CERTIFICATE);
        lockedDataReferences.put(dataReferenceId, proxyCertificate);
    }

    @Override
    public boolean releaseLockedDataReference(UUID dataReferenceId, User proxyCertificate) {
        try {
            initializationLatch.await(INITIALIZATION_TIMEOUT, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            LOGGER.error(INITIALIZATION_TIMEOUT_ERROR_MESSAGE, e);
        }
        Assertions.isDefined(proxyCertificate, PLEASE_PROVIDE_A_VALID_PROXY_CERTIFICATE);
        boolean released = false;
        if (lockedDataReferences.containsKey(dataReferenceId)) {
            User certificate = lockedDataReferences.get(dataReferenceId);
            if (certificate != null && certificate.equals(proxyCertificate)) {
                lockedDataReferences.remove(dataReferenceId);
                released = true;
            } else {
                throw new IllegalArgumentException("User not allowed to release data reference: "
                                                   + proxyCertificate + ".." + dataReferenceId);
            }
        }
        return released;
    }
    
    private synchronized void initialize() {

        createConnectionPool();
        try {
            final Connection connection = connectionPool.getConnection();
            connection.close();
        } catch (SQLException e) {
            throw new IllegalStateException("Connecting to data management catalog db failed.", e);
        }
        initializeDatabase();
        initializationLatch.countDown();
    }

    private void initializeDatabase() {
        Connection connection = null;
        try {
            connection = connectionPool.getConnection();
            setupOrMigrateDatabase(connection);
            connection.commit();
        } catch (SQLException e) {
            LOGGER.error("Failed to initialize database:", e);
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException e1) {
                    e1 = null;
                }
            }
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    LOGGER.error("Failed to close connection:", e);
                }
            }
        }
    }
    
    private void createConnectionPool() {
        connectionPoolDatasource = new EmbeddedConnectionPoolDataSource();
        connectionPoolDatasource.setDatabaseName(configuration.getDatabaseURL());
        connectionPoolDatasource.setCreateDatabase("create");
        connectionPool = new SharedPoolDataSource();
        connectionPool.setConnectionPoolDataSource(connectionPoolDatasource);
        connectionPool.setDefaultAutoCommit(false);
        connectionPool.setDefaultTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
        final int noLimit = -1;
        connectionPool.setMaxActive(noLimit);
        connectionPool.setDefaultReadOnly(false);
        LOGGER.debug("Start data management catalog: " + configuration.getDatabaseURL());
    }
    
    private void shutDown() {
        if (connectionPool != null) {
            /* Catching Exception is not allowed due to CheckStyle,
             * thus this quirky Executor-construction is used to shut down the connection pool.
             */
            final ExecutorService executor = Executors.newFixedThreadPool(1);
            try {
                final Future<Boolean> future = executor.submit(new Callable<Boolean>() {
                    @Override
                    public Boolean call() throws Exception {
                        connectionPool.close();
                        return true;
                    }
                });
                future.get(3, TimeUnit.SECONDS);
                executor.shutdown();
            } catch (ExecutionException e) {
                final Throwable cause = e.getCause();
                LOGGER.error("Failed to close connection pool:", cause);
            } catch (InterruptedException e) {
                LOGGER.error("Failed to close connection pool due to interruption:", e);
            } catch (TimeoutException e) {
                LOGGER.error("Failed to close connection pool due to timeout:", e);
            } finally {
                connectionPool = null;
            }
        }
        connectionPoolDatasource = new EmbeddedConnectionPoolDataSource();
        connectionPoolDatasource.setDatabaseName(configuration.getDatabaseURL());
        connectionPoolDatasource.setShutdownDatabase("shutdown");
        try {
            connectionPoolDatasource.getConnection();
        } catch (SQLException e) {
            // when Derby shuts down a database, it throws an SQLException with an SQLState of 08006
            if (!e.getSQLState().equals("08006")) {
                LOGGER.error("Failed to shut down database:", e);
            }
        }
    }
    
    private DataReference restoreReferenceFromResultSet(final ResultSet resultSet, final Connection connection, boolean retried) {
        DataReference dr = null;
        try {
            String sType = resultSet.getString(TYPE);
            DataReferenceType drType = DataReference.DataReferenceType.valueOf(sType);
            if (drType == null) {
                LOGGER.error("Unknown data type entry found in data management catalog db: " + sType);
            } else {
                UUID drId = UUID.fromString(resultSet.getString(GUID));
                String parentId = resultSet.getString(PARENT_GUID);

                if (parentId != null) {
                    PlatformIdentifier pId = PlatformIdentifierFactory.fromNodeId(resultSet.getString(PARENT_EXT_HOST));
                    ParentRevision pR = new ParentRevision(UUID.fromString(parentId), pId, resultSet.getInt(PARENT_REVISION));

                    dr = new DataReference(drType, drId, platformService.getPlatformIdentifier(), pR);
                } else {
                    dr = new DataReference(drType, drId, platformService.getPlatformIdentifier());
                }
                loadRevisions(dr, connection, false);
            }
        } catch (SQLException e) {
            if (e instanceof SQLTransientConnectionException && !retried) {
                waitForRetry();
                return restoreReferenceFromResultSet(resultSet, connection, true);
            } else {
                throw new RuntimeException("Failed to restore data reference from data management catalog db.", e);
            }
        }
        return dr;
    }

    private void loadRevisions(final DataReference dataReference, final Connection connection, final boolean retried) {
        String message = "Failed to get revisions of data reference from data management catalog db: " + dataReference;
        dataReference.clear();
        try {
            Statement stmt = connection.createStatement();
            stmt.setFetchSize(MAX_QUERY_FETCH_SIZE);
            ResultSet rs = stmt.executeQuery(SELECT + NUMBER + COMMA + LOCATION + FROM + REVISIONS
                    + WHERE + NUMBER + " != 0 " + AND + DATAREFERENCE_GUID
                    + EQUAL + APO + dataReference.getIdentifier().toString() + APO);

            while (rs.next()) {
                int revisionNumber = rs.getInt(1);
                URI location = new URI(rs.getString(2));

                dataReference.addRevision(revisionNumber, location);
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            if (e instanceof SQLTransientConnectionException && !retried) {
                waitForRetry();
                loadRevisions(dataReference, connection, true);
            } else {
                throw new RuntimeException(message, e);
            }
        } catch (URISyntaxException e) {
            throw new RuntimeException(message, e);
        }
    }

    private boolean tableExists(Statement statement, String tablename) throws SQLException {
        boolean isExistentTable = false;
        ResultSet rs = null;
        try {
            final String sql = SELECT + " tablename FROM SYS.SYSTABLES" //
                    + WHERE + "tablename = " + APO + tablename + APO + AND + "tabletype = " + APO + "T" + APO;
            rs = statement.executeQuery(sql);
            isExistentTable = rs.next();
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (SQLException e) {
                e = null;
            }
        }
        return isExistentTable;
    }

    private boolean viewExists(Statement statement, String viewname) throws SQLException {
        boolean isExistentView = false;
        ResultSet rs = null;
        try {
            final String sql = SELECT + " tablename FROM SYS.SYSTABLES" //
                    + WHERE + "tablename = " + APO + viewname + APO + AND + "tabletype = " + APO + "V" + APO;
            rs = statement.executeQuery(sql);
            isExistentView = rs.next();
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (SQLException e) {
                e = null;
            }
        }
        return isExistentView;
    }

    @SuppressWarnings("unused")
    private void setupOrMigrateDatabase(final Connection connection) throws SQLException {
        boolean autoCommit = false;
        Statement statement = connection.createStatement();
        if (!tableExists(statement, CatalogConstants.DATAREFERENCES)) {
            // fresh db
            LOGGER.info("Setting up database [2]");
            createTables(connection);
            createViews(connection);
        } else if (!tableExists(statement, CatalogConstants.METADATA)
                && !tableExists(statement, CatalogConstants.REFERENCE_METADATA)
                && tableExists(statement, CatalogConstants.ALL_METADATA)) {
            // thats what we want
            final boolean weltfrieden = true;
        } else if (tableExists(statement, CatalogConstants.METADATA)
                && tableExists(statement, CatalogConstants.REFERENCE_METADATA)
                && !tableExists(statement, CatalogConstants.ALL_METADATA)) {
            LOGGER.info("Migrating database [1] -> [2]");
            String sql;
            // drop current_metadata view as is relies on metadata table which has to be renamed
            if (viewExists(statement, CatalogConstants.CURRENT_METADATA)) {
                statement.executeUpdate("DROP VIEW " + CatalogConstants.CURRENT_METADATA);
            }
            createTable(CatalogConstants.ALL_METADATA, connection);
            // create revision independent revision to fulfill integrity constraint
            sql = INSERT_INTO + CatalogConstants.REVISIONS + " " //
                    + SELECT + " DISTINCT(" + GUID + ")" + COMMA + DataReference.REVISION_INDEPENDENT_REVISION + COMMA + APO + APO //
                    + FROM + DATAREFERENCES;
            statement.execute(sql);
            // transfer from METADATA
            sql = INSERT_INTO + CatalogConstants.ALL_METADATA + " " //
                    + SELECT + " " + DATAREFERENCE_GUID + COMMA + REVISION_NUMBER + COMMA //
                    + KEY + COMMA + VALUE + COMMA + READ_ONLY + FROM + METADATA;
            statement.execute(sql);
            statement.executeUpdate("DROP TABLE " + METADATA);
            // transfer from REFERENCE_METADATA
            sql = INSERT_INTO + CatalogConstants.ALL_METADATA + " " //
                    + SELECT + " " + DATAREFERENCE_GUID + COMMA + DataReference.REVISION_INDEPENDENT_REVISION + COMMA //
                    + KEY + COMMA + VALUE + COMMA + READ_ONLY + FROM + REFERENCE_METADATA;
            statement.execute(sql);
            statement.executeUpdate("DROP TABLE " + REFERENCE_METADATA);
            // create HEAD_REVISION lookup table
            createTable(CatalogConstants.HEAD_REVISION, connection);
            // create HEAD_REVISION overview
            sql = INSERT_INTO + CatalogConstants.HEAD_REVISION + " " //
                    + SELECT + DATAREFERENCE_GUID + COMMA + "MAX(" + NUMBER + ")" + FROM + REVISIONS //
                    + " GROUP BY " + DATAREFERENCE_GUID;
            statement.execute(sql);
            createViews(connection);
        } else {
            throw new RuntimeException("Unknown DB state!");
        }
        statement.close();
    }
    
    private void createTables(final Connection connection) {
        final Runnable task = new SQLRunnable(MAX_RETRIES) {
            
            @Override
            protected void sqlRun() throws SQLTransientConnectionException {
                createTablesTrial(connection);
            }
            
            @Override
            protected void handleSQLException(SQLException sqlException) {
                throw new RuntimeException("Failed to create tables.", sqlException);
            }
            
        };
        task.run();
    }

    private void createTablesTrial(final Connection connection) throws SQLTransientConnectionException {
        try {
            createTable(CatalogConstants.DATAREFERENCES, connection);
            createTable(CatalogConstants.REVISIONS, connection);
            createTable(CatalogConstants.HEAD_REVISION, connection);
            createTable(CatalogConstants.ALL_METADATA, connection);
        } catch (SQLException e) {
            if (e instanceof SQLTransientConnectionException) {
                throw (SQLTransientConnectionException) e;
            }
            throw new RuntimeException("Failed to create tables in data management catalog db.", e);
        }
    }

    private void createTable(final String tableName, final Connection connection) throws SQLException {
        Statement stmt = connection.createStatement();
        LOGGER.debug(String.format("Creating table '%s'", tableName));

        // test for DR_TABLE existence
        if (DATAREFERENCES.equals(tableName) && !tableExists(stmt, DATAREFERENCES)) {
            String sql = CREATE_TABLE + DATAREFERENCES + "(" //
                    + GUID + CHAR_36_NOT_NULL + COMMA //
                    + TYPE + " VARCHAR(255) NOT NULL," + PARENT_GUID + " CHAR(36)," //
                    + PARENT_REVISION + " INT," //
                    + PARENT_EXT_HOST + " VARCHAR(255)," //
                    + PARENT_EXT_INSTANCE_ID + " VARCHAR(255), " //
                    + PRIMARY_KEY + "(" + GUID + ")" //
                    + ")";
            stmt.executeUpdate(sql);
        }

        // test for REV_TABLE existence
        if (REVISIONS.equals(tableName) && !tableExists(stmt, REVISIONS)) {
            String sql = CREATE_TABLE + REVISIONS + "(" //
                    + DATAREFERENCE_GUID + CHAR_36_NOT_NULL + COMMA //
                    + NUMBER + " INT NOT NULL," //
                    + LOCATION + " VARCHAR(255) NOT NULL, " //
                    + PRIMARY_KEY + "(" + DATAREFERENCE_GUID + COMMA + NUMBER + ")" + COMMA //
                    + FOREIGN_KEY + "(" + DATAREFERENCE_GUID + ")" + REFERENCES
                    + DATAREFERENCES + "(" + GUID + ")" + ON_DELETE_CASCADE //
                    + ")";
            stmt.executeUpdate(sql);
        }

        // test for HEAD_REVISION existence
        if (HEAD_REVISION.equals(tableName) && !tableExists(stmt, HEAD_REVISION)) {
            String sql = CREATE_TABLE + HEAD_REVISION + "(" //
                    + DATAREFERENCE_GUID + CHAR_36_NOT_NULL + COMMA //
                    + REVISION_NUMBER + " INT NOT NULL," //
                    + PRIMARY_KEY + "(" + DATAREFERENCE_GUID + ")" + COMMA //
                    + FOREIGN_KEY + "(" + DATAREFERENCE_GUID + COMMA + REVISION_NUMBER + ")" + REFERENCES
                    + " " + REVISIONS + "(" + DATAREFERENCE_GUID + COMMA + NUMBER + ")" + ON_DELETE_CASCADE //
                    + ")";
            stmt.executeUpdate(sql);
            stmt.executeUpdate(CREATE_INDEX + HEAD_REVISION + US + DATAREFERENCE_GUID
                    + ON + HEAD_REVISION + "(" + DATAREFERENCE_GUID + ")");
        }

        // test for METADATA_TABLE existence
        if (METADATA.equals(tableName) && !tableExists(stmt, METADATA)) {
            String sql = CREATE_TABLE + METADATA + "(" //
                    + DATAREFERENCE_GUID + CHAR_36_NOT_NULL + COMMA //
                    + REVISION_NUMBER + " " + "INT NOT NULL" + COMMA //
                    + KEY + " " + VARCHAR + "(" + MetaData.MAX_VALUE_LENGTH + ") NOT NULL" + COMMA //
                    + VALUE + " " + TYPE_VARCHAR_MAX_NOT_NULL + COMMA //
                    + READ_ONLY + " " + CHAR_5_NOT_NULL + COMMA //
                    + PRIMARY_KEY + "(" + DATAREFERENCE_GUID + COMMA + REVISION_NUMBER + COMMA + KEY + ")" + COMMA //
                    + FOREIGN_KEY + "(" + DATAREFERENCE_GUID + COMMA + REVISION_NUMBER + ")" + REFERENCES
                    + " " + REVISIONS + "(" + DATAREFERENCE_GUID + COMMA + NUMBER + ")" + ON_DELETE_CASCADE //
                    + ")";
            stmt.executeUpdate(sql);
            stmt.executeUpdate(CREATE_INDEX + METADATA + US + KEY + ON + METADATA + "(" + KEY + ")");
            stmt.executeUpdate(CREATE_INDEX + METADATA + US + VALUE + ON + METADATA + "(" + VALUE + ")");
            stmt.executeUpdate(CREATE_INDEX + METADATA + US + KEY + US + VALUE + ON + METADATA + "("
                + KEY + COMMA + VALUE + ")");
        }

        // test for REFERENCE_METADATA_TABLE existence
        if (REFERENCE_METADATA.equals(tableName) && !tableExists(stmt, REFERENCE_METADATA)) {
            String sql = CREATE_TABLE + REFERENCE_METADATA + "(" //
                    + DATAREFERENCE_GUID + " " + CHAR_36_NOT_NULL + COMMA //
                    + KEY + " " + TYPE_VARCHAR_MAX_NOT_NULL + COMMA //
                    + VALUE + " " + TYPE_VARCHAR_MAX_NOT_NULL + COMMA //
                    + READ_ONLY + " " + CHAR_5_NOT_NULL + COMMA //
                    + PRIMARY_KEY + " (" + DATAREFERENCE_GUID + COMMA + KEY + "), " //
                    + FOREIGN_KEY + " (" + DATAREFERENCE_GUID + ") REFERENCES " + DATAREFERENCES + "(" + GUID + ") ON DELETE CASCADE )";
            stmt.executeUpdate(sql);
            stmt.executeUpdate(CREATE_INDEX + REFERENCE_METADATA + US + KEY + ON + REFERENCE_METADATA + "("
                + KEY + ")");
            stmt.executeUpdate(CREATE_INDEX + REFERENCE_METADATA + US + VALUE + ON + REFERENCE_METADATA + "("
                + VALUE + ")");
            stmt.executeUpdate(CREATE_INDEX + REFERENCE_METADATA + US + KEY + US + VALUE + ON
                + REFERENCE_METADATA + "(" + KEY + COMMA + VALUE + ")");
        }

        // test for ALL_METADATA_TABLE existence
        if (ALL_METADATA.equals(tableName) && !tableExists(stmt, ALL_METADATA)) {
            String sql = CREATE_TABLE + ALL_METADATA + "(" //
                    + DATAREFERENCE_GUID + CHAR_36_NOT_NULL + COMMA //
                    + REVISION_NUMBER + " " + "INT" + COMMA //
                    + KEY + " " + VARCHAR + "(" + MetaData.MAX_VALUE_LENGTH + ") NOT NULL" + COMMA //
                    + VALUE + " " + TYPE_VARCHAR_MAX_NOT_NULL + COMMA //
                    + READ_ONLY + " " + CHAR_5_NOT_NULL + COMMA //
                    + " PRIMARY KEY (" + DATAREFERENCE_GUID + COMMA + REVISION_NUMBER + COMMA + KEY + ")"
                     + COMMA //
                    + "FOREIGN KEY (" + DATAREFERENCE_GUID + COMMA + REVISION_NUMBER + ") " + REFERENCES
                    + " " + REVISIONS + "(" + DATAREFERENCE_GUID + COMMA + NUMBER + ") " + ON_DELETE_CASCADE //
                    + " )"
                    ;
            stmt.executeUpdate(sql);
            stmt.executeUpdate(CREATE_INDEX + ALL_METADATA + US + DATAREFERENCE_GUID + US + REVISION_NUMBER //
                               + ON + ALL_METADATA + "(" + DATAREFERENCE_GUID + COMMA + REVISION_NUMBER + ")");
        }
        stmt.close();
    }
    
    private void createViews(final Connection connection) {
        final Runnable task = new SQLRunnable(MAX_RETRIES) {
            
            @Override
            protected void sqlRun() throws SQLTransientConnectionException {
                createViewsTrial(connection);
            }
            
            @Override
            protected void handleSQLException(SQLException sqlException) {
                throw new RuntimeException("Failed to create views.", sqlException);
            }
            
        };
        task.run();
    }

    private void createViewsTrial(final Connection connection) throws SQLTransientConnectionException {
        try {
            createView(CatalogConstants.REFERENCE_METADATA, connection);
            createView(CatalogConstants.METADATA, connection);
            createView(CatalogConstants.CURRENT_METADATA, connection);
        } catch (SQLException e) {
            if (e instanceof SQLTransientConnectionException) {
                throw (SQLTransientConnectionException) e;
            }
            throw new RuntimeException("Failed to create tables in data management catalog db.", e);
        }
    }

    private void createView(final String viewName, final Connection connection) throws SQLException {
        Statement stmt = connection.createStatement();
        LOGGER.debug(String.format("Creating view '%s'", viewName));
        if (CatalogConstants.CURRENT_METADATA.equals(viewName)
                && !viewExists(stmt, CatalogConstants.CURRENT_METADATA)) {
            final String sql = CREATE_VIEW + CatalogConstants.CURRENT_METADATA + AS //
                    + SELECT //
                    + CatalogConstants.ALL_METADATA + DOT + DATAREFERENCE_GUID + COMMA //
                    + CatalogConstants.ALL_METADATA + DOT + KEY + COMMA //
                    + CatalogConstants.ALL_METADATA + DOT + VALUE + COMMA //
                    + CatalogConstants.ALL_METADATA + DOT + READ_ONLY + COMMA // 
                    + "0" + AS + "revision_dependent" //
                    + FROM + CatalogConstants.HEAD_REVISION + " JOIN " + CatalogConstants.ALL_METADATA //
                    + " ON " + CatalogConstants.HEAD_REVISION + DOT + DATAREFERENCE_GUID //
                    + EQUAL + CatalogConstants.ALL_METADATA + DOT + DATAREFERENCE_GUID //
                    + AND + CatalogConstants.HEAD_REVISION + DOT + REVISION_NUMBER //
                    + EQUAL + CatalogConstants.ALL_METADATA + DOT + REVISION_NUMBER;
            stmt.executeUpdate(sql);
        }
        if (CatalogConstants.REFERENCE_METADATA.equals(viewName)
                && !viewExists(stmt, CatalogConstants.REFERENCE_METADATA)) {
            final String sql = CREATE_VIEW + " " + CatalogConstants.REFERENCE_METADATA + AS //
                    + SELECT + DATAREFERENCE_GUID + COMMA + KEY + COMMA + VALUE + COMMA + READ_ONLY //
                    + FROM + ALL_METADATA + WHERE + REVISION_NUMBER + " = " + DataReference.REVISION_INDEPENDENT_REVISION;
            stmt.executeUpdate(sql);
        }
        if (CatalogConstants.METADATA.equals(viewName)
                && !viewExists(stmt, CatalogConstants.METADATA)) {
            final String sql = CREATE_VIEW + " " + CatalogConstants.METADATA + AS //
                    + SELECT + DATAREFERENCE_GUID + COMMA + REVISION_NUMBER + COMMA + KEY + COMMA + VALUE + COMMA + READ_ONLY //
                    + FROM + ALL_METADATA + WHERE + REVISION_NUMBER + " != " + DataReference.REVISION_INDEPENDENT_REVISION;
            stmt.executeUpdate(sql);
        }
        stmt.close();
    }

    private void waitForRetry() {
        LOGGER.info("Waiting 10 seconds to retry SQL statement execution");
        try {
            Thread.sleep(TIME_TO_WAIT_FOR_RETRY);
        } catch (InterruptedException e) {
            LOGGER.warn("Waiting for retrying a failed SQL statement was interupted");
        }
    }
    
    /**
     * Manages the retries for sql statements.
     *
     * @author Christian Weiss
     */
    private abstract static class SQLRunnable implements Runnable {
        
        private final int maxAttempts;
        
        private SQLRunnable() {
            this(3);
        }
        
        private SQLRunnable(final int maxAttempts) {
            this.maxAttempts = maxAttempts;
        }
        
        @Override
        public final void run() {
            SQLException exception;
            int attemptCount = 0;
            do {
                ++attemptCount;
                exception = null;
                try {
                    sqlRun();
                    break;
                } catch (SQLTransientConnectionException e) {
                    exception = e;
                    waitForRetry();
                }
            } while (attemptCount <= maxAttempts);
            if (exception != null) {
                handleSQLException(exception);
            }
        }

        private void waitForRetry() {
            LOGGER.info("Waiting half o a second to retry SQL statement execution");
            final int halfOfASecond = 500;
            try {
                Thread.sleep(halfOfASecond);
            } catch (InterruptedException e) {
                LOGGER.warn("Waiting for retrying a failed SQL statement was interupted");
            }
        }
        
        protected abstract void sqlRun() throws SQLTransientConnectionException;
        
        protected abstract void handleSQLException(final SQLException sqlException);
        
    }

}
