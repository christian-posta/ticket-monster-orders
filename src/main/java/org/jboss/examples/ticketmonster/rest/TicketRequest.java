package org.jboss.examples.ticketmonster.rest;

import org.jboss.examples.ticketmonster.model.TicketPriceGuide;

/**
 * <p>
 * A {@link BookingRequest} will contain multiple {@link TicketRequest}s.
 * </p>
 * 
 * @author Marius Bogoevici
 * @author Pete Muir
 * 
 */
public class TicketRequest {

    private long ticketPriceGuideId;

    private int quantity;

    public TicketRequest() {
        // Empty constructor
    }

    public TicketRequest(TicketPriceGuide ticketPriceGuide, int quantity) {
        this.ticketPriceGuideId = ticketPriceGuide.getId();
        this.quantity = quantity;
    }

    public long getTicketPriceGuideId() {
        return ticketPriceGuideId;
    }

    public void setTicketPriceGuideId(long ticketPriceGuideId) {
        this.ticketPriceGuideId = ticketPriceGuideId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
