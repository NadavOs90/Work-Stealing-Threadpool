/**
 * 
 */
package bgu.spl.a2.sim.tools;

import java.util.Random;

import bgu.spl.a2.sim.Product;

/**
 * @author ostrowsk
 *
 */
public class RandomSumPliers implements Tool {

	/* (non-Javadoc)
	 * @see bgu.spl.a2.sim.tools.Tool#getType()
	 */
	public String getType() {
		return "rs-pliers";
	}

	/* (non-Javadoc)
	 * @see bgu.spl.a2.sim.tools.Tool#useOn(bgu.spl.a2.sim.Product)
	 */
	public long useOn(Product p) {
		long ans = 0;
		for(Product part : p.getParts()){
    		ans+=Math.abs(func(part.getFinalId()));
    		
    	}
     	return ans;	
	}
	
	private long func(long id){
		Random rand = new Random(id);
		long ans = 0;
		for(long i=0; i<id%10000; i++)
			ans += rand.nextInt();
		return ans;
	}

}
