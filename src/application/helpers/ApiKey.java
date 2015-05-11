package application.helpers;

public enum ApiKey {
	ZEIT("c51304fc35b3fae719591dc339d2608dca64076b66772aa9fa29"),
	GUARDIAN("9hh2pj5ngu2byeq6c7ua3cp6");
	
	private final String key;
	
	ApiKey(String key) {
		this.key = key;
	}
	
	public String getKey() {
		return this.key;
	}
 }
