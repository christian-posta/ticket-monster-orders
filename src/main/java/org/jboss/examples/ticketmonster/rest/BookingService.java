package org.jboss.examples.ticketmonster.rest;

import org.jboss.examples.ticketmonster.model.*;
import org.jboss.examples.ticketmonster.service.AllocatedSeats;
import org.jboss.examples.ticketmonster.service.Cancelled;
import org.jboss.examples.ticketmonster.service.Created;
import org.jboss.examples.ticketmonster.service.SeatAllocationService;

import javax.ejb.Stateless;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.Response;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;

/**
 * <p>
 *     A JAX-RS endpoint for handling {@link Booking}s. Inherits the GET
 *     methods from {@link BaseEntityService}, and implements additional REST methods.
 * </p>
 *
 * @author Marius Bogoevici
 * @author Pete Muir
 */
@Path("/bookings")
/**
 * <p>
 *     This is a stateless service, we declare it as an EJB for transaction demarcation
 * </p>
 */
@Stateless
public class BookingService extends BaseEntityService<Booking> {

    @Inject
    SeatAllocationService seatAllocationService;

    @Inject @Cancelled
    private Event<Booking> cancelledBookingEvent;

    @Inject @Created
    private Event<Booking> newBookingEvent;
    
    public BookingService() {
        super(Booking.class);
    }
    
    @DELETE
    public Response deleteAllBookings() {
    	List<Booking> bookings = getAll(new MultivaluedHashMap<String, String>());
    	for (Booking booking : bookings) {
    		deleteBooking(booking.getId());
    	}
        return Response.noContent().build();
    }

    /**
     * <p>
     * Delete a booking by id
     * </p>
     * @param id
     * @return
     */
    @DELETE
    @Path("/{id:[0-9][0-9]*}")
    public Response deleteBooking(@PathParam("id") Long id) {
        Booking booking = getEntityManager().find(Booking.class, id);
        if (booking == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        getEntityManager().remove(booking);
        // Group together seats by section so that we can deallocate them in a group
        Map<Section, List<Seat>> seatsBySection = new TreeMap<Section, java.util.List<Seat>>(SectionComparator.instance());
        for (Ticket ticket : booking.getTickets()) {
            List<Seat> seats = seatsBySection.get(ticket.getSeat().getSection());
            if (seats == null) {
                seats = new ArrayList<Seat>();
                seatsBySection.put(ticket.getSeat().getSection(), seats);
            }
            seats.add(ticket.getSeat());
        }
        // Deallocate each section block
        for (Map.Entry<Section, List<Seat>> sectionListEntry : seatsBySection.entrySet()) {
            seatAllocationService.deallocateSeats( sectionListEntry.getKey(),
                    booking.getPerformanceId(), sectionListEntry.getValue());
        }
        cancelledBookingEvent.fire(booking);
        return Response.noContent().build();
    }

    /**
     * <p>
     *   Create a booking. Data is contained in the bookingRequest object
     * </p>
     * @param bookingRequest
     * @return
     */
    @POST
    /**
     * <p> Data is received in JSON format. For easy handling, it will be unmarshalled in the support
     * {@link BookingRequest} class.
     */
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createBooking(BookingRequest bookingRequest) {
        try {
            // identify the ticket price ids in this request
            Set<Long> ticketPriceIds = bookingRequest.ticketPriceIds();
            
            // As we can have a mix of ticket types in a booking, we need to load all of them that are relevant,
            // id
            Map<Long, TicketPriceGuide> ticketPricesById = loadTicketPrices(ticketPriceIds);

            // Now, start to create the booking from the posted data
            // Set the simple stuff first!
            PerformanceId performance = new PerformanceId(bookingRequest.getPerformance(), bookingRequest.getPerformanceName());

            Booking booking = new Booking();
            booking.setContactEmail(bookingRequest.getEmail());
            booking.setPerformanceId(performance);

            // TODO ceposta: why is this hardcoed?
            booking.setCancellationCode("abc");

            // Now, we iterate over each ticket that was requested, and organize them by section. Each
            // section will have a map of ticketCategory->ticketsRequested
            // we want to allocate ticket requests that belong to the same section contiguously
            Map<Section, Map<TicketCategory, TicketRequest>> ticketRequestsPerSection
                    = new TreeMap<Section, java.util.Map<TicketCategory, TicketRequest>>(SectionComparator.instance());

            for (TicketRequest ticketRequest : bookingRequest.getTicketRequests()) {

                final TicketPriceGuide ticketPriceGuide = ticketPricesById.get(ticketRequest.getTicketPriceGuideId());

                if (!ticketRequestsPerSection.containsKey(ticketPriceGuide.getSection())) {
                    ticketRequestsPerSection
                            .put(ticketPriceGuide.getSection(), new HashMap<TicketCategory, TicketRequest>());
                }
                ticketRequestsPerSection.get(ticketPriceGuide.getSection())
                        .put(extractTicketCategory(ticketPricesById, ticketRequest), ticketRequest);
            }

            // Now, we can allocate the tickets
            // Iterate over the sections, finding the candidate seats for allocation
            // The process will lock the record for a given
            // Use deterministic ordering to prevent deadlocks
            Map<Section, AllocatedSeats> allocatedSeatsPerSection = new TreeMap<Section, AllocatedSeats>(SectionComparator.instance());

            List<Section> failedSections = new ArrayList<Section>();

            for (Section section : ticketRequestsPerSection.keySet()) {
                int totalTicketsRequestedPerSection = 0;
                // Compute the total number of tickets required (a ticket category doesn't impact the actual seat!)
                final Map<TicketCategory, TicketRequest> ticketRequestsByCategories = ticketRequestsPerSection.get(section);
                // calculate the total quantity of tickets to be allocated in this section
                for (TicketRequest ticketRequest : ticketRequestsByCategories.values()) {
                    totalTicketsRequestedPerSection += ticketRequest.getQuantity();
                }

                // *********************
                // try to allocate seats
                // *********************
                AllocatedSeats allocatedSeats = seatAllocationService.allocateSeats(section, performance, totalTicketsRequestedPerSection, true);
                if (allocatedSeats.getSeats().size() == totalTicketsRequestedPerSection) {
                    allocatedSeatsPerSection.put(section, allocatedSeats);
                } else {
                    failedSections.add(section);
                }
            }

            // if there are no failed sections, return success!!
            // this is kinda silly though because we may still want to allocate sections up to what we can and
            // ask display to the user which allocations we could get and let them decide what they want to do?
            // or we have a reservation step before they get the payment which will give them that information.
            if (failedSections.isEmpty()) {
                for (Section section : allocatedSeatsPerSection.keySet()) {
                    // allocation was successful, begin generating tickets
                    // associate each allocated seat with a ticket, assigning a price category to it
                    final Map<TicketCategory, TicketRequest> ticketRequestsByCategories = ticketRequestsPerSection.get(section);
                    AllocatedSeats allocatedSeats = allocatedSeatsPerSection.get(section);
                    allocatedSeats.markOccupied();
                    int seatCounter = 0;
                    // Now, add a ticket for each requested ticket to the booking
                    for (TicketCategory ticketCategory : ticketRequestsByCategories.keySet()) {
                        final TicketRequest ticketRequest = ticketRequestsByCategories.get(ticketCategory);
                        final TicketPriceGuide ticketPriceGuide = ticketPricesById.get(ticketRequest.getTicketPriceGuideId());
                        for (int i = 0; i < ticketRequest.getQuantity(); i++) {
                            Ticket ticket = new Ticket(allocatedSeats.getSeats().get(seatCounter + i), ticketCategory, ticketPriceGuide.getPrice());
                            // getEntityManager().persist(ticket);
                            booking.getTickets().add(ticket);
                        }
                        seatCounter += ticketRequest.getQuantity();
                    }
                }
                // Persist the booking, including cascaded relationships
                booking.setPerformanceId(performance);
                booking.setCancellationCode("abc");
                getEntityManager().persist(booking);
                newBookingEvent.fire(booking);
                return Response.ok().entity(booking).type(MediaType.APPLICATION_JSON_TYPE).build();
            } else {

                // cannot allocated all the sections so we just error out!?
                // TODO ceposta: we need to change this so we still allocate some? and report back how many we got?
                // and possibly ask about a waitlist?
                Map<String, Object> responseEntity = new HashMap<String, Object>();
                responseEntity.put("errors", Collections.singletonList("Cannot allocate the requested number of seats!"));
                return Response.status(Response.Status.BAD_REQUEST).entity(responseEntity).build();
            }
        } catch (ConstraintViolationException e) {
            // If validation of the data failed using Bean Validation, then send an error
            Map<String, Object> errors = new HashMap<String, Object>();
            List<String> errorMessages = new ArrayList<String>();
            for (ConstraintViolation<?> constraintViolation : e.getConstraintViolations()) {
                errorMessages.add(constraintViolation.getMessage());
            }
            errors.put("errors", errorMessages);
            // A WebApplicationException can wrap a response
            // Throwing the exception causes an automatic rollback
            throw new RestServiceException(Response.status(Response.Status.BAD_REQUEST).entity(errors).build());
        } catch (Exception e) {
            // Finally, handle unexpected exceptions
            Map<String, Object> errors = new HashMap<String, Object>();
            errors.put("errors", Collections.singletonList(e.getMessage()));
            errors.put("stacktrace", Collections.singletonList(getStackTrace(e)));
            // A WebApplicationException can wrap a response
            // Throwing the exception causes an automatic rollback
            throw new RestServiceException(Response.status(Response.Status.BAD_REQUEST).entity(errors).build());
        }
    }

    private TicketCategory extractTicketCategory(Map<Long, TicketPriceGuide> ticketPricesById, TicketRequest ticketRequest) {
        return ticketPricesById.get(ticketRequest.getTicketPriceGuideId()).getTicketCategory();
    }

    private String getStackTrace(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }

    /**
     * Utility method for loading ticket prices
     * @param priceCategoryIds
     * @return
     */
    private Map<Long, TicketPriceGuide> loadTicketPrices(Set<Long> priceCategoryIds) {
        List<TicketPriceGuide> ticketPriceGuides = (List<TicketPriceGuide>) getEntityManager()
                .createQuery("select p from TicketPriceGuide p where p.id in :ids", TicketPriceGuide.class)
                .setParameter("ids", priceCategoryIds).getResultList();
        // Now, map them by id
        Map<Long, TicketPriceGuide> ticketPricesById = new HashMap<Long, TicketPriceGuide>();
        for (TicketPriceGuide ticketPriceGuide : ticketPriceGuides) {
            ticketPricesById.put(ticketPriceGuide.getId(), ticketPriceGuide);
        }
        return ticketPricesById;
    }

}
