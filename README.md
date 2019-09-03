# List of SF Food Truck currently open

This command line project fetches and displays the food trucks currently open in SF.
The data is pulled from a San Francisco government website. The sorted data (name and address) is displayed on the command line in a paginated view.

## Getting Started

Pull the project using git clone.
cd to the top level of the project.
Issue command to generate executable artifact:
```
mvn clean compile assembly:single
```
Issue command to run:
```
java -jar target/FoodTruckFinder.jar
```

### Prerequisites

Make sure you have maven  installed.
```mvn -v```

java 8 or above installed.
```java -version```

## Running the app

The CLI app will work with current Pacific date and time to find the open Food Trucks.
```
java -jar target/FoodTruckFinder.jar
```
For sake of testing however, there is a provision of changing the day and hour of the food truck availability. 
To test various cases pass the two optional environment variables;
```
java -Dday=Wednesday -Dhours=13 -jar target/FoodTruckFinder.jar
```
This should list the opened food trucks on Wednesday at 1:00 PM.
--OR--
```
java -Dday=Sunday -Dhours=21 -jar target/FoodTruckFinder.jar
```
Expectedly this should return a smaller list, ie trucks open on Sunday at 9:00pm.

## Built With

* [Maven](https://maven.apache.org/) - Dependency Management

## Contributing

Please read [CONTRIBUTING.md](https://gist.github.com/PurpleBooth/b24679402957c63ec426) for details on our code of conduct, and the process for submitting pull requests to us.

## Authors

* **Atul Prasad** - *Initial work* - [atul555](https://github.com/atul555)

See also the list of [contributors](https://github.com/your/project/contributors) who participated in this project.

## License

This project is licensed under the MIT License.

## Acknowledgments

* Hat tip to anyone whose code was used
* Inspiration
* Redfin

