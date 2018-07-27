package plugins.executers.ibm.mq.client;

import com.ibm.jms.JMSBytesMessage;
import com.ibm.mq.jms.*;
import org.testng.TestException;
import plugins.executers.ibm.mq.xml.conditions.TextCondition;

import javax.jms.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MqClient implements AutoCloseable {
    private String XML_PATTERN_STR = "<(\\S+?)(.*?)>(.*?)</\\1>";
    private Pattern pattern = Pattern.compile(XML_PATTERN_STR, Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.MULTILINE);
    private String XML_HEADER = "(<\\?.*?\\?>)";
    private Pattern headerPattern = Pattern.compile(XML_HEADER, Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);

    private boolean isXMLLike(String inXMLStr) {
        boolean result = false;

        if (inXMLStr != null) {
            String trimmedString = inXMLStr.trim();

            if (trimmedString.startsWith("<") && trimmedString.endsWith(">")) {
                Matcher headerMatcher = headerPattern.matcher(trimmedString);
                if (headerMatcher.find()) {
                    trimmedString = trimmedString.replace(headerMatcher.group(0), "");
                }
                trimmedString = trimmedString.trim();
                Matcher matcher = pattern.matcher(trimmedString);
                result = matcher.matches();
            }
        }
        return result;
    }

    private interface BrowserAction<T> {
        T execute(QueueBrowser browser) throws JMSException;
    }

    private interface ReceiverAction<T> {
        T execute(QueueReceiver receiver) throws JMSException;
    }

    private interface SenderAction<T> {
        T execute(QueueSender sender) throws JMSException;
    }

    private MQQueueConnection connection;
    private MQQueueSession session;
    private MQQueue queue;

    public MqClient(MqConfig config) {
        MQQueueConnectionFactory factory = new MQQueueConnectionFactory();
        factory.setHostName(config.getHost());
        try {
            factory.setPort(config.getPort());
            factory.setChannel(config.getMqChannel());
            factory.setQueueManager(config.getMqManager());
            factory.setTransportType(JMSC.MQJMS_TP_CLIENT_MQ_TCPIP);

            connection = (MQQueueConnection) factory.createConnection(config.getUser(), config.getPassword());
            session = (MQQueueSession) connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            queue = (MQQueue) session.createQueue(config.getMqQueue());

            connection.start();
        } catch (JMSException e) {
            throw new TestException(String.format(
                    "Не удалось создать соединение (Host:%s, Port:%s, MqQueue:%s, MqManager:%s, MqChannel:%s, User:%s, Password:%s)",
                    config.getHost(), config.getPort(), config.getMqQueue(), config.getMqManager(), config.getMqChannel(),
                    config.getUser(), config.getPassword()),
                    e);
        }
    }

    @Override
    public void close() {
        try {
            if (session != null)
                session.close();
        } catch (JMSException e) {
            throw new MqClientException("Не удалось закрыть сессию MQ", e);
        }
        try {
            if (connection != null)
                connection.close();
        } catch (JMSException e) {
            throw new MqClientException("Не удалось закрыть соединение MQ", e);
        }
    }

    private <T> T executeWithBrowser(BrowserAction<T> action) {
        QueueBrowser browser = null;
        try {
            browser = session.createBrowser(queue);
            return action.execute(browser);
        } catch (JMSException e) {
            throw new MqClientException("Ошибка при работе с браузером сообщений", e);
        } finally {
            try {
                if (browser != null)
                    browser.close();
            } catch (JMSException e) {
                throw new MqClientException("Не удалось закрыть браузер сообщений", e);
            }
        }
    }

    private <T> T executeWithReceiver(String selector, ReceiverAction<T> action) {
        QueueReceiver receiver = null;
        try {
            return action.execute(session.createReceiver(queue, selector));
        } catch (JMSException e) {
            throw new MqClientException("Ошибка при работе с получателем сообщений", e);
        } finally {
            try {
                if (receiver != null)
                    receiver.close();
            } catch (JMSException e) {
                throw new MqClientException("Не удалось закрыть получателя сообщений", e);
            }
        }
    }

    private <T> T executeWithSender(SenderAction<T> action) {
        QueueSender sender = null;
        try {
            sender = session.createSender(queue);
            return action.execute(sender);
        } catch (JMSException e) {
            throw new MqClientException("Ошибка при работе с отправителем сообщений", e);
        } finally {
            try {
                if (sender != null)
                    sender.close();
            } catch (JMSException e) {
                throw new MqClientException("Не удалось закрыть отправителя сообщений", e);
            }
        }
    }

    /**
     * Получить все сообщения из очереди (без удаления).
     *
     * @return Список всех прочитанных сообщений.
     * @throws MqClientException -
     */
    private List<Message> getAllMessages() {
        return executeWithBrowser(browser -> {
            Enumeration<?> messages = browser.getEnumeration();
            List<Message> results = new ArrayList<>();
            while (messages.hasMoreElements()) {
                results.add((Message) messages.nextElement());
            }
            return results;
        });
    }

    /**
     * Забрать сообщение из очереди (чтение с удалением) по CorrelId. Если очередь пуста, то
     * возникает ожидание поступления сообщения (как только в очередь добавится
     * новое сообщение, оно тут же будет возвращенно данным методом).
     */
    private Message receiveMessageByCorrelId(String correlId, long timeout) {
        return executeWithReceiver("JMSCorrelationID = '" + correlId + "'", receiver -> receiver.receive(timeout));
    }

    /**
     * Забрать сообщение из очереди (чтение с удалением) по CorrelId. Если очередь пуста, то
     * возникает ожидание поступления сообщения (как только в очередь добавится
     * новое сообщение, оно тут же будет возвращенно данным методом).
     */
    private Message receiveMessageByMessageId(String messageId, long timeout) {
        return executeWithReceiver("JMSMessageID = '" + messageId + "'", receiver -> receiver.receive(timeout));
    }

    /**
     * Получить содержимое сообщения из очереди (чтение с удалением) по CorrelId. Если очередь пуста, то
     * возникает ожидание поступления сообщения (как только в очередь добавится
     * новое сообщение, оно тут же будет возвращенно данным методом).
     */
    public byte[] getMessageContentByCorrelId(String correlId, long timeout) {
        JMSBytesMessage message = (JMSBytesMessage) receiveMessageByCorrelId(correlId, timeout);
        if (message == null)
            throw new MqClientException("Не удалось получить сообщение");
        return getMessageContent(message);
    }

    private byte[] getMessageContent(JMSBytesMessage message) {
        try {
            long messageLength = message.getBodyLength();
            if (messageLength > Integer.MAX_VALUE) {
                throw new MqClientException(String.format("Количество символов в ответе mq [%s] больше, чем допустимое [%s]",
                        String.valueOf(messageLength), String.valueOf(Integer.MAX_VALUE)));
            }
            byte[] messageBody = new byte[(int) messageLength];
            message.readBytes(messageBody, (int) messageLength);
            return messageBody;
        } catch (JMSException e) {
            throw new MqClientException("Не удалось получить содержимое сообщения", e);
        }
    }
    /*
    private byte[] getMessageContent(JMSBytesMessage message) {
        try {
            long messageLength = message.getBodyLength();
            if (messageLength > Integer.MAX_VALUE) {
                throw new MqClientException(String.format("Количество символов в ответе mq [%s] больше, чем допустимое [%s]",
                        String.valueOf(messageLength), String.valueOf(Integer.MAX_VALUE)));
            }
            byte[] messageBody = new byte[(int) messageLength];
            message.readBytes(messageBody, (int) messageLength);
            return messageBody;
        } catch (JMSException e) {
            throw new MqClientException("Не удалось получить содержимое сообщения", e);
        }
    }*/

    /**
     * Получить содержимое сообщения из очереди (чтение с удалением) по CorrelId. Если очередь пуста, то
     * возникает ожидание поступления сообщения (как только в очереди появится новое сообщение, оно тут же будет возвращенно данным методом).
     */
    private byte[] getMessageContentByMessageId(String messageId, long timeout) {
        JMSBytesMessage message = (JMSBytesMessage) receiveMessageByMessageId(messageId, timeout);
        if (message == null)
            throw new MqClientException("Не удалось получить сообщение");
        return getMessageContent(message);
    }

    /**
     * Отправить сообщение в очередь. При повторной отправке одного и того же
     * собщения, ему будет назначен новый ID.
     *
     * @param message Передаваемое сообщение.
     * @throws MqClientException -
     */
    private void send(Message message) {
        executeWithSender(sender -> {
            sender.send(message);
            return null;
        });
    }

    /**
     * Получить messageId JSON-сообщения, удовлетворяющего переданным условиям
     *
     * @param conditions Условия, которым должно удовлетворять JSON-сообщение
     * @return Идентификатор сообщения в очереди 'messageId', если подходящих JSON-сообщений нет - вернуть 'null'
     * @throws MqClientException Ошибки при работе с очередью
     */
    private String getMessageIdAppropriateQueueJsonMessage(List<TextCondition> conditions) throws MqClientException {
        try {
            List<Message> messages = getAllMessages();      // Все сообщения из очереди на данный момент
            Charset jsonCharset = Charset.forName("UTF-8");     // В приходящих из ВИО JSON-сообщениях кодировка UTF-8
            for (Message message : messages) {      // Бежать по всем сообщениям из очереди
                boolean match = true;   // Флаг "сообщение удовлетворяет заданным условиям"

                // Получить тело JSON-сообщения
                byte[] messageBody = getMessageContent((JMSBytesMessage) message);

                for (TextCondition condition : conditions) {       // Бежать по всем условиям, в условиях лежат JsonPathTextCondition
                    if (!condition.match(messageBody, jsonCharset)) {
                        match = false;      // Если не выполнено условие, то флаг сбросить
                        break;         // Достаточно одного невыполненного условия, остальные проверять нет смысла
                    }
                }
                if (match)
                    return message.getJMSMessageID();       // Если ВСЕ условия выполнены, то вернуть 'messageId'
            }
            return null;
        } catch (JMSException ex) {
            throw new MqClientException("Ошибка при работе с очередью mq", ex);
        }
    }

    /**
     * Получить messageId сообщения, удовлетворяющего переданным условиям
     *
     * @param conditions Условия, которым должно удовлетворять сообщение
     * @return messageId
     * @throws MqClientException Ошибки при работе с очередью
     */
    private String getMessageIdAppropriateQueueMessage(List<TextCondition> conditions) throws MqClientException {
        try {
            List<Message> messages = getAllMessages();
            for (Message message : messages) {
                byte[] messageBody = getMessageContent((JMSBytesMessage) message);
                boolean match = true;
                for (TextCondition condition : conditions) {
                    if (!condition.match(messageBody)) {
                        match = false;      // Если не выполнено условие, то флаг сбросить
                        break;      // Достаточно одного невыполненного условия, остальные проверять нет смысла
                    }
                }
                if (match)
                    return message.getJMSMessageID();
            }
            return null;
        } catch (JMSException e) {
            throw new MqClientException("Ошибка при работе с очередью mq", e);
        }
    }

    /**
     * Получить тело сообщения удовлетворяющее условиям conditions.
     *
     * @param conditions Условия, которым должно удовлетворять сообщение
     * @param timeout    время для удаления сообщения из очереди.
     * @return byte[] сообщения.
     */
    public byte[] getMessageBody(List<TextCondition> conditions, int timeout) {
        if(timeout == 0) timeout = 1;
        int finalTimeout = timeout;
        return executeWithBrowser(browser -> {
            Enumeration<?> messages = browser.getEnumeration();
            Message message;
            byte[] messageBody;
            boolean match;
            while (messages.hasMoreElements()) {
                message = (Message) messages.nextElement();
                messageBody = getMessageContent((JMSBytesMessage) message);
                match = true;
                for (TextCondition condition : conditions) {
                    if (!condition.match(messageBody)) {
                        match = false;
                        break;
                    }
                }
                if (match) {
                    receiveMessageByMessageId(message.getJMSMessageID(), finalTimeout);//удаляет сообщение из очереди
                    return messageBody;
                }
            }
            return null;
        });
    }

    /**
     * Удалит все сообщения из очереди.
     *
     * @param timeout    время для удаления сообщения из очереди.
     * @return int количество удалённых сообщений в очереди.
     */
    public int deleteAllMessage(int timeout) {
        final int[] countMessage = {0};
        if(timeout == 0) timeout = 1;
        int finalTimeout = timeout;
        executeWithBrowser(browser -> {
            Enumeration<?> messages = browser.getEnumeration();
            Message message;
            while (messages.hasMoreElements()) {
                message = (Message) messages.nextElement();
                receiveMessageByMessageId(message.getJMSMessageID(), finalTimeout);
                countMessage[0]++;
            }
            return null;
        });
        return countMessage[0];
    }


    /**
     * Получить сообщение, удовлетворяющее переданным условиям.
     *
     * @param conditions Условия, которым должно удовлетворять сообщение
     * @param timeout    timeout в мс.
     * @return Сообщение.
     * @throws MqClientException -
     */
    public byte[] getAppropriateQueueMessage(List<TextCondition> conditions, long timeout) throws MqClientException {
        String messageId = getMessageIdAppropriateQueueMessage(conditions);
        if (messageId == null)
            throw new MqClientException("Не удалось найти сообщение, удовлетворяющее заданным условиям");
        return getMessageContentByMessageId(messageId, timeout);
    }

    /**
     * Получить JSON-сообщение, удовлетворяющее переданным условиям.
     *
     * @param conditions Условия, которым должно удовлетворять сообщение
     * @param interval   Интервал опроса в мс
     * @param timeout    timeout в мс
     * @return Соответствующее условиям JSON-сообщение
     */
    public byte[] getAppropriateQueueJsonMessage(List<TextCondition> conditions, int interval, int timeout) {
        Calendar timeoutCalendar = Calendar.getInstance();    // Текущее время в календарь-таймаут
        timeoutCalendar.add(Calendar.MILLISECOND, timeout);        // Прибавить timeout
        Exception exception = null;
        do {    // Пока не достигнем времени таймаута
            try {
                // Получить идентификатор сообщения в очереди, удовлетворяющего условиям => подходящее сообщение есть в очереди
                String messageId = getMessageIdAppropriateQueueJsonMessage(conditions);

                //Если подходящее под условия сообщение нашлось, то вЫчитать его и удалить из очереди
                if (messageId != null)
                    return getMessageContentByMessageId(messageId, interval);

                // А если очередь пустая (messageId == null), то ждать interval мс
                TimeUnit.MILLISECONDS.sleep(interval);
            } catch (Exception ex) {
                exception = ex;
            }

        } while (Calendar.getInstance().before(timeoutCalendar));    // Пока не достигли времени таймаута
        throw new MqClientException("Не удалось найти сообщение, удовлетворяющее заданным условиям", exception);
    }

    /**
     * Получить сообщение, удовлетворяющее переданным условиям.
     *
     * @param conditions Условия, которым должно удовлетворять сообщение
     * @param interval   Интервал опроса в мс
     * @param timeout    timeout в мс
     * @return Соответствующее условиям сообщение
     * @throws MqClientException -
     */
    public byte[] getAppropriateQueueMessage(List<TextCondition> conditions, int timeout, int interval) throws MqClientException {
        Calendar timeoutCalendar = Calendar.getInstance();    // Текущее время в календарь-таймаут
        timeoutCalendar.add(Calendar.MILLISECOND, timeout);        // Прибавить timeout
        Exception exception = null;

        do {    // Получаем messageID, пока не достигнем времени таймаута
            try {
                String messageId = getMessageIdAppropriateQueueMessage(conditions);
                if (messageId != null)
                    return getMessageContentByMessageId(messageId, interval);
                TimeUnit.MILLISECONDS.sleep(interval);       // Если очередь пустая - нет messageId, то ждать interval мс
            } catch (Exception e) {
                exception = e;
            }
        } while (Calendar.getInstance().before(timeoutCalendar));    // Пока не достигли времени таймаута
        throw new MqClientException("Не удалось найти сообщение, удовлетворяющее заданным условиям", exception);
    }

    /**
     * Удалить все сообщения, удовлетворяющие переданным условиям.
     *
     * @param conditions Условия, которым должно удовлетворять сообщение.
     * @throws MqClientException -
     */
    public void deleteAppropriateQueueMessages(List<TextCondition> conditions, int timeout) throws MqClientException {
        String messageId;
        while ((messageId = getMessageIdAppropriateQueueMessage(conditions)) != null)
            getMessageContentByMessageId(messageId, timeout);        // Прочитать сообщение и удалить его из очереди
    }

    /**
     * Отправить сообщение только со свойствами.
     *
     * @param messageBody Тело сообщения.
     * @return Идентификатор сообщения 'messageId'.
     * @throws MqClientException -
     */
    public String sendQueueMessage(byte[] messageBody, MqProperty... properties) throws MqClientException {
        return sendQueueMessage(messageBody, null, properties);
    }

    /**
     * Отправить сообщение со свойствами и CorrelID.
     *
     * @param messageBody Тело сообщения.
     * @return Идентификатор сообщения 'messageId'.
     * @throws MqClientException -
     */
    public String sendQueueMessage(byte[] messageBody, String correlId, MqProperty... properties) throws MqClientException {
        try {
            JMSBytesMessage message = (JMSBytesMessage) session.createBytesMessage();
            for (MqProperty property : properties)
                message.setStringProperty(property.key(), property.value());
            if (correlId != null)
                message.setJMSCorrelationID(correlId);
            message.writeBytes(messageBody);
            send(message);
            return message.getJMSMessageID();
        } catch (JMSException e) {
            throw new MqClientException("Не удалось отправить сообщение через очередь mq", e);
        }
    }
}