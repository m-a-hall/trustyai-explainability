package org.kie.trustyai.explainability.model;

import java.time.LocalDateTime;

public class PredictionMetadata {

    private final LocalDateTime predictionTime;

    private final String id;

    private final String datapointTag;

    public PredictionMetadata(String id, LocalDateTime predictionTime) {
        this.id = id;
        this.predictionTime = predictionTime;
        this.datapointTag = "";
    }

    public PredictionMetadata(String id, LocalDateTime predictionTime, String datapointTag) {
        this.id = id;
        this.predictionTime = predictionTime;
        this.datapointTag = datapointTag;
    }

    public String getId() {
        return id;
    }

    public LocalDateTime getPredictionTime() {
        return predictionTime;
    }

    public String getDataPointTag() {
        return datapointTag;
    }
}
