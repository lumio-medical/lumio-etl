package com.lumiomedical.etl.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Pierre Lecerf (plecerf@lumiomedical.com)
 * Created on 2020/06/25
 */
public final class Logging
{
    private static final String packageName = "com.lumiomedical.etl";
    public static final Logger logger = LoggerFactory.getLogger(packageName);

    /**
     *
     * @param subpackage
     * @return
     */
    public static Logger logger(String subpackage)
    {
        return LoggerFactory.getLogger(packageName + "." + subpackage);
    }
}
