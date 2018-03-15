/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bgu.spl.a2.sim;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;

import bgu.spl.a2.sim.tasks.ManufactureTask;
import com.google.gson.Gson;
import bgu.spl.a2.WorkStealingThreadPool;
import bgu.spl.a2.sim.conf.ManufactoringPlan;
import bgu.spl.a2.sim.tools.GcdScrewDriver;
import bgu.spl.a2.sim.tools.NextPrimeHammer;
import bgu.spl.a2.sim.tools.RandomSumPliers;
import bgu.spl.a2.sim.tools.Tool;


/**
 * A class describing the simulator for part 2 of the assignment
 */
public class Simulator {
	
	private static WorkStealingThreadPool pool;
	private static Warehouse storage;
	private static Waves[][] wStrings;
	

	/**
	* Begin the simulation
	* Should not be called before attachWorkStealingThreadPool()
	*/
    public static ConcurrentLinkedQueue<Product> start() {
    	ConcurrentLinkedQueue<Product> ans = new ConcurrentLinkedQueue<Product>();
    	pool.start();
		for (Waves[] currentProductList : wStrings) {
    		int numTasks = Arrays.stream(currentProductList)
    				.mapToInt((specificItem) -> specificItem.getQty())
    				.sum();
    		CountDownLatch latch = new CountDownLatch(numTasks);
    		ArrayList<ManufactureTask> tasks = new ArrayList<>();
    		for(int j=0; j<currentProductList.length;j++){
    			for(int k=0; k<currentProductList[j].getQty(); k++){
    				tasks.add(createTask(ans,currentProductList[j],k,latch));
    			}
    		}
    		for(ManufactureTask myTask : tasks){
    			pool.submit(myTask);
    		}
			//Now we can wait for a wave to finish
			try { latch.await(); }
				catch (InterruptedException e) { e.printStackTrace(); } 
    	}
    	try { pool.shutdown(); }
			catch (InterruptedException e) { e.printStackTrace(); }
		return ans;
	}
    
    /**
     * 
     * @param ans - the list of the completed products
     * @param currentProd - the current wave
     * @param k - the number to add to the start id of the product
     * @param l - the count down latch
     * @return - a task for the work steeling thread pool 
     */
	private static ManufactureTask createTask(ConcurrentLinkedQueue<Product> ans, Waves currentProd, int k,CountDownLatch l) {
		long startId = currentProd.getStartId() + k;
		String prodName = currentProd.getProduct();
		ManufactureTask myTask = createTask(startId, prodName, storage, ans);
		myTask.getResult().whenResolved(()->{
			l.countDown();
		});
		return myTask;
	}
	
	/**
	 * 
	 * @param startId
	 * @param pName
	 * @param storage
	 * @return - the ManufactureTask for the product according to the parameters
	 */
	private static ManufactureTask createTask(long startId,String pName, Warehouse storage, ConcurrentLinkedQueue<Product> ans) {
		Product product = new Product(startId, pName);
		ans.add(product);
		return new ManufactureTask(product ,storage);
	}
	
	/**
	* attach a WorkStealingThreadPool to the Simulator, this WorkStealingThreadPool will be used to run the simulation
	* @param myWorkStealingThreadPool - the WorkStealingThreadPool which will be used by the simulator
	*/
	public static void attachWorkStealingThreadPool(WorkStealingThreadPool myWorkStealingThreadPool) {
		pool = myWorkStealingThreadPool;
	}
	
	/**
	 * adds tools from the config file to the warehouse
	 * @param tools
	 */
	private static void addTools(Tools[] tools) {
		for(Tools t : tools){
			Tool tool = null;
			if(t.getTool().equals("gs-driver"))
				tool = new GcdScrewDriver();
			else if (t.getTool().equals("np-hammer"))
				tool = new NextPrimeHammer();
			else if(t.getTool().equals("rs-pliers"))
				tool = new RandomSumPliers();
			else
				try {
					throw new Exception("badTool");
				} catch (Exception e) {
					e.printStackTrace();
				}
			storage.addTool(tool, t.getQty());
		}
	}
	
	/**
	 * adds plans from the config file to the warehouse
	 * @param plans
	 */
	private static void addPlans(Plan[] plans) {
		for(Plan plan : plans){
			ManufactoringPlan mp = new ManufactoringPlan(plan.getProduct(), plan.getParts(),plan.getTools());
			storage.addPlan(mp);
		}	
	}
	
	public static void main(String [] args){
		Gson gson = new Gson();
		JsonObject o = null;
		BufferedReader jsonFile = null;
		try{
			String fileName = "sample2.json";
			if(args.length > 0)
				fileName = args[0];
			jsonFile = new BufferedReader(new FileReader(fileName));
			o = gson.fromJson(jsonFile, JsonObject.class);
			jsonFile.close();
		} catch (IOException e){System.out.println("Error reading File!!");} 
		if(o == null) System.out.println("null Jason object");
		storage = new Warehouse();
		Plan[] plans = o.getPlans();
		wStrings = o.getWaves();
		Tools[] tools = o.getTools(); 
		addTools(tools);
		addPlans(plans);
		WorkStealingThreadPool deadPool = new WorkStealingThreadPool(o.getThreads());
		attachWorkStealingThreadPool(deadPool);
		ConcurrentLinkedQueue<Product> SimulationResult;
		SimulationResult = start();
		FileOutputStream fout = null;
		ObjectOutputStream oos = null;
		try { fout = new FileOutputStream("result.ser"); }
			catch (FileNotFoundException e) { e.printStackTrace(); }
		try { oos = new ObjectOutputStream(fout); }
			catch (IOException e) { e.printStackTrace(); }
		try { oos.writeObject(SimulationResult); }
			catch (IOException e) { e.printStackTrace(); }
		try{
			oos.flush();
			oos.close();
		}
		catch (IOException e) { e.printStackTrace(); }
		try { fout.close(); }
		catch (IOException e) { e.printStackTrace(); }
	}
}
