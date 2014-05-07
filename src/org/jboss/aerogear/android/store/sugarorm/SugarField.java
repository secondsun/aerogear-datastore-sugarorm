package org.jboss.aerogear.android.store.sugarorm;

import java.lang.reflect.Field;
import org.jboss.aerogear.android.RecordId;
import org.jboss.aerogear.android.impl.reflection.RecordIdNotFoundException;
import org.jboss.aerogear.android.impl.reflection.Scan;

public class SugarField {

    private final Field javaField;
    private final boolean isIdentityField;
    private final Class type;

    public SugarField(Field field) {
        if (ignoreField(field)) {
            throw new IllegalArgumentException("The Field parameter must not be marked @Ignore");
        }
        this.javaField = field;

        isIdentityField = javaField.isAnnotationPresent(RecordId.class);

        if (javaField.getType().isPrimitive()) {
            type = javaField.getType();
        } else {
            Class<?> subClass = javaField.getType();
            Field subClassField = getRecordIdField(subClass);

            if (!subClassField.getType().isPrimitive()) {
                throw new IllegalArgumentException("The RecordId of a non Primitive class must be a primitive");
            }
            
            type = subClassField.getType();

        }

    }

    public Field getJavaField() {
        return javaField;
    }

    public boolean isIdentityField() {
        return isIdentityField;
    }

    public Class getType() {
        return type;
    }

    
    
    /**
     * If this field is annotated with the Ignore annotation
     *
     * @param field a field to check
     * @return if the field should be ignored by SurgarORM
     */
    public static boolean ignoreField(Field field) {
        return field.getAnnotation(Ignore.class) == null;
    }

    private static Field getRecordIdField(Class klass) {
        for (Field field : klass.getDeclaredFields()) {
            if (field.isAnnotationPresent(RecordId.class)) {
                return field;
            }
        }
        Class superclass = klass.getSuperclass();
        if (superclass != null) {
            return getRecordIdField(superclass);
        }
        throw new RecordIdNotFoundException(klass);
    }

}
