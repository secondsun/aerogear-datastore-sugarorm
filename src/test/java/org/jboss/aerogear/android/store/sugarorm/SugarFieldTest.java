package org.jboss.aerogear.android.store.sugarorm;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import org.jboss.aerogear.android.store.sugarorm.data.Data.ComplexData;
import org.jboss.aerogear.android.store.sugarorm.data.Data.SimpleData;
import org.junit.Assert;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;


@RunWith(RobolectricTestRunner.class)
public class SugarFieldTest {

    @Test
    public void testSimpleData() throws IllegalAccessException {
        List<SugarField> fields = new ArrayList<SugarField>(2);
        SimpleData data = new SimpleData();
        data.setData("New Data");
        
        for (Field field : SimpleData.class.getDeclaredFields()) {
            SugarField sugarField = new SugarField(field);
            fields.add(sugarField);
            assertTrue(sugarField.getJavaField().equals(field));
            if (sugarField.getName().equals("id")) {
                assertTrue(sugarField.isIdentityField());
                Assert.assertEquals(Long.class, sugarField.getType());
            } else {
                Assert.assertFalse(sugarField.isIdentityField());
                Assert.assertEquals("New Data", sugarField.get(data));
            }
        }
        
        Assert.assertEquals(2, fields.size());
    }

    
    @Test
    public void testComplexData() throws IllegalAccessException {
        List<SugarField> fields = new ArrayList<SugarField>(2);
        ComplexData data = new ComplexData();
        data.setData(new SimpleData());
        
        for (Field field : ComplexData.class.getDeclaredFields()) {
            SugarField sugarField = new SugarField(field);
            fields.add(sugarField);
            assertTrue(sugarField.getJavaField().equals(field));
            if (sugarField.getName().equals("id")) {
                assertTrue(sugarField.isIdentityField());
                Assert.assertEquals(String.class, sugarField.getType());
                Assert.assertEquals("4", sugarField.get(data));
            } else {
                Assert.assertEquals("data", sugarField.getName());
                Assert.assertEquals("4", sugarField.get(data));
            }
        }
        
        Assert.assertEquals(2, fields.size());
    }
    
}
