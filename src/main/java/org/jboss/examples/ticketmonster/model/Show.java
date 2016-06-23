package org.jboss.examples.ticketmonster.model;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import static javax.persistence.CascadeType.ALL;
import static javax.persistence.FetchType.EAGER;
import static javax.persistence.GenerationType.IDENTITY;

/**
 * <p>
 * A show is an instance of an event taking place at a particular venue. A show can have multiple performances.
 * </p>
 * 
 * <p>
 * A show contains a set of performances, and a set of ticket prices for each section of the venue for this show.
 * </p>
 * 
 * <p>
 * The event and venue form the natural id of this entity, and therefore must be unique. JPA requires us to use the class level
 * <code>@Table</code> constraint.
 * </p>
 * 
 * @author Shane Bryzak
 * @author Pete Muir
 */
/*
 * We suppress the warning about not specifying a serialVersionUID, as we are still developing this app, and want the JVM to
 * generate the serialVersionUID for us. When we put this app into production, we'll generate and embed the serialVersionUID
 */
@SuppressWarnings("serial")
@Entity
@Table(name="Appearance", uniqueConstraints = @UniqueConstraint(columnNames = { "event_id", "venue_id" }))
public class Show implements Serializable {

    /* Declaration of fields */

    /**
     * The synthetic id of the object.
     */
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    /**
     * <p>
     * The event of which this show is an instance. The <code>@ManyToOne<code> JPA mapping establishes this relationship.
     * </p>
     * 
     * <p>
     * The <code>@NotNull</code> Bean Validation constraint means that the event must be specified.
     * </p>
     */
    @NotNull
    @Embedded
    private EventId eventId;

    /**
     * <p>
     * The venue where this show takes place. The <code>@ManyToOne<code> JPA mapping establishes this relationship.
     * </p>
     * 
     * <p>
     * The <code>@NotNull</code> Bean Validation constraint means that the venue must be specified.
     * </p>
     */
    @NotNull
    @Embedded
    private VenueId venueId;

    /**
     * <p>
     * The set of performances of this show.
     * </p>
     * 
     * <p>
     * The <code>@OneToMany<code> JPA mapping establishes this relationship. TODO Explain EAGER fetch. 
     * This relationship is bi-directional (a performance knows which show it is part of), and the <code>mappedBy</code>
     * attribute establishes this. We cascade all persistence operations to the set of performances, so, for example if a show
     * is removed, then all of it's performances will also be removed.
     * </p>
     * 
     * <p>
     * Normally a collection is loaded from the database in the order of the rows, but here we want to make sure that
     * performances are ordered by date - we let the RDBMS do the heavy lifting. The
     * <code>@OrderBy<code> annotation instructs JPA to do this.
     * </p>
     */
    @Embedded
    private Set<PerformanceId> performances = new HashSet<PerformanceId>();

    /**
     * <p>
     * The set of ticket prices available for this show.
     * </p>
     * 
     * <p>
     * The <code>@OneToMany<code> JPA mapping establishes this relationship. TODO Explain EAGER fetch. 
     * This relationship is bi-directional (a ticket price category knows which show it is part of), and the <code>mappedBy</code>
     * attribute establishes this. We cascade all persistence operations to the set of performances, so, for example if a show
     * is removed, then all of it's ticket price categories are also removed.
     * </p>
     */
    @OneToMany(mappedBy = "show", cascade = ALL, fetch = EAGER)
    private Set<TicketPrice> ticketPrices = new HashSet<TicketPrice>();

    /* Boilerplate getters and setters */

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public EventId getEventId() {
        return eventId;
    }

    public void setEventId(EventId eventId) {
        this.eventId = eventId;
    }

    public Set<PerformanceId> getPerformances() {
        return performances;
    }

    public void setPerformances(Set<PerformanceId> performances) {
        this.performances = performances;
    }

    public VenueId getVenueId() {
        return venueId;
    }

    public void setVenueId(VenueId venueId) {
        this.venueId = venueId;
    }

    public Set<TicketPrice> getTicketPrices() {
        return ticketPrices;
    }

    public void setTicketPrices(Set<TicketPrice> ticketPrices) {
        this.ticketPrices = ticketPrices;
    }

    /* toString(), equals() and hashCode() for Show, using the natural identity of the object */
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        Show show = (Show) o;

        if (eventId != null ? !eventId.equals(show.eventId) : show.eventId != null)
            return false;
        if (venueId != null ? !venueId.equals(show.venueId) : show.venueId != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = eventId != null ? eventId.hashCode() : 0;
        result = 31 * result + (venueId != null ? venueId.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return eventId + " at " + venueId;
    }
}
