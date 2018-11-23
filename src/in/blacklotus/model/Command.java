package in.blacklotus.model;

public class Command {
	
	private String name;
	
	private String[] args;

	public Command(String name, String[] args) {
		super();
		
		this.name = name;
		
		this.args = args;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String[] getArgs() {
		return args;
	}

	public void setArgs(String[] args) {
		this.args = args;
	}
	
	public String printRawCommand() {
		
		String str = this.name;
		
		for(String arg : args) {
			
			str += " " + arg;
		}
		
		return str;
	}

}
