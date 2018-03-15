package bgu.spl.a2.sim;

/**
 * an object for parsing the json file
 * @author ostrowsk
 *
 */

public class JsonObject {
	private int threads;
	private Plan[] plans;
	private Tools[] tools;
	private Waves[][] waves;
	
	public int getThreads() {
		return threads;
	}

	public Plan[] getPlans() {
		return plans;
	}

	public Tools[] getTools() {
		return tools;
	}

	public Waves[][] getWaves() {
		return waves;
	}
}
/**
 * a class for the plans 
 * @author ostrowsk
 *
 */

class Plan {
	private String product;
	private String[] tools;
	private String[] parts;
	public String getProduct() {
		return product;
	}
	public String[] getTools() {
		return tools;
	}
	public String[] getParts() {
		return parts;
	}
}

/**
 * a class for the tools
 * @author ostrowsk
 *
 */

class Tools {
	private int qty;
	private String tool;
	public int getQty() {
		return qty;
	}
	public String getTool() {
		return tool;
	}
}

/**
 * a class for the waves
 * @author ostrowsk
 *
 */

class Waves{
	private int qty;
	private long startId;
	private String product;
	public int getQty() {
		return qty;
	}
	public long getStartId() {
		return startId;
	}
	public String getProduct() {
		return product;
	}

}