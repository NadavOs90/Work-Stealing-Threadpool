/**
 * 
 */
package bgu.spl.a2.sim.tools;

import bgu.spl.a2.sim.Product;

/**
 * @author ostrowsk
 *
 */
public class NextPrimeHammer implements Tool {

	/* (non-Javadoc)
	 * @see bgu.spl.a2.sim.tools.Tool#getType()
	 */
	public String getType() {
		return "np-hammer";
	}

	/* (non-Javadoc)
	 * @see bgu.spl.a2.sim.tools.Tool#useOn(bgu.spl.a2.sim.Product)
	 */
	private long func(long id) {
        long v = id + 1;
        while (!isPrime(v)) {
            v++;
        }
        return v;
    }
	
    private boolean isPrime(long value) {
        if(value < 2) return false;
    	if(value == 2) return true;
        long sq = (long) Math.sqrt(value);
        for (long i = 2; i <= sq; i++) {
            if (value % i == 0) {
                return false;
            }
        }
        return true;
    }
	
	public long useOn(Product p) {
		long ans = 0;
		for(Product part : p.getParts()){
    		ans+=Math.abs(func(part.getFinalId()));
    		
    	}
		return ans;
	}

}
