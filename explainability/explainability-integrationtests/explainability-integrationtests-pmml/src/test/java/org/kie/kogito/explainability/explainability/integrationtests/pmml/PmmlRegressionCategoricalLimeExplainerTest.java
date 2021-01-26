/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kie.kogito.explainability.explainability.integrationtests.pmml;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.kie.api.pmml.PMML4Result;
import org.kie.kogito.explainability.Config;
import org.kie.kogito.explainability.local.lime.LimeConfig;
import org.kie.kogito.explainability.local.lime.LimeExplainer;
import org.kie.kogito.explainability.model.Feature;
import org.kie.kogito.explainability.model.FeatureFactory;
import org.kie.kogito.explainability.model.Output;
import org.kie.kogito.explainability.model.PerturbationContext;
import org.kie.kogito.explainability.model.Prediction;
import org.kie.kogito.explainability.model.PredictionInput;
import org.kie.kogito.explainability.model.PredictionOutput;
import org.kie.kogito.explainability.model.PredictionProvider;
import org.kie.kogito.explainability.model.Saliency;
import org.kie.kogito.explainability.model.Type;
import org.kie.kogito.explainability.model.Value;
import org.kie.kogito.explainability.utils.ExplainabilityMetrics;
import org.kie.kogito.explainability.utils.ValidationUtils;
import org.kie.pmml.api.runtime.PMMLRuntime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.kie.pmml.evaluator.assembler.factories.PMMLRuntimeFactoryInternal.getPMMLRuntime;

class PmmlRegressionCategoricalLimeExplainerTest {

    private static PMMLRuntime categoricalVariableRegressionRuntime;

    @BeforeAll
    static void setUpBefore() throws URISyntaxException {
        categoricalVariableRegressionRuntime = getPMMLRuntime(ResourceReaderUtils.getResourceAsFile("categoricalvariablesregression/categoricalVariablesRegression.pmml"));
        Config.INSTANCE.setAsyncTimeout(5000);
        Config.INSTANCE.setAsyncTimeUnit(TimeUnit.MILLISECONDS);
    }

    @Test
    void testPMMLRegressionCategorical() throws Exception {
        List<Feature> features = new ArrayList<>();
        features.add(FeatureFactory.newCategoricalFeature("mapX", "red"));
        features.add(FeatureFactory.newCategoricalFeature("mapY", "classB"));
        PredictionInput input = new PredictionInput(features);

        Random random = new Random();
        random.setSeed(4);
        LimeConfig limeConfig = new LimeConfig()
                .withSamples(500)
                .withPerturbationContext(new PerturbationContext(random, 1));
        LimeExplainer limeExplainer = new LimeExplainer(limeConfig);
        PredictionProvider model = inputs -> CompletableFuture.supplyAsync(() -> {
            List<PredictionOutput> outputs = new ArrayList<>();
            for (PredictionInput input1 : inputs) {
                List<Feature> features1 = input1.getFeatures();
                CategoricalVariablesRegressionExecutor pmmlModel = new CategoricalVariablesRegressionExecutor(
                        features1.get(0).getValue().asString(), features1.get(1).getValue().asString());
                PMML4Result result = pmmlModel.execute(categoricalVariableRegressionRuntime);
                String score = result.getResultVariables().get("result").toString();
                PredictionOutput predictionOutput = new PredictionOutput(List.of(new Output("result", Type.NUMBER, new Value<>(score), 1d)));
                outputs.add(predictionOutput);
            }
            return outputs;
        });
        List<PredictionOutput> predictionOutputs = model.predictAsync(List.of(input))
                .get(Config.INSTANCE.getAsyncTimeout(), Config.INSTANCE.getAsyncTimeUnit());
        assertThat(predictionOutputs).isNotNull();
        assertThat(predictionOutputs).isNotEmpty();
        PredictionOutput output = predictionOutputs.get(0);
        assertThat(output).isNotNull();
        Prediction prediction = new Prediction(input, output);
        Map<String, Saliency> saliencyMap = limeExplainer.explainAsync(prediction, model)
                .get(Config.INSTANCE.getAsyncTimeout(), Config.INSTANCE.getAsyncTimeUnit());
        for (Saliency saliency : saliencyMap.values()) {
            assertThat(saliency).isNotNull();
            double v = ExplainabilityMetrics.impactScore(model, prediction, saliency.getTopFeatures(2));
            assertThat(v).isEqualTo(1d);
        }
        assertDoesNotThrow(() -> ValidationUtils.validateLocalSaliencyStability(model, prediction, limeExplainer, 1,
                                                                                0.5, 0.5));
    }
}
