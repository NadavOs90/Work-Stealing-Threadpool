package bgu.spl.a2.sim.tasks;

import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicInteger;
import bgu.spl.a2.Deferred;
import bgu.spl.a2.Task;
import bgu.spl.a2.sim.Product;
import bgu.spl.a2.sim.Warehouse;
import bgu.spl.a2.sim.conf.ManufactoringPlan;
import bgu.spl.a2.sim.tools.Tool;

/**
 * @author Nadav
 *
 */
public class ManufactureTask extends Task<Product> {
	
	private Product product;
	private Warehouse ware;
	private ConcurrentLinkedDeque<Task<Product>> parts;

	public ManufactureTask(Product p, Warehouse storage) {
		product = p;
		ware = storage;
		parts = new ConcurrentLinkedDeque<Task<Product>>();
	}
	
	/**
	 * Acquires the tools according to the manufacturing plan
	 * uses all the tools
	 * when it finishes the function adds the results to the final id
	 * @param mp - the plan that contains a list of tools
	 */
	private void useTools(ManufactoringPlan mp){
		AtomicInteger counter = new AtomicInteger(mp.getTools().length);
		if(counter.get() == 0){
			complete(product);
		}
		for(int i=0; i<mp.getTools().length; i++){
			Deferred<Tool> temp = ware.acquireTool(mp.getTools()[i]);
			temp.whenResolved(()->{
				product.addToFinalId(temp.get().useOn(product));
				ToolReleaseTask toolToRelease = new ToolReleaseTask(temp.get(),ware);
				spawn(toolToRelease);
				if(counter.decrementAndGet() == 0){
					complete(product);
				}
			});
		}
	}

	@Override
	protected void start() {
		ManufactoringPlan mp = ware.getPlan(product.getName());
		if(mp.getParts().length == 0){
			useTools(mp);
		}
		else{
			ManufactureTask[] spawns = new ManufactureTask[mp.getParts().length];
			for(int i=0; i<mp.getParts().length; i++){
				Product temp = new Product(product.getStartId()+1, mp.getParts()[i]);
				product.addPart(temp);
				ManufactureTask newTask = new ManufactureTask(temp, ware);
				spawns[i] = newTask;
				parts.add(newTask);
			}
			spawn(spawns);
			whenResolved(parts, ()->{
				useTools(mp);
			});	
		}
	}
}
