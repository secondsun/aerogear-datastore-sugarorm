package org.jboss.aerogear.android.store.sugarorm;

import com.google.common.collect.Sets;
import java.lang.reflect.Field;
import java.util.Calendar;
import java.util.Date;
import java.util.Set;
import org.jboss.aerogear.android.RecordId;
import org.jboss.aerogear.android.impl.reflection.RecordIdNotFoundException;
import org.jboss.aerogear.android.impl.reflection.Scan;

public class SugarField {

    private final Field javaField;
    private final boolean isIdentityField;
    private final Class type;
    private final Field subClassIdentityField;

    private static final Set SUPPORTED_TYPES = Sets.newHashSet(
            Short.class, short.class,
            Integer.class, int.class,
            Long.class, long.class,
            Float.class, float.class,
            Double.class, double.class,
            Boolean.class, boolean.class,
            Date.class, String.class, CharSequence.class,
            Calendar.class
    );

    public SugarField(Field field) {
        if (ignoreField(field)) {
            throw new IllegalArgumentException("The Field parameter must not be marked @Ignore");
        }
        this.javaField = field;

        isIdentityField = javaField.isAnnotationPresent(RecordId.class);

        if (SUPPORTED_TYPES.contains(javaField.getType())) {
            type = javaField.getType();
            subClassIdentityField = null;
        } else {
            Class<?> subClass = javaField.getType();
            Field subClassField = getRecordIdField(subClass);

            if (!SUPPORTED_TYPES.contains(subClassField.getType())) {
                throw new IllegalArgumentException("Subtype recordID type not supported.");
            }

            type = subClassField.getType();
            subClassIdentityField = subClassField;

        }

        javaField.setAccessible(true);

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

    public String getName() {
        return javaField.getName();
    }

    public Object get(Object instance) throws IllegalArgumentException, IllegalAccessException {
        if (isSupported()) {
            return javaField.get(instance);
        } else {
            Object subClassInstance = javaField.get(instance);
            if (subClassInstance != null) {
                return Scan.findIdValueIn(subClassInstance);
            } else {
                return null;
            }
        }
    }

    public boolean isSupported() {
        return SUPPORTED_TYPES.contains(javaField.getType());
    }

    /**
     * If this field is annotated with the Ignore annotation
     *
     * @param field a field to check
     * @return if the field should be ignored by SurgarORM
     */
    public static boolean ignoreField(Field field) {
        return field.getAnnotation(Ignore.class) != null;
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
