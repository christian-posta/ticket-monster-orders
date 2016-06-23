package org.jboss.examples.ticketmonster.rest;

import org.jboss.examples.ticketmonster.model.PerformanceId;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * <p>
 * A {@link BookingRequest} is populated with unmarshalled JSON data, and handed to 
 * {@link BookingService#createBooking(BookingRequest)}.
 * </p>
 * 
 * @author Marius Bogoevici
 * @author Pete Muir
 * 
 */
public class BookingRequest {

    private List<TicketRequest> ticketRequests = new ArrayList<TicketRequest>();
    private long performance;
    private String performanceName;
    private String email;
    
    public BookingRequest() {
        // Empty constructor for JAXB
    }

    public BookingRequest(PerformanceId performance, String email) {
        this.performance = performance.getId();
        this.email = email;
    }

    public List<TicketRequest> getTicketRequests() {
        return ticketRequests;
    }

    public void setTicketRequests(List<TicketRequest> ticketRequests) {
        this.ticketRequests = ticketRequests;
    }
    
    public BookingRequest addTicketRequest(TicketRequest ticketRequest) {
        ticketRequests.add(ticketRequest);
        return this;
    }

    public long getPerformance() {
        return performance;
    }

    public void setPerformance(long performance) {

        this.performance = performance;
    }

    public String getPerformanceName() {
        return performanceName;
    }

    public void setPerformanceName(String performanceName) {
        this.performanceName = performanceName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Utility method - computes the unique pricing ids in the request
     *
     * @return
     */
    Set<Long> ticketPriceIds() {
        Set<Long> ticketPriceIds = new HashSet<Long>();
        for (TicketRequest ticketRequest : getTicketRequests()) {
            if (ticketPriceIds.contains(ticketRequest.getTicketPriceGuideId())) {
                // TODO ceposta: should we just ignore this? who cares if there's a dup?
                throw new RuntimeException("Duplicate price category id");
            }
            ticketPriceIds.add(ticketRequest.getTicketPriceGuideId());
        }
        return ticketPriceIds;
    }
}
