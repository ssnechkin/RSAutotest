package plugins.executers.ibm.mq.client;

public class MqClientException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public MqClientException(String message) {
        super(message);
    }

    public MqClientException(String message, Throwable cause) {
        super(message, cause);
    }

    public MqClientException(Throwable cause) {
        super(cause);
    }
}
