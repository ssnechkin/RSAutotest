package plugins.executers.ibm.mq.xml.wrapper;

public class XmlWrapperException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public XmlWrapperException(String message) {
        super(message);
    }

    public XmlWrapperException(String message, Throwable t) {
        super(message, t);
    }

}
