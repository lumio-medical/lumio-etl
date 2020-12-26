package com.lumiomedical.etl.extractor.ftp;

import com.lumiomedical.etl.logging.Logging;
import com.lumiomedical.flow.actor.extractor.ExtractionException;
import com.lumiomedical.flow.actor.extractor.Extractor;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * @author Pierre Lecerf (plecerf@lumiomedical.com)
 * Created on 2020/04/28
 */
public class FTPStreamer implements Extractor<InputStream>
{
    private final String host;
    private final int port;
    private final String user;
    private final String password;
    private final String path;

    /**
     *
     * @param host
     * @param port
     * @param user
     * @param password
     * @param path
     */
    public FTPStreamer(String host, int port, String user, String password, String path)
    {
        this.host = host;
        this.port = port;
        this.user = user;
        this.password = password;
        this.path = path;
    }

    @Override
    public InputStream extract() throws ExtractionException
    {
        try {
            String credentials = this.user + ":" + this.password;
            String location = this.host + ":" + this.port + "/" + this.path;

            String url = "ftp://" + credentials + "@" + location;

            Logging.logger.info("Initializing stream from FTP server at " + location);

            URLConnection conn = new URL(url).openConnection();

            return conn.getInputStream();
        }
        catch (IOException e) {
            throw new ExtractionException("An error occurred while attempting to perform an FTP extraction.", e);
        }
    }
}
