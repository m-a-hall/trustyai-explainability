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
package org.kie.kogito.explainability.local.lime;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.kie.kogito.explainability.TestUtils;
import org.kie.kogito.explainability.model.Feature;
import org.kie.kogito.explainability.model.Prediction;
import org.kie.kogito.explainability.model.PredictionInput;
import org.kie.kogito.explainability.model.PredictionOutput;
import org.kie.kogito.explainability.model.PredictionProvider;
import org.kie.kogito.explainability.model.Saliency;
import org.kie.kogito.explainability.utils.DataUtils;

import static org.junit.jupiter.api.Assertions.assertTrue;

class LimeStabilityTest {

    @BeforeAll
    static void setUpBefore() {
        DataUtils.setSeed(4);
    }

    @Test
    void testStabilityWithNumericData() {
        PredictionProvider sumSkipModel = TestUtils.getSumSkipModel(0);
        List<Feature> featureList = new LinkedList<>();
        for (int i = 0; i < 5; i++) {
            featureList.add(TestUtils.getMockedNumericFeature(i));
        }
        assertStable(sumSkipModel, featureList);
    }

    private void assertStable(PredictionProvider sumSkipModel, List<Feature> featureList) {
        PredictionInput input = new PredictionInput(featureList);
        List<PredictionOutput> predictionOutputs = sumSkipModel.predict(List.of(input));
        Prediction prediction = new Prediction(input, predictionOutputs.get(0));
        List<Saliency> saliencies = new LinkedList<>();
        LimeExplainer limeExplainer = new LimeExplainer(10, 1);
        for (int i = 0; i < 100; i++) {
            Saliency saliency = limeExplainer.explain(prediction, sumSkipModel);
            saliencies.add(saliency);
        }
        List<String> names = new LinkedList<>();
        saliencies.stream().map(s -> s.getPositiveFeatures(1)).forEach(f -> names.add(f.get(0).getFeature().getName()));
        Map<String, Long> frequencyMap = names.stream().collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        boolean topFeature = false;
        for (Map.Entry<String, Long> entry : frequencyMap.entrySet()) {
            if (entry.getValue() >= 0.9) {
                topFeature = true;
                break;
            }
        }
        assertTrue(topFeature);
    }

    @Test
    void testStabilityWithTextData() {
        PredictionProvider sumSkipModel = TestUtils.getDummyTextClassifier();
        List<Feature> featureList = new LinkedList<>();
        for (int i = 0; i < 4; i++) {
            featureList.add(TestUtils.getMockedTextFeature("foo "+i));
        }
        featureList.add(TestUtils.getMockedTextFeature("money"));
        assertStable(sumSkipModel, featureList);
    }
}
