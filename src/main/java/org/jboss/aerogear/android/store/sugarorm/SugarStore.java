package org.jboss.aerogear.android.store.sugarorm;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import com.orm.StringUtil;
import com.orm.SugarRecord;
import static com.orm.SugarRecord.findById;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.jboss.aerogear.android.Callback;
import org.jboss.aerogear.android.ReadFilter;
import org.jboss.aerogear.android.datamanager.IdGenerator;
import org.jboss.aerogear.android.datamanager.Store;
import org.jboss.aerogear.android.datamanager.StoreType;
import org.jboss.aerogear.android.impl.datamanager.DefaultIdGenerator;
import org.json.JSONObject;

public class SugarStore<T> extends SugarDb implements Store<T> {

    private final Context context;
    private static Map<Class, SugarStore> STORE_MAP = new HashMap<Class, SugarStore>();
    private static final String TAG = SugarStore.class.getSimpleName();
    private final Class<T> klass;
    private final String className;
    private final String tableName;
    private SQLiteDatabase database;
    private static final Map<Class, ArrayList<SugarField>> fields = new HashMap<Class, ArrayList<SugarField>>();
    private final IdGenerator idGenerator = new DefaultIdGenerator();

    public SugarStore(Class<T> klass, Context context) {
        super(context);
        this.context = context;
        this.klass = klass;
        this.className = klass.getSimpleName();
        this.tableName = className;
    }

    @Override
    public StoreType getType() {
        return SugarStoreType.TYPE;
    }

    @Override
    public Collection<T> readAll() {
        List<T> result = new ArrayList<T>();

        Cursor c = database.query(getTableName(), null, null, null, null, null, null);
        try {
            while (c.moveToNext()) {
                result.add(inflate(c));
            }
        } catch (InstantiationException e) {
            Log.e(TAG, e.getMessage(), e);
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            Log.e(TAG, e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
            c.close();
        }

        return result;
    }

    @Override
    public T read(Serializable id) {
        Cursor cursor = null;
        try {
            cursor = database.query(getTableName(), null, getIdentityColumn() + "=?", new String[]{String.valueOf(id)}, null, null, null, "1");
            if (cursor.moveToNext()) {
                T entity = inflate(cursor);
                return entity;
            } else {
                return null;
            }
        } catch (InstantiationException ex) {
            Log.e(TAG, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        } catch (IllegalAccessException ex) {
            Log.e(TAG, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    @Override
    public List<T> readWithFilter(ReadFilter filter) {
        StringBuilder queryBuilder = new StringBuilder("");
        JSONObject where = filter.getWhere();
        ArrayList<String> arguments = new ArrayList<String>(filter.getWhere().length());
        StringBuilder limit = new StringBuilder();

        if (where != null) {
            Iterator keysIter = where.keys();
            while (keysIter.hasNext()) {
                String key = keysIter.next().toString();
                queryBuilder.append(key).append(" = ?");
                arguments.add(where.optString(key));
            }
        }

        if (filter.getOffset() != null) {
            limit.append(filter.getOffset()).append(", ");
        }

        if (filter.getLimit() != null) {
            limit.append(filter.getLimit());
        }

        Cursor cursor = database.query(getTableName(), null, queryBuilder.toString(), arguments.toArray(new String[arguments.size()]), null, null, null, limit.toString());

        try {
            ArrayList<T> toReturn = new ArrayList<T>();
            while (cursor.moveToNext()) {
                try {
                    toReturn.add(inflate(cursor));
                } catch (InstantiationException e) {
                    Log.e(TAG, e.getMessage(), e);
                    throw new RuntimeException(e);
                } catch (IllegalAccessException e) {
                    Log.e(TAG, e.getMessage(), e);
                    throw new RuntimeException(e);
                }
            }
            return toReturn;
        } finally {
            cursor.close();
        }

    }

    @Override
    public void save(T item) {
        List<SugarField> columns = getTableFields();
        ContentValues values = new ContentValues(columns.size());
        SugarField idField = null;
        Object idValue = null;
        for (SugarField column : columns) {

            Class<?> columnType = column.getType();
            try {
                String columnName = StringUtil.toSQLName(column.getName());
                Object columnValue = column.get(item);

                if (column.isIdentityField()) {
                    idField = column;
                    idValue = column.get(item);
                    if (idValue == null) {
                        column.getJavaField().set(item, idGenerator.generate());
                    }
                }

                if (columnType.equals(Short.class) || columnType.equals(short.class)) {
                    values.put(columnName, (Short) columnValue);
                } else if (columnType.equals(Integer.class) || columnType.equals(int.class)) {
                    values.put(columnName, (Integer) columnValue);
                } else if (columnType.equals(Long.class) || columnType.equals(long.class)) {
                    values.put(columnName, (Long) columnValue);
                } else if (columnType.equals(Float.class) || columnType.equals(float.class)) {
                    values.put(columnName, (Float) columnValue);
                } else if (columnType.equals(Double.class) || columnType.equals(double.class)) {
                    values.put(columnName, (Double) columnValue);
                } else if (columnType.equals(Boolean.class) || columnType.equals(boolean.class)) {
                    values.put(columnName, (Boolean) columnValue);
                } else if (Date.class.equals(columnType)) {
                    values.put(columnName, ((Date) column.get(this)).getTime());
                } else if (Calendar.class.equals(columnType)) {
                    values.put(columnName, ((Calendar) column.get(this)).getTimeInMillis());
                } else {
                    values.put(columnName, String.valueOf(columnValue));
                }

            } catch (IllegalAccessException e) {
                Log.e("Sugar", e.getMessage());
            }
        }

        if (idValue == null) {
            database.insert(getTableName(), null, values);
        } else {
            if (database.query(getTableName(), null, "ID = ?", new String[]{String.valueOf(idValue)}, null, null, null).moveToNext()) {
                database.update(getTableName(), values, idField.getName() + " = ?", new String[]{String.valueOf(idValue)});
            } else {
                database.insert(getTableName(), null, values);
            }
        }

        Log.i("Sugar", getClass().getSimpleName() + " saved : " + idValue);
    }

    @Override
    public void reset() {
        database.delete(getTableName(), null, null);
    }

    @Override
    public void remove(Serializable id) {
        database.delete(getTableName(), String.format("%s = ?", getIdentityColumn()), new String[]{id.toString()});
    }

    @Override
    public boolean isEmpty() {
        Cursor q = database.query(getTableName(), null, null, null, null, null, null);
        try {
            return !q.moveToNext();
        } finally {
            q.close();
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        super.createTable(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    String getTableName() {
        return tableName;
    }

    @Override

    /**
     * Gets all of the fields which will be used to make the object/table.
     *
     * @return a list of fields with sugar metadata
     * @throws IllegalArgumentException if one of the fields in the wrapped
     * class are bad.
     */
    List<SugarField> getTableFields() {

        if (fields.get(klass) == null || fields.get(klass).isEmpty()) {
            synchronized (klass) {
                if (fields.get(klass) == null) {
                    fields.put(klass, new ArrayList<SugarField>());
                }
                for (Field field : klass.getDeclaredFields()) {
                    if (!field.isAnnotationPresent(Ignore.class)) {
                        fields.get(klass).add(new SugarField(field));
                    }
                }
            }
        }
        return new ArrayList<SugarField>(fields.get(klass));
    }

    public void open(final Callback<SugarStore<T>> onReady) {
        final Looper looper = Looper.myLooper();
        AsyncTask.THREAD_POOL_EXECUTOR.execute(
                new Runnable() {
                    private Exception exception;

                    @Override
                    public void run() {
                        try {
                            openSync();

                        } catch (Exception e) {
                            this.exception = e;
                            Log.e(TAG, "There was an error loading the database", e);
                        }
                        if (exception != null) {
                            new Handler(looper).post(new Runnable() {

                                @Override
                                public void run() {
                                    onReady.onFailure(exception);
                                }
                            });
                        } else {
                            new Handler(looper).post(new Runnable() {

                                @Override
                                public void run() {
                                    onReady.onSuccess(SugarStore.this);
                                }
                            });

                        }

                    }
                });

    }

    public void openSync() {
        this.database = getWritableDatabase();
        for (SugarField field : getTableFields()) {
            if (field.getJavaField().isAnnotationPresent(collecion mpping annotation))
        }
    }

    private T inflate(Cursor cursor) throws InstantiationException, IllegalAccessException {
        T entity = klass.newInstance();
        Map<SugarField, Long> entities = new HashMap<SugarField, Long>();
        List<SugarField> columns = getTableFields();
        for (SugarField sugarField : columns) {
            Field field = sugarField.getJavaField();
            field.setAccessible(true);

            Class fieldType = sugarField.getType();
            String colName = StringUtil.toSQLName(sugarField.getName());

            int columnIndex = cursor.getColumnIndex(colName);

            if (cursor.isNull(columnIndex)) {
                continue;
            }

            if (colName.equalsIgnoreCase("id")) {
                long cid = cursor.getLong(columnIndex);
                field.set(entity, Long.valueOf(cid));
            } else if (fieldType.equals(long.class) || fieldType.equals(Long.class)) {
                field.set(entity,
                        cursor.getLong(columnIndex));
            } else if (fieldType.equals(String.class)) {
                String val = cursor.getString(columnIndex);
                field.set(entity, val != null && val.equals("null") ? null : val);
            } else if (fieldType.equals(double.class) || fieldType.equals(Double.class)) {
                field.set(entity,
                        cursor.getDouble(columnIndex));
            } else if (fieldType.equals(boolean.class) || fieldType.equals(Boolean.class)) {
                field.set(entity,
                        cursor.getString(columnIndex).equals("1"));
            } else if (sugarField.getType().getName().equals("[B")) {
                field.set(entity,
                        cursor.getBlob(columnIndex));
            } else if (fieldType.equals(int.class) || fieldType.equals(Integer.class)) {
                field.set(entity,
                        cursor.getInt(columnIndex));
            } else if (fieldType.equals(float.class) || fieldType.equals(Float.class)) {
                field.set(entity,
                        cursor.getFloat(columnIndex));
            } else if (fieldType.equals(short.class) || fieldType.equals(Short.class)) {
                field.set(entity,
                        cursor.getShort(columnIndex));
            } else if (fieldType.equals(Timestamp.class)) {
                long l = cursor.getLong(columnIndex);
                field.set(entity, new Timestamp(l));
            } else if (fieldType.equals(Date.class)) {
                long l = cursor.getLong(columnIndex);
                field.set(entity, new Date(l));
            } else if (fieldType.equals(Calendar.class)) {
                long l = cursor.getLong(columnIndex);
                Calendar c = Calendar.getInstance();
                c.setTimeInMillis(l);
                field.set(entity, c);
            } else if (Enum.class.isAssignableFrom(fieldType)) {
                try {
                    Method valueOf = sugarField.getType().getMethod("valueOf", String.class);
                    String strVal = cursor.getString(columnIndex);
                    Object enumVal = valueOf.invoke(sugarField.getType(), strVal);
                    field.set(entity, enumVal);
                } catch (Exception e) {
                    Log.e(TAG, "Enum cannot be read from Sqlite3 database. Please check the type of field " + sugarField.getName());
                    throw new RuntimeException(e);
                }
            } else if (SugarRecord.class.isAssignableFrom(fieldType)) {
                long id = cursor.getLong(columnIndex);
                if (id > 0) {
                    entities.put(sugarField, id);
                } else {
                    field.set(entity, null);
                }
            } else {
                Log.e(TAG, "Class cannot be read from Sqlite3 database. Please check the type of field " + sugarField.getName() + "(" + sugarField.getType().getName() + ")");
                throw new RuntimeException("Class cannot be read from Sqlite3 database. Please check the type of field " + sugarField.getName() + "(" + sugarField.getType().getName() + ")");
            }

        }

        for (SugarField f : entities.keySet()) {
            try {
                f.getJavaField().set(entity, findById((Class<? extends SugarRecord<?>>) f.getType(),
                        entities.get(f)));
            } catch (SQLiteException e) {
                Log.e(TAG, e.getMessage(), e);
                throw new RuntimeException(e);
            } catch (IllegalArgumentException e) {
                Log.e(TAG, e.getMessage(), e);
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                Log.e(TAG, e.getMessage(), e);
                throw new RuntimeException(e);
            }
        }
        return entity;
    }

    protected String getIdentityColumn() {
        for (SugarField field : getTableFields()) {
            if (field.isIdentityField()) {
                return StringUtil.toSQLName(field.getName());
            }
        }

        throw new IllegalStateException("There is not @RecordId field in class " + klass.getSimpleName() + ".");
    }

    @Override
    public void save(Collection<T> items) {
        try {
            database.beginTransaction();
            
            for (T item : items) {
                save(item);
            }
            
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }

    }
    
    private <K extends Object> SugarStore<K> getStore(Class<K> klass) {
        if (STORE_MAP.get(klass) == null) {
            throw new IllegalStateException(klass + " was not detected and initialized");
        }
        return STORE_MAP.get(klass);
    }
    
}
