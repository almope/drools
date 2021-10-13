/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.pmml.compiler.commons.implementations;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.dmg.pmml.DataDictionary;
import org.dmg.pmml.DataField;
import org.dmg.pmml.Field;
import org.dmg.pmml.MiningSchema;
import org.dmg.pmml.Model;
import org.dmg.pmml.Output;
import org.dmg.pmml.PMML;
import org.dmg.pmml.mining.MiningModel;
import org.junit.Test;
import org.kie.pmml.api.enums.MINING_FUNCTION;
import org.kie.pmml.commons.model.KiePMMLModel;
import org.kie.pmml.commons.testingutility.KiePMMLTestingModel;
import org.kie.pmml.compiler.api.dto.CommonCompilationDTO;
import org.kie.pmml.compiler.api.mocks.TestModel;
import org.kie.pmml.compiler.commons.mocks.HasClassLoaderMock;
import org.kie.pmml.compiler.commons.utils.KiePMMLUtil;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.kie.pmml.commons.Constants.PACKAGE_NAME;
import static org.kie.pmml.compiler.api.CommonTestingUtils.getFieldsFromDataDictionary;
import static org.kie.pmml.compiler.api.testutils.PMMLModelTestUtils.getDataField;
import static org.kie.pmml.compiler.api.testutils.PMMLModelTestUtils.getPMMLWithMiningRandomTestModel;
import static org.kie.pmml.compiler.api.testutils.PMMLModelTestUtils.getPMMLWithRandomTestModel;
import static org.kie.pmml.compiler.api.testutils.PMMLModelTestUtils.getRandomDataType;
import static org.kie.pmml.compiler.api.testutils.PMMLModelTestUtils.getRandomMiningSchema;
import static org.kie.pmml.compiler.api.testutils.PMMLModelTestUtils.getRandomOutput;
import static org.kie.pmml.compiler.commons.implementations.KiePMMLModelRetriever.getFromCommonDataAndTransformationDictionaryAndModel;
import static org.kie.pmml.compiler.commons.implementations.KiePMMLModelRetriever.getFromCommonDataAndTransformationDictionaryAndModelWithSources;
import static org.kie.pmml.compiler.commons.implementations.KiePMMLModelRetriever.getFromCommonDataAndTransformationDictionaryAndModelWithSourcesCompiled;
import static org.kie.test.util.filesystem.FileUtils.getFileInputStream;

public class KiePMMLModelRetrieverTest {

    private static final String MULTIPLE_TARGETS_SOURCE = "MultipleTargetsFieldSample.pmml";
    private static final String ONE_MINING_TARGET_SOURCE = "OneMiningTargetFieldSample.pmml";
    private static final String MINING_MODEL_WITH_NESTED_REFERS_SOURCE = "MiningWithNestedRefers.pmml";
    private PMML pmml;

    @Test
    public void getFromCommonDataAndTransformationDictionaryAndModelWithProvider() throws Exception {
        pmml = KiePMMLUtil.load(getFileInputStream(MULTIPLE_TARGETS_SOURCE), MULTIPLE_TARGETS_SOURCE);
        TestModel model = new TestModel();
        pmml.getModels().set(0, model);
        final CommonCompilationDTO<TestModel> compilationDTO = new CommonCompilationDTO<>(PACKAGE_NAME,
                                                                                          pmml,
                                                                                          model,
                                                                                          new HasClassLoaderMock());
        final Optional<KiePMMLModel> retrieved = getFromCommonDataAndTransformationDictionaryAndModel(compilationDTO);
        assertNotNull(retrieved);
        assertTrue(retrieved.isPresent());
        assertTrue(retrieved.get() instanceof KiePMMLTestingModel);
    }

    @Test
    public void getFromCommonDataAndTransformationDictionaryAndModelWithoutProvider() throws Exception {
        pmml = KiePMMLUtil.load(getFileInputStream(ONE_MINING_TARGET_SOURCE), ONE_MINING_TARGET_SOURCE);
        final CommonCompilationDTO compilationDTO = new CommonCompilationDTO(PACKAGE_NAME,
                                                                             pmml,
                                                                             pmml.getModels().get(0),
                                                                             new HasClassLoaderMock());
        final Optional<KiePMMLModel> retrieved = getFromCommonDataAndTransformationDictionaryAndModel(compilationDTO);
        assertNotNull(retrieved);
        assertFalse(retrieved.isPresent());
    }

    @Test
    public void getFromCommonDataAndTransformationDictionaryAndModelWithSourcesWithProvider() {
        pmml = getPMMLWithRandomTestModel();
        final CommonCompilationDTO compilationDTO = new CommonCompilationDTO(PACKAGE_NAME,
                                                                             pmml,
                                                                             pmml.getModels().get(0),
                                                                             new HasClassLoaderMock());
        final Optional<KiePMMLModel> retrieved =
                getFromCommonDataAndTransformationDictionaryAndModelWithSources(compilationDTO);
        assertNotNull(retrieved);
    }

    @Test
    public void getFromDataDictionaryAndModelWithSourcesWithoutProvider() throws Exception {
        pmml = KiePMMLUtil.load(getFileInputStream(ONE_MINING_TARGET_SOURCE), ONE_MINING_TARGET_SOURCE);
        final CommonCompilationDTO compilationDTO = new CommonCompilationDTO(PACKAGE_NAME,
                                                                             pmml,
                                                                             pmml.getModels().get(0),
                                                                             new HasClassLoaderMock());
        final Optional<KiePMMLModel> retrieved =
                getFromCommonDataAndTransformationDictionaryAndModelWithSources(compilationDTO);
        assertNotNull(retrieved);
        assertFalse(retrieved.isPresent());
    }

    @Test
    public void getFromCommonDataAndTransformationDictionaryAndModelWithSourcesCompiledWithProvider() throws Exception {
        pmml = getPMMLWithMiningRandomTestModel();
        MiningModel parentModel = (MiningModel) pmml.getModels().get(0);
        Model model = parentModel.getSegmentation().getSegments().get(0).getModel();
        final CommonCompilationDTO compilationDTO = new CommonCompilationDTO(PACKAGE_NAME,
                                                                             pmml,
                                                                             model,
                                                                             new HasClassLoaderMock());
        final Optional<KiePMMLModel> retrieved =
                getFromCommonDataAndTransformationDictionaryAndModelWithSourcesCompiled(compilationDTO);
        assertNotNull(retrieved);
        assertTrue(retrieved.isPresent());
    }

    @Test
    public void getFromCommonDataAndTransformationDictionaryAndModelWithSourcesCompiledWithoutProvider() throws Exception {
        pmml = KiePMMLUtil.load(getFileInputStream(MINING_MODEL_WITH_NESTED_REFERS_SOURCE),
                                MINING_MODEL_WITH_NESTED_REFERS_SOURCE);
        MiningModel parentModel = (MiningModel) pmml.getModels().get(0);
        Model model = parentModel.getSegmentation().getSegments().get(0).getModel();
        final CommonCompilationDTO compilationDTO = new CommonCompilationDTO(PACKAGE_NAME,
                                                                             pmml,
                                                                             model,
                                                                             new HasClassLoaderMock());
        final Optional<KiePMMLModel> retrieved =
                getFromCommonDataAndTransformationDictionaryAndModelWithSourcesCompiled(compilationDTO);
        assertNotNull(retrieved);
        assertFalse(retrieved.isPresent());
    }

    @Test
    public void getPopulatedWithPMMLModelFields() {
        KiePMMLTestingModel toPopulate = KiePMMLTestingModel.builder("TESTINGMODEL",
                                                                     Collections.emptyList(),
                                                                     MINING_FUNCTION.REGRESSION).build();
        assertTrue(toPopulate.getMiningFields().isEmpty());
        assertTrue(toPopulate.getOutputFields().isEmpty());
        final MiningSchema miningSchema = getRandomMiningSchema();
        DataDictionary dataDictionary = new DataDictionary();
        dataDictionary.addDataFields(miningSchema.getMiningFields().stream()
                                             .map(miningField -> getDataField(miningField.getName().getValue(),
                                                                              miningField.getOpType(),
                                                                              getRandomDataType())).toArray(DataField[]::new));
        final Output output = getRandomOutput();
        final List<Field<?>> fields = getFieldsFromDataDictionary(dataDictionary);
        KiePMMLTestingModel populated =
                (KiePMMLTestingModel) KiePMMLModelRetriever.getPopulatedWithPMMLModelFields(toPopulate,
                                                                                            fields,
                                                                                            miningSchema, output);
        assertEquals(miningSchema.getMiningFields().size(), populated.getMiningFields().size());
        assertEquals(output.getOutputFields().size(), populated.getOutputFields().size());
        toPopulate = KiePMMLTestingModel.builder("TESTINGMODEL",
                                                 Collections.emptyList(),
                                                 MINING_FUNCTION.REGRESSION).build();
        populated = (KiePMMLTestingModel) KiePMMLModelRetriever.getPopulatedWithPMMLModelFields(toPopulate,
                                                                                                fields,
                                                                                                miningSchema, null);
        assertEquals(miningSchema.getMiningFields().size(), populated.getMiningFields().size());
        assertTrue(populated.getOutputFields().isEmpty());
        toPopulate = KiePMMLTestingModel.builder("TESTINGMODEL",
                                                 Collections.emptyList(),
                                                 MINING_FUNCTION.REGRESSION).build();
        populated = (KiePMMLTestingModel) KiePMMLModelRetriever.getPopulatedWithPMMLModelFields(toPopulate,
                                                                                                fields, null,
                                                                                                output);
        assertTrue(populated.getMiningFields().isEmpty());
        assertEquals(output.getOutputFields().size(), populated.getOutputFields().size());
        toPopulate = KiePMMLTestingModel.builder("TESTINGMODEL",
                                                 Collections.emptyList(),
                                                 MINING_FUNCTION.REGRESSION).build();
        populated = (KiePMMLTestingModel) KiePMMLModelRetriever.getPopulatedWithPMMLModelFields(toPopulate,
                                                                                                fields, null,
                                                                                                null);
        assertTrue(populated.getMiningFields().isEmpty());
        assertTrue(populated.getOutputFields().isEmpty());
    }
}