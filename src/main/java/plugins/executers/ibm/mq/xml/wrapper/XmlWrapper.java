package plugins.executers.ibm.mq.xml.wrapper;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class XmlWrapper {

    private Document xml;
/*
    public XmlWrapper(byte[] sourceXmlBytes) {
        this(new String(sourceXmlBytes, StandardCharsets.UTF_8));
    }

    public XmlWrapper(byte[] sourceXmlString) {
        XMLInputFactory factory = XMLInputFactory.newInstance();
        XMLStreamReader reader = null;
        try {
            reader = factory.createXMLStreamReader(new ByteArrayInputStream(sourceXmlString));
            while (reader.hasNext()) {
                reader.next();
                reader.is
            }
        } catch (XMLStreamException e) {
            e.printStackTrace();
        }
    }*/
/*
    public XmlWrapper(String sourceXmlString) {
        this(sourceXmlString.getBytes(StandardCharsets.UTF_8));
    }
*/

    public XmlWrapper(byte[] sourceXmlString) {
        try {
            DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
            //domFactory.setIgnoringComments(true);
            //domFactory.setCoalescing(true);
            //domFactory.setIgnoringElementContentWhitespace(true);
            //domFactory.setValidating(true);
            //domFactory.setExpandEntityReferences(true);
            DocumentBuilder builder = domFactory.newDocumentBuilder();
            builder.setErrorHandler(null);
            //builder.setEntityResolver(null);
            xml = builder.parse(new InputSource(new ByteArrayInputStream(sourceXmlString)));
        } catch (SAXException e) {
            throw new XmlWrapperException("ошибка при разборе документа", e);
        } catch (IOException e) {
            throw new XmlWrapperException("файл XML-документа не найден", e);
        } catch (ParserConfigurationException e) {
            throw new XmlWrapperException("ошибка при создании билдера документа", e);
        }
    }

    public XmlWrapper(String sourceXmlString) {
        this(sourceXmlString.getBytes(StandardCharsets.UTF_8));
    }

/*    public XmlWrapper(String sourceXmlString) {
        try {
            DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = domFactory.newDocumentBuilder();
            xml = builder.parse(new InputSource(new ByteArrayInputStream(sourceXmlString.getBytes(StandardCharsets.UTF_8))));
            //xml = builder.parse(sourceXmlString);
        } catch (SAXException e) {
            throw new XmlWrapperException("ошибка при разборе документа", e);
        } catch (IOException e) {
            throw new XmlWrapperException("файл XML-документа не найден", e);
        } catch (ParserConfigurationException e) {
            throw new XmlWrapperException("ошибка при создании билдера документа", e);
        }
    }*/
    public byte[] getBytes() {
        try {
            Transformer xformer = TransformerFactory.newInstance().newTransformer();
            xformer.setOutputProperty(OutputKeys.ENCODING, xml.getXmlEncoding());
            xformer.setOutputProperty(OutputKeys.INDENT, "yes");
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            xformer.transform(new DOMSource(xml), new StreamResult(os));
            return os.toByteArray();
        } catch (TransformerConfigurationException e) {
            throw new XmlWrapperException("ошибка при создании трансформера XML", e);
        } catch (TransformerFactoryConfigurationError e) {
            throw new XmlWrapperException("ошибка при создании фабрики трансформеров XML", e);
        } catch (TransformerException e) {
            throw new XmlWrapperException("ошибка при сохранении XML-документа", e);
        }
    }

    public boolean checkElement(String xPath) {
        try {
            return findNodes(xPath).item(0) != null;
        } catch (XmlWrapperException falseException) {
            return false;
        }
    }

    public void updateValue(String xpathExpression, String newValue) {
        NodeList nodes = findNodes(xpathExpression);
        for (int idx = 0; idx < nodes.getLength(); idx++) {
            nodes.item(idx).setTextContent(newValue);
        }
    }

    public String readValue(String xpathExpression) {
        NodeList nodes = findNodes(xpathExpression);
        return nodes.item(0).getTextContent().trim();
    }

    /**
     * Ищет элементы по заданному XPath выражению.
     * @param xpathExpression Выражение XPath.
     * @return Список найденных элементов.
     */
    protected NodeList findNodes(String xpathExpression) {
        XPath xpath = XPathFactory.newInstance().newXPath();
        NodeList nodes;
        try {
            nodes = (NodeList) xpath.evaluate(xpathExpression, xml, XPathConstants.NODESET);
        } catch (XPathExpressionException e) {
            String message = String.format("ошибка при поиске элементов по XPath-выражению '%s'", xpathExpression);
            throw new XmlWrapperException(message, e);
        }
        if (nodes.getLength() == 0) {
            String message = String.format("не найдено элементов по XPath-выражению '%s'", xpathExpression);
            throw new XmlWrapperException(message);
        }
        return nodes;
    }
}