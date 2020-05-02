# MySQL-For-Council-Member-Expenses

# Idea:
The database is to query from the data set of ```Council_Member_Expenses.csv```. 
<br>
The relationships between tables are:
- ```Expense``` is a weak entity. 
- One to many from ```Councilor``` to ```Expense```.
- One to many from ```Ward``` to ```Councilor```.
- Each ```Expense``` must belong to one ```Councilor```, but not every ```Councilor``` has an ```Expense```.
- Not every ```Councilor``` has one ```Ward```, and not every ```Ward``` has a ```Councilor```.
<br>

# How to run:

Step 1: Clone or download the project.
<br>
Step 2: Open the folder in IntelliJ IDEA
<br>
Step 3: Run ```src/Main.java```
<br>
Step 4: Enter commands in the console.
<br>
(You may need to include ```hsqldb.jar``` as a dependency)
<br>
Valid queries:
1. print the names and IDs of all the known wards.
2. print all the expenses, with associated ward and councilor name.
3. Print the names of all councilors.
4. Print total expenses for a specified councilor.
5. Print total expenses for a specified ward.
6. Delete a councilor by 'name'.
7. Delete an Expense by 'id'.
8. Show the highest single‐time expense for each councilor.

