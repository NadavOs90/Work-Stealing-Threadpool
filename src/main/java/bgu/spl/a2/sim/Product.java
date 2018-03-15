package bgu.spl.a2.sim;

import java.util.LinkedList;
import java.util.List;

/**
 * A class that represents a product produced during the simulation.
 */
public class Product implements java.io.Serializable{

	private static final long serialVersionUID = 1L;//only because the form told us to
	private String name;
	private final long startId;
	private long finalId;
	private List<Product> parts;
	
	/**
	* Constructor 
	* @param startId - Product start id
	* @param name - Product name
	*/
    public Product(long startId, String name){
    	this.name = name;
    	this.startId = startId;
    	finalId = startId;
    	parts = new LinkedList<Product>();
    }

	/**
	* @return The product name as a string
	*/
    public String getName(){
    	return name;
    }

	/**
	* @return The product start ID as a long. start ID should never be changed.
	*/
    public long getStartId(){
    	return startId;
    }
    
	/**
	* @return The product final ID as a long. 
	* final ID is the ID the product received as the sum of all UseOn(); 
	*/
    public long getFinalId(){
    	return finalId;
    }

	/**
	* @return Returns all parts of this product as a List of Products
	*/
    public List<Product> getParts(){
    	return parts;
    }

	/**
	* Add a new part to the product
	* @param p - part to be added as a Product object
	*/
    public void addPart(Product p){
    	parts.add(p);
    }
    
    /**
     * updates the final sum by adding the @param
     * @param n - the long to be added
     */
    public synchronized void addToFinalId(long n){
    	finalId += n;
    }
}
