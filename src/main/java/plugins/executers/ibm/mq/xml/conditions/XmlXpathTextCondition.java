package plugins.executers.ibm.mq.xml.conditions;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import plugins.executers.ibm.mq.xml.wrapper.XmlWrapper;
import plugins.executers.ibm.mq.xml.wrapper.XmlWrapperException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * @author platov.nikolay
 * Проверяет, что в переданном тексте по указанному в конструкторе XPath содержится образец текта, указанный в конструкторе
 */
public class XmlXpathTextCondition implements TextCondition {

    private final String xPath;
    private final String sample;
    private final String description;


    /**
     * @param xPath  XPath � XML
     * @param sample ������� ������
     */
    public XmlXpathTextCondition(String xPath, String sample) {
        this(xPath, sample, "");
    }

    public XmlXpathTextCondition(String xPath, String sample, String description) {
        if (xPath == null || xPath.isEmpty())
            throw new IllegalArgumentException("XPath не может быть null или пустой строкой");
        if (sample == null || sample.isEmpty())
            throw new IllegalArgumentException("Образец не может быть null или пустой строкой");
        if (description == null)
            throw new IllegalArgumentException("Описание не может быть null");
        this.xPath = xPath;
        this.sample = sample;
        this.description = description;
    }

    @Override
    public String description() {
        return description;
    }

    @Override
    public boolean match(String text) {
        return (match(text.getBytes(StandardCharsets.UTF_8)));
        /*if (text == null || text.isEmpty()) return false;
        try {
            XmlWrapper xmlGenerator = new XmlWrapper(text);
            return sample.equals(xmlGenerator.readValue(xPath).trim());
        } catch (XmlWrapperException e) {
            return false;
        }*/
    }

    @Override
    public boolean match(byte[] text) {
        Document xml;
        String s;
        try {
            DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = domFactory.newDocumentBuilder();
            builder.setErrorHandler(null);
            xml = builder.parse(new InputSource(new ByteArrayInputStream(text)));
            s = ((NodeList) XPathFactory.newInstance().newXPath().evaluate(xPath, xml, XPathConstants.NODESET)).item(0).getTextContent().trim();
        } catch (Exception e) {
            return false;
        }
        if (s != null) return sample.equals(s);
        return false;
    }

    @Override
    public boolean match(byte[] text, Charset charset) {
        if (text == null || text.length == 0 || charset == null) return false;
        try {
            XmlWrapper xmlGenerator = new XmlWrapper(new String(text, charset));

            // debug: вывести все, что приходит из очереди MQ
            String tmp = xmlGenerator.readValue(xPath).trim();
            return sample.equals(tmp);

            //return sample.equals(xmlGenerator.readValue(xPath).trim());
        } catch (XmlWrapperException e) {
            return false;
        }
    }

    @Override
    public String toString() {
        return String.format("По XPath пути '%s' содержится подстрока '%s'", xPath, sample);
    }
}
