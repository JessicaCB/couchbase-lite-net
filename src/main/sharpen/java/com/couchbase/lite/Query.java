package com.couchbase.lite;

import com.couchbase.lite.internal.InterfaceAudience;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

/**
 * Represents a query of a CouchbaseLite 'view', or of a view-like resource like _all_documents.
 */
public class Query {

    public enum IndexUpdateMode {
        NEVER, BEFORE, AFTER
    }

    /**
     * The database that contains this view.
     */
    private Database database;

    /**
     * The view object associated with this query
     */
    private View view;  // null for _all_docs query

    /**
     * Is this query based on a temporary view?
     */
    private boolean temporaryView;

    /**
     * The number of initial rows to skip. Default value is 0.
     * Should only be used with small values. For efficient paging, use startKey and limit.
     */
    private int skip;

    /**
     * The maximum number of rows to return. Default value is 0, meaning 'unlimited'.
     */
    private int limit = Integer.MAX_VALUE;

    /**
     * If non-nil, the key value to start at.
     */
    private Object startKey;

    /**
     * If non-nil, the key value to end after.
     */
    private Object endKey;

    /**
     * If non-nil, the document ID to start at.
     * (Useful if the view contains multiple identical keys, making .startKey ambiguous.)
     */
    private String startKeyDocId;

    /**
     * If non-nil, the document ID to end at.
     * (Useful if the view contains multiple identical keys, making .endKey ambiguous.)
     */
    private String endKeyDocId;

    /**
     * If set, the view will not be updated for this query, even if the database has changed.
     * This allows faster results at the expense of returning possibly out-of-date data.
     */
    private IndexUpdateMode indexUpdateMode;

    /**
     * Should the rows be returned in descending key order? Default value is NO.
     */
    private boolean descending;

    /**
     *  If set to YES, the results will include the entire document contents of the associated rows.
     *  These can be accessed via QueryRow's -documentProperties property.
     *  This slows down the query, but can be a good optimization if you know you'll need the entire
     *  contents of each document. (This property is equivalent to "include_docs" in the CouchDB API.)
     */
    private boolean prefetch;

    /**
     * If set to YES, disables use of the reduce function.
     * (Equivalent to setting "?reduce=false" in the REST API.)
     */
    private boolean mapOnly;

    /**
     * If set to YES, queries created by -createAllDocumentsQuery will include deleted documents.
     * This property has no effect in other types of queries.
     */
    private boolean includeDeleted;

    /**
     * If non-nil, the query will fetch only the rows with the given keys.
     */
    private List<Object> keys;

    /**
     * If non-zero, enables grouping of results, in views that have reduce functions.
     */
    private int groupLevel;

    /**
     * If a query is running and the user calls stop() on this query, the future
     * will be used in order to cancel the query in progress.
     */
    protected Future updateQueryFuture;

    private long lastSequence;

    /**
     * Constructor
     */
    @InterfaceAudience.Private
    Query(Database database, View view) {
        this.database = database;
        this.view = view;
        limit = Integer.MAX_VALUE;
        mapOnly = (view != null && view.getReduce() == null);
        indexUpdateMode = IndexUpdateMode.NEVER;
    }

    /**
     * Constructor
     */
    @InterfaceAudience.Private
    Query(Database database, Mapper mapFunction) {
        this(database, database.makeAnonymousView());
        temporaryView = true;
        view.setMap(mapFunction, "");
    }

    /**
     * Constructor
     */
    @InterfaceAudience.Private
    Query(Database database, Query query) {
        this(database, query.getView());
        limit = query.limit;
        skip = query.skip;
        startKey = query.startKey;
        endKey = query.endKey;
        descending = query.descending;
        prefetch = query.prefetch;
        keys = query.keys;
        groupLevel = query.groupLevel;
        mapOnly = query.mapOnly;
        startKeyDocId = query.startKeyDocId;
        endKeyDocId = query.endKeyDocId;
        indexUpdateMode = query.indexUpdateMode;
    }

    /**
     * The database this query is associated with
     */
    @InterfaceAudience.Public
    public Database getDatabase() {
        return database;
    }

    @InterfaceAudience.Public
    public int getLimit() {
        return limit;
    }

    @InterfaceAudience.Public
    public void setLimit(int limit) {
        this.limit = limit;
    }


    @InterfaceAudience.Public
    public int getSkip() {
        return skip;
    }

    @InterfaceAudience.Public
    public void setSkip(int skip) {
        this.skip = skip;
    }

    @InterfaceAudience.Public
    public boolean isDescending() {
        return descending;
    }

    @InterfaceAudience.Public
    public void setDescending(boolean descending) {
        this.descending = descending;
    }

    @InterfaceAudience.Public
    public Object getStartKey() {
        return startKey;
    }

    @InterfaceAudience.Public
    public void setStartKey(Object startKey) {
        this.startKey = startKey;
    }

    @InterfaceAudience.Public
    public Object getEndKey() {
        return endKey;
    }

    @InterfaceAudience.Public
    public void setEndKey(Object endKey) {
        this.endKey = endKey;
    }

    @InterfaceAudience.Public
    public String getStartKeyDocId() {
        return startKeyDocId;
    }

    @InterfaceAudience.Public
    public void setStartKeyDocId(String startKeyDocId) {
        this.startKeyDocId = startKeyDocId;
    }

    @InterfaceAudience.Public
    public String getEndKeyDocId() {
        return endKeyDocId;
    }

    @InterfaceAudience.Public
    public void setEndKeyDocId(String endKeyDocId) {
        this.endKeyDocId = endKeyDocId;
    }

    @InterfaceAudience.Public
    public IndexUpdateMode getIndexUpdateMode() {
        return indexUpdateMode;
    }

    @InterfaceAudience.Public
    public void setIndexUpdateMode(IndexUpdateMode indexUpdateMode) {
        this.indexUpdateMode = indexUpdateMode;
    }

    @InterfaceAudience.Public
    public List<Object> getKeys() {
        return keys;
    }

    @InterfaceAudience.Public
    public void setKeys(List<Object> keys) {
        this.keys = keys;
    }

    @InterfaceAudience.Public
    public boolean isMapOnly() {
        return mapOnly;
    }

    @InterfaceAudience.Public
    public void setMapOnly(boolean mapOnly) {
        this.mapOnly = mapOnly;
    }

    @InterfaceAudience.Public
    public int getGroupLevel() {
        return groupLevel;
    }

    @InterfaceAudience.Public
    public void setGroupLevel(int groupLevel) {
        this.groupLevel = groupLevel;
    }

    @InterfaceAudience.Public
    public boolean shouldPrefetch() {
        return prefetch;
    }

    @InterfaceAudience.Public
    public void setPrefetch(boolean prefetch) {
        this.prefetch = prefetch;
    }

    @InterfaceAudience.Public
    public boolean shouldIncludeDeleted() {
        return includeDeleted;
    }

    @InterfaceAudience.Public
    public void setIncludeDeleted(boolean includeDeleted) {
        this.includeDeleted = includeDeleted;
    }

    /**
     * Sends the query to the server and returns an enumerator over the result rows (Synchronous).
     * If the query fails, this method returns nil and sets the query's .error property.
     */
    @InterfaceAudience.Public
    public QueryEnumerator run() throws CouchbaseLiteException {
        List<Long> outSequence = new ArrayList<Long>();
        String viewName = (view != null) ? view.getName() : null;
        List<QueryRow> rows = database.queryViewNamed(viewName, getQueryOptions(), outSequence);
        lastSequence = outSequence.get(0);
        return new QueryEnumerator(database, rows, lastSequence);
    }

    /**
     * Returns a live query with the same parameters.
     */
    @InterfaceAudience.Public
    public LiveQuery toLiveQuery() {
        if (view == null) {
            throw new IllegalStateException("Cannot convert a Query to LiveQuery if the view is null");
        }
        return new LiveQuery(this);
    }

    /**
     *  Starts an asynchronous query. Returns immediately, then calls the onLiveQueryChanged block when the
     *  query completes, passing it the row enumerator. If the query fails, the block will receive
     *  a non-nil enumerator but its .error property will be set to a value reflecting the error.
     *  The originating Query's .error property will NOT change.
     */
    @InterfaceAudience.Public
    public Future runAsync(final QueryCompleteListener onComplete) {
        return runAsyncInternal(onComplete);
    }

    @InterfaceAudience.Private
    Future runAsyncInternal(final QueryCompleteListener onComplete) {

        return database.getManager().runAsync(new Runnable() {
            @Override
            public void run() {
                try {
                    String viewName = view.getName();
                    QueryOptions options = getQueryOptions();
                    List<Long> outSequence = new ArrayList<Long>();
                    List<QueryRow> rows = database.queryViewNamed(viewName, options, outSequence);
                    long sequenceNumber = outSequence.get(0);
                    QueryEnumerator enumerator = new QueryEnumerator(database, rows, sequenceNumber);
                    onComplete.completed(enumerator, null);

                } catch (Throwable t) {
                    onComplete.completed(null, t);
                }
            }
        });

    }

    public View getView() {
        return view;
    }

    private QueryOptions getQueryOptions() {
        QueryOptions queryOptions = new QueryOptions();
        queryOptions.setStartKey(getStartKey());
        queryOptions.setEndKey(getEndKey());
        queryOptions.setStartKey(getStartKey());
        queryOptions.setKeys(getKeys());
        queryOptions.setSkip(getSkip());
        queryOptions.setLimit(getLimit());
        queryOptions.setReduce(!isMapOnly());
        queryOptions.setReduceSpecified(true);
        queryOptions.setGroupLevel(getGroupLevel());
        queryOptions.setDescending(isDescending());
        queryOptions.setIncludeDocs(shouldPrefetch());
        queryOptions.setUpdateSeq(true);
        queryOptions.setInclusiveEnd(true);
        queryOptions.setIncludeDeletedDocs(shouldIncludeDeleted());
        queryOptions.setStale(getIndexUpdateMode());
        return queryOptions;
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        if (temporaryView) {
            view.delete();
        }
    }

    public static interface QueryCompleteListener {
        public void completed(QueryEnumerator rows, Throwable error);
    }



}