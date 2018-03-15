package bgu.spl.a2.sim.tools;

import bgu.spl.a2.sim.Product;
import java.math.BigInteger;


/**
 * @author ostrowsk
 *
 */
public class GcdScrewDriver implements Tool {

	/* (non-Javadoc)
	 * @see bgu.spl.a2.sim.tools.Tool#getType()
	 */
	public String getType() {
		return "gs-driver";
	}

	/* (non-Javadoc)
	 * @see bgu.spl.a2.sim.tools.Tool#useOn(bgu.spl.a2.sim.Product)
	 */
	private long func(long id){
    	BigInteger b1 = BigInteger.valueOf(id);
        BigInteger b2 = BigInteger.valueOf(reverse(id));
        long value= (b1.gcd(b2)).longValue();
        return value;
    }

	/**
	 * reverses the order of the digits of a long
	 * @param id - the long to reverse
	 * @return - the reversed long id
	 */
	private long reverse(long id) {
		long ans = 0;
	    while( id != 0 ){
	        ans = ans * 10;
	        ans = ans + (id % 10);
	        id = id / 10;
	    }
	    return ans;
	}

	public long useOn(Product p) {
		long ans = 0;
		for(Product part : p.getParts()){
    		ans+=Math.abs(func(part.getFinalId()));
    	}
		return ans;
		
	}
}
