package com.lumiomedical.etl.transformer.http;

import com.lumiomedical.etl.logging.Logging;
import com.lumiomedical.flow.actor.transformer.TransformationException;
import com.lumiomedical.flow.actor.transformer.Transformer;

import java.io.IOException;
import java.io.InputStream;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * @author Pierre Lecerf (plecerf@lumiomedical.com)
 * Created on 2020/12/17
 */
public class BasicHttpClientTransformer implements Transformer<HttpRequest, InputStream>
{
    private final HttpClient client;

    /**
     *
     * @param client
     */
    public BasicHttpClientTransformer(HttpClient client)
    {
        this.client = client;
    }

    /**
     *
     */
    public BasicHttpClientTransformer()
    {
        this(HttpClient.newBuilder().followRedirects(HttpClient.Redirect.ALWAYS)
        .build());
    }

    @Override
    public InputStream transform(HttpRequest request) throws TransformationException
    {
        try {
            Logging.logger.info("Initializing stream from HTTP resource at " + request.uri());
            HttpResponse<InputStream> response = this.client.send(request, HttpResponse.BodyHandlers.ofInputStream());

            if (response.statusCode() < 200 || response.statusCode() >= 300)
                throw new TransformationException("The server responded with a non-successful status code: "+response.statusCode());

            return response.body();
        }
        catch (InterruptedException | IOException e) {
            throw new TransformationException("An error occurred while attempting to send the request.", e);
        }
    }
}
