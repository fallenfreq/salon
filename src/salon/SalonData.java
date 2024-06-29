package salon;

import java.util.Arrays;
import java.util.Comparator;

@FunctionalInterface
interface TriFunction<T, U, V, R> {
  R apply(T t, U u, V v);
}


interface HasPrimaryKey<K> {
  K getPrimaryKey();
}


class Service implements HasPrimaryKey<Integer> {
  private Integer serviceId;
  private String serviceName;
  private Integer serviceCost;

  Service(Integer serviceId, String serviceName, Integer serviceCost) {
    this.serviceId = serviceId;
    this.serviceName = serviceName;
    this.serviceCost = serviceCost;
  }

  @Override
  public Integer getPrimaryKey() {
    return serviceId;
  }

  public Integer getServiceId() {
    return serviceId;
  }

  public String getServiceName() {
    return serviceName;
  }

  public Integer getServiceCost() {
    return serviceCost;
  }

  public String toHeaderString() {
    return String.format("   | %-5s | %-15s | %-7s |%n", "ID", "Name", "Price");
  }

  public String toBodyString() {
    return String.format("   | %-5s | %-15s | %-7s |%n", serviceId, serviceName, "£" + serviceCost);
  }

  @Override
  public String toString() {
    return "\n" + toHeaderString() + toBodyString();
  }
}


class Stylist implements HasPrimaryKey<Integer> {
  private Integer stylistId;
  private String stylistName;
  private String title;
  private Integer totalEarnings = 0;

  Stylist(Integer stylistId, String stylistName, String title) {
    this.stylistId = stylistId;
    this.stylistName = stylistName;
    this.title = title;
  }

  @Override
  public Integer getPrimaryKey() {
    return stylistId;
  }

  public Integer getStylistId() {
    return stylistId;
  }

  public String getStylistName() {
    return stylistName;
  }

  public String getTitle() {
    return title;
  }

  public Integer addToTotal(Integer amount) {
    return totalEarnings += amount;
  }

  public Integer getTotalEarnings() {
    return totalEarnings;
  }

  public String toHeaderString() {
    return String.format("   | %-5s | %-15s | %-15s |%n", "ID", "Stylist Name", "Total Earnings");
  }

  public String toBodyString() {
    return String.format(
      "   | %-5s | %-15s | %-15s |%n", stylistId, stylistName, "£" + totalEarnings);
  }

  @Override
  public String toString() {
    return "\n" + toHeaderString() + toBodyString();
  }
}


class Booking implements HasPrimaryKey<Integer> {
  private Integer bookingId;
  private Integer clientId;
  private Integer[] serviceIds;
  private Integer stylistId;

  Booking(Integer bookingId, Integer clientId, Integer[] serviceId, Integer stylistId) {
    this.bookingId = bookingId;
    this.clientId = clientId;
    this.serviceIds = serviceId;
    this.stylistId = stylistId;
  }

  @Override
  public Integer getPrimaryKey() {
    return bookingId;
  }

  public Integer getBookingId() {
    return bookingId;
  }

  public Integer getClientId() {
    return clientId;
  }

  public Integer[] getServiceIds() {
    return serviceIds;
  }

  public Integer getStylistId() {
    return stylistId;
  }

  public String toHeaderString() {
    return String.format(
      "   | %-5s | %-10s | %-10s | %-15s |%n", "ID", "Client ID", "Stylist ID", "Service IDs");
  }

  public String toBodyString() {
    return String.format(
      "   | %-5s | %-10s | %-10s | %-15s |%n",
      bookingId, clientId, stylistId, Arrays.toString(serviceIds));
  }

  @Override
  public String toString() {
    return "\n" + toHeaderString() + toBodyString();
  }
}


// Client class representing client details
class Client implements HasPrimaryKey<Integer> {
  private Integer clientId;
  private String firstName;
  private String lastName;
  private String phone;
  private Integer totalSpend = 0;
  // Other client details as needed

  public Client(Integer clientId, String firstName, String lastName, String phone) {
    this.clientId = clientId;
    this.firstName = firstName;
    this.lastName = lastName;
    this.phone = phone;
  }

  @Override
  public Integer getPrimaryKey() {
    return clientId;
  }

  public Integer getClientId() {
    return clientId;
  }

  public String getFirstName() {
    return firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public String getPhone() {
    return phone;
  }

  public Integer addToTotal(Integer amount) {
    return totalSpend += amount;
  }

  public Integer getTotalSpend() {
    return totalSpend;
  }

  public String toHeaderString() {
    return String.format(
      "   | %-5s | %-15s | %-15s | %-15s |%n", "ID", "First Name", "Last Name", "Phone");
  }

  public String toBodyString() {
    return String.format(
      "   | %-5s | %-15s | %-15s | %-15s |%n", clientId, firstName, lastName, phone);
  }

  @Override
  public String toString() {
    return "\n" + toHeaderString() + toBodyString();
  }
}


class SalonData {
  RBTree<Integer, Service> serviceTree = new RBTree<>();
  private Integer nextServiceId = 1;
  RBTree<Integer, Booking> bookingTree = new RBTree<>();
  private Integer nextBookingId = 1;
  RBTree<Integer, Client> clientTree = new RBTree<>();
  private Integer nextClientId = 1;
  RBTree<Integer, Stylist> stylistTree = new RBTree<>();
  private Integer nextStylistId = 1;

  // TODO: need to check if they already exist in the system
  // if they do then just update instead
  Service addService(String serviceName, Integer serviceCost) {
    Service service = new Service(nextServiceId++, serviceName, serviceCost);
    serviceTree.add(service);
    return service;
  }

  // The primary key is added to the end of index keys to make them unique. You can lookup a value
  // if it has a unique key otherwise its not currently reliable. You can still index non unique
  // keys though to keep a list of values in order.
  Booking addBooking(Integer clientId, Stylist stylist, Integer... serviceIds) {
    Integer totalServicesCost = calcTotalServicesCost(serviceIds);
    // keep track of clients total spend and stylists revenue
    stylist.addToTotal(totalServicesCost);
    stylistTree.add(stylist); // will update the index
    if (clientId != null) {
      Client client = clientTree.get(clientId);
      client.addToTotal(totalServicesCost);
      clientTree.add(client); // will update the index
    }
    Booking booking = new Booking(nextBookingId++, clientId, serviceIds, stylist.getStylistId());
    bookingTree.add(booking);
    return booking;
  }

  Booking addBooking(Integer clientId, Integer... serviceIds) {
    Stylist stylist = lowestEarnings();
    if (stylist == null) {
      throw new IllegalStateException("No stylists in the system");
    }
    return addBooking(clientId, stylist, serviceIds);
  }

  Client addClient(String firstName, String lastName, String phone) {
    Client client = new Client(nextClientId++, firstName, lastName, phone);
    clientTree.add(client);
    return client;
  }

  Stylist addStylist(String stylistName, String title) {
    Stylist stylist = new Stylist(nextStylistId++, stylistName, title);
    stylistTree.add(stylist);
    // Add a sentinel bookings so that lowestEarnings picks up clients
    addBooking(null, stylist);
    return stylist;
  }

  public Integer calcTotalServicesCost(Integer[] serviceIds) {
    Integer totalCost = 0;
    for (Integer serviceId : serviceIds) {
      totalCost += serviceTree.get(serviceId).getServiceCost();
    }
    return totalCost;
  }

  public Stylist lowestEarnings() {
    Aggregate<String, Result<Booking, Integer>, Result<Booking, Integer>> aggragate =
      bookingTree
        .aggregate(
          0,
          (b, i) -> b.getStylistId(),
          (acc, b) -> acc + calcTotalServicesCost(b.getServiceIds()))
        .aggregate(
          null,
          (b, i) -> "min",
          (acc, result) -> {
            if (acc == null || result.getAccumulator() <= acc.getAccumulator()) {
              return result;
            } else {
              return acc;
            }
          });

    Integer lowestStylistId = aggragate.get("min").getAccumulator().getValue(0).getStylistId();
    return stylistTree.get(lowestStylistId);
  }

  // Method to count the total number of bookings each stylist has completed
  public Aggregate<Integer, Booking, Integer> countStylistBookings() {
    return bookingTree.aggregate(
      0, (b, i) -> stylistTree.get(b.getStylistId()).getStylistId(), (acc, b) -> acc + 1);
  }

  public Aggregate<Integer, Booking, Aggregate<Integer, Booking, Integer>> countStylistsClients() {
    return bookingTree.aggregate(
      null,
      (b, i) -> b.getStylistId(),
      (acc, b) -> {
        if (acc == null) {
          acc =
            new Aggregate<Integer, Booking, Integer>(
              0, (b2, i) -> b2.getClientId(), (acc2, b2, agg) -> acc2 + 1);
        }
        acc.put(b);
        return acc;
      });
  }

  // Sort all clients by service cost using cached value on client (highest cost first)
  public IndexTree<Integer, Integer, Client> sortClientsServiceCostCached() {
    return clientTree.sort(
      clientEntry -> clientEntry.getValue().getTotalSpend(), Comparator.reverseOrder());
  }

  // Sort all clients by service cost (highest cost first) if there was no cache on client
  public IndexTree<Integer, Integer, Result<Booking, Integer>> sortClientsByServiceCost() {
    return bookingTree
      .aggregate(
        0,
        (b, i) -> b.getClientId(),
        (acc, b) -> acc + calcTotalServicesCost(b.getServiceIds()))
      .sort(spendEntry -> spendEntry.getValue().getAccumulator(), Comparator.reverseOrder());
  }

  public IndexTree<Integer, String, Client> sortClientsByLastName() {
    return clientTree.sort(clientEntry -> clientEntry.getValue().getLastName());
  }

  // Method to calculate the total cost of each service type
  public Aggregate<Integer, Booking, Integer> calculateServiceRevenue() {
    return bookingTree.aggregate(
      0,
      (b, i) -> i == null ? 0 : b.getServiceIds()[i],
      (acc, b, aggregate) -> {
        Integer[] serviceIds = b.getServiceIds();
        for (int i = 0; i < serviceIds.length; i++) {
          aggregate.put(b, i, acc + serviceTree.get(serviceIds[i]).getServiceCost());
        }
        return acc;
      });
  }

  public Client findClientWithLowestServiceCost() {
    Aggregate<String, Booking, Booking> lowestCostBooking =
      bookingTree.aggregate(
        null, // set initial value to null
        (b, i) -> "min", // set key to store the min value
        (acc, b) -> { // keep track of the lowest
          if (acc == null
            || calcTotalServicesCost(b.getServiceIds()) <= calcTotalServicesCost(
              acc.getServiceIds())) {
            return b;
          } else {
            return acc;
          }
        });
    return clientTree.get(lowestCostBooking.get("min").getAccumulator().getClientId());
  }

  public Client findClientWithLowestTotalServiceCostCached() {
    Aggregate<String, Client, Client> lowestCostBooking =
      clientTree.aggregate(
        null, // set initial value to null
        (c, i) -> "min", // set key to store the min value
        (acc, c) -> { // keep track of the lowest
          if (acc == null || c.getTotalSpend() <= acc.getTotalSpend()) {
            return c;
          } else {
            return acc;
          }
        });

    return lowestCostBooking.get("min").getAccumulator();
  }

  public Client findClientWithLowestTotalServiceCost() {
    Aggregate<String, Result<Booking, Integer>, Result<Booking, Integer>> aggragate =
      bookingTree
        .aggregate(
          0,
          (b, i) -> b.getClientId(),
          (acc, b) -> acc + calcTotalServicesCost(b.getServiceIds()))
        .aggregate(
          null,
          (b, i) -> "min",
          (acc, result) -> {
            if (acc == null || result.getAccumulator() <= acc.getAccumulator()) {
              return result;
            } else {
              return acc;
            }
          });

    Integer lowestSpendClientId = aggragate.get("min").getAccumulator().getValue(0).getClientId();
    return clientTree.get(lowestSpendClientId);
  }

  public Client findClientWithHighestServiceCost() {
    Aggregate<String, Booking, Booking> highestCostBooking =
      bookingTree.aggregate(
        null, // set initial value to null
        (b, i) -> "max", // set key to store the max value
        (acc, b) -> { // keep track of the highest
          if (acc == null
            || calcTotalServicesCost(b.getServiceIds()) > calcTotalServicesCost(
              acc.getServiceIds())) {
            return b;
          } else {
            return acc;
          }
        });
    return clientTree.get(highestCostBooking.get("max").getAccumulator().getClientId());
  }

  public Client findClientWithHighestTotalServiceCostCached() {
    Aggregate<String, Client, Client> highestCostClient =
      clientTree.aggregate(
        null, // set initial value to null
        (c, i) -> "max", // set key to store the max value
        (acc, c) -> { // keep track of the highest
          if (acc == null || c.getTotalSpend() > acc.getTotalSpend()) {
            return c;
          } else {
            return acc;
          }
        });

    return highestCostClient.get("max").getAccumulator();
  }

  public Client findClientWithHighestTotalServiceCost() {
    Aggregate<String, Result<Booking, Integer>, Result<Booking, Integer>> aggragate =
      bookingTree
        .aggregate(
          0,
          (b, i) -> b.getClientId(),
          (acc, b) -> acc + calcTotalServicesCost(b.getServiceIds()))
        .aggregate(
          null,
          (b, i) -> "max",
          (acc, result) -> {
            if (acc == null || result.getAccumulator() > acc.getAccumulator()) {
              return result;
            } else {
              return acc;
            }
          });

    Integer highestSpendClientId = aggragate.get("max").getAccumulator().getValue(0).getClientId();
    return clientTree.get(highestSpendClientId);
  }

  // Find the allocated customers to stylist with their required services
  public RBTree<Integer, Booking> filterByStylist(Integer stylistId) {
    return bookingTree.filter(booking -> booking.getStylistId() == stylistId);
  }
}
