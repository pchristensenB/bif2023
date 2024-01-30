## Box Impact Fund CSV script

This repository is a companion piece to a Box Developer blog post. It contains a script that will generate a CSV file based on files with metadata in a given folder.

### Create JAR file
This is a maven project and assumes maven is installed.
To generate the jar file open a terminal at the root of the project and run "mvn package". This will generate a jar file in the ./target directory. 

### Run the script
To run the script open the terminal at the ./target directory and run: "java -jar BOX_FOLDER_ID BOX_TOKEN PATH_TO_OUTPUT_FILE"
java -jar 1234567 abc1234!@£$ ./myfolder/report.csv

This will create the CSV file.
