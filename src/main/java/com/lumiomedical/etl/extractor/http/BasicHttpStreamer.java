package com.lumiomedical.etl.extractor.http;

import com.lumiomedical.etl.logging.Logging;
import com.lumiomedical.flow.actor.extractor.ExtractionException;
import com.lumiomedical.flow.actor.extractor.Extractor;

import java.io.IOException;
import java.io.InputStream;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * @author Pierre Lecerf (plecerf@lumiomedical.com)
 * Created on 2020/12/17
 */
public class BasicHttpStreamer implements Extractor<InputStream>
{
    private final HttpClient client;
    private final HttpRequest request;

    /**
     *
     * @param client
     * @param request
     */
    public BasicHttpStreamer(HttpClient client, HttpRequest request)
    {
        this.client = client;
        this.request = request;
    }

    /**
     *
     * @param request
     */
    public BasicHttpStreamer(HttpRequest request)
    {
        this(HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.ALWAYS)
            .build(), request);
    }

    @Override
    public InputStream extract() throws ExtractionException
    {
        try {
            Logging.logger.info("Initializing stream from HTTP resource at " + this.request.uri());
            HttpResponse<InputStream> response = this.client.send(this.request, HttpResponse.BodyHandlers.ofInputStream());

            if (response.statusCode() < 200 || response.statusCode() >= 300)
                throw new ExtractionException("The server responded with a non-successful status code: "+response.statusCode());

            return response.body();
        }
        catch (InterruptedException | IOException e) {
            throw new ExtractionException("An error occurred while attempting to send the request.", e);
        }
    }
}
