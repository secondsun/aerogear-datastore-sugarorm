package org.jboss.aerogear.android.store.sugarorm;

import android.content.Context;
import android.database.Cursor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.jboss.aerogear.android.ReadFilter;
import org.jboss.aerogear.android.store.sugarorm.data.Data;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertTrue;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class SugarStoreTest {
    
    private Context context;
    private SugarStore<Data.SimpleData> store;
    private SugarStore<Data.ComplexData> nestedStore;
    

    @Before
    public void setUp() throws InterruptedException {
        // Let's not run this test on Mac OS X with Java 1.7 until SQLite is compatible with that configuration
        Assume.assumeTrue(!System.getProperty("os.name").toLowerCase().startsWith("mac os x")
                || !System.getProperty("java.version").startsWith("1.7.0"));

        this.context = Robolectric.application.getApplicationContext();
        this.store = new SugarStore<Data.SimpleData>(Data.SimpleData.class, context);
        store.openSync();
        
        this.nestedStore = new SugarStore<Data.ComplexData>(Data.ComplexData.class, context);
        
    }

    @Test
    public void testSave() throws InterruptedException {

        Data.SimpleData data = new Data.SimpleData();
        data.setData("name");
        store.save(data);
        
        Cursor res = store.getReadableDatabase().query(store.getTableName(), null, store.getIdentityColumn() + "=?", new String[]{String.valueOf(4)}, null, null, null, "1");
        assertTrue(res.moveToNext());
        assertEquals(4, res.getInt(0));
        assertEquals("name", res.getString(1));
        
        assertFalse(store.isEmpty());
        
    }

    @Test
    public void testRead() throws InterruptedException {

        Data.SimpleData data = new Data.SimpleData();
        data.setData("name");
        store.save(data);
        
        data = store.read(4);
        Assert.assertNotNull(data);
        assertEquals("name", data.getData());
        assertEquals(4l, (long) data.getId());
                
        
    }
    
    @Test
    public void testReadAll() throws InterruptedException {

        Data.SimpleData data1 = new Data.SimpleData();
        data1.setData("name1");
        data1.setId(1l);
        store.save(data1);
        
        Data.SimpleData data2 = new Data.SimpleData();
        data2.setData("name2");
        data2.setId(2l);
        store.save(data2);
        
        Data.SimpleData data = store.read(4);
        Assert.assertNull(data);
        
        Collection<Data.SimpleData> all = store.readAll();
        List<Data.SimpleData> sorted = new ArrayList<Data.SimpleData>(all);
        Collections.sort(sorted);
        
        assertEquals(2, all.size());
        data1 = sorted.get(0);
        data2 = sorted.get(1);
        
        assertEquals(1,(long) data1.getId());
        assertEquals(2,(long) data2.getId());
        assertEquals("name1",data1.getData());
        assertEquals("name2",data2.getData());
    }
    
    @Test
    public void testReadFilter() throws InterruptedException, JSONException {

        for (int i = 0; i < 100; i++) {
            Data.SimpleData data1 = new Data.SimpleData();
            data1.setData(String.format("name%d", i));
            data1.setId((long)i);
            store.save(data1);
        }
        
        ReadFilter filter = new ReadFilter();
        filter.setLimit(20);
        
        List<Data.SimpleData> results = store.readWithFilter(filter);
        assertEquals(20, results.size());
        
        filter = new ReadFilter();
        filter.setLimit(100);
        filter.setOffset(80);
        
        results = store.readWithFilter(filter);
        assertEquals(20, results.size());
        
        
        filter = new ReadFilter();
        filter.setWhere(new JSONObject("{\"data\":\"name1\"}"));
        
        results = store.readWithFilter(filter);
        assertEquals(1, results.size());
        assertEquals(1l,(long) results.get(0).getId());
        
        
        
    }
    
    
    @Test
    public void testRemove() throws InterruptedException, JSONException {

        for (int i = 0; i < 100; i++) {
            Data.SimpleData data1 = new Data.SimpleData();
            data1.setData(String.format("name%d", i));
            data1.setId((long)i);
            store.save(data1);
        }
        
        store.remove(1);
        assertEquals(99, store.readAll().size());
        Assert.assertNull(store.read(1));
        
    }
    
    @Test
    public void testDefaultDBIsEmpty() {
        assertTrue(store.isEmpty());
    }
    
}
