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
public class HttpStreamer implements Extractor<HttpResponse<InputStream>>
{
    private final HttpClient client;
    private final HttpRequest request;

    /**
     *
     * @param client
     * @param request
     */
    public HttpStreamer(HttpClient client, HttpRequest request)
    {
        this.client = client;
        this.request = request;
    }

    /**
     *
     * @param request
     */
    public HttpStreamer(HttpRequest request)
    {
        this(HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.ALWAYS)
        .build(), request);
    }

    @Override
    public HttpResponse<InputStream> extract() throws ExtractionException
    {
        try {
            Logging.logger.info("Initializing stream from HTTP resource at " + this.request.uri());
            return this.client.send(this.request, HttpResponse.BodyHandlers.ofInputStream());
        }
        catch (InterruptedException | IOException e) {
            throw new ExtractionException("An error occurred while attempting to send the request.", e);
        }
    }
}
