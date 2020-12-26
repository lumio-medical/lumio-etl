package com.lumiomedical.etl.extractor.aws;

import com.amazonaws.services.s3.AmazonS3;
import com.lumiomedical.etl.logging.Logging;
import com.lumiomedical.etl.utility.aws.S3;
import com.lumiomedical.flow.actor.extractor.Extractor;

import java.io.InputStream;

/**
 * @author Pierre Lecerf (plecerf@lumiomedical.com)
 * Created on 2020/02/26
 */
public class AmazonS3Streamer implements Extractor<InputStream>
{
    private final AmazonS3 s3;
    private final String bucket;
    private final String fileName;

    /**
     *
     * @param s3
     * @param bucket
     * @param fileName
     */
    public AmazonS3Streamer(AmazonS3 s3, String bucket, String fileName)
    {
        this.s3 = s3;
        this.bucket = bucket;
        this.fileName = fileName;
    }

    @Override
    public InputStream extract()
    {
        Logging.logger.info("Initializing stream from S3 bucket " + this.bucket + " and key " + this.fileName);
        return S3.getStream(this.s3, this.bucket, this.fileName);
    }
}
