package org.jboss.aerogear.android.store.sugarorm;

import org.jboss.aerogear.android.datamanager.Store;
import org.jboss.aerogear.android.datamanager.StoreFactory;
import org.jboss.aerogear.android.impl.datamanager.StoreConfig;

public class SugarStoreFactory implements StoreFactory{

    @Override
    public Store createStore(StoreConfig config) {
        return new SugarStore(config.getKlass(), config.getContext());
    }
    
}
