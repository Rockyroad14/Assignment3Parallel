#include <list>
#include <iostream>
#include <string>
#include <cstdlib>
#include <ctime>
#include <mutex>
#include <random>
#include <algorithm>
#include <vector>
#include <thread>
#include <mutex>

#define NUM_THREADS 4
#define HASH_SIZE 100
#define MAX 500000

using namespace std;

struct HeadNode
{
    Node* next;
    std::mutex m;

    HeadNode() : next(nullptr) {}
};

struct Node
{
    int id;
    Node* next;
    std::mutex m;

    Node(int id) : id(id), next(nullptr) {}
};

std::vector<thread> threads;
Node *head = nullptr;
int* arr;
atomic<int> index(0);
std::mutex arrayLock;
atomic<bool> flag(true);


void servant()
{
    int localIndex;
    int localValue;
    // Loop until list is empty
    while(flag)
    {
        // Seed the random number generator
        srand(time(0));
        // Make a random choice between 0-2 for the operation
        int choice = rand() % 3;

        arrayLock.lock();
        localIndex = index;
        index++;
        localValue = arr[localIndex];
        arrayLock.unlock();

        






    }

}


int* createArray(int* arr)
{
    for (int i = 0; i < MAX; i++)
    {
        arr[i] = i + 1;
    }

    // shuffle the array
    srand(time(0));

    for (int i = 0; i < MAX; i++)
    {
        int index = rand() % MAX;
        int index2 = rand() % MAX;
        swap(arr[index], arr[index2]);
    }

    return arr;
}

int main(void)
{
    // create an array of 500,000 random positive non repeating integers
    arr = (int*) malloc(MAX * sizeof(int));

    arr = createArray(arr);


    for(int i = 0; i < NUM_THREADS; i++)
    {
        threads.push_back(thread(servant, i));
    }

    for(auto& t : threads)
    {
        t.join();
    }

    
    
    free(arr);

    return 0;
}