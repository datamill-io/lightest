package com.googlecode.lightest.core

import groovy.xml.DOMBuilder
import groovy.xml.XmlUtil

import javax.xml.namespace.NamespaceContext
import javax.xml.xpath.XPathConstants
import javax.xml.xpath.XPathFactory

import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.NodeList

/**
 * The Lightest customizations to the TestNG XSLT report transforms.*/
class LightestTransformer extends CustomizedTransformer {
    public static final String XSL_NS = 'http://www.w3.org/1999/XSL/Transform'
    public static final String SVG_NS = 'http://www.w3.org/2000/svg'

    protected DOMBuilder builder

    private NamespaceContext namespaceContext
    private XPathFactory xpathFactory

    LightestTransformer() {
        namespaceContext = createNamespaceContext()
        xpathFactory = XPathFactory.newInstance()
    }

    InputStream getTransformAsStream() {
        return this.class.getResourceAsStream('/light-testng-results.xsl')
    }

    private NamespaceContext createNamespaceContext() {
        return [getNamespaceURI: { String prefix ->
            if (prefix == 'xsl') {
                return XSL_NS
            }
            if (prefix == 'svg') {
                return SVG_NS
            }
        },
            getPrefix      : { String namespaceURI ->
                if (namespaceURI == XSL_NS) {
                    return 'xsl'
                }
                if (namespaceURI == SVG_NS) {
                    return 'svg'
                }
            },
            getPrefixes    : { String namespaceURI ->
                if (namespaceURI == XSL_NS) {
                    return ['xsl'].iterator()
                }
                if (namespaceURI == SVG_NS) {
                    return ['svg'].iterator()
                }
            }] as NamespaceContext
    }

    protected List<org.w3c.dom.Node> eval(String xpathExpr, Object context) {
        def xpath = xpathFactory.newXPath()

        xpath.setNamespaceContext(namespaceContext)

        def result = xpath.evaluate(xpathExpr, context, XPathConstants.NODESET)
        def list = []

        for (i in 0..<result.getLength()) {
            list << result.item(i)
        }

        return list
    }

    void customize(Document doc) {
        return
        builder = new DOMBuilder(doc)

        def head = doc.getElementsByTagName('head').item(0)

        def outputs = doc.getElementsByTagNameNS(XSL_NS, 'output')
        outputs.each { it.setAttribute('indent', 'no') }

        def script = doc.createElement('script')
        script.setAttribute('type', 'text/javascript')
        script.setAttribute('src', 'jquery-1.11.1.min.js')
        head.appendChild(script)

        script = doc.createElement('script')
        script.setAttribute('type', 'text/javascript')
        script.setAttribute('src', 'lightest-base.js')
        head.appendChild(script)

        def xslVariable = doc.createElementNS(XSL_NS, 'xsl:variable')
        xslVariable.setAttribute('name', 'result-id')
        xslVariable.setAttribute('select', '@id')
        def div = getDetailsDiv(doc)
        div.getParentNode().insertBefore(xslVariable, div)
        div.setAttribute('result-id', '{$result-id}')

        def outerFrameset = doc.getElementsByTagName('frameset').item(0)
        def innerFrameset = doc.createElement('frameset')
        outerFrameset.setAttribute('frameborder', '1')
        innerFrameset.setAttribute('rows', '45%, 55%')
        innerFrameset.setAttribute('frameborder', '1')

        def contentFrame = outerFrameset.getElementsByTagName('frame').item(1)
        def detailsFrame = doc.createElement('frame')
        detailsFrame.setAttribute('name', 'details')
        detailsFrame.setAttribute('src', 'blank.html')

        outerFrameset.removeChild(contentFrame)
        outerFrameset.appendChild(innerFrameset)
        innerFrameset.appendChild(contentFrame)
        innerFrameset.appendChild(detailsFrame)

        addPendingMethodsSupport(doc)
    }

    private Element getDetailsDiv(Document doc) {
        def divs = doc.getElementsByTagName('div')

        for (i in 0..<divs.getLength()) {
            def div = divs.item(i)
            if (div.getAttribute('id') == '{$detailsId}') {
                return div
            }
        }

        return null
    }

    private void addPendingMethodsSupport(Document doc) {
        // pass the pending methods as a parameter when calling the testMethods
        // template, both at the suite and test levels

        def templateNames = ['suiteContentFile', 'suiteTestCasesContentFiles']

        templateNames.each {
            def xpath = "//xsl:template[@name='${it}']/descendant::xsl:call-template[@name='testMethods']"

            def callTemplate = eval(xpath, doc)[0]
            def select = (it == 'suiteContentFile' ? '$testCaseElements/class/test-pending' : './class/test-pending')
            def withParam = builder.'xsl:with-param'(name: 'pendingMethods', select: select)

            if (callTemplate) {
                callTemplate.appendChild(withParam)
            }
        }

        // the testMethods template should accept a parameter named
        // "pendingMethods"

        def xpath = "//xsl:template[@name='testMethods']"
        def testMethods = eval(xpath, doc)[0]
        def param = builder.'xsl:param'(name: 'pendingMethods')

        testMethods.insertBefore(param, testMethods.getFirstChild())

        // add a toggle to show / hide the pending methods section

        xpath = "label[@for='methodsFilter_CONF']"

        def configFilter = eval(xpath, testMethods)[0]
        def input = builder.input(id: 'pending-methods-toggle', type: 'checkbox', checked: 'true',
                                  onclick: 'togglePendingMethodsTable(this);')
        def label = builder.label('for': 'pending-methods-toggle', ' Pending ')

        testMethods.insertBefore(input, configFilter)
        testMethods.insertBefore(label, configFilter)

        // add the pending methods section

        xpath = "div[@id='testMethodsByStatus']"

        def testMethodsByStatus = eval(xpath, testMethods)[0]
        def pendingMethods = builder.div(id: 'pending-methods') {
            table(class: 'testMethodsTable', width: '100%', cellpadding: '0', cellspacing: '0') {
                tr(class: 'methodsTableHeader') {
                    td(width: '100%', 'Name')
                    td(nowrap: 'true', 'Pending Invocations')
                }
                'xsl:for-each'(select: '$pendingMethods') {
                    tr(style: 'background-color: #cfe;') {
                        td(style: 'padding: 3px;') {
                            'xsl:value-of'(select: '@signature')
                        }
                        td(style: 'padding: 3px; text-align: right;') {
                            'xsl:value-of'(select: '@pending-invocations')
                        }
                    }
                }
            }
        }

        testMethods.insertBefore(pendingMethods, testMethodsByStatus)

        addPendingMethodsChartSupport(doc)
    }

    private void addPendingMethodsChartSupport(Document doc) {
        // modify the total method count to include pending method invocations

        def xpath = "//xsl:function[@name='testng:suiteMethodsCount']"
        def function = eval(xpath, doc)[0]
        def oldValueOf = eval("xsl:value-of", function)[0]
        def newValueOf = builder.'xsl:value-of'(select: '''
            if ($state = '*')
                then count($testCasesElements/class/test-method[not(@is-config)]) + sum($testCasesElements/class/test-pending/@pending-invocations)
                else count($testCasesElements/class/test-method[(@status=$state) and (not(@is-config))]) ''')

        function.replaceChild(newValueOf, oldValueOf)

        // add the pending method variables in the SVG creation template

        def graphZone = eval("//svg:svg[@id='graphzone']", doc)[0]
        def firstEntry = eval("svg:rect", graphZone)[0]
        def variables = [pendingCount  : 'sum($testCaseElements/class/test-pending/@pending-invocations)',
                         pendingPercent: 'format-number($pendingCount div $totalCount, \'###%\')',
                         pendingAngle  : '($pendingCount div $totalCount) * $pi * 2',
                         pendingX      : '$radius * math:cos($pendingAngle)',
                         pendingY      : '-1 * $radius * math:sin($pendingAngle)',
                         pendingArc    : 'if ($pendingAngle >= $pi) then 1 else 0']

        variables.each { name, select ->
            def variable = builder.'xsl:variable'(name: name, select: select)
            graphZone.insertBefore(variable, firstEntry)
        }

        // add an entry to the graph legend

        def pendingEntryColor = builder.rect(style: 'fill:#cfe;stroke-width:1;stroke:black;', x: '10', y: '46',
                                             width: '20', height: '20')
        def pendingEntryText = builder.text(class: 'legendtext', x: '40', y: '67', 'Pending ') {
            'xsl:value-of'(select: '$pendingPercent')
        }

        graphZone.insertBefore(pendingEntryColor, firstEntry)
        graphZone.insertBefore(pendingEntryText, firstEntry)

        // add a slice to the pie chart

        def pieChart = eval("svg:g", graphZone)[0]
        def variable = builder.'xsl:variable'(name: 'pendingRotation',
                                              select: '(($failedCount + $passedCount + $skippedCount) div $totalCount) * 360')
        def slice = builder.'xsl:if'(test: '($pendingCount div $totalCount) > 0') {
            g(style: 'fill:#cfe;', transform: 'rotate(-{$pendingRotation})') {
                path(d: 'M 0 0 h {$radius} A {$radius},{$radius} 0,{$pendingArc},0 {$pendingX},{$pendingY} z')
            }
        }

        pieChart.appendChild(variable)
        pieChart.appendChild(slice)
    }

    static void main(args) {
        if (args.size() < 2) {
            println "Usage: LightestTransformer XMLFILE OUTPUTDIR"
            System.exit(0)
        }

        def xmlFile = new File(args[0])
        def outputDir = new File(args[1]).getCanonicalFile()
        def outputFile = new File(outputDir, 'index.html')
        def transformer = new LightestTransformer()
        def startTime = System.currentTimeMillis()

        transformer.setParameter('testNgXslt.outputDir', outputDir.getPath())
        transformer.transform(xmlFile.text, new FileOutputStream(outputFile))

        def endTime = System.currentTimeMillis()

        println "Transformed to ${outputFile} in ${endTime - startTime}ms."
    }
}