package se.akerfeldt.remotestick;


public class Device {
	
	final private Integer id;
	
	final private String name;
	
	final private DeviceCommand lastCommand;

	public Device(Integer id, String name, DeviceCommand lastCommand) {
		super();
		this.id = id;
		this.name = name;
		this.lastCommand = lastCommand;
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

	@Override
	public String toString() {
		return name;
	}
	
}
