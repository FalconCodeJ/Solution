package com.gridnine.testing;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.time.Duration;


/**
 * Factory class to get sample list of flights.
 */
class FlightBuilder {
    static List<Flight> createFlights() {
        LocalDateTime threeDaysFromNow = LocalDateTime.now().plusDays(3);
        return Arrays.asList(
                //A normal flight with two hour duration
                createFlight(threeDaysFromNow, threeDaysFromNow.plusHours(2)),
                //A normal multi segment flight
                createFlight(threeDaysFromNow, threeDaysFromNow.plusHours(2),
                        threeDaysFromNow.plusHours(3), threeDaysFromNow.plusHours(5)),
                //A flight departing in the past
                createFlight(threeDaysFromNow.minusDays(6), threeDaysFromNow),
                //A flight that departs before it arrives
                createFlight(threeDaysFromNow, threeDaysFromNow.minusHours(6)),
                //A flight with more than two hours ground time
                createFlight(threeDaysFromNow, threeDaysFromNow.plusHours(2),
                        threeDaysFromNow.plusHours(5), threeDaysFromNow.plusHours(6)),
                //Another flight with more than two hours ground time
                createFlight(threeDaysFromNow, threeDaysFromNow.plusHours(2),
                        threeDaysFromNow.plusHours(3), threeDaysFromNow.plusHours(4),
                        threeDaysFromNow.plusHours(6), threeDaysFromNow.plusHours(7)));
    }

    private static Flight createFlight(final LocalDateTime... dates) {
        if ((dates.length % 2) != 0) {
            throw new IllegalArgumentException(
                    "you must pass an even number of dates");
        }
        List<Segment> segments = new ArrayList<>(dates.length / 2);
        for (int i = 0; i < (dates.length - 1); i += 2) {
            segments.add(new Segment(dates[i], dates[i + 1]));
        }
        return new Flight(segments);
    }
}

/**
 * Bean that represents a flight.
 */
class Flight {
    private final List<Segment> segments;

    Flight(final List<Segment> segs) {
        segments = segs;
    }

    List<Segment> getSegments() {
        return segments;
    }

    @Override
    public String toString() {
        return segments.stream().map(Object::toString)
                .collect(Collectors.joining(" "));
    }
}

/**
 * Bean that represents a flight segment.
 */
class Segment {
    private final LocalDateTime departureDate;

    private final LocalDateTime arrivalDate;

    Segment(final LocalDateTime dep, final LocalDateTime arr) {
        departureDate = Objects.requireNonNull(dep);
        arrivalDate = Objects.requireNonNull(arr);
    }

    LocalDateTime getDepartureDate() {
        return departureDate;
    }

    LocalDateTime getArrivalDate() {
        return arrivalDate;
    }

    @Override
    public String toString() {
        DateTimeFormatter fmt =
                DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
        return '[' + departureDate.format(fmt) + '|' + arrivalDate.format(fmt)
                + ']';
    }
}


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
