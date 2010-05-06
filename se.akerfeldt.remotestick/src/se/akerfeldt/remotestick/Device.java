package se.akerfeldt.remotestick;

import java.util.List;


public class Device {
	
	private final Integer id;
	
	private final String name;
	
	private final DeviceCommand lastCommand;

	/* TODO: Could be changed to a single int instead */
	private final List<Integer> supportedMethods;

	public Device(Integer id, String name, DeviceCommand lastCommand, List<Integer> supportedMethods) {
		super();
		this.id = id;
		this.name = name;
		this.lastCommand = lastCommand;
		this.supportedMethods = supportedMethods;
	}

	public Integer getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public DeviceCommand getLastCommand() {
		return lastCommand;
	}

	public List<Integer> getSupportedMethods() {
		return supportedMethods;
	}

	@Override
	public String toString() {
		return id + "/" + name + supportedMethods;
	}
	
}
