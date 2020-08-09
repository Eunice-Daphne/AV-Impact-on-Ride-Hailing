# Autonomous Vehicles Impact on Ride-hailing

## Problem Definition

The project focuses on resource assignment to the agents with two kinds of assignment strategies, fair assignment (for crowdsourced) and optimal assignment (owned taxicabs). All the agents are introduced to the system at the beginning and are placed at a random location on the road network. Resources are introduced to the system in a streaming fashion, each with an origin and a destination. The performance evaluation is done based on different parameters(hubs, window period, number of agents etc) on both fair and optimal assignments.

## Overall Logic

This project uses the COMSET code for the implementation of the fundamental logic of the system which simulates taxicabs (agents) searching for customers (resources) to pick up in the Manhattan city area. This simulator is used as the testbed for developing and testing our own algorithms for fair and optimal assignments with a few modifications to the actual logic. The main modifications include introducing resources in the streaming fashion for every time window into the system instead of one by one and moving the agents to random hubs instead of random intersections when no resource is assigned to it. The algorithms used for fair assignment is Minimum Cost Edge Algorithm and for optimal assignment is Maximum Weighted Bipartite Matching using JGraphT.

More details about COMSET are available on Github

(https://github.com/Chessnl/COMSET-GISCUP/)

## Input

Manhattan dataset was used which is a subset of the New York TLC Trip Record YELLOW Data which contains the records with both pick-up and drop-off within the Manhattan area. 
The agent count, window size, hubs and assignment type can be modified in the config file of the project for the performance evaluation.
Attributes considered:  Pickup datetime, Drop off datetime, Trip distance, Pickup Longitude, Pickup Latitude, Drop off Longitude, Drop off Latitude, Tolls amount. A json file of the Manhattan city map is taken from openstreetmap.org. 

You can find the datasets in the below link

https://www1.nyc.gov/site/tlc/about/tlc-trip-record-data.page

Note: Our project uses latitude and longitude for pick up and drop off points. So, it is advisable to use data that has Pickup Longitude, Pickup Latitude, Drop off Longitude, Drop off Latitude. Datasets before July 2016 have this information.

## Output 

The performance of the fair and optimal assignments are evaluated based on Average Resource Wait Time, Average Agent Search Time, Percentage of Expired resources, Total Simulation Time individually using different parameters like agents counts, assignment window, hubs count. The graphs are evaluated based on the different statistics which explain how the resources are handled, agent search strategy, the impact of algorithms, etc.

## Prerequisites

1. **Java Version:**
   Our project uses the COMSET system and requires JAVA 8 or up.

2. **IntelliJ:**
   When the project is cloned, it should support Maven. IntelliJ was used for building this project and link to set up the Maven project in IntelliJ is as follows,

   https://www.jetbrains.com/help/idea/maven-support.html#maven_import_project_start

3. **JGraphT Library:**
   Once the project is loaded, the JGraphT library should be included in the project structure. Below is the link for importing the library.

   https://github.com/jgrapht/jgrapht/wiki/Users:-How-to-use-JGraphT-as-a-dependency-in-your-projects#developing-using-intellij-idea

   The jar file we are using in the project is:

   jgrapht-core-1.4.0.jar

4. **MySQL Workbench 8.0:**
   The MySQL database or any other database connection is needed for running the project. 

   Link to the software: https://dev.mysql.com/downloads/workbench/

   The MySQL JDBC connector has to be imported in the project libraries in the IntelliJ project. Below is the link for importing the library.

   https://dev.mysql.com/downloads/connector/j/

   The jar file we are using in the project is:

   mysql-connector-java-8.0.19.jar

   Note : If using another database, the connection statements and libraries should be changed accordingly.

5. **jheaps library:**
   This is a library which needs to be imported in the project in case any issue is encountered with jheaps. 
   Below is the link for importing the library.

   https://jar-download.com/artifacts/org.jheaps/jheaps

   The jar file we are using in the project is:

   jheaps-0.13.jar

## Installing, building, and running the project

1. **Downloading the project:**
   Download the project from GitHub and unzip it. Import the project folder into IntelliJ (or other editors - please follow respective import procedures) as a  Maven project.

2. **Creating the Database tables:**

Create a schema named av_schema and  create the following tables within the schema. 

    Create schema av_schema 


   **Mandatory Table :** 

   Create an Assignment table with the following constraints in your respective database.

   Table name : assignmentTable
   
    CREATE TABLE av_schema.assignmentTable (
    ResourceID varchar(25),
    AgentID varchar(25),
    Weight varchar(25));
   
   ![Alt text](./table%201.png?raw=true "Sample Output")


   **Optional Table :** 

   Table name : fullstatistics

   Create a Statistics table with the following constraints in your respective database.

   ![Alt text](./table%202.png?raw=true "Sample Output")
   

   Creating this statistics table is purely optional. This was created to make a comparison of the final results.  Updating the Statistics to the Database is already commented out in the uploaded code which can be uncommented if needed. (Simulator.java file line no : 388) 

   Note: Please make sure to change the connection statements if using other databases. Also, change the username and password values according to your database. The changes have to be made in the following file. Assisgnment.java - line no : 54, FairAssignmentJGraphT.java - line no : 72, OptimalAssignmentJGraphT.java - line no : 81 and Statistics.java - line no : 35

3. **Downloading the libraries:**

   Download the JGraphT,  Jheaps and MySQL jars mentioned above into the project structure of IntelliJ. These jars are already placed in the external library folder for convenience and can be downloaded directly from here. 

   For IntelliJ, follow the below instructions. 

   File -> Project Structure -> Libraries -> New project library -> Import the three libraries mentioned. Please follow respective steps if using another editor.

4. **Running the project:**

   **Step 1** : Make sure the assignment table is created in the MySql database and connected to the project. 

   **Step 2** : Set all the input parameters in the configuration file which is  found in the path, Project -> etc -> config.properties. All the parameters are already set for convenience. 

   To set the data path
   
   **comset.dataset_file = datasets/yellow_tripdata_2016-05.csv**


   To set the hubs path and hub count
   
   **comset.hubs_file = datasets/hubs_50.csv**
   
   **comset.hubs_count = 50**


   To set the agent cardinality
   
   **comset.number_of_agents = 5000**


   To set the assignment type (1 for fair assignment and 2 for optimal assignment)
   
   **comset.assignmenttype = 2**


   To set the window size 
   
   **comset.windowsize = 30**


   To set the maximum resource lifetime 
   
   **comset.resource_maximum_life_time = 600**


   **Step 3** : To run the project go to main.java (main class for this project) and hit run 

## Sample Output : 
![Alt text](./sample%20output.png?raw=true "Sample Output")

## AUTHORS:
* **Anjana Anand**
* **Anusha Voloju**
* **Eunice Daphne**
* **Sudha Anusha Sagi**




