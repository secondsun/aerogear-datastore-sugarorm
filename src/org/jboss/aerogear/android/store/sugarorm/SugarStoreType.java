package org.jboss.aerogear.android.store.sugarorm;

import org.jboss.aerogear.android.datamanager.StoreType;

public class SugarStoreType implements StoreType {

    public static final StoreType TYPE = new SugarStoreType();
    
    @Override
    public String getName() {
        return "SUGAR_STORE";
    }
    
}
