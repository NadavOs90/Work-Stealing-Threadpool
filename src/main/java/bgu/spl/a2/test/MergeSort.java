package bgu.spl.a2.test;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.util.Arrays;
import java.util.Random;
import java.util.Vector;
import java.util.concurrent.CountDownLatch;

import bgu.spl.a2.Task;
import bgu.spl.a2.WorkStealingThreadPool;

public class MergeSort extends Task<int[]> {
	
    private final int[] array;

    public MergeSort(int[] array) {
        this.array = array;
    }

    @Override
    protected void start() {
    	int length = array.length;
    	if(length >= 2){
    		MergeSort leftTask = new MergeSort(Arrays.copyOfRange(array, 0, length/2));
    		MergeSort rightTask = new MergeSort(Arrays.copyOfRange(array, length/2, length));
    		spawn(leftTask,rightTask);
    		Vector<MergeSort> sons = new Vector<MergeSort>();
    		sons.add(leftTask);
    		sons.add(rightTask);
    		whenResolved(sons, ()->{
    			complete(merge(leftTask.getResult().get(),rightTask.getResult().get()));
    		} );
    	}else
    	{
    		complete(array);
    	}
    }

    public int[] merge(int[] a, int[] b) {
        int[] answer = new int[a.length + b.length];
        int i = a.length - 1, j = b.length - 1, k = answer.length;

        while (k > 0)
            answer[--k] = 
                (j < 0 || (i >= 0 && a[i] >= b[j])) ? a[i--] : b[j--];
                
        return answer;
    }
    
        public static boolean testMe(int[] arr){
		for(int i=1;i<arr.length;i++){
			if(arr[i] < arr[i-1])
				return false;
		}
		return true;
	}
	public static void main(String[] args) {
		int steps = 0;
		
		int loops = 200; // while loops 
		int size = 2000; // array size ->1
		int tasksAmount = 10; // amount of tasks per loop ->10
		int threads =4; // number of threads ->10
		long sumTime = 0;
		
		int[] pleaseWork = new int[size];
		Random r = new Random();
		
		for(int i = 0; i < size; i++)
			pleaseWork[i] = r.nextInt(2000);
		
		boolean keepGoing = true;
		do{
			WorkStealingThreadPool pool = new WorkStealingThreadPool(threads);
			Vector<MergeSort> tasks = new Vector<MergeSort>();
			for(int i = 0; i < tasksAmount; i++){
				tasks.add(new MergeSort(pleaseWork));
			}
			
			
			CountDownLatch l = new CountDownLatch(tasksAmount);
			pool.start();
			long startTime = System.currentTimeMillis();
			for(int i=0; i< tasksAmount; i++){
				pool.submit(tasks.elementAt(i));
			}
			
			for(int i = 0; i < tasksAmount; i++){
				tasks.elementAt(i).getResult().whenResolved(()->{l.countDown();});
			}
			

			long stopTime;
			long totalTime = 0;
			try {
				l.await();
				
			} catch (InterruptedException e) {
				e.printStackTrace();
			} 
			stopTime = System.currentTimeMillis();
			totalTime = stopTime - startTime;
			sumTime += totalTime;
			try {
				pool.shutdown();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			for(int i=0;i<tasksAmount;i++){
				keepGoing = keepGoing && testMe(tasks.elementAt(i).getResult().get()) && tasks.elementAt(i).getResult().get().length == size;
			}

			System.out.println("test " + steps + " " + keepGoing + " it took " + totalTime + " ms");
			steps++;
		}while(keepGoing && steps < loops);
		if(keepGoing)
			System.out.println("All Tests Passed!!! total time is: " + sumTime );
		else
			System.out.println("test " + steps+ " failed!!!");
	}

}
