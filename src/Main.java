import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;
import java.sql.PreparedStatement;

import java.io.FileReader;
import java.io.IOException;
import java.io.BufferedReader;
import java.util.Scanner;

public class Main {
    static Connection connection;

    public static void main(String[] args) throws Exception {
		MyDatabase db = new MyDatabase();
		runConsole(db);

		System.out.println("Exiting...");
	}

	public static void runConsole(MyDatabase db){
		Scanner console = new Scanner(System.in);

		System.out.println("Command: ");
		System.out.println("w: print the names and IDs of all the known wards. ");
		System.out.println("e: print all the expenses, with associated ward and councilor name. ");
		System.out.println("c: Print the names of all councilors. ");
		System.out.println("ct name: Print total expenses for councilor 'name' (e.g., ct Brain Bowman). ");
		System.out.println("wt name: Print total expenses for ward 'name' (e.g., wt Transcona). ");
		System.out.println("dc name: Delete councilor named 'name' (e.g., dc Brian Bowman). ");
		System.out.println("de id: delete expense 'id' (e.g., de 17000). ");
		System.out.println("m: Show the highest single‐time expense for each councilor, with associated councilor name. ");
		System.out.println();
		System.out.print("Please enter your command: ");

		String line = console.nextLine();
        String [] parts;
        String arg = "";

		while(line != null && !line.equals("q")){
            parts = line.split("\\s+");
            if (line.indexOf(" ") > 0)
                arg = line.substring(line.indexOf(" ")).trim();
			if (parts[0].equals("h"))
				printHelp();
			else if (parts[0].equals("w")){
				db.allWards();
			}
			else if (parts[0].equals("c")){
				db.allCouncilors();
			}
			else if (parts[0].equals("e")){
				db.allExpenses();
			}
			else if (parts[0].equals("wt")){
				try{
					if (parts.length >= 2)
						db.singleWard(arg);
					else
						System.out.println("Require an argument for this command");
				} catch(Exception e){
					System.out.println("id must be an integer");
				}
			}
			else if (parts[0].equals("ct")){
				if (parts.length >= 2)
					db.singleCouncilor(arg);
				else
					System.out.println("Require an argument for this command");
			}
			else if (parts[0].equals("de")){
				try {
					if (parts.length >= 2 )
						db.deleteExpense(Integer.parseInt(arg));
					else
						System.out.println("Require an argument for this command");
				} catch(Exception e){
					System.out.println("id must be an integer");
				}
			}
			else if (parts[0].equals("dc")){
				if (parts.length >= 2 )
					db.deleteCouncilor(arg);
				else
					System.out.println("Require an argument for this command");
			}
			else if (parts[0].equals("m")){
				db.highestExpense();
			}
			else
				System.out.println("Read the help with h, or find help somewhere else.");

			System.out.println();
			System.out.print("Please enter your command: ");
			line = console.nextLine();
		}

		console.close();
	}

	private static void printHelp(){
		System.out.println("Winnipeg Council Member Expenses console");
		System.out.println("Commands:");
		System.out.println("h - Get help");
		System.out.println("w - Print all wards");
		System.out.println("c - Print all coucillors");
		System.out.println("ct name - Print total expenses for councilors 'name'");
		System.out.println("wt name - Print total expenses for ward 'name'");
		System.out.println("dc name - Delete councilors named 'name'");
		System.out.println("de id - delete expense 'id'");
		System.out.println("m - Show the highest single-time expense for each councilors");
		System.out.println("---- end help ----- ");
	}
}

class MyDatabase{
	private Connection connection;
	private final String filename = "Council_Member_Expenses.csv";
	public MyDatabase(){
		try {
			Class.forName("org.hsqldb.jdbcDriver");
			connection = DriverManager.getConnection("jdbc:hsqldb:mem:mymemdb", "SA", "");

			createTables();
			readInData();
		}
		catch (ClassNotFoundException e) {
			e.printStackTrace(System.out);
		}
		catch (SQLException e) {
			e.printStackTrace(System.out);
		}
	}

	public void allCouncilors() {
		try {
			String sql = "SELECT * from Councilors;";

			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(sql);

			while (resultSet.next()) {
				System.out.println(resultSet.getInt("cID") + ": " + resultSet.getString("councilMbr"));
			}

			resultSet.close();
			statement.close();

		}catch (SQLException e) {
				e.printStackTrace(System.out);
		}
	}

	public void allExpenses(){
		try {
			String sql = "SELECT eID, C.councilMbr, W.WardOffice, description, amt "
					   + "FROM expenses, Councilors C, Wards W "
					   + "WHERE expenses.cID = C.cID AND C.wardID = W.wardID;";

			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(sql);

			while (resultSet.next()) {
				System.out.println(resultSet.getInt("eID") + ": " + resultSet.getString("councilMbr") + ": " + resultSet.getString("WardOffice") + ": "
								   + resultSet.getString("description") + ": " + resultSet.getDouble("amt"));
			}

			resultSet.close();
			statement.close();

		}catch (SQLException e) {
				e.printStackTrace(System.out);
		}
	}

	public void singleCouncilor(String councilMbr) {
		try {
            PreparedStatement getSingle = connection.prepareStatement(
                "SELECT C.cID, C.councilMbr, SUM(amt) AS totalAmt "
                + "FROM Councilors C, Expenses E "
                + "WHERE C.cID = E.cID AND C.councilMbr = ? "
				+ "GROUP BY C.cID, C.councilMbr; "
            );
            getSingle.setString(1, councilMbr);

            ResultSet resultSet =  getSingle.executeQuery();
            while (resultSet.next()) {
                System.out.println(resultSet.getInt("cID") + ": " + resultSet.getString("councilMbr") + ": " + resultSet.getDouble("totalAmt"));
            }
            resultSet.close();
            getSingle.close();
        }catch (SQLException e) {
            e.printStackTrace(System.out);
        }
	}

	public void singleWard(String wardOffice) {
		try {
            PreparedStatement getSingle = connection.prepareStatement(
                "SELECT W.wardID, wardOffice, SUM(amt) AS totalAmt "
                + "FROM Wards W, Councilors C, Expenses E "
                + "WHERE W.wardID=C.wardID AND C.cID=E.cID AND W.wardOffice = ? "
				+ "GROUP BY W.wardID, wardOffice; "
            );
            getSingle.setString(1, wardOffice);

            ResultSet resultSet =  getSingle.executeQuery();
            while (resultSet.next()) {
                System.out.println(resultSet.getInt("wardID") + ": " + resultSet.getString("wardOffice") + ": " + resultSet.getDouble("totalAmt"));
            }
            resultSet.close();
            getSingle.close();
        }catch (SQLException e) {
            e.printStackTrace(System.out);
        }
	}

	public void deleteExpense(int expenseID){
		try {
            PreparedStatement deleteCouncil = connection.prepareStatement(
                "DELETE FROM Expenses WHERE eID = ?;"
            );
            deleteCouncil.setInt(1, expenseID);

            deleteCouncil.executeUpdate();
            deleteCouncil.close();
        }catch (SQLException e) {
            e.printStackTrace(System.out);
        }
	}

	public void deleteCouncilor(String councilMbr){
		try {
            PreparedStatement deleteCouncil = connection.prepareStatement(
                "DELETE FROM Councilors WHERE councilMbr = ?;"
            );
            deleteCouncil.setString(1, councilMbr);

            deleteCouncil.executeUpdate();
            deleteCouncil.close();
        }catch (SQLException e) {
            e.printStackTrace(System.out);
        }
	}

	public void highestExpense() {
		try {
            PreparedStatement deleteCouncil = connection.prepareStatement(
                "SELECT C.councilMbr, MAX(E.amt) AS highest "
              + "FROM Expenses E, Councilors C "
              + "WHERE C.cID = E.cID "
              + "GROUP BY C.councilMbr; "
            );
            ResultSet resultSet =  deleteCouncil.executeQuery();
            while (resultSet.next()) {
                System.out.println( resultSet.getString("councilMbr") + ": " + resultSet.getDouble("highest") );
            }
            resultSet.close(); 
            deleteCouncil.close();
        }catch (SQLException e) {
            e.printStackTrace(System.out);
        }
	}

	private void createTables(){
		String wards = " CREATE TABLE Wards ( "
			+ " wardID INTEGER, "
			+ " wardOffice VARCHAR(60), "
            + " PRIMARY KEY (wardID) ); ";
		try {
			connection.createStatement().executeUpdate(wards);

			String createCouncilor = " CREATE TABLE Councilors ( "
				+ " cID INTEGER IDENTITY, "
            	+ " wardID INTEGER, "
            	+ " councilMbr VARCHAR(60), "
				+ " PRIMARY KEY (cID), "
				+ " FOREIGN KEY (wardID) REFERENCES Wards); ";

			connection.createStatement().executeUpdate(createCouncilor);

             String createExpense = " CREATE TABLE Expenses ( "
              + " eID INTEGER IDENTITY, "
              + " cID INTEGER, "
              + " jDate VARCHAR(10), "
              + " vendor VARCHAR(60), "
              + " eType VARCHAR(60), "
              + " description VARCHAR(200), "
              + " acct VARCHAR(100), "
              + " amt DOUBLE, "
              + " PRIMARY KEY (eID), "
              + " FOREIGN KEY (cID) REFERENCES Councilors ON DELETE CASCADE); ";
            
			 connection.createStatement().executeUpdate(createExpense);
		}
		catch (SQLException e) {
			e.printStackTrace(System.out);
		}
	}

	public void allWards() {
		try {
			String sql = "SELECT * from WARDS;";

			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(sql);

			while (resultSet.next()) {
				System.out.println(resultSet.getInt("wardID") + ": " + resultSet.getString("wardOffice"));
			}

			resultSet.close();
			statement.close();

		}catch (SQLException e) {
				e.printStackTrace(System.out);
		}
	}

    private int getOrMakeWard(String wardId, String wardName) {
        /* See if ward exists, create it if it doesn't
        Return the id regardless of existance */
        int wardIDtoReturn = -1;
        try {
			PreparedStatement pstmt = connection.prepareStatement(
					"SELECT * FROM Wards WHERE wardID = ?;"
				);
            pstmt.setInt(1, Integer.parseInt(wardId));

            ResultSet resultSet = pstmt.executeQuery();

			if (resultSet.next()) {
                // at least 1 row exists. Get the ID
				wardIDtoReturn = resultSet.getInt("wardID");
            }
            else {
                // make the new ward
                PreparedStatement addWard = connection.prepareStatement(
					"INSERT INTO Wards (wardID, wardOffice) VALUES (?, ?);"
                );
                wardIDtoReturn = Integer.parseInt(wardId);
				addWard.setInt(1, wardIDtoReturn);
				addWard.setString(2, wardName);

                addWard.executeUpdate();
                addWard.close();
            }

			resultSet.close();
			pstmt.close();

		}catch (SQLException e) {
				e.printStackTrace(System.out);
		}

        return wardIDtoReturn;
    }

    private int getOrMakeCouncilor(String name, int wardID) {
        int cID = -1;
        try {
			PreparedStatement pstmt = connection.prepareStatement(
					"SELECT cID FROM Councilors WHERE councilMbr = ?;"
				);
            pstmt.setString(1, name);

            ResultSet resultSet = pstmt.executeQuery();

			if (resultSet.next()) {
                // at least 1 row exists. Get the ID
				cID = resultSet.getInt("cID");
            }
            else {
                // make the new ward
                PreparedStatement addCouncil = connection.prepareStatement(
                    "INSERT INTO Councilors (councilMbr, wardID) VALUES (?, ?);",
                    Statement.RETURN_GENERATED_KEYS
                );

                addCouncil.setString(1, name);
                addCouncil.setInt(2, wardID);
                addCouncil.executeUpdate();
                ResultSet keys = addCouncil.getGeneratedKeys();
                if (keys.next())
                    cID = keys.getInt(1);
                addCouncil.close();
            }
			resultSet.close();
			pstmt.close();

		}catch (SQLException e) {
				e.printStackTrace(System.out);
		}
        return cID;
    }

	private void readInData(){
		BufferedReader in = null;
		String addition;

		try {
			in = new BufferedReader((new FileReader(filename)));

			// throw away the first line - the header
			in.readLine();

			// pre-load loop
			String line = in.readLine();
			while (line != null) {
				// split naively on commas
				String[] parts = line.split(",");

                int wardID = getOrMakeWard(parts[0], parts[1]);
                int councilorId = getOrMakeCouncilor(parts[2], wardID);

                PreparedStatement addExpense = connection.prepareStatement(
                    "INSERT INTO expenses  (cID, description, acct, amt)" +
                    " VALUES (?, ?, ?, ?);"
                );
                addExpense.setInt(1, councilorId);
                int length = parts.length;
                addExpense.setDouble(4, Double.parseDouble(parts[length-1])); // amount
                
            	int index = length - 2;
                String forAccount = parts[index];
                if( parts[index].length() > 0 && parts[index].charAt(parts[index].length()-1) == '"' )
                {
                	while( parts[index].charAt(0) != '"' )
                	{
                		index--;  
                		forAccount = parts[index] + ", " + forAccount;                		            		
                	}  
                }
                addExpense.setString(3, forAccount); // account
                
                index--;
                String forDescription = parts[index];
                if( parts[index].length() > 0 && parts[index].charAt(parts[index].length()-1) == '"' )
                {
                	while( parts[index].charAt(0) != '"' )
                	{
                		index--;
                		forDescription = parts[index]  + ", " + forDescription;
                	}               	
                }
                addExpense.setString(2, forDescription); // description
                
                addExpense.executeUpdate();
                addExpense.close();

				// get next line
				line = in.readLine();
			}
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		catch (SQLException e) {
			e.printStackTrace(System.out);
		}
	}
}