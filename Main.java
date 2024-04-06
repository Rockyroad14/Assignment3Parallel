// Assignment 3 Part 1
// Author: Jared Reich

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.Random;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.atomic.AtomicInteger;
import java.io.PrintWriter;
import java.io.File;
import java.io.FileNotFoundException;

public class Main 
{
    private static final int gifts = 500000; 
    private static final int THREADS = 4;
    private static final AtomicInteger insertCounter = new AtomicInteger(0);
    private static final AtomicInteger deleteCounter = new AtomicInteger(0);
     
    private static final int checkPoint = gifts / 100; 
    private static volatile int nextCheckPoint = checkPoint; 


    public static void beginProgram(LinkedList<Integer> sharedList, int sectionSize) throws FileNotFoundException
    {
        Thread[] threads = new Thread[THREADS];

        // Start threads
        startThreads(threads, sharedList, sectionSize);

        // Progress checking and reporting loop
        checkProgress();

        // Wait for all threads to complete
        joinThreads(threads);

    }
    
    // Starting threads
    public static void startThreads(Thread[] threads, LinkedList<Integer> sharedList, int sectionSize) throws FileNotFoundException
    {
        for (int i = 0; i < threads.length; i++) 
        {

            int start = i * sectionSize;
            int end = (i + 1) * sectionSize - 1;

            if (i == THREADS - 1) 
            {
                end = gifts - 1; // Adjust for the last section
            }

            // Every iteration of the loop creates a new thread with new Servant object
            Servant servant = new Servant(sharedList, start, end, insertCounter, deleteCounter, "output" + i + ".txt");

            threads[i] = new Thread(servant::runServant);

            // Start the thread
            threads[i].start();
        }
    }

    // Joining threads back together
    public static void joinThreads(Thread[] threads) 
    {
        for (Thread t : threads) 
        {
            try 
            {
                t.join();
            } 
            
            catch (InterruptedException e) 
            {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    // Extra thread used to check on progress and debugging purposes.
    public static void checkProgress()
    {
        new Thread(() -> 
        {
            while (nextCheckPoint <= gifts) 
            {
                int combinedTotal = insertCounter.get() + deleteCounter.get();

                if (combinedTotal >= nextCheckPoint) 
                {
                    System.out.println("Percent Done: " + ((combinedTotal  / 5000) - 1) + "%");
                    nextCheckPoint += checkPoint;
                }
                
                try 
                {
                    Thread.sleep(100); // Check every 100 milliseconds
                } 
                
                catch (InterruptedException e) 
                {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }).start();
    }

    

    public static void main(String[] args) throws FileNotFoundException  
    {
        LinkedList<Integer> sharedList = new LinkedList<>();
        int sectionSize = gifts / THREADS; // Divide work among threads
        long startTime = System.currentTimeMillis();
        
        beginProgram(sharedList, sectionSize);

        long endTime = System.currentTimeMillis();
        long runTime = endTime - startTime;
        System.out.println("Total Time is " + (runTime / 1000) + " seconds");
        System.out.println("Gifts Added: " + insertCounter.get() + "\nGifts Deleted: " + deleteCounter.get());
    }
}



class Servant 
{

    private final LinkedList<Integer> giftChain;
    private final int startBatch;
    private final int endBatch;
    private final Random random = new Random();
    private final AtomicBoolean allRemoved = new AtomicBoolean(false);
    private static AtomicInteger totalInserts = new AtomicInteger(0);
    private static AtomicInteger totalDeletes = new AtomicInteger(0);
    private PrintWriter fileWriter;


    public Servant(LinkedList<Integer> giftChain, int start, int end, AtomicInteger totalInserts, AtomicInteger totalDeletes, String outputFileName) throws FileNotFoundException 
    {
        
        this.fileWriter = new PrintWriter(new File(outputFileName));
        this.startBatch = start;
        this.endBatch = end;
        this.giftChain = giftChain;
        
        
        Servant.totalInserts = totalInserts;
        Servant.totalDeletes = totalDeletes;
        
    }

    

    public void clearHashSets(HashSet<Integer> addedGifts, HashSet<Integer> removedGifts) 
    {
        addedGifts.clear();
        removedGifts.clear();
    }

    public void runServant() 
    {
        int batchStart = startBatch / 1000;

        int batchEnd = endBatch / 1000;

        HashSet<Integer> addedGifts = new HashSet<>(125000);

        HashSet<Integer> removedGifts = new HashSet<>(125000);

        // Let threads deal with inserting and deleteing in batches of 1000
        for (int batch = batchStart; batch <= batchEnd; batch++) 
        {
            // Minor adjustments to ensure that the last batch is handled correctly
            int startValue = batch * 1000 + 1;
            int endValue = (batch + 1) * 1000;

            // Checking if batches are finsihed in hashset
            while (addedGifts.size() < 1000 || removedGifts.size() < 1000) 
            {
                int randomOperation = random.nextInt(3) + 1;
                int value = startValue + random.nextInt(endValue - startValue + 1);

                doOperation(randomOperation, value, addedGifts, removedGifts);
            }

            clearHashSets(addedGifts, removedGifts);
        }

        allRemoved.set(true);

        if (fileWriter != null) 
        {
            fileWriter.close();
        }
    }

    boolean doOperation(int operation, int value, HashSet<Integer> addedGifts, HashSet<Integer> removedGifts)
    {
        boolean result = false;

        if (operation == 1) {
            // Add
            if (addedGifts.add(value)) {
                giftChain.add(value);
                fileWriter.println(value + " Added");
                totalInserts.incrementAndGet();
                result = true;
            }
        }
        else if(operation == 2) 
        {
            // Remove
            if (addedGifts.contains(value) && giftChain.remove(value)) 
            {
                removedGifts.add(value);
                fileWriter.println(value);
                totalDeletes.incrementAndGet();
                result = true;
            }
        }
        else if(operation == 3) 
        {
            // Search for gifts
            if (giftChain.contains(value)) 
            {
                fileWriter.println("List contains: " + value);
                result = true;
            }
        }
        else 
        {
            System.out.println("Invalid operation: " + operation);
            result = false;
        }
        
        return result;
    }



}



// Node class for the linked list
class Node<T> 
{
    T item;
    int key;
    Node<T> next;
    Lock lock = new ReentrantLock();
 
    // handle null items and create node constructor
    Node(T item) 
    {
       this.item = item;
       this.key = (item != null) ? item.hashCode() : 0; // Use 0 or another value for null items
    }
    
    
    void lock() 
    {
       lock.lock();
    }
 
    void unlock() 
    {
       lock.unlock();
    }
}
 
class LinkedList<T> 
{
    private Node<T> head;
 
    public LinkedList() 
    {
       
       head = new Node<>(null); 
       head.key = Integer.MIN_VALUE;
 
       
       Node<T> tail = new Node<>(null); 
       tail.key = Integer.MAX_VALUE;
 
       head.next = tail; 
    }
 
    
    // Remove a node from the list by locking the nodes around the node to be removed.
    public boolean remove(T item) 
    {
       int key = (item != null) ? item.hashCode() : 0;
       head.lock();
       Node<T> pred = head;
       try 
       {
          Node<T> curr = pred.next;
          curr.lock();
          try 
          {
             while (curr.key < key) 
             {
                pred.unlock();
                pred = curr;
                curr = curr.next;
                curr.lock();
             }
             if (curr.key == key) 
             {
                pred.next = curr.next;
                return true;
             }
             return false;
          } 
          finally 
          {
             curr.unlock();
          }
       } 
       finally 
       {
          pred.unlock();
       }
    }

    // Add the item to the list by proceding through the list and adding the item in the correct position with locks.
    public boolean add(T item) 
    {
       int key = (item != null) ? item.hashCode() : 0;
       head.lock();
       Node<T> pred = head;
       try 
        {
          Node<T> curr = pred.next;
          curr.lock();
          try 
          {
             while (curr.key < key) 
             {
                pred.unlock();
                pred = curr;
                curr = curr.next;
                curr.lock();
             }
             if (curr.key == key) 
             {
                return false;
             }
             Node<T> newNode = new Node<>(item);
             newNode.next = curr;
             pred.next = newNode;
             return true;
          } 
          finally 
          {
             curr.unlock();
          }
       } 
       finally 
       {
          pred.unlock();
       }
    }
 
    // validate the node is in the list and hasnt been deleted between the time it was found and not locked. Optimistic search
    public boolean validate(Node<T> pred, Node<T> curr) 
    {
       Node<T> node = head;
       while (node.key <= pred.key) 
       {
           if (node == pred)
               return pred.next == curr;
           node = node.next;
       }
       return false;
   }
 
   // using locks search the list for the item, using no locks is Optimistic Searching.
    public boolean contains(T item) 
    {
       int key = item.hashCode();

       head.lock();
       Node<T> pred = head; 

       try 
       {
           Node<T> curr = pred.next;
           curr.lock();
           try 
           {
               while (curr.key < key) 
               {
                   pred.unlock();
                   pred = curr;
                   curr = curr.next;
                   curr.lock();
               }
               return curr.key == key;
           } 
           finally 
           {
               curr.unlock();
           }
       } 
       finally 
       {
           pred.unlock();
       }
   }
 
}