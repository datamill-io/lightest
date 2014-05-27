package com.googlecode.lightest.core

import groovy.xml.DOMBuilder

import javax.xml.transform.Source
import javax.xml.transform.stream.StreamResult
import javax.xml.transform.stream.StreamSource
import javax.xml.transform.Transformer
import javax.xml.transform.TransformerFactory

import org.apache.xml.serialize.OutputFormat
import org.apache.xml.serialize.XMLSerializer
import org.w3c.dom.Document

/**
 * Applies an XSL transform, with customizations. This is a facade
 * encapsulating the basic javax.xml.transform.Transformer functionality.
 * The customize() method is called at most once, after which the customized
 * stylesheet is cached inside the internal transformer variable.
 *
 * This implementation is NOT thread-safe.*/
abstract class CustomizedTransformer {
    public static final String DEFAULT_TRANSFORMER_FACTORY =
            'net.sf.saxon.TransformerFactoryImpl'

    // TODO - why do we get a test error when this is private?
    protected Transformer transformer

    private Map<String, Object> params

    CustomizedTransformer() {
        params = [:]
    }

    /**
     * Returns the XSL transform source as an InputStream.*/
    abstract InputStream getTransformAsStream()

    /**
     * Adds a parameter for the transformation.
     *
     * @param name
     * @param value
     */
    void setParameter(String name, Object value) {
        params[name] = value
    }

    /**
     * Transforms the given XML string with customizations, and writes
     * the result to the given output stream. This version calls the version of
     * transform() that taks an InputStream as its first parameter.
     *
     * @param s the XML string to transform. Assumed to be UTF-8 encoded.
     * @param os
     */
    void transform(String s, OutputStream os) {
        def is = new ByteArrayInputStream(s.getBytes("UTF-8"))
        transform(is, os)
    }

    /**
     * Transforms the given input stream XML with customizations, and writes
     * the result to the given output stream.
     *
     * @param is
     * @param os
     */
    void transform(InputStream is, OutputStream os) {
        if (!transformer) {
            initTransformer()
        }

        transformer.reset()

        params.each { k, v -> transformer.setParameter(k, v)
        }

        transformer.transform(new StreamSource(is), new StreamResult(os))
    }

    /**
     * Creates a new Transformer instance based on the customized stylesheet
     * and sets the associated field.*/
    private void initTransformer() {
        System.setProperty('javax.xml.transform.TransformerFactory',
                           DEFAULT_TRANSFORMER_FACTORY)

        def source = getTransformSource()
        def factory = TransformerFactory.newInstance()

        transformer = factory.newTransformer(source)
    }

    /**
     * Returns a StreamSource representing the customized XSL transform.
     * Invokes customize() to perform the customization.*/
    private Source getTransformSource() {
        def transformStream = getTransformAsStream()

        assert transformStream != null

        def reader = new InputStreamReader(transformStream)
        def doc = DOMBuilder.parse(reader)
        def root = doc.documentElement

        customize(doc)

        def format = new OutputFormat(doc)
        def os = new ByteArrayOutputStream()
        def serializer = new XMLSerializer(os, format)

        format.setIndenting(true)
        serializer.serialize(doc)

        return new StreamSource(new ByteArrayInputStream(os.toByteArray()))
    }

    /**
     * Customizes the XSL transform XML. This will be invoked before the
     * transform is applied to the source document.
     *
     * @param doc the document object of the XSLT document.
     */
    abstract void customize(Document doc)
}