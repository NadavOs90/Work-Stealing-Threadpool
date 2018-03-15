package bgu.spl.a2.sim;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import bgu.spl.a2.Deferred;
import bgu.spl.a2.VersionMonitor;
import bgu.spl.a2.sim.conf.ManufactoringPlan;
import bgu.spl.a2.sim.tools.GcdScrewDriver;
import bgu.spl.a2.sim.tools.NextPrimeHammer;
import bgu.spl.a2.sim.tools.RandomSumPliers;
import bgu.spl.a2.sim.tools.Tool;

/**
 * A class representing the warehouse in your simulation
 * 
 * Note for implementors: you may add methods and synchronize any of the
 * existing methods in this class *BUT* you must be able to explain why the
 * synchronization is needed. In addition, the methods you add to this class can
 * only be private!!!
 *
 */
public class Warehouse {
	
	ConcurrentHashMap<String, Integer> toolSuply;
	ConcurrentHashMap<String, ManufactoringPlan> mplans;
	VersionMonitor vm;
	ConcurrentHashMap<String, ConcurrentLinkedQueue<Deferred<Tool>>> defTool;

	/**
	* Constructor
	*/
    public Warehouse(){
    	toolSuply = new ConcurrentHashMap<String, Integer>();
    	toolSuply.put("np-hammer", 0);
    	toolSuply.put("gs-driver", 0);
    	toolSuply.put("rs-pliers", 0);
    	mplans = new ConcurrentHashMap<String, ManufactoringPlan>();
    	defTool = new ConcurrentHashMap<String, ConcurrentLinkedQueue<Deferred<Tool>>>();
    	defTool.put("np-hammer", new ConcurrentLinkedQueue<Deferred<Tool>>());
    	defTool.put("gs-driver", new ConcurrentLinkedQueue<Deferred<Tool>>());
    	defTool.put("rs-pliers", new ConcurrentLinkedQueue<Deferred<Tool>>());
    }

	/**
	* Tool acquisition procedure
	* Note that this procedure is non-blocking and should return immediatly
	* @param type - string describing the required tool
	* @return a deferred promise for the  requested tool
	*/
    public Deferred<Tool> acquireTool(String type){
		if(toolSuply.get(type) == null)
			try { throw new Exception("No such Tool");}
				catch (Exception e) {e.printStackTrace();}
		Tool t = null;
		if(type.equals("gs-driver"))
			t = new GcdScrewDriver();
		else if (type.equals("np-hammer"))
			t = new NextPrimeHammer();
		else if(type.equals("rs-pliers"))
			t = new RandomSumPliers();
		else
			try { throw new Exception("badTool"); }
				catch (Exception e) { e.printStackTrace(); }
    	Deferred<Tool> ans = new Deferred<Tool>();
    	ans.whenResolved(()->{
    		int newQty = toolSuply.get(type).intValue() - 1;
    		toolSuply.put(type, newQty);
    	});
    	if(toolSuply.get(type) > 0)
    		ans.resolve(t);
    	else
    		defTool.get(type).add(ans);
		return ans;
    }

	/**
	* Tool return procedure - releases a tool which becomes available in the warehouse upon completion.
	 * synchronized - to avoid deadlock incase of many prosseses releasing the same tool type
	* @param tool - The tool to be returned
	*/
    public synchronized void releaseTool(Tool tool){
    	int newQty = toolSuply.get(tool.getType()).intValue() + 1;
    	toolSuply.put(tool.getType(), newQty );
    	if(!defTool.get(tool.getType()).isEmpty())
    		defTool.get(tool.getType()).poll().resolve(tool);
    }

	
	/**
	* Getter for ManufactoringPlans
	* @param product - a string with the product name for which a ManufactoringPlan is desired
	* @return A ManufactoringPlan for product
	*/
    public ManufactoringPlan getPlan(String product){
		return mplans.get(product);
	}
	
	/**
	* Store a ManufactoringPlan in the warehouse for later retrieval
	* @param plan - a ManufactoringPlan to be stored
	*/
    public void addPlan(ManufactoringPlan plan){
    	mplans.put(plan.getProductName(), plan);
    }
    
	/**
	* Store a qty Amount of tools of type tool in the warehouse for later retrieval
	* @param tool - type of tool to be stored
	* @param qty - amount of tools of type tool to be stored
	*/
    public void addTool(Tool tool, int qty){
    	toolSuply.put(tool.getType(), toolSuply.get(tool.getType())+qty);
    }

}
