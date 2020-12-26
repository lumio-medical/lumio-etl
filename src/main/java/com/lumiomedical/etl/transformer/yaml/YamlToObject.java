package com.lumiomedical.etl.transformer.yaml;

import com.fasterxml.jackson.databind.JsonNode;
import com.lumiomedical.flow.actor.transformer.TransformationException;
import com.lumiomedical.flow.actor.transformer.Transformer;
import com.noleme.json.JsonException;
import com.noleme.json.Yaml;

/**
 * @author Pierre Lecerf (plecerf@lumiomedical.com)
 * Created on 2020/12/26
 */
public class YamlToObject<T> implements Transformer<JsonNode, T>
{
    private final Class<T> type;

    /**
     *
     * @param type
     */
    public YamlToObject(Class<T> type)
    {
        this.type = type;
    }

    @Override
    public T transform(JsonNode node) throws TransformationException
    {
        try {
            return Yaml.fromYaml(node, type);
        }
        catch (JsonException e) {
            throw new TransformationException(e.getMessage(), e);
        }
    }
}
