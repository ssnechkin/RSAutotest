package plugins.executers.ibm.mq.client;

public class MqConfig {

    public static class MqConfigBuilder {
        private String host;
        private int port;
        private String mqChannel;
        private String mqManager;
        private String user;
        private String password;
        private String mqQueue;

        public MqConfigBuilder host(String host) {
            this.host = host;
            return this;
        }

        public MqConfigBuilder port(int port) {
            this.port = port;
            return this;
        }

        public MqConfigBuilder mqChannel(String mqChannel) {
            this.mqChannel = mqChannel;
            return this;
        }

        public MqConfigBuilder mqManager(String mqManager) {
            this.mqManager = mqManager;
            return this;
        }

        public MqConfigBuilder user(String user) {
            this.user = user;
            return this;
        }

        public MqConfigBuilder password(String password) {
            this.password = password;
            return this;
        }

        public MqConfigBuilder mqQueue(String mqQueue) {
            this.mqQueue = mqQueue;
            return this;
        }

        public MqConfig build() {
            return new MqConfig(host, port, mqChannel, mqManager, user, password, mqQueue);
        }
    }

    private final String host;
    private final int port;
    private final String mqChannel;
    private final String mqManager;
    private final String user;
    private final String password;
    private final String mqQueue;

    public MqConfig(
            String host,
            int port,
            String mqChannel,
            String mqManager,
            String user,
            String password,
            String mqQueue) {
        this.host = host;
        this.port = port;
        this.mqChannel = mqChannel;
        this.mqManager = mqManager;
        this.user = user;
        this.password = password;
        this.mqQueue = mqQueue;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getMqChannel() {
        return mqChannel;
    }

    public String getMqManager() {
        return mqManager;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

    public String getMqQueue() {
        return mqQueue;
    }
}

