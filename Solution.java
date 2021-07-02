/**
 * Filters of List of Flights
 */

interface flightFilter<T>{
     List<T> dynamicFilter(List<T> b, int... a);
}

class Filters implements flightFilter<Flight> {

    List<Predicate<Segment>> allPredicates = Arrays.asList(
            x -> x.getDepartureDate().isAfter(LocalDateTime.now()),
            x -> x.getDepartureDate().isBefore(x.getArrivalDate()),
            x -> Duration.between(x.getDepartureDate(),x.getArrivalDate()).toHours() > 0 && Duration.between(x.getDepartureDate(),x.getArrivalDate()).toHours() <=2

            );
    @Override
     public List<Flight> dynamicFilter(List<Flight> flights, int... indexes) {

        Predicate<Segment> compositePredicate = allPredicates.get(indexes[0]);
        for (int i = 1; i < indexes.length; i++) {
            compositePredicate = compositePredicate.and(allPredicates.get(indexes[i]));
        }

        List<Flight> sortedList = new ArrayList<>();

        for (Flight flight : flights) {
            if (flight.getSegments().stream().allMatch(compositePredicate)) {
                sortedList.add(flight);
            }
        }
        return sortedList;
    }
}



/**
 * Check
 */
class Main {
    public static void main(String[] argc) {

        flightFilter<Flight> object = new Filters();

        System.out.println("All flights");
        FlightBuilder.createFlights().forEach(System.out::println);
        System.out.println();

        System.out.println("Dynamic filters:");
        System.out.println();

        System.out.println("DepartureBeforeNowFilter: ");
        object.dynamicFilter(FlightBuilder.createFlights(),0).forEach(System.out::println);
        System.out.println();

        System.out.println("DepartureBeforeArrivalSegmentsFilter: ");
        object.dynamicFilter(FlightBuilder.createFlights(),1).forEach(System.out::println);
        System.out.println();

        System.out.println("GroundOverTwoHoursFilter: ");
        object.dynamicFilter(FlightBuilder.createFlights(),2).forEach(System.out::println);
        System.out.println();



        System.out.println("Apply all filters: ");
        object.dynamicFilter(FlightBuilder.createFlights(),0,1,2).forEach(System.out::println);
    }
