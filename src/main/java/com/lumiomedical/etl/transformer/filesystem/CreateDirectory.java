package com.lumiomedical.etl.transformer.filesystem;

import com.lumiomedical.etl.logging.Logging;
import com.lumiomedical.flow.actor.transformer.TransformationException;
import com.lumiomedical.flow.actor.transformer.Transformer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Function;

/**
 * @author Pierre Lecerf (plecerf@lumiomedical.com)
 * Created on 2020/12/22
 */
public class CreateDirectory <I> implements Transformer<I, I>
{
    private final Path path;
    private final Function<I, Path> pathCreator;

    public CreateDirectory(Path path)
    {
        this.path = path;
        this.pathCreator = null;
    }

    public CreateDirectory(String path)
    {
        this(Path.of(path));
    }

    public CreateDirectory(Function<I, Path> pathCreator)
    {
        this.path = null;
        this.pathCreator = pathCreator;
    }

    @Override
    public I transform(I input) throws TransformationException
    {
        try {
            Path path = this.path != null
                ? this.path
                : this.pathCreator.apply(input)
            ;

            if (!Files.exists(path))
            {
                Logging.logger.info("Initializing stream from filesystem at " + path);
                Files.createDirectory(path);
            }
            else if (Files.isDirectory(path))
                Logging.logger.info("Directory already exists at " + path.toString());
            else
                Logging.logger.info("A file already exists at " + path.toString());

            return input;
        }
        catch (IOException e) {
            throw new TransformationException(e.getMessage(), e);
        }
    }
}
