package org.jboss.aerogear.android.store.sugarorm;

import android.content.Context;
import android.database.Cursor;
import org.jboss.aerogear.android.store.sugarorm.data.Data;
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
        
        Cursor res = store.getReadableDatabase().rawQuery("select * from SimpleData", new String[0]);
        assertTrue(res.moveToNext());
        assertEquals(4, res.getInt(0));
        assertEquals("name", res.getString(1));
        
        assertFalse(store.isEmpty());
        
    }

    @Test
    public void testDefaultDBIsEmpty() {
        assertTrue(store.isEmpty());
    }
    
}
