/*
 * Template JAVA User Interface
 * =============================
 *
 * Database Management Systems
 * Department of Computer Science &amp; Engineering
 * University of California - Riverside
 *
 * Target DBMS: 'Postgres'
 *
 */


import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;

/**
 * This class defines a simple embedded SQL utility class that is designed to
 * work with PostgreSQL JDBC drivers.
 *
 */
public class Cafe {

   // reference to physical database connection.
   private Connection _connection = null;

   // handling the keyboard inputs through a BufferedReader
   // This variable can be global for convenience.
   static BufferedReader in = new BufferedReader(
                                new InputStreamReader(System.in));

   // login & type of current user
   String user_login;

   /**
    * Creates a new instance of Cafe
    *
    * @param hostname the MySQL or PostgreSQL server hostname
    * @param database the name of the database
    * @param username the user name used to login to the database
    * @param password the user login password
    * @throws java.sql.SQLException when failed to make a connection.
    */
   public Cafe(String dbname, String dbport, String user, String passwd) throws SQLException {

      System.out.print("Connecting to database...");
      try{
         // constructs the connection URL
         String url = "jdbc:postgresql://localhost:" + dbport + "/" + dbname;
         System.out.println ("Connection URL: " + url + "\n");

         // obtain a physical connection
         this._connection = DriverManager.getConnection(url, user, passwd);
         System.out.println("Done");
      }catch (Exception e){
         System.err.println("Error - Unable to Connect to Database: " + e.getMessage() );
         System.out.println("Make sure you started postgres on this machine");
         System.exit(-1);
      }//end catch
   }//end Cafe

   /**
    * Method to execute an update SQL statement.  Update SQL instructions
    * includes CREATE, INSERT, UPDATE, DELETE, and DROP.
    *
    * @param sql the input SQL string
    * @throws java.sql.SQLException when update failed
    */
   public void executeUpdate (String sql) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the update instruction
      stmt.executeUpdate (sql);

      // close the instruction
      stmt.close ();
   }//end executeUpdate

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and outputs the results to
    * standard out.
    *
    * @param query the input query string
    * @return the number of rows returned
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int executeQueryAndPrintResult (String query) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the query instruction
      ResultSet rs = stmt.executeQuery (query);

      /*
       ** obtains the metadata object for the returned result set.  The metadata
       ** contains row and column info.
       */
      ResultSetMetaData rsmd = rs.getMetaData ();
      int numCol = rsmd.getColumnCount ();
      int rowCount = 0;

      // iterates through the result set and output them to standard out.
      boolean outputHeader = true;
      while (rs.next()){
		 if(outputHeader){
			for(int i = 1; i <= numCol; i++){
			System.out.print(rsmd.getColumnName(i) + "\t");
			}
			System.out.println();
			outputHeader = false;
		 }
         for (int i=1; i<=numCol; ++i)
            System.out.print (rs.getString (i) + "\t");
         System.out.println ();
         ++rowCount;
      }//end while
      stmt.close ();
      return rowCount;
   }//end executeQuery

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and returns the results as
    * a list of records. Each record in turn is a list of attribute values
    *
    * @param query the input query string
    * @return the query result as a list of records
    * @throws java.sql.SQLException when failed to execute the query
    */
   public List<List<String>> executeQueryAndReturnResult (String query) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the query instruction
      ResultSet rs = stmt.executeQuery (query);

      /*
       ** obtains the metadata object for the returned result set.  The metadata
       ** contains row and column info.
       */
      ResultSetMetaData rsmd = rs.getMetaData ();
      int numCol = rsmd.getColumnCount ();
      int rowCount = 0;

      // iterates through the result set and saves the data returned by the query.
      boolean outputHeader = false;
      List<List<String>> result  = new ArrayList<List<String>>();
      while (rs.next()){
        List<String> record = new ArrayList<String>();
		for (int i=1; i<=numCol; ++i)
			record.add(rs.getString (i));
        result.add(record);
      }//end while
      stmt.close ();
      return result;
   }//end executeQueryAndReturnResult

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and returns the number of results
    *
    * @param query the input query string
    * @return the number of rows returned
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int executeQuery (String query) throws SQLException {
       // creates a statement object
       Statement stmt = this._connection.createStatement ();

       // issues the query instruction
       ResultSet rs = stmt.executeQuery (query);

       int rowCount = 0;

       // iterates through the result set and count nuber of results.
       while (rs.next()){
          rowCount++;
       }//end while
       stmt.close ();
       return rowCount;
   }

   /**
    * Method to fetch the last value from sequence. This
    * method issues the query to the DBMS and returns the current
    * value of sequence used for autogenerated keys
    *
    * @param sequence name of the DB sequence
    * @return current value of a sequence
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int getCurrSeqVal(String sequence) throws SQLException {
	Statement stmt = this._connection.createStatement ();

	ResultSet rs = stmt.executeQuery (String.format("Select currval('%s')", sequence));
	if (rs.next())
		return rs.getInt(1);
	return -1;
   }

   /**
    * Method to close the physical connection if it is open.
    */
   public void cleanup(){
      try{
         if (this._connection != null){
            this._connection.close ();
         }//end if
      }catch (SQLException e){
         // ignored.
      }//end try
   }//end cleanup

   /**
    * The main execution method
    *
    * @param args the command line arguments this inclues the <mysql|pgsql> <login file>
    */
   public static void main (String[] args) {
      if (args.length != 3) {
         System.err.println (
            "Usage: " +
            "java [-classpath <classpath>] " +
            Cafe.class.getName () +
            " <dbname> <port> <user>");
         return;
      }//end if

      Greeting();
      Cafe esql = null;
      try{
         // use postgres JDBC driver.
         Class.forName ("org.postgresql.Driver").newInstance ();
         // instantiate the Cafe object and creates a physical
         // connection.
         String dbname = args[0];
         String dbport = args[1];
         String user = args[2];
         esql = new Cafe (dbname, dbport, user, "");

         boolean keepon = true;
         while(keepon) {
            // These are sample SQL statements
            System.out.println("MAIN MENU");
            System.out.println("---------");
            System.out.println("1. Create user");
            System.out.println("2. Log in");
            System.out.println("9. < EXIT");
            String authorisedUser = null;
            switch (readChoice()){
               case 1: CreateUser(esql); break;
               case 2: authorisedUser = LogIn(esql); break;
               case 9: keepon = false; break;
               default : System.out.println("Unrecognized choice!"); break;
            }//end switch
            if (authorisedUser != null) {
              boolean usermenu = true;
              while(usermenu) {
                System.out.println("MAIN MENU");
                System.out.println("---------");
                System.out.println("1. Goto Menu");
                System.out.println("2. Update Profile");
                System.out.println("3. Place a Order");
                System.out.println("4. Update a Order");
                System.out.println(".........................");
                System.out.println("9. Log out");
                switch (readChoice()){
                   case 1: Menu(esql); break;
                   case 2: UpdateProfile(esql); break;
                   case 3: PlaceOrder(esql); break;
                   case 4: UpdateOrder(esql); break;
                   case 9: usermenu = false; break;
                   default : System.out.println("Unrecognized choice!"); break;
                }
              }
            }
         }//end while
      }catch(Exception e) {
         System.err.println (e.getMessage ());
      }finally{
         // make sure to cleanup the created table and close the connection.
         try{
            if(esql != null) {
               System.out.print("Disconnecting from database...");
               esql.cleanup ();
               System.out.println("Done\n\nBye !");
            }//end if
         }catch (Exception e) {
            // ignored.
         }//end try
      }//end try
   }//end main

   public static void Greeting(){
      System.out.println(
         "\n\n*******************************************************\n" +
         "              User Interface      	               \n" +
         "*******************************************************\n");
   }//end Greeting

   /*
    * Reads the users choice given from the keyboard
    * @int
    **/
   public static int readChoice() {
      int input;
      // returns only if a correct value is given.
      do {
         System.out.print("Please make your choice: ");
         try { // read the integer, parse it and break.
            input = Integer.parseInt(in.readLine());
            break;
         }catch (Exception e) {
            System.out.println("Your input is invalid!");
            continue;
         }//end try
      }while (true);
      return input;
   }//end readChoice

   /*
    * Creates a new user with privided login, passowrd and phoneNum
    **/
   public static void CreateUser(Cafe esql){
      try{
         System.out.print("\tEnter user login: ");
         String login = in.readLine(); esql.user_login = login;
         System.out.print("\tEnter user password: ");
         String password = in.readLine();
         System.out.print("\tEnter user phone: ");
         String phone = in.readLine();
         
	    String type="Customer";
	    String favItems="";

				 String query = String.format("INSERT INTO USERS (phoneNum, login, password, favItems, type) VALUES ('%s','%s','%s','%s','%s')", phone, login, password, favItems, type);

         esql.executeUpdate(query);
         System.out.println ("User successfully created!");
      }catch(Exception e){
         System.err.println (e.getMessage ());
      }
   }//end CreateUser


   /*
    * Check log in credentials for an existing user
    * @return User login or null is the user does not exist
    **/
   public static String LogIn(Cafe esql){
      try{
         System.out.print("\tEnter user login: ");
         String login = in.readLine(); esql.user_login = login;
         System.out.print("\tEnter user password: ");
         String password = in.readLine();

         String query = String.format("SELECT * FROM USERS WHERE login = '%s' AND password = '%s'", login, password);
         int userNum = esql.executeQuery(query);
	 if (userNum > 0)
		return login;
         return null;
      }catch(Exception e){
         System.err.println (e.getMessage ());
         return null;
      }
   }//end

// Rest of the functions definition go in here

  

  public static void UpdateProfile(Cafe esql){

    String attribute = "";
    String set = "";
    String query = "";
    String target = esql.user_login;

    try {
      boolean keepon = true;
      while (keepon) {
        // Check user
        System.out.println("USER MENU ["+target+"]");
        System.out.println("---------");
        System.out.println("1. phoneNum");
        System.out.println("2. password");
        System.out.println("3. favoriteItem");
        System.out.println("4. type (manager only)");
        System.out.println("5. target (manager only)");
        System.out.println(".........................");
        System.out.println("9. exit");

        switch (readChoice()){
          case 1: 
            System.out.print("\tEnter phoneNum: ");
            set = in.readLine(); 
            attribute = "phoneNum"; 
            query = String.format("UPDATE Users SET %s = '%s' WHERE Users.login = '%s';", attribute, set, target); 
            keepon=false;
            break;
          case 2: 
            System.out.print("\tEnter password: ");
            set = in.readLine(); 
            attribute = "password"; 
            query = String.format("UPDATE Users SET %s = '%s' WHERE Users.login = '%s';", attribute, set, target); 
            keepon=false;
            //System.out.print(query);
            break;
          case 3: 
            System.out.print("\tEnter favoriteItem: ");
            set = in.readLine(); 
            attribute = "favItems"; 
            // Ok so I've realized that favItems is 400 char plural while item names are 50 char singular. I was gonna put a checker to make sure the favItem exists, but now I think I'll leave it actually
            query = String.format("UPDATE Users SET %s = '%s' WHERE Users.login = '%s';", attribute, set, target); 
            keepon=false;
            break;
          case 4: 
            System.out.print("\tEnter type: ");
            if(!GetType(esql).equalsIgnoreCase("Manager ")){
              System.out.print("ERROR: Need manager privilage. You are a "+GetType(esql)+".\n");
              return;
            } 
            set = in.readLine(); 
            attribute = "type"; 
            query = String.format("UPDATE Users SET %s = '%s' WHERE Users.login = '%s';", attribute, set, target); 
            keepon=false;
            break;
          case 5: 
            if(!GetType(esql).equalsIgnoreCase("Manager ")){
              System.out.print("ERROR: Need manager privilage. You are a "+GetType(esql)+".\n");
              return;
            } 
            System.out.print("\tEnter target login: ");
            //Check if target exists first
            String input = in.readLine(); 
            if(LoginExists(esql, input)){
              target = input; 
              continue;
            }else{
              System.out.println("ERROR: Invalid login."); 
              keepon=false;
              break;
            }
          case 9:
            return;
        }
        esql.executeUpdate(query); 
      }
    } catch (Exception e) {
      // e.printStackTrace();
      System.err.println (e.getMessage ());
    } 
  }

  //Helper functions

  public static String GetType(Cafe esql){
    try {
      String query = String.format("SELECT U.type FROM Users U WHERE U.login = '%s';", esql.user_login);
      List<List<String>> result;
      result = esql.executeQueryAndReturnResult (query);
      return result.get(0).get(0); 
    } catch (Exception e) {
      // e.printStackTrace();
         System.err.println (e.getMessage ());
    }
    return "";
  }

  public static boolean LoginExists(Cafe esql, String l){
    try {
      String query = String.format("SELECT U.type FROM Users U WHERE U.login = '%s';", l);
      int result;
      result = esql.executeQuery (query);
      return result > 0; 
    } catch (Exception e) {
      // e.printStackTrace();
         System.err.println (e.getMessage ());
    }
    return false;
  }

  public static void Menu(Cafe esql){
     boolean keepon = true;
     int selection = 0;
     String itemName;
     String itemType;
     String type = GetType(esql).replaceAll("\\s", "");
     String query;
     if(type.equalsIgnoreCase("Manager")){
      System.out.println("====================");
      System.out.println(GetType(esql));
      System.out.println("====================");
     }

     try{
     while(keepon){
      System.out.println("Welcome to The Menu");
      System.out.println("1. See all menu");
      System.out.println("2. Search an item by its name");
      System.out.println("3. Search an item by its type");
      if(type.equalsIgnoreCase("Manager")){
         System.out.println("4. Add an item");
         System.out.println("5. Delete an item by its name");
         System.out.println("6. Update an item by its name");
      }
      System.out.println("9. Exit");
      System.out.println("----------------------------");
      System.out.println("----------------------------");
      System.out.print("Type your choice: ");

      selection =  Integer.parseInt(in.readLine());

      switch(selection){
         case 1:
            query = "SELECT * FROM Menu;";
            //esql.executeQueryAndPrintResult(query);
            List<List<String>> list_of_menu = esql.executeQueryAndReturnResult(query);
            System.out.println("----------------------------");
            for(int i=0; i<list_of_menu.size(); i++){
               
               System.out.println("Menu"+ (i+1));
               System.out.println("----------------------------");
               for(int j=0; j<list_of_menu.get(i).size(); j++){
                     System.out.println(list_of_menu.get(i).get(j));
               }
                  System.out.println("----------------------------");
            }
         break;

         case 2:
            System.out.print("Type the name of item: ");
            itemName =  in.readLine();
            query = String.format("SELECT * FROM Menu M WHERE M.itemName = '%s';", itemName);
            List<List<String>> itemDetail = esql.executeQueryAndReturnResult(query);
            System.out.println("----------------------------");
            for(int i=0; i<itemDetail.size(); i++){
               System.out.println("Menu"+ (i+1));
               System.out.println("----------------------------");
               for(int j=0; j<itemDetail.get(i).size(); j++){
                     System.out.println(itemDetail.get(i).get(j));
               }
               System.out.println("----------------------------");
            }
         break;

         case 3:
            System.out.print("Type the type you want to find: ");
            itemType =  in.readLine();
            query = String.format("SELECT * FROM Menu M WHERE M.type = '%s';", itemType);
            List<List<String>> typeDetail = esql.executeQueryAndReturnResult(query);
            System.out.println("----------------------------");
            for(int i=0; i<typeDetail.size(); i++){
               System.out.println("Menu"+ (i+1));
               System.out.println("----------------------------");
               for(int j=0; j<typeDetail.get(i).size(); j++){
                     System.out.println(typeDetail.get(i).get(j));
               }
               System.out.println("----------------------------");
            }
         break;

         case 4:
         case 5:
         case 6:
         ManageMenuHelper(esql, selection);
         break;
         case 9:
         keepon = false;
         break;

         default:
         System.out.println("Invalid Number!");
      }
     }
     }catch (Exception e){
        System.err.println (e.getMessage ());
     }
  }

  public static void ManageMenuHelper(Cafe esql, int selection){
     try{
     String itemName;
     String type = "";
     int price;
     String description;
     String imageURL;

     String memberType = GetType(esql).replaceAll("\\s", "");
     String query = "";

     if(memberType.equals("Manager")){
        switch(selection){
           case 4:
           System.out.print("Type the name of item: ");
           itemName = in.readLine();
           System.out.print("Type the type of item: ");
           type = in.readLine();
           System.out.print("Type the price of item: ");
           price = Integer.parseInt(in.readLine());
           System.out.print("Type the description of item: ");
           description = in.readLine();
           System.out.print("Type the imageURL of item: ");
           imageURL = in.readLine();
           
           query = String.format("INSERT INTO Menu (itemName, type, price, description, imageURL) VALUES ('%s','%s','%d','%s','%s')", itemName, type, price, description, imageURL);
           try{
           esql.executeUpdate(query);
           }catch(Exception e){
              System.err.println (e.getMessage ());
           }
           break;

           case 5:
           System.out.print("Type the name of item to delete: ");
           itemName = in.readLine();

           query = String.format("DELETE FROM itemStatus where itemName = '%s'", itemName);
           try{
           esql.executeUpdate(query);
           }catch(Exception e){
              System.err.println (e.getMessage ());
           }

           query = String.format("DELETE FROM Menu where itemName = '%s'", itemName);
           try{
           esql.executeUpdate(query);
           }catch(Exception e){
              System.err.println (e.getMessage ());
           }
           break;

           case 6:
           System.out.print("Type the name of item to update: ");
           itemName = in.readLine();
           int selectionForUpdate;
           System.out.println("1. Type");
           System.out.println("2. Price");
           System.out.println("3. Description");
           System.out.println("4. image URL");
           System.out.print("Type the number of the attribute to update: ");
           selectionForUpdate = Integer.parseInt(in.readLine());
           
           switch(selectionForUpdate){
              case 1:
              System.out.print("Type the type of item: ");
              type = in.readLine();
              query = String.format("UPDATE Menu SET type = '%s' WHERE itemName = '%s'", type, itemName);
              break;
              case 2:
              System.out.print("Type the price: ");
              price = Integer.parseInt(in.readLine());
              query = String.format("UPDATE Menu SET price = '%d' WHERE itemName = '%s'", price, itemName);
              break;
              case 3:
              System.out.print("Type the description: ");
              description = in.readLine();
              query = String.format("UPDATE Menu SET description = '%s' WHERE itemName = '%s'", description, itemName);
              break;
              case 4:
              System.out.print("Type the image URL: ");
              imageURL = in.readLine();
              query = String.format("UPDATE Menu SET imageURL = '%s' WHERE imageURL = '%s'", imageURL, itemName);
              break;
              default:              
           }

           try{
           esql.executeUpdate(query);
           }catch(Exception e){
              System.err.println (e.getMessage ());
           }
         break;
        }
     }
     else{
        System.out.println("You don't have permission");
     }
     } catch(IOException e){

     }
  }


  public static void PlaceOrder(Cafe esql){
    String set = "";
    String query = "";

    try {
      boolean keepon = true;
      query = String.format("INSERT INTO Orders (orderid, login, paid, timeStampRecieved, total) VALUES (DEFAULT, '%s', false, NOW(), 0)", esql.user_login);
      
      esql.executeUpdate(query); 
      while (keepon) {
        System.out.println("NEXT ITEM (9 to quit): ");
        set = in.readLine(); 
        if(set.compareToIgnoreCase("9")==0){
          keepon=false;
        }else{
          //System.out.println(set.compareToIgnoreCase("9"));
          query = String.format("INSERT INTO ItemStatus (orderid, itemName, lastUpdated, status, comments) VALUES (LASTVAL(), '%s', NOW(), 'Hasn''t started', '')", set);
          //System.out.println(query);
          esql.executeUpdate(query);
          query = String.format("UPDATE Orders SET total = total + (SELECT MAX(M.price) FROM Menu M WHERE M.itemName = '%s') WHERE orderid=LASTVAL()", set);
          //System.out.println(query);
          esql.executeUpdate(query);
          // query = String.format("SELECT orderid, total FROM Orders WHERE orderid=LASTVAL()");
          // //System.out.println(query);
          // esql.executeQueryAndPrintResult(query);

          // Print order
          query = String.format("SELECT * FROM ItemStatus WHERE orderid=LASTVAL()");
          System.out.println(query);
          esql.executeQueryAndPrintResult(query);
          // Check user
        }
      }
      
      query = String.format("SELECT * FROM Orders WHERE orderid=LASTVAL()");
      //System.out.println(query);
      esql.executeQueryAndPrintResult(query);

    } catch (Exception e) {
      // e.printStackTrace();
      System.err.println (e.getMessage ());
    } 
  }


  public static void UpdateOrder(Cafe esql){
     int orderid;
     String itemName;
     Double price;
     String query;
     String type = GetType(esql).replaceAll("\\s", "");
     try{
     if(type.equalsIgnoreCase("Customer")){
        System.out.println("Customer - Update");
        System.out.print("Input the orderid to modify: ");
        orderid = Integer.parseInt(in.readLine());
        if(GetPaidType(esql, orderid).equals("f")){
           //=======
           // Check the orderer of order matches log-in name.
           //=======
           System.out.println("You can add an item");
           System.out.print("Input the name of item to add: ");
           itemName = in.readLine();
           price = GetItemPrice(esql, itemName);
           query = String.format("UPDATE Orders SET total = total + '%f' WHERE orderid = '%d'", price, orderid);
           esql.executeUpdate(query);
           System.out.println("Total: " +  GetOrderTotal(esql, orderid));
        }
        else{
           System.out.println("You can't change the paid order.");
        }
     }
     else if(type.equalsIgnoreCase("Manager")||type.equalsIgnoreCase("Employee")){
        System.out.println("Manager or Employee - Update");
        System.out.print("Input the orderid to change unpaid to paid: ");
        orderid = Integer.parseInt(in.readLine());
        if(GetPaidType(esql, orderid).equals("t")){
          System.out.println("Can't change the paid order.");
        }
        else if(GetPaidType(esql, orderid).equals("f")){
          System.out.println("The order is unpaid.");
          query = String.format("UPDATE Orders SET paid = true WHERE orderid = '%d'", orderid);
          esql.executeUpdate(query);
          System.out.println("The payment status changed From unpaid to paid.");
        }
     }
     }
     catch(Exception e){
        System.out.println("Error!");
     }
  }
   // Helper function
   public static String GetPaidType(Cafe esql, int orderid){
    try {
      String query = String.format("SELECT O.paid FROM Orders O WHERE O.orderid = '%d';", orderid);
      List<List<String>> result;
      result = esql.executeQueryAndReturnResult (query);
      return result.get(0).get(0); 
    } catch (Exception e) {
      // e.printStackTrace();
         System.err.println (e.getMessage ());
    }
    return "";
  }

   public static Double GetOrderTotal(Cafe esql, int orderid){
    try {
      String query = String.format("SELECT O.total FROM Orders O WHERE O.orderid = '%d';", orderid);
      List<List<String>> result;
      result = esql.executeQueryAndReturnResult (query);
      return Double.parseDouble(result.get(0).get(0)); 
    } catch (Exception e) {
      // e.printStackTrace();
         System.err.println (e.getMessage ());
    }
    return 0.0;
  }

   public static Double GetItemPrice(Cafe esql, String itemName){
    try {
      String query = String.format("SELECT M.price FROM Menu M WHERE M.itemName = '%s';", itemName);
      List<List<String>> result;
      result = esql.executeQueryAndReturnResult(query);
      System.out.println(result.get(0).get(0));
      return Double.parseDouble(result.get(0).get(0)); 
    } catch (Exception e) {
      // e.printStackTrace();
         System.err.println (e.getMessage ());
    }
    return 0.0;
  }

}//end Cafe

