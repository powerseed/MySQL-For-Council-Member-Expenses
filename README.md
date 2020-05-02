# MySQL-For-Council-Member-Expenses

# Idea:
The database is to query from the data set of ```Council_Member_Expenses.csv```. 
<br>
The relationships between tables are:
- ```Expense``` is a weak entity. 
- One to many from ```Councilor``` to ```Expense```.
- One to many from ```Ward``` to ```Councilor```.
- Each ```Expense``` must belong to one ```Councilor```, but not every ```Councilor``` has an ```Expense```.
- Not each ```Councilor``` has one ```Ward```, and not every ```Ward``` has a ```Councilor```.
<br>

# How to run:

Step 1: Clone or download the project.
<br>
Step 2: Open the folder in IntelliJ IDEA
<br>
Step 3: Run ```Main.java```
<br>
Step 4: Enter commands in the console.
