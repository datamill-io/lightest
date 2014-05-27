package com.googlecode.lightest.core

import com.googlecode.lightest.core.TestRegistry
import groovy.xml.StreamingMarkupBuilder
import java.text.SimpleDateFormat
import org.testng.ISuite
import org.testng.ITestResult
import org.testng.reporters.XMLReporterConfig
import org.testng.xml.XmlSuite

/**
 * This reporter creates XML files for each test method results containing task
 * information throughout the test run, and creates an HTML report at the end.*/
class DefaultDetailsReporter extends LightestReporter {
    /** the default stylesheet to the XML report detail files will reference */
    public static final String DEFAULT_STYLESHEET_REF = 'lightest-details.xsl'

    /** the className of the file to which the suite XML will be saved */
    public static final String SUITE_FILE_NAME = 'testng.xml'

    SimpleDateFormat dateFormatter
    String xmlStylesheetRef

    /**
     * Provides a hint to the stylesheet that the detailed message should be
     * rendered as preformatted text.*/
    boolean preformatDetailedMessage

    /**
     * the names of the IReporterInfoSource classes that will provide data for
     * the report details. This reporter will instantiate the info sources by
     * their class names, using the current Thread's context class loader*/
    List<String> infoSourceClassNames

    private AntBuilder ant

    DefaultDetailsReporter() {
        this(null)
    }

    /**
     * @param ant the AntBuilder that will be used to perform ant operations.
     *             This should be specified only for testing purposes; the
     *             default behavior is to create a new AntBuilder for each set
     *             of operations, which results in better memory behavior.
     */
    DefaultDetailsReporter(AntBuilder ant) {
        this.ant = ant
        this.dateFormatter = new SimpleDateFormat(XMLReporterConfig.FMT_DEFAULT)
        this.xmlStylesheetRef = DEFAULT_STYLESHEET_REF
        this.preformatDetailedMessage = true
        this.infoSourceClassNames = []

        setScheduled(false)
    }

    /**
     * Copies some resource files to the output directory. */
    @Override
    void generateBaseReport(TestRegistry testRegistry, String outputDir) {
        copyResources(outputDir)
    }

    private void copyResources(String outputDir) {
        def ant = this.ant ?: new AntBuilder()
        def resources = ['jquery-1.11.1.min.js',
                         'jquery.hoverIntent.js',
                         'lightest-report.css',
                         'lightest-details.js',
                         'lightest-details.xsl']

        ant.mkdir(dir: outputDir)

        for (resource in resources) {
            def stream = this.class.getResourceAsStream("/${resource}")
            def file = new File(outputDir, resource)

            ant.echo(message: stream.text, file: file.getCanonicalPath())
        }
    }

    /**
     * Copies the XML rendering of the suite to the appropriate location under
     * the output directory.*/
    @Override
    void updateReport(ISuite suite, TestRegistry testRegistry, String outputDir) {
        def suiteXML = suite.getXmlSuite().toXml()
        def suiteFile = new File(new File(outputDir), SUITE_FILE_NAME)
        def ant = this.ant ?: new AntBuilder()

        ant.mkdir(dir: outputDir)
        ant.echo(message: suiteXML, file: suiteFile.getCanonicalPath())
    }

    /**
     * Writes out a detail file.
     *
     * @param result
     * @param testRegistry
     * @param outputDir
     */
    @Override
    void updateReport(LightestTestResult result, TestRegistry testRegistry,
                      List<XmlSuite> xmlSuites, List<ISuite> suites, String outputDir) {
        createReportDetails(result, outputDir)
    }

    protected List<IReporterInfoSource> getInfoSources() {
        def infoSources = [new TestEnvironmentInfoSource()]
        def classLoader = Thread.currentThread().contextClassLoader

        infoSourceClassNames.each { className ->
            def infoSourceClass = Class.forName(className, true, classLoader)
            infoSources << infoSourceClass.newInstance()
        }

        return infoSources
    }

    private createReportDetails(LightestTestResult result, String outputDir) {
        def startTime = dateFormatter.format(result.startMillis)
        def endTime = dateFormatter.format(result.endMillis)

        def builder = new StreamingMarkupBuilder()
        def markup = {
            'test-result'(id: result.id, name: result.name, status: getStatusString(result.status),
                          'started-at': startTime, 'finished-at': endTime,
                          'preformat-detailed-message': (preformatDetailedMessage ? 'true' : 'false')) {
                'info-sources' {
                    for (infoSource in getInfoSources()) {
                        infoSource.configure(result)
                        "${infoSource.typeName}"(title: infoSource.title, header1: infoSource.headers[0],
                                                 header2: infoSource.headers[1]) {
                            for (entry in infoSource.data) {
                                "${entry.key}"(entry.value)
                            }
                        }
                    }
                }
                for (taskResult in result.taskResults) {
                    out << getTaskResultMarkup(taskResult)
                }
            }
        }

        def ant = this.ant ?: new AntBuilder()
        def detailsFile = new File(outputDir, "test-result-${result.id}.xml")
        def detailsText = ("<?xml-stylesheet type='text/xsl' href='$xmlStylesheetRef'?>" + builder.bind(markup))

        ant.echo(message: detailsText, file: detailsFile.getCanonicalPath())
    }

    protected Closure getTaskResultMarkup(ITaskResult taskResult) {
        def childResults = taskResult.children()
        def markup = {
            'task-result'(getTaskResultAttributes(taskResult)) {
                for (taskParam in taskResult.task.params) {
                    'param'(name: taskParam.key, value: taskParam.value)
                }
                if (taskResult.task.value) {
                    'value'(taskResult.task.value)
                }
                if (taskResult.detailedMessage) {
                    'detailed-message'(taskResult.detailedMessage)
                } else {
                    'detailed-message'()
                }
                for (resultLink in taskResult.links) {
                    'link'(href: resultLink.href, rel: resultLink.rel ?: "", title: resultLink.title ?: "",
                           resultLink.text)
                }
                if (childResults.size() > 0) {
                    'nested-results' {
                        for (childResult in childResults) {
                            out << getTaskResultMarkup(childResult)
                        }
                    }
                } else {
                    'nested-results'()
                }
            }
        }

        return markup
    }

    protected Map getTaskResultAttributes(ITaskResult taskResult) {
        def attrs = [:]
        def task = taskResult.getTask()

        def startTime = dateFormatter.format(taskResult.getStartTime())
        def endTime = dateFormatter.format(taskResult.getEndTime())
        def duration = "${taskResult.getEndTime() - taskResult.getStartTime()}"

        attrs.name = task.name
        attrs.description = task.description
        attrs.status = taskResult.status
        attrs.params = task.params.toString()
        attrs.message = taskResult.message
        attrs[XMLReporterConfig.ATTR_STARTED_AT] = startTime
        attrs[XMLReporterConfig.ATTR_FINISHED_AT] = endTime
        attrs[XMLReporterConfig.ATTR_DURATION_MS] = duration

        return attrs
    }

    protected String getStatusString(int testResultStatus) {
        switch (testResultStatus) {
            case ITestResult.SUCCESS:
                return "PASS"
            case ITestResult.FAILURE:
                return "FAIL"
            case ITestResult.SKIP:
                return "SKIP"
            case ITestResult.SUCCESS_PERCENTAGE_FAILURE:
                return "SUCCESS_PERCENTAGE_FAILURE"
        }
        return null
    }
}