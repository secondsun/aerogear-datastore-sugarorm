package org.jboss.aerogear.android.store.sugarorm;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.orm.StringUtil;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import org.jboss.aerogear.android.Callback;
import org.jboss.aerogear.android.ReadFilter;
import org.jboss.aerogear.android.datamanager.IdGenerator;
import org.jboss.aerogear.android.datamanager.Store;
import org.jboss.aerogear.android.datamanager.StoreType;
import org.jboss.aerogear.android.impl.datamanager.DefaultIdGenerator;

public class SugarStore<T> extends SugarDb implements Store<T> {

    private final Context context;
    private static final String TAG = SugarStore.class.getSimpleName();
    private final Class<T> klass;
    private final String className;
    private final String tableName;
    private SQLiteDatabase database;
    private static final Multimap<Class, SugarField> fields = LinkedListMultimap.create();
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
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public T read(Serializable id) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<T> readWithFilter(ReadFilter filter) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void remove(Serializable id) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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
        if (fields.get(klass).isEmpty()) {
            synchronized (klass) {
                for (Field field : klass.getDeclaredFields()) {
                    if (!field.isAnnotationPresent(Ignore.class)) {
                        fields.put(klass, new SugarField(field));
                    }
                }
            }
        }
        return Lists.newArrayList(fields.get(klass));
    }

    public void open(final Callback<SugarStore<T>> onReady) {
        final Looper looper = Looper.myLooper();
        AsyncTask.THREAD_POOL_EXECUTOR.execute(
                new Runnable() {
                    private Exception exception;

                    @Override
                    public void run() {
                        try {
                            SugarStore.this.database = getWritableDatabase();

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
    }

}
