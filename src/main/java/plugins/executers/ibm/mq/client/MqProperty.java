package plugins.executers.ibm.mq.client;

public class MqProperty {

	private final String key;
	private final String value;
	
	public MqProperty(String key, String value) {
		this.key = key;
		this.value = value;
	}
	
	public String key() {
		return key;
	}
	
	public String value() {
		return value;
	}
}
