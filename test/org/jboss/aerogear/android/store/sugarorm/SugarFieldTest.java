package org.jboss.aerogear.android.store.sugarorm;

import com.google.gson.JsonObject;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import org.jboss.aerogear.android.RecordId;
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
    
    public  static final class ComplexData {
        @RecordId
        private String id = "4";
        private SimpleData data;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public SimpleData getData() {
            return data;
        }

        public void setData(SimpleData data) {
            this.data = data;
        }
        
        
    }
    
    public static final class SimpleData {
        @RecordId
        private Long id = 4l;

        private String data;
        
        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getData() {
            return data;
        }

        public void setData(String data) {
            this.data = data;
        }
        
        
        
        
    }
    
    private static final class BadFieldData {
        private JsonObject object;

        public JsonObject getObject() {
            return object;
        }

        public void setObject(JsonObject object) {
            this.object = object;
        }
        
        
    }
    
    private static final class BadSubClass {
        @RecordId 
        private Long data;
        
        private BadSubSubClass subclass;
        
    }

    private static final class BadSubSubClass {

        public BadSubSubClass() {
        }
    }
    
}
