package org.jboss.aerogear.android.store.sugarorm.factory;

import android.content.Context;
import org.jboss.aerogear.android.Config;
import org.jboss.aerogear.android.datamanager.Store;
import org.jboss.aerogear.android.impl.datamanager.StoreConfiguration;
import org.jboss.aerogear.android.store.sugarorm.SugarStore;

public class SugarStoreConfiguration  extends StoreConfiguration<SugarStoreConfiguration>
        implements Config<SugarStoreConfiguration> {

    private Context context;

    public Context getContext() {
        return context;
    }

    public SugarStoreConfiguration setContext(Context context) {
        this.context = context;
        return this;
    }
    
    
    
    @Override
    protected Store buildStore() {
        return new SugarStore(super.getClass(), getContext());
    }
    
    
}
