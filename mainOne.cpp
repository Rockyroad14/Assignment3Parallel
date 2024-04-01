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

#define NUM_THREADS 4
#define MAX 500000

using namespace std;

std::vector<thread> threads;


struct Node
{
    int id;
    Node* next;
    std::mutex m;

    Node(int id) : id(id), next(nullptr) {}
};

void servant(int id)
{

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
        int index = rand() % MAX + 1;
        int index2 = rand() % MAX + 1;
        swap(arr[index], arr[index2]);
    }

    return arr;
}

int main(void)
{
    // create an array of 500,000 random positive non repeating integers
    std::list<Node> list;
    int* arr = (int*) malloc(MAX * sizeof(int));

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