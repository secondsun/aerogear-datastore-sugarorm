package org.jboss.aerogear.android.store.sugarorm;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import com.orm.QueryBuilder;
import com.orm.StringUtil;
import com.orm.SugarConfig;
import static com.orm.SugarConfig.getDatabaseVersion;
import java.util.List;

abstract class SugarDb extends SQLiteOpenHelper {
    
    private final Context context;

    public SugarDb(Context context) {
        super(context, SugarConfig.getDatabaseName(context), null, getDatabaseVersion(context));
        this.context = context;
    }

    protected void createTable(SQLiteDatabase sqLiteDatabase) {
        Log.i("Sugar", "create table");
        List<SugarField> fields = getTableFields();
        StringBuilder sb = new StringBuilder("CREATE TABLE ").append(getTableName()).append(
                " (  ");
        String comma = " ";
        
        for (SugarField column : fields) {
            String columnName = StringUtil.toSQLName(column.getJavaField().getName());
            String columnType = QueryBuilder.getColumnType(column.getType());

            if (columnType != null) {

                sb.append(comma).append(columnName).append(" ").append(columnType);
                
                if (column.isIdentityField()) {
                    sb.append(" PRIMARY KEY ");
                }
            }
            
            comma = ", ";
            
        }
        
        sb.append(" ) ");

        Log.i("Sugar", "creating table " + getTableName());

        if (!"".equals(sb.toString())) {
            sqLiteDatabase.execSQL(sb.toString());
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        throw new RuntimeException("Not yet implemented");
    }

    abstract String getTableName();

    abstract List<SugarField> getTableFields();
}
