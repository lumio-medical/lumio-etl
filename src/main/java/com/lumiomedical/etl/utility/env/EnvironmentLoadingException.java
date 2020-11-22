package com.lumiomedical.etl.utility.env;

/**
 * @author Thomas Walter (twalter@lumiomedical.com)
 * Created on 2020/03/10
 */
public class EnvironmentLoadingException extends Exception
{
    public EnvironmentLoadingException(String message)
    {
        super(message);
    }

    public EnvironmentLoadingException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
