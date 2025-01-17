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
package org.kie.trustyai.explainability.model;

import java.security.SecureRandom;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.kie.trustyai.explainability.utils.DataUtils;

/**
 * Feature distribution based on list of {@code Values}.
 */
public class GenericFeatureDistribution implements FeatureDistribution {

    private final Feature feature;
    private final List<Value> values;
    private final Random random;

    public GenericFeatureDistribution(Feature feature, List<Value> values) {
        this(feature, values, new SecureRandom());
    }

    public GenericFeatureDistribution(Feature feature, List<Value> values, Random random) {
        this.feature = feature;
        this.values = Collections.unmodifiableList(values);
        this.random = random;
    }

    @Override
    public Feature getFeature() {
        return feature;
    }

    @Override
    public Value sample() {
        if (values.isEmpty()) {
            return new Value(null);
        } else {
            List<Value> samples = sample(1);
            if (samples.isEmpty()) {
                return new Value(null);
            } else {
                return samples.get(0);
            }
        }
    }

    @Override
    public List<Value> sample(int sampleSize) {
        return DataUtils.sampleWithReplacement(values, sampleSize, random);
    }

    @Override
    public List<Value> getAllSamples() {
        List<Value> copy = new java.util.ArrayList<>(values);
        Collections.shuffle(copy, random);
        // Collections.shuffle(copy);
        return copy;
    }

    @Override
    public boolean isEmpty() {
        return values.isEmpty();
    }
}
