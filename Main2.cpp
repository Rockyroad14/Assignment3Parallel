// Name: Jared Reich

#include<iostream>
#include<random>
#include<cmath>
#include<algorithm>
#include<vector>
#include<thread>
#include<mutex>
#include<numeric>
#include<chrono>
#include<set> 

using namespace std;

mutex mtx; 

void generateTemperature(int& reading);
// For Generating temperature readings
int getRandomValue();


class MarsModule 
{
    private:
        mutex mtx_lowest, mtx_highest, mtx_interval; 
        int readings[8] = {0};
        set<int> all_readings;
    public:
        int fiveLowestTemp[5];
        int fiveHighestTemp[5];

        int highest_interval_val = -100;
        int lowestTempVal = 70; 
        int total_interval_distance = 0;
        int current_highest_interval = 0;

    
    //constructor
    MarsModule() {}

    // Starting the Threads
    void startThreads(vector<thread>& threads) 
    {
        for(int i = 0; i < 8; ++i) 
        {
            threads[i] = thread(generateTemperature, ref(readings[i]));
        }
    }

    // Join the threads
    void joinThreads(vector<thread>& threads) 
    {
        for(auto& th : threads) 
        {
            if(th.joinable()) {
                th.join();
            }
        }
    }
    // Print the hourly report
    void printHourReport(int interval, vector<int>& interval_max, vector<int>& interval_min)
    {
        cout << "Minites: " << interval + 10 << ": Highest Temp = " << interval_max[interval]
             << ", Lowest Temp = " << interval_min[interval] << endl;
    }

    // Calculate the highest and lowest temperatures
    void calculateTemps(vector<int>& hourMax, vector<int>& hourMin)
    {
        for(int i = 0; i < 6; ++i) 
        {
            int interval_difference = hourMax[i] - hourMin[i];
            if(interval_difference > total_interval_distance) 
            {
                total_interval_distance = interval_difference;
                current_highest_interval = i;
                highest_interval_val = hourMax[i];
                lowestTempVal = hourMin[i];
            }
        }
    }

    // Begin the module for Mar Rover
    void beginModule() 
    {
        int interval = 0;
        vector<int> hourMax(6, -101), hourMin(6, 71);

        // Loop to simulate 60 minutes
        for(int minute = 0; minute < 60; ++minute) 
        {

            // Create 8 threads to simulate the 8 temperature readings
            vector<thread> threads(8);

            // Start the threads
            startThreads(threads);

            // Join the threads
            joinThreads(threads);


            // Gather current readings
            for(int i = 0; i < 8; ++i) 
            {
                all_readings.insert(readings[i]); 
            }

            // Update the highest and lowest temperatures for the hour
            hourMax[interval] = max(hourMax[interval], *max_element(readings, readings + 8));
            hourMin[interval] = min(hourMin[interval], *min_element(readings, readings + 8));

            if((minute + 1) % 10 == 0) 
            {
                printHourReport(interval, hourMax, hourMin);
                ++interval;
            }
        }

        calculateTemps(hourMax, hourMin);
      
        updateFiveLowest();
        
        updateFiveHighest();
    }

    // Update the five highest temperatures
    void updateFiveHighest()
    {
        auto j = all_readings.end();
        for(int i = 0; i < 5 && j != all_readings.begin(); ) {
            --j; 
            fiveHighestTemp[i] = *j;
            ++i; 
        }
    }
    // Update the five lowest temperatures
    void updateFiveLowest()
    {
        auto j = all_readings.begin();
        for(int i = 0; i < 5 && j != all_readings.end(); ++i, ++j) 
        {
            fiveLowestTemp[i] = *j;
        }
    }
};

// Get the temperature reading randomly
void generateTemperature(int& reading) 
{
    int temp = getRandomValue();
    lock_guard<mutex> guard(mtx); 
    reading = temp;
}

// Random number generator for temps
int getRandomValue() {
    thread_local static random_device rd;  
    thread_local static mt19937 gen(rd()); 
    uniform_int_distribution<> distr(-100, 70); 
    
    return distr(gen); 
}

// Print the output in table format
void printOutput(MarsModule* begin, std::chrono::duration<double, std::milli> duration)
{
    std::cout << "Finished in " << (duration.count() / 1000)  << "Seconds" << std::endl;

    cout << "Highest Temperatures: \n";
    cout << "---------------------\n";
    for(int temp : begin->fiveHighestTemp) 
    {
        cout << "|" << temp << " ";
    }
    cout << "|";
    cout << "\n---------------------\n";
    cout << endl;

    cout << "Lowest Temperatures: \n";
    cout << "---------------------------\n";
    for(int temp : begin->fiveLowestTemp) 
    {
        cout << "|" << temp << " ";
    }
    cout << "|";
    cout << "\n---------------------------\n";
    cout << endl;

    cout << "Highest variation: Interval " << begin->current_highest_interval + 1
         << " a dirrence of " << begin->total_interval_distance << " degrees." << endl;
}


int main() {
    cout << "Mars Rover Hourly Readings:" << endl;

    // Create the MarsModule object
    MarsModule begin;
    // Start the clock
    auto start = std::chrono::high_resolution_clock::now();
    begin.beginModule();
    auto end = std::chrono::high_resolution_clock::now();
    //End the clock
    auto duration = std::chrono::duration<double, std::milli>(end - start);
    
    printOutput(&begin, duration);

    return 0;
}