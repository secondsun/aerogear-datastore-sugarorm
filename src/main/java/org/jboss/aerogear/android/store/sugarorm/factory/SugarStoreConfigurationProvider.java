/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jboss.aerogear.android.store.sugarorm.factory;

import org.jboss.aerogear.android.ConfigurationProvider;
import org.jboss.aerogear.android.DataManager;

/**
 *
 * @author summers
 */
public class SugarStoreConfigurationProvider implements ConfigurationProvider<SugarStoreConfiguration> {

    static {
    DataManager.registerConfigurationProvider(SugarStoreConfiguration.class,
                new SugarStoreConfigurationProvider());
    }
    
    @Override
    public SugarStoreConfiguration newConfiguration() {
        return new SugarStoreConfiguration();
    }
    
}
