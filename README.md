# entities-linker

This project is based on this framework: https://github.com/marcocor/query-annotator-stub. Details could be find in the report.pdf.

## How to run this code
1. import this maven project to Eclipse
2. use GeneFeatures.GenerateFeatures() generate training data
3. train binary classifier
4. use GeneFeatures.TestDataSet() generate test data
5. apply the trained classifier on the test data
6. show result: BenchmarkMain calls newAnnotator4.java

## project hierarchy
Its hierarchy is almostly same as framework (https://github.com/marcocor/query-annotator-stub). Important changes include:
- [newAnnotator4.java](src/main/java/annotatorstub/annotator/newAnnotator4.java) disambigulate candidate set based on the result of the trained classifier.
- [EmbeddingHelper.java](src/main/java/annotatorstub/utils/EmbeddingHelper.java) is used to construct contextual features using word embedding.
- [GeneFeatures.java](src/main/java/annotatorstub/utils/GeneFeatures.java) is used to generate features (with labels) on training and test data set.

