package com.googlecode.lightest.core

import com.googlecode.lightest.core.TestRegistry
import org.testng.IReporter
import org.testng.ISuite
import org.testng.xml.XmlSuite

/**
 * This reporter creates XML files for each test method results containing task
 * information throughout the test run, and creates an HTML report at the end.*/
class DefaultSummaryReporter extends LightestReporter implements IReporter {
    /** the className of the file to which the configuration text will be saved */
    public static final String CONFIG_FILE_NAME = 'run.config'

    /** the className of the results XML to which the transform will be applied */
    public static final String RESULTS_FILE_NAME = 'testng-results.xml'

    private CustomizedTransformer transformer
    private AntBuilder ant

    DefaultSummaryReporter() {
        this(new LightestTransformer(), new AntBuilder())
    }

    DefaultSummaryReporter(CustomizedTransformer transformer, AntBuilder ant) {
        this.transformer = transformer
        this.ant = ant

        setScheduled(true)
    }

    /**
     * Copies some resource files to the output directory. If configText has
     * been set, it is written to a file named CONFIG_FILE_NAME. */
    @Override
    void generateBaseReport(TestRegistry testRegistry, String outputDir) {
        if (configText) {
            def configFile = new File(outputDir, CONFIG_FILE_NAME)

            ant.mkdir(dir: outputDir)
            ant.echo(message: configText, file: configFile.getCanonicalPath())
        }

        copyResources(outputDir)
    }

    private void copyResources(String outputDir) {
        def resources = ['jquery-1.11.1.min.js',
                         'lightest-base.js',
                         'blank.html']

        ant.mkdir(dir: outputDir)

        for (resource in resources) {
            def stream = this.class.getResourceAsStream("/${resource}")
            def file = new File(outputDir, resource)

            ant.echo(message: stream.text, file: file.getCanonicalPath())
        }
    }

    /**
     * Writes out the report summary.
     *
     * @param result
     * @param testRegistry
     * @param outputDir
     */
    @Override
    void updateReport(ISuite suite, TestRegistry testRegistry, String outputDir) {
        createReportSummary(outputDir)
    }

    private synchronized void createReportSummary(String outputDir) {

        def testngResultsFile = new File(outputDir, RESULTS_FILE_NAME)

        if (!testngResultsFile.exists()) {
            println 'No report summary to generate.'
            return
        }

        def is = new FileInputStream(testngResultsFile)
        def os = new FileOutputStream(new File(outputDir, 'index.html'))

        transformer.setParameter('testNgXslt.outputDir', outputDir)
        transformer.setParameter('testNgXslt.reportTitle', 'Lightest Results')
        transformer.setParameter('testNgXslt.chartScaleFactor', 0.75)
        transformer.transform(is, os)

        is.close()
        os.close()
    }

    /**
     * Creates the final report summary.
     *
     * @param xmlSuites
     * @param suites
     * @param outputDirectory
     */
    void generateReport(List<XmlSuite> xmlSuites, List<ISuite> suites,
                        String outputDirectory) {
        createReportSummary(outputDirectory)
    }
}