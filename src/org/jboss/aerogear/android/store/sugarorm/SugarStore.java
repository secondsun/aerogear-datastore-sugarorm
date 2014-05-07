package org.jboss.aerogear.android.store.sugarorm;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import org.jboss.aerogear.android.Callback;
import org.jboss.aerogear.android.ReadFilter;
import org.jboss.aerogear.android.datamanager.Store;
import org.jboss.aerogear.android.datamanager.StoreType;

public class SugarStore<T> extends SugarDb implements Store<T> {

    private final Context context;
    private static final String TAG = SugarStore.class.getSimpleName();
    private final Class<T> klass;
    private final String className;
    private final String tableName;
    private SQLiteDatabase database;
    private static final Multimap<Class, SugarField> fields = LinkedListMultimap.create();
    
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
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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
     * @throws IllegalArgumentException if one of the fields in the wrapped class are bad.  
     */
    List<SugarField> getTableFields() {
        if (fields.get(klass) == null) {
            synchronized(klass) {
                for (Field field : klass.getFields()) {
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
        AsyncTask.THREAD_POOL_EXECUTOR.execute (
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

}
