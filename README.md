# PayMeLater 

Expense Tracker is a web application for tracking expenses shared amongst friends. It is based on social trust, meaning that users can only see and add expenses for groups that they are a part of.

# Prerequisites
```
Java 11 or later
Maven 3.6.3 or later
MySQL 5.7 or later
```
# Installation
```
Clone the repository
Create a MySQL database and user for the application, and configure the database connection in the src/main/resources/application.properties file.
Run the following command to build and run the application: mvn clean package exec:java
```
#Usage

```
Register for an account on the homepage.
Create a new group by clicking on the "New Group" button in the navbar.
Invite your friends to the group by clicking on the "Invite" button on the group page.
Add expenses for the group by clicking on the "New Expense" button on the group page.
View the balance for each member of the group on the group page.
```
