/*
 * Copyright 2000-2015 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.codeInsight.completion;

import com.intellij.JavaTestUtil;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.compiler.chainsSearch.ChainRelevance;
import com.intellij.compiler.chainsSearch.completion.MethodsChainsCompletionContributor;
import com.intellij.compiler.chainsSearch.completion.lookup.ChainCompletionMethodCallLookupElement;
import com.intellij.compiler.chainsSearch.completion.lookup.WeightableChainLookupElement;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.util.registry.Registry;
import com.intellij.testFramework.SkipSlowTestLocally;
import com.intellij.util.SmartList;

import java.util.List;

/**
 * @author Dmitry Batkovich
 */
@SkipSlowTestLocally
public class MethodChainsCompletionTest extends AbstractCompilerAwareTest {
  private final static String TEST_INDEX_FILE_NAME = "TestIndex.java";
  private final static String TEST_COMPLETION_FILE_NAME = "TestCompletion.java";
  private final static String BEFORE_COMPLETION_FILE = "BeforeCompletion.java";
  private final static String AFTER_COMPLETION_FILE = "AfterCompletion.java";

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    installCompiler();
    Registry.get(MethodsChainsCompletionContributor.REGISTRY_KEY).setValue(true);
  }

  @Override
  protected void tearDown() throws Exception {
    try {
      Registry.get(MethodsChainsCompletionContributor.REGISTRY_KEY).setValue(false);
    } finally {
      super.tearDown();
    }
  }

  protected String getTestDataPath() {
    return JavaTestUtil.getJavaTestDataPath() + "/codeInsight/completion/methodChains/";
  }

  public void testOneRelevantMethod() {
    assertAdvisorLookupElementEquals("e.getProject", 0, 9, 1, 0, assertOneElement(doCompletion()));
  }

  public void testCyclingMethodsNotShowed() {
    assertEmpty(doCompletion());
  }

  public void testStaticMethod() {
    List<WeightableChainLookupElement> elements = doCompletion();
    assertSize(2, elements);
    assertAdvisorLookupElementEquals("getInstance", 0, 3, 1, 0, elements.get(0));
  }

  public void testStaticMethodAndMethod() {
    List<WeightableChainLookupElement> elements = doCompletion();
    assertEquals(String.valueOf(elements), 2, elements.size());
    assertAdvisorLookupElementEquals("findClass", 0, 4, 1, 1, elements.get(1));
    assertAdvisorLookupElementEquals("m.getContainingClass", 0, 6, 1, 0, elements.get(0));
  }

  public void testOneChainContainsOther() {
    assertAdvisorLookupElementEquals("p.getBaseDir", 0, 9, 1, 0, assertOneElement(doCompletion()));
  }

  public void testOneChainContainsOther2() {
    assertLookupElementStringEquals(assertOneElement(doCompletion()), "getManager");
  }

  public void testTwoVariablesWithOneTypeOrSuperType() {
    assertAdvisorLookupElementEquals("c.getProject", 0, 5, 1, 0, assertOneElement(doCompletion()));
  }

  public void testSuperClassMethodsCallings() {
    assertAdvisorLookupElementEquals("m.getProject", 0, 9, 1, 0, assertOneElement(doCompletion()));
  }

  public void testMethodsWithParametersInContext() {
    assertAdvisorLookupElementEquals("getInstance().findFile().findElementAt", 0, 5, 3, 0, assertOneElement(doCompletion()));
  }

  public void testChainsWithIndependentCallings() {
    assertOneElement(doCompletion());
  }

  public void _testMethodReturnsSubclassOfTargetClassShowed2() {
    assertOneElement(doCompletion());
  }

  public void _testResultsForSuperClassesShowed() {
    // if no other elements found we search by super classes
    assertOneElement(doCompletion());
  }

  public void _testInnerClasses() {
    assertAdvisorLookupElementEquals("j.getEntry", 0, 9, 1, 0, assertOneElement(doCompletion()));
  }

  public void testMethodsWithSameName() {
    assertAdvisorLookupElementEquals("f.createType", 1, 5, 1, 0, assertOneElement(doCompletion()));
  }

  public void testBigrams2() {
    List<WeightableChainLookupElement> collection = doCompletion();
    assertAdvisorLookupElementEquals("e.getContainingFile().getVirtualFile", 0, 9, 2, 0, assertOneElement(collection));
  }

  public void _testBigrams3() {
    List<WeightableChainLookupElement> elements = doCompletion();
    assertSize(1, elements);
    assertAdvisorLookupElementEquals("getInstance().findFile", 2, 9, 2, 0, elements.get(0));
  }

  public void testMethodWithNoQualifiedVariableInContext() {
    assertOneElement(doCompletion());
  }

  public void testMethodIsNotRelevantForField() {
    assertOneElement(doCompletion());
  }

  public void testNotRelevantMethodsFilteredInResult() {
    assertOneElement(doCompletion());
  }

  public void testGetterInContext() {
    assertAdvisorLookupElementEquals("getMyElement().getProject", 0, 9, 1, 0, assertOneElement(doCompletion()));
  }

  public void _testMethodParameterCompletion() {
    assertOneElement(doCompletion());
  }

  public void testNoWayToObtainVariableExplicitly() {
    assertOneElement(doCompletion());
  }

  public void _testCyclingInstancesObtaining() {
    assertEmpty(doCompletion());
  }

  public void testCyclingInstancesObtaining2() {
    assertOneElement(doCompletion());
  }

  public void testMethodsWithSameNameWithoutSameParent() {
    assertSize(2, doCompletion());
  }

  public void testResultQualifierNotSameWithTarget() {
    assertEmpty(doCompletion());
  }

  public void testResultOrdering() {
    List<WeightableChainLookupElement> lookupElements = doCompletion();
    assertSize(4, lookupElements);
    assertLookupElementStringEquals(lookupElements.get(0), "f.createFileFromText");
    assertLookupElementStringEquals(lookupElements.get(1), "getInstance().findFile");
    assertLookupElementStringEquals(lookupElements.get(2), "getInstance().getPsiFile");
    assertLookupElementStringEquals(lookupElements.get(3), "getContainingClass");
  }

  public void testResultRelevance() {
    List<WeightableChainLookupElement> weightableChainLookupElements = doCompletion();
    assertSize(1, weightableChainLookupElements);
    //assertEquals("e.getContainingClass", weightableChainLookupElements.get(0).getLookupString());
    assertEquals("getInstance().findClass", weightableChainLookupElements.get(0).getLookupString());
  }

  public void testResultRelevance3() {
    List<WeightableChainLookupElement> weightableChainLookupElements = doCompletion();
    assertSize(2, weightableChainLookupElements);
    assertEquals("e.getProject1", weightableChainLookupElements.get(0).getLookupString());
    assertEquals("getProject", weightableChainLookupElements.get(1).getLookupString());
  }

  public void testRenderingVariableInContextAndNotInContext() {
    doTestRendering();
  }

  public void testRenderingStaticMethods() {
    doTestRendering();
  }

  public void testRenderingIntroduceVariable() {
    doTestRendering();
  }

  public void testMethodQualifierClass() {
    doTestRendering();
  }

  public void testArray() {
    WeightableChainLookupElement element = assertOneElement(doCompletion());
    assertEquals("c.getMethods", element.getLookupString());
  }

  public void assertAdvisorLookupElementEquals(String lookupText,
                                               int unreachableParametersCount,
                                               int lastMethodWeight,
                                               int chainSize,
                                               int notMatchedStringVars,
                                               WeightableChainLookupElement actualLookupElement) {
    assertLookupElementStringEquals(actualLookupElement, lookupText);
    assertChainRelevanceEquals(actualLookupElement.getChainRelevance(), lastMethodWeight, chainSize, notMatchedStringVars,
                               unreachableParametersCount);
  }

  private static void assertLookupElementStringEquals(LookupElement lookupElement, String lookupText) {
    assertEquals(lookupText, lookupElement.getLookupString());
  }

  private static void assertChainRelevanceEquals(ChainRelevance chainRelevance,
                                                 int lastMethodWeight,
                                                 int chainSize,
                                                 int notMatchedStringVars,
                                                 int unreachableParametersCount) {
    assertEquals(notMatchedStringVars, chainRelevance.getNotMatchedStringVars());
    assertEquals(chainSize, chainRelevance.getChainSize());
    assertEquals(unreachableParametersCount, chainRelevance.getUnreachableParametersCount());
    assertEquals(lastMethodWeight, chainRelevance.getLastMethodOccurrences());
  }

  private void doTestRendering() {
    PropertiesComponent.getInstance(getProject())
      .setValue(ChainCompletionMethodCallLookupElement.PROP_METHODS_CHAIN_COMPLETION_AUTO_COMPLETION, String.valueOf(true));
    compileAndIndexData(TEST_INDEX_FILE_NAME);
    myFixture.configureByFiles(getBeforeCompletionFilePath());
    myFixture.complete(CompletionType.BASIC);
    PropertiesComponent.getInstance(getProject())
      .setValue(ChainCompletionMethodCallLookupElement.PROP_METHODS_CHAIN_COMPLETION_AUTO_COMPLETION, String.valueOf(false));
    myFixture.checkResultByFile(getAfterCompletionFilePath());
  }

  private List<WeightableChainLookupElement> doCompletion() {
    compileAndIndexData(TEST_INDEX_FILE_NAME);
    LookupElement[] allLookupElements = runCompletion();
    List<WeightableChainLookupElement> targetLookupElements = new SmartList<>();
    for (LookupElement lookupElement : allLookupElements) {
      if (lookupElement instanceof WeightableChainLookupElement) {
        targetLookupElements.add((WeightableChainLookupElement)lookupElement);
      }
    }
    return targetLookupElements;
  }

  private LookupElement[] runCompletion() {
    myFixture.configureByFiles(getTestCompletionFilePath());
    LookupElement[] lookupElements =
      myFixture.complete(CompletionType.BASIC);
    return lookupElements == null ? LookupElement.EMPTY_ARRAY : lookupElements;
  }

  private String getTestCompletionFilePath() {
    return getName() + "/" + TEST_COMPLETION_FILE_NAME;
  }

  private String getBeforeCompletionFilePath() {
    return getName() + "/" + BEFORE_COMPLETION_FILE;
  }

  private String getAfterCompletionFilePath() {
    return getName() + "/" + AFTER_COMPLETION_FILE;
  }
}
