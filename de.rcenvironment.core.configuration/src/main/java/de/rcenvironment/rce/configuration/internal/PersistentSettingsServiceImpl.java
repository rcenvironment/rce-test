/*
 * Copyright (C) 2006-2012 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.configuration.internal;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.osgi.framework.BundleContext;

import de.rcenvironment.rce.configuration.ConfigurationService;
import de.rcenvironment.rce.configuration.PersistentSettingsService;


/**
 * Implementation of simple key-value store to persist settings of an RCE platform.
 *
 * @author Sascha Zur
 */
public class PersistentSettingsServiceImpl implements PersistentSettingsService {

    private static final String PERSISTENTDATA_FOLDER = "persistentSettings";
    private static final String PERSISTENTDATA_FILE = "persSet.json";

    private static final String ERROR_MSG_SAVE = "Could not save persistent settings: ";
    private static final String ERROR_MSG_LOAD = "Could not find persistent settings file. It will be created: ";

    private ConfigurationService configurationService;

    private String persistentSettingsDir = "";

    private Map<String, String> store;

    /**
     * {@inheritDoc}
     * @see de.rcenvironment.rce.configuration.PersistentSettingsService#saveStringValue(java.lang.String, java.lang.String)
     */
    @Override
    public synchronized void saveStringValue(String key, String value) {
        saveStringValue(key, value, PERSISTENTDATA_FILE);
    }

  
    
    private void saveStore(String filename) {
        File currentFile =  new File((persistentSettingsDir + File.separator + PERSISTENTDATA_FILE));
        if (currentFile.exists()){
            try {
                FileUtils.copyFile(currentFile, new File((persistentSettingsDir + File.separator + PERSISTENTDATA_FILE + ".bak ")));
            } catch (IOException e) {
                Logger.getAnonymousLogger().warning("PersistentSettingsService: Could not create bak File. ");
            }
        }

        JsonGenerator g;
        try {
            g =  new JsonFactory().createJsonGenerator(new FileWriter(
                new File((persistentSettingsDir + File.separator + filename))));
            g.useDefaultPrettyPrinter();
            g.writeStartObject();
            for (String storeKey : store.keySet()){
                g.writeStringField(storeKey, store.get(storeKey));
            }
            g.writeEndObject();
            g.close();
        } catch (IOException e) {
            Logger.getAnonymousLogger().warning(ERROR_MSG_SAVE + e.getMessage() + "1");            
        }      



    }

    /**
     * {@inheritDoc}
     *
     * @see de.rcenvironment.rce.configuration.PersistentSettingsService#readStringValue(java.lang.String)
     */
    @Override
    public synchronized String readStringValue(String key) {
        return readStringValue(key, PERSISTENTDATA_FILE);
    }
  
    private Map<String, String> readStore(String filename) {
        Map<String, String> result = new HashMap<String, String>();

        JsonFactory f = new JsonFactory();
        JsonParser jp;
        try {
            jp = f.createJsonParser(new File(persistentSettingsDir + File.separator + filename));
            jp.nextToken();
            while (jp.hasCurrentToken() && jp.nextToken() != JsonToken.END_OBJECT) {
                String fieldname = jp.getCurrentName();
                if (jp.nextToken() == JsonToken.END_OBJECT){
                    break;
                } else {                    
                    result.put(fieldname, jp.getText());
                }
            }
            jp.close(); 
        } catch (JsonParseException e) {
            result = null;
            Logger.getAnonymousLogger().warning(ERROR_MSG_LOAD + e.getMessage() 
                + "(This is normal if RCE is startet for the first time)");            
        } catch (IOException e) {
            result = null;
            Logger.getAnonymousLogger().warning(ERROR_MSG_LOAD + e.getMessage()
                + "(This is normal if RCE is startet for the first time)");            
        }


        return result;
    }
    /**
     * {@inheritDoc}
     *
     * @see de.rcenvironment.rce.configuration.PersistentSettingsService
     *      #saveStringValue(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public void saveStringValue(String key, String value, String filename) {
        if (store != null){
            store.put(key, value);

        } else {
            store = readStore(filename);
            if (store == null){
                store = new HashMap<String, String>();
            }    
            store.put(key, value);
        }
        saveStore(filename);

        
    }

    /**
     * {@inheritDoc}
     *
     * @see de.rcenvironment.rce.configuration.PersistentSettingsService
     *      #readStringValue(java.lang.String, java.lang.String)
     */
    @Override
    public String readStringValue(String key, String filename) {
        store = readStore(filename);
        if (store != null && store.containsKey(key)){
            return store.get(key);
        }
        return null;
    }
    /**
     * {@inheritDoc}
     *
     * @see de.rcenvironment.rce.configuration.PersistentSettingsService#delete(java.lang.String)
     */
    @Override
    public synchronized void delete(String key) {
        delete(key, PERSISTENTDATA_FILE);
    }   

    /**
     * {@inheritDoc}
     *
     * @see de.rcenvironment.rce.configuration.PersistentSettingsService#delete(java.lang.String, java.lang.String)
     */
    @Override
    public void delete(String key, String filename) {
        store = readStore(filename);
        if (store != null){
            store.remove(key);
        }
        saveStore(filename);
        
    }

    protected void activate(BundleContext context) {
        if (persistentSettingsDir.equals("")) {
            persistentSettingsDir = configurationService.getPlatformHome() + File.separator + PERSISTENTDATA_FOLDER;
            File dir = new File(persistentSettingsDir);
            if (!dir.exists()){
                dir.mkdirs();
            }
        }
    }
    protected void bindConfigurationService(ConfigurationService newConfigurationService) {
        configurationService = newConfigurationService;

    }


    public String getPersistentSettingsDir() {
        return persistentSettingsDir;
    }


    public void setPersistentSettingsDir(String persistentSettingsDir) {
        this.persistentSettingsDir = persistentSettingsDir;
    }

    /**
     * {@inheritDoc}
     *
     * @see de.rcenvironment.rce.configuration.PersistentSettingsService#readMapWithStringList(java.lang.String)
     */
    @Override
    public Map<String, List<String>> readMapWithStringList(String filename) {
        ObjectMapper mapper = new ObjectMapper();
        File f = null;
        f = new File((persistentSettingsDir + File.separator + filename));
        if (!f.exists()) {
            new File(persistentSettingsDir).mkdirs();
        }
        Map<String, List<String>> result = new HashMap <String, List<String>>();
        if (f.exists()){
            try {
                result = mapper.readValue(f, new TypeReference <Map<String, List<String>>>() { });
            } catch (JsonGenerationException e) {
                Logger.getAnonymousLogger().severe(e.getMessage());
            } catch (JsonMappingException e) {
                Logger.getAnonymousLogger().severe(e.getMessage());
            } catch (IOException e) {
                Logger.getAnonymousLogger().severe(e.getMessage());
            }
        }
        return result;
    }

    /**
     * {@inheritDoc}
     *
     * @see de.rcenvironment.rce.configuration.PersistentSettingsService#saveMapWithStringList(java.util.Map, java.util.Map)
     */
    @Override
    public void saveMapWithStringList(Map<String, List<String>> map, String filename) {
        ObjectMapper mapper = new ObjectMapper();
        File f = new File(persistentSettingsDir + File.separator + filename);
        if (!f.exists()){
            new File(persistentSettingsDir).mkdirs();
        }
        if (f.exists()){
            try {
                FileUtils.copyFile(f, new File((persistentSettingsDir + File.separator + filename + ".bak")));
            } catch (IOException e) {
                Logger.getAnonymousLogger().warning("PersistentSettingsService: Could not create bak File.");
            }
        }

        // write JSON to a file
        try {
            mapper.writeValue(f, map);
        } catch (JsonGenerationException e) {
            Logger.getAnonymousLogger().severe(e.getMessage());
        } catch (JsonMappingException e) {
            Logger.getAnonymousLogger().severe(e.getMessage());
        } catch (IOException e) {
            Logger.getAnonymousLogger().severe(e.getMessage());
        }
    }



}
