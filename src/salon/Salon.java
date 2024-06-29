package salon;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

// StringBuilder report = new StringBuilder();

public class Salon {
  static SalonData salonData = new SalonData();

  public static void main(String[] args) {
    // Creating a secondary index by last name
    salonData.clientTree.saveIndex(
      salonData.clientTree.<String>sort(clientEntry -> clientEntry.getValue().getLastName()),
      "lastNameIndex");

    salonData.stylistTree.saveIndex(
      salonData.stylistTree.<String>sort(
        stylistEntry -> stylistEntry.getValue().getStylistName()),
      "stylistNameIndex");

    salonData.clientTree.saveIndex(
      salonData.clientTree.<Integer>sort(clientEntry -> clientEntry.getValue().getTotalSpend()),
      "totalSpendIndex");

    salonData.stylistTree.saveIndex(
      salonData.stylistTree.<Integer>sort(
        stylistEntry -> stylistEntry.getValue().getTotalEarnings()),
      "totalEarningsIndex");

    // Options for insert-client command
    Options insertClientOptions = new Options();
    insertClientOptions.addOption(
      Option.builder("f")
        .longOpt("first-name")
        .desc("First name of the client")
        .hasArg()
        .required(true)
        .build());
    insertClientOptions.addOption(
      Option.builder("l")
        .longOpt("last-name")
        .desc("Last name of the client")
        .hasArg()
        .required(true)
        .build());
    insertClientOptions.addOption(
      Option.builder("p")
        .longOpt("phone")
        .desc("Phone number of the client")
        .hasArg()
        .required(true)
        .build());

    // Options for insert-stylist command
    Options insertStylistOptions = new Options();
    insertStylistOptions.addOption(
      Option.builder("n")
        .longOpt("name")
        .desc("Name of the stylist")
        .hasArg()
        .required(true)
        .build());
    insertStylistOptions.addOption(
      Option.builder("t")
        .longOpt("title")
        .desc("Title of the stylist")
        .hasArg()
        .required(true)
        .build());

    // Options for insert-service command
    Options insertServiceOptions = new Options();
    insertServiceOptions.addOption(
      Option.builder("s")
        .longOpt("service")
        .desc("Name of the service")
        .hasArg()
        .required(true)
        .build());
    insertServiceOptions.addOption(
      Option.builder("p")
        .longOpt("price")
        .desc("Price of the service in pounds")
        .hasArg()
        .required(true)
        .build());

    // Options for insert-booking command
    Options insertBookingOptions = new Options();
    insertBookingOptions.addOption(
      Option.builder("c")
        .longOpt("client-id")
        .desc("Client ID for the booking")
        .hasArg()
        .required(true)
        .build());
    insertBookingOptions.addOption(
      Option.builder("s")
        .longOpt("service-ids")
        .desc("List of comma separated service ID's for the booking")
        .hasArg()
        .required(true)
        .build());

    // Options for query command
    Options queryOptions = new Options();
    queryOptions.addOption(
      Option.builder("a")
        .longOpt("client-allocations")
        .desc(
          "Client allocations for a given stylist by name or pass no stylist name to see all allocations")
        .hasArg()
        .optionalArg(true)
        .build());
    queryOptions.addOption(
      Option.builder("c")
        .longOpt("sort-client")
        .desc("Sort clients by service-cost or last-name")
        .hasArg()
        .build());
    queryOptions.addOption(
      Option.builder("i")
        .longOpt("total-service-revenue")
        .desc("Calculate total service revenue")
        .build());
    queryOptions.addOption(
      Option.builder("h")
        .longOpt("highest-spending-client")
        .desc("Find highest spending client")
        .build());
    queryOptions.addOption(
      Option.builder("b")
        .longOpt("lowest-spending-client")
        .desc("Find lowest spending client")
        .build());
    queryOptions.addOption(
      Option.builder("l")
        .longOpt("list")
        .desc("List data from specific table")
        .hasArg()
        .build());

    // Scanner for reading input
    Scanner scanner = new Scanner(System.in);
    CommandLineParser parser = new DefaultParser();
    HelpFormatter formatter = new HelpFormatter();

    Runnable printHelp =
      () -> runPrintHelp(
        formatter,
        insertClientOptions,
        insertStylistOptions,
        insertServiceOptions,
        insertBookingOptions,
        queryOptions);

    while (true) {
      System.out.print("Type help to see commands, demo to insert demo data or exit to quit > ");
      String inputLine = scanner.nextLine().trim();
      if (inputLine.equalsIgnoreCase("exit")) {
        System.out.println("Exiting...");
        break;
      } else if (inputLine.equalsIgnoreCase("help")) {
        printHelp.run();
        continue;
      } else if (inputLine.equalsIgnoreCase("demo")) {
        addExampleData();
        System.out.println("Example data addded");
        continue;
      }

      List<String> inputArgs = parseCommandLine(inputLine);
      if (inputArgs.size() < 1) {
        System.out.println("No command provided");
        System.out.println("");
        continue;
      }

      String command = inputArgs.get(0);
      List<String> commandArgsList = inputArgs.subList(1, inputArgs.size());
      String[] commandArgs = commandArgsList.toArray(new String[0]);

      CommandLine cmd = null;

      try {
        switch (command) {
          case "insert-client":
            cmd = parser.parse(insertClientOptions, commandArgs, false);
            handleInsertClient(cmd);
            System.out.println("");
            break;
          case "insert-stylist":
            cmd = parser.parse(insertStylistOptions, commandArgs, false);
            handleInsertStylist(cmd);
            System.out.println("");
            break;
          case "insert-service":
            cmd = parser.parse(insertServiceOptions, commandArgs, false);
            handleInsertService(cmd);
            System.out.println("");
            break;
          case "insert-booking":
            cmd = parser.parse(insertBookingOptions, commandArgs, false);
            handleInsertBooking(cmd);
            System.out.println("");
            break;
          case "query":
            cmd = parser.parse(queryOptions, commandArgs, false);
            handleQuery(cmd);
            System.out.println("");
            break;
          default:
            System.out.println("Unknown command: " + command);
            System.out.println("Type help to see all commands: " + command);
            System.out.println("");
        }
      } catch (Exception e) {
        System.out.println(e.getMessage());
        System.out.println("");
      }
    }

    scanner.close();
  }

  private static void addExampleData() {
    salonData.addClient("John", "Doe", "+44 842018472");
    salonData.addClient("Jane", "Smith", "(44) 8420184");
    salonData.addClient("Alice", "Johnson", "842-01847201");

    // Adding some example services
    salonData.addService("Haircut", 50);
    salonData.addService("Shave", 30);
    salonData.addService("Style", 70);
    salonData.addService("Colour", 75);

    // Adding some example stylists
    salonData.addStylist("Fred Sharp", "Senior Stylist");
    salonData.addStylist("Sally Robins", "Junior Stylist");
    salonData.addStylist("Mr hair", "Junior Stylist");

    // Adding some example bookings
    salonData.addBooking(2, 3);
    salonData.addBooking(1, 1);
    salonData.addBooking(1, 2);
    salonData.addBooking(3, 1);
    salonData.addBooking(1, 2);
  }

  private static String validString(String arg, String argName) {
    String result = Validator.isValidStringLength(arg, 1, 70);
    if (result == null) {
      throw new ValidationException(
        argName + " should be between 1 and 70 characters. You entered: " + arg);
    }
    return result;
  }

  private static Integer validInteger(String arg, String argName) {
    Integer result = Validator.isValidNumberInRange(arg, 1, Integer.MAX_VALUE);
    if (result == null) {
      throw new ValidationException(
        argName
          + " should be an intger between 1 and "
          + Integer.MAX_VALUE
          + ". You entered: "
          + arg);
    }
    return result;
  }

  private static <V> V validValue(V arg, String argName, Set<V> set) {
    V result = Validator.validateAllowedValues(arg, set);
    if (result == null) {
      throw new ValidationException(
        argName + " should be one of these values " + set + ". You entered: " + arg);
    }
    return result;
  }

  private static void handleInsertClient(CommandLine cmd) {
    String firstName = cmd.getOptionValue("first-name");
    String lastName = cmd.getOptionValue("last-name");
    String phone = cmd.getOptionValue("phone");

    validString(firstName, "first-name");
    validString(lastName, "last-name");
    if (Validator.isValidPhoneNumber(phone) == null) {
      throw new ValidationException("phone should be a valid phone number. You entered: " + phone);
    }

    Client client = salonData.addClient(firstName, lastName, phone);
    System.out.print("Client added");
    System.out.print(client);
  }

  private static void handleInsertStylist(CommandLine cmd) {
    String name = cmd.getOptionValue("name");
    String title = cmd.getOptionValue("title");

    validString(name, "name");
    validString(title, "title");

    Stylist stylist = salonData.addStylist(name, title);
    System.out.print("Stylist added");
    System.out.print(stylist);
  }

  private static void handleInsertService(CommandLine cmd) {
    String serviceName = cmd.getOptionValue("service");
    String priceRaw = cmd.getOptionValue("price");

    validString(serviceName, "service");
    Integer price = validInteger(priceRaw, "price");

    Service service = salonData.addService(serviceName, price);
    System.out.print("Stylist added");
    System.out.print(service);
  }

  private static void handleInsertBooking(CommandLine cmd) {
    String clinetIdRaw = cmd.getOptionValue("client-id");
    String csvServiceIds = cmd.getOptionValue("service-ids");

    Integer clinetId = validInteger(clinetIdRaw, "client-id");
    Integer[] serviceIds =
      Validator.validateCommaSeparatedString(
        csvServiceIds, serviceId -> validInteger(serviceId, "Each csv of service-ids"));
    // TODO Check that the clinetId and serviceIds are in the system
    Booking booking = salonData.addBooking(clinetId, serviceIds);
    System.out.print("Booking added");
    System.out.print(booking);
  }

  private static void handleQuery(CommandLine cmd) {
    boolean clientAllocations = cmd.hasOption("client-allocations");
    boolean totalServiceRevenue = cmd.hasOption("total-service-revenue");
    boolean highestSpendingClient = cmd.hasOption("highest-spending-client");
    boolean lowestSpendingClient = cmd.hasOption("lowest-spending-client");
    String sortClientsBy = cmd.getOptionValue("sort-client");
    String dataToList = cmd.getOptionValue("list");

    if (sortClientsBy != null) {
      validValue(sortClientsBy, "sort-client", Set.of("service-cost", "last-name"));
    }
    if (dataToList != null) {
      validValue(dataToList, "list", Set.of("clients", "services", "stylists", "bookings"));
    }

    if (clientAllocations) {
      String stylistName = cmd.getOptionValue("client-allocations");
      if (stylistName != null) {
        // TODO this is still using the default toString to print but the service data is accessible
        // on the object
        System.out.println(
          "NOTICE: This is still using the default toString to print but the service data is accessible on the object.");
        System.out.println(
          "NOTICE: The data keys are client IDs the stylist has served and the result contains client info");
        System.out.println("Client Allocations for " + stylistName);
        Stylist stylist = salonData.stylistTree.get(stylistName, "stylistNameIndex");
        if (stylist == null) {
          throw new ValidationException("There is no stylist in the system named " + stylistName);
        }
        System.out.print(salonData.countStylistsClients().get(stylist.getStylistId()));
      } else {
        // TODO this is still using the default toString to print but the service data is accessible
        // on the object
        System.out.println(
          "NOTICE: This is still using the default toString to print but the service data is accessible on the object.");
        System.out.println(
          "NOTICE: The data keys are stylist ids and the values are nested data with keys of each client ID the stylist has served");
        System.out.println("All Client Allocations");
        System.out.print(salonData.countStylistsClients());
      }
    }

    if (totalServiceRevenue) {
      // TODO this is still using the default toString to print
      System.out.println("NOTICE: This is still using the default toString to Print");
      System.out.println(
        "NOTICE: The data keys are service IDs and the Result contains the service revenue");
      System.out.println("Total Revenue by Service");
      System.out.print(salonData.calculateServiceRevenue());
    }

    if (highestSpendingClient) {
      System.out.println("Highest Spending Client");
      System.out.print(salonData.findClientWithHighestTotalServiceCostCached());
    }

    if (lowestSpendingClient) {
      System.out.println("Lowest Spending Client");
      System.out.print(salonData.findClientWithLowestTotalServiceCostCached());
    }

    if (sortClientsBy != null) {
      switch (sortClientsBy) {
        case "service-cost":
          System.out.println("Clients sorted by service cost");
          System.out.print(salonData.sortClientsServiceCostCached());
          break;
        case "last-name":
          System.out.println("Clients sorted by last name");
          System.out.print(salonData.sortClientsByLastName());
          break;
      }
    }

    if (dataToList != null) {
      switch (dataToList) {
        case "clients":
          System.out.println("All Clients");
          System.out.print(salonData.clientTree);
          break;
        case "services":
          System.out.println("All Services");
          System.out.print(salonData.serviceTree);
          break;
        case "stylists":
          System.out.println("All Stylists");
          System.out.print(salonData.stylistTree);
          break;
        case "bookings":
          System.out.println("All Bookings");
          System.out.print(salonData.bookingTree);
          break;
      }
    }
  }

  public static List<String> parseCommandLine(String inputLine) {
    List<String> inputArgs = new ArrayList<>();
    Pattern regex = Pattern.compile("[^\\s\"']+|\"([^\"]*)\"|'([^']*)'");
    Matcher regexMatcher = regex.matcher(inputLine);
    while (regexMatcher.find()) {
      if (regexMatcher.group(1) != null) {
        // Add double-quoted string without the quotes
        inputArgs.add(regexMatcher.group(1));
      } else if (regexMatcher.group(2) != null) {
        // Add single-quoted string without the quotes
        inputArgs.add(regexMatcher.group(2));
      } else {
        // Add unquoted word
        inputArgs.add(regexMatcher.group());
      }
    }
    return inputArgs;
  }

  private static void runPrintHelp(
    HelpFormatter formatter,
    Options insertClientOptions,
    Options insertStylistOptions,
    Options insertServiceOptions,
    Options insertBookingOptions,
    Options queryOptions) {
    formatter.printHelp("insert-client", insertClientOptions);
    System.out.println("");
    formatter.printHelp("insert-stylist", insertStylistOptions);
    System.out.println("");
    formatter.printHelp("insert-service", insertServiceOptions);
    System.out.println("");
    formatter.printHelp("insert-booking", insertBookingOptions);
    System.out.println("");
    formatter.printHelp("query", queryOptions);
    System.out.println("");
  }

  public class Enterprise {
    // Instance variables
    private String enterpriseName;
    private String industry;
    private int totalEmployees;

    // Constructor
    public Enterprise(String enterpriseName, String industry, int totalEmployees) {
      this.enterpriseName = enterpriseName;
      this.industry = industry;
      this.totalEmployees = totalEmployees;
    }

    // Getter method for enterpriseName
    public String getName() {
      return enterpriseName;
    }

    // Getter method for totalEmployees
    public int getTotalEmployees() {
      return totalEmployees;
    }

    // Setter method for totalEmployees
    public void setTotalEmployees(int totalEmployees) {
      this.totalEmployees = totalEmployees;
    }

    // Getter method for industry
    public String getIndustry() {
      return industry;
    }
  }
}
