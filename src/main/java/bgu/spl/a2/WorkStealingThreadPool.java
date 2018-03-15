package bgu.spl.a2;


import java.util.Random;
import java.util.concurrent.ConcurrentLinkedDeque;

public class WorkStealingThreadPool {
	private ConcurrentLinkedDeque<Task<?>>[] deques;
	private Thread[] Tprocessors;
	private Processor[] processors;
	private VersionMonitor versionMonitor;
	private boolean shutDown = false;

	
    /**
     * creates a {@link WorkStealingThreadPool} which has nthreads
     * {@link Processor}s. Note, threads should not get started until calling to
     * the {@link #start()} method.
     *
     * Implementors note: you may not add other constructors to this class nor
     * you allowed to add any other parameter to this constructor - changing
     * this may cause automatic tests to fail..
     *
     * @param nthreads the number of threads that should be started by this
     * thread pool
     */
    @SuppressWarnings("unchecked")
	public WorkStealingThreadPool(int nthreads) {
    	deques = new ConcurrentLinkedDeque[nthreads];
    	Tprocessors = new Thread[nthreads];
    	processors = new Processor[nthreads];
    	versionMonitor = new VersionMonitor();
    	
    	for(int i=0; i < nthreads; i++){
    		deques[i] = new ConcurrentLinkedDeque<Task<?>>();
    		processors[i] = new Processor(i,this);
    		Tprocessors[i] = new Thread(processors[i]); 
    	}
    }

    /**
     * submits a task to be executed by a processor belongs to this thread pool
     *
     * @param task the task to execute
     */
    public void submit(Task<?> task) {
    	Random r = new Random();
    	int index = r.nextInt(processors.length);
    	addFirst(index,task);
    }

    /**
     * closes the thread pool - this method interrupts all the threads and wait
     * for them to stop - it is returns *only* when there are no live threads in
     * the queue.
     *
     * after calling this method - one should not use the queue anymore.
     *
     * @throws InterruptedException if the thread that shut down the threads is
     * interrupted
     * @throws UnsupportedOperationException if the thread that attempts to
     * shutdown the queue is itself a processor of this queue
     */
    public void shutdown() throws InterruptedException, UnsupportedOperationException {
    	for(int i = 0; i< Tprocessors.length;i++)
    		if(Thread.currentThread() == Tprocessors[i])
    			throw new UnsupportedOperationException("attempting to shut the WorkStealingThreadPool by using its own processors");
    	
    	this.shutDown = true;
     	this.versionMonitor.inc();
       
        for(int i = 0; i < processors.length; i++)
        	Tprocessors[i].interrupt();
       
        for(int i = 0; i < processors.length; i++)
        	Tprocessors[i].join();
  
        for(int i = 0; i < deques.length; i++)
        	deques[i].clear();
    }

    /**
     * start the threads belongs to this thread pool
     */
    public void start() {
    	for(Thread t: Tprocessors)
    		t.start();    	
    }

    /*
     * polls a task from the processor where (id = index).
     * if task is null -> dequeue is empty then return false.
     * 	else handle task. 
     */
	boolean handle(int index) {
		Task<?> temp = deques[index].pollFirst();
		if(temp != null){
			temp.handle(processors[index]);
			return true;
		}
		return false;
	}

	/**
	 * this method find a processor to steal tasks from.
	 * @param index - id of the processor which wants to begin the stealing process
	 * @param version - the version of this.versionMonitor on which to lock the thread in case no stealing will occure.
	 * @throws InterruptedException - from the this.versionMonitor.await method
	 */
	void steal(int index, int version) throws InterruptedException {
		int numOfDeques = deques.length;
		int stealFrom = (index + 1) % numOfDeques;
		boolean stole = false;

		while (stealFrom != index && !stole){
			int length = deques[stealFrom].size();
			if(length > 1){
				stealUpTo(length/2,stealFrom,index);
				stole = true;
			}
			else
				stealFrom = (stealFrom + 1) % numOfDeques;
		}
		if(!stole && !shutDown){
				this.versionMonitor.await(version);
		}	

	}
	
	/**
	 * this method steals up to tasksToSteal tasks from stealFrom and gives them to giveTo
	 * @param tasksToSteal - max number of tasks to steal
	 * @param stealFrom - processor to steal tasks from.
	 * @param giveTo - processor to give tasks to.
	 */
	private void stealUpTo(int tasksToSteal, int stealFrom, int giveTo) {	
		boolean keepGoing = true;
		while(keepGoing && tasksToSteal != 0){
			Task<?> temp = deques[stealFrom].pollLast();
			if(temp != null){
				addFirst(giveTo, temp);
				tasksToSteal--;
			}
			else
				keepGoing = false;
		}

	}
	
	/**
	 * adds 'task' to processor with id = 'addTo' 
	 * @param addTo
	 * @param task
	 */
	void addFirst(int addTo , Task<?> task){
		deques[addTo].addFirst(task);
		this.versionMonitor.inc();//after adding update version monitor.
	}

	//returns the current version of the versionMonitor.
	int getCurrVersion() {
		return this.versionMonitor.getVersion();
	}





}
