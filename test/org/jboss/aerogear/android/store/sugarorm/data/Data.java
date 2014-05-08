package org.jboss.aerogear.android.store.sugarorm.data;

import com.google.gson.JsonObject;
import org.jboss.aerogear.android.RecordId;

public class Data {
    
    
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
    
    public static final class BadSubClass {
        @RecordId 
        private Long data;
        
        private BadSubSubClass subclass;
        
    }

    public static final class BadSubSubClass {

        public BadSubSubClass() {
        }
    }
    
}
