package com.lumiomedical.etl.utility.env;

import java.util.Map;
import java.util.Properties;

/**
 * @author Thomas Walter (twalter@lumiomedical.com)
 * Created on 2020/03/10
 */
public final class Environment
{
    private Environment() {}

    /**
     * Load variables environments into a properties object
     *
     * @param variablesMapping Mapping from the variable environment to the property key
     * @return A mapping between the given mapping keys with the corresponding variable environment values
     * @throws EnvironmentLoadingException Thrown whenever an environment variable is not set
     */
    public static Properties loadProperties(Map<String, String> variablesMapping) throws EnvironmentLoadingException
    {
        Map<String, String> env = System.getenv();
        var properties = new Properties();

        for (var entry : variablesMapping.entrySet())
        {
            if (!env.containsKey(entry.getKey()) || env.get(entry.getKey()).isEmpty())
                throw new EnvironmentLoadingException("Environment variable '" + entry.getKey() + "' is not declared or empty and is mandatory.");

            properties.setProperty(entry.getValue(), env.get(entry.getKey()));
        }

        return properties;
    }
}
