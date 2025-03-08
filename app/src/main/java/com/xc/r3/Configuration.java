package com.xc.r3;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class Configuration implements Serializable {
    private Map<String, ModelConfiguration> models;

    public void setModels(Map<String, ModelConfiguration> models) {
        this.models = models;
    }

    public Map<String, ModelConfiguration> getModels() {
        return models;
    }

    public ModelConfiguration getModelConfiguration(String modelName) {
        return models.get(modelName);
    }
}