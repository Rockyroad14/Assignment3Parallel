# Parallel Assignment 3
## Part 1:
In this part of assignment, the minotaur asks to have his servants take random gifts and insert into a linked list. Also look randomly for gifts and delete once writing a thank you card.
### How to run:
To run the program use `javac Main.java` and `java Main` as the two commands to run the programs

Expected Output:
```
Percent Done: 1%

...

Percent Done: 100%
Total Time is 20 seconds
Gifts Added: 500000
Gifts Deleted: 500000
```

### Implementation and Proof of Correctness:
The whole program relies on the Locking mechanism used in the linked list. In the add function, removes, and contains. While traversing the list 2 nodes are locked at a given time which is __Fine Grained Synchronization__. Every thread is given this functions calls which might wait for a lock. With searching the Linked List we use __Optimistic Synchronization__ which searches without the use of locks since we are not modifying values. A separate function is called to validate that the node is indeed inside the list using a lock/mutex. Each thread is given a random set of the 500000 gifts from the bag and then performs the operations in batches to reduce collisions and contention between threads.


### Efficiency of Program
In terms of runtime the time complexity of add, remove and contain are linear O(N) operations but with threading involved linear time is not guaranteed, if only in a best case scenario. The use of Locks creates contention between the threads that are either adding or removing. Space Complexity is O(N) as well with each node consuming space in memory.

### Experimental Evaluation:

Within the output is the given runtime of the program which starts the internal clock at first call of the threads. Once the threads are joined back in the function the clock is stopped and converted to seconds.
The average runtime of the program with a computer hardware of an overclocked 4 core 8 thread cpu to 4.5 GHZ is 20 Seconds.

## Part 2:
This part requires that 8 threads read at regular intervals and compile the temperature data into highest temps, lowest temps and readings taken every minute.

### How to Run:
Run by compiling `g++ Main2.cpp` and run by `./a.exe`

```
Minites: 10: Highest Temp = 66, Lowest Temp = -100
Minites: 11: Highest Temp = 69, Lowest Temp = -100
Minites: 13: Highest Temp = 70, Lowest Temp = -100
Minites: 14: Highest Temp = 70, Lowest Temp = -100
Minites: 15: Highest Temp = 61, Lowest Temp = -100
Finished in 0.0397665Seconds
Highest Temperatures:
---------------------
|70 |69 |68 |66 |65 |
---------------------

Lowest Temperatures:
---------------------------
|-100 |-99 |-98 |-97 |-96 |
---------------------------
```
Possible Expected Ouput

### Implementation and Proof of correctness:
* Report Generation: The program generates a report at the end of every hour, as specified in the prompt. The report includes the top 5 highest temperatures, top 5 lowest temperatures, and the 10-minute interval with the largest temperature difference.
* Temperature Readings: The program simulates temperature readings every minute using 8 temperature sensors. The readings are stored accurately in shared memory space.
* Temperature Calculation: The program correctly calculates the highest and lowest temperatures for each hour and identifies the interval with the largest temperature difference.
* Locks Safety: Mutexes are used to ensure thread safety when accessing shared resources, preventing race conditions and data corruption.
* Data Integrity: The program maintains the integrity of temperature readings by properly synchronizing access to shared memory using mutexes.
* Progress Guarantee: The program simulates temperature readings for 60 minutes, generating a report at the end of each hour, demonstrating progress guarantee.

### Efficiency of Design:

Since all threads are grabing a sinmple output in parallel and only calling one function that reads and does not write output the threads encounter almost no collisions and contentions. There is a shared memory space with mutexes.

### Experimental Evaluation:

The experimental evaluation of this program ran the program in a average time of `0.03 seconds`

