package org.jboss.examples.ticketmonster.rest.dto;

import org.jboss.examples.ticketmonster.model.*;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

@XmlRootElement
public class ShowDTO implements Serializable
{

   private Long id;
   private NestedEventIdDTO event;
   private Set<NestedPerformanceIdDTO> performances = new HashSet<NestedPerformanceIdDTO>();
   private NestedVenueIdDTO venue;
   private Set<NestedTicketPriceDTO> ticketPrices = new HashSet<NestedTicketPriceDTO>();
   private String displayTitle;

   public ShowDTO()
   {
   }

   public ShowDTO(final Show entity)
   {
      if (entity != null)
      {
         this.id = entity.getId();
         this.event = new NestedEventIdDTO(entity.getEventId());
         Iterator<PerformanceId> iterPerformances = entity.getPerformances()
               .iterator();
         while (iterPerformances.hasNext())
         {
            PerformanceId element = iterPerformances.next();
            this.performances.add(new NestedPerformanceIdDTO(element));
         }
         this.venue = new NestedVenueIdDTO(entity.getVenueId());
         Iterator<TicketPrice> iterTicketPrices = entity.getTicketPrices()
               .iterator();
         while (iterTicketPrices.hasNext())
         {
            TicketPrice element = iterTicketPrices.next();
            this.ticketPrices.add(new NestedTicketPriceDTO(element));
         }
         this.displayTitle = entity.toString();
      }
   }

   public Show fromDTO(Show entity, EntityManager em)
   {
      if (entity == null)
      {
         entity = new Show();
      }
      if (this.event != null)
      {
         entity.setEventId(this.event.eventId());
      }
      Iterator<PerformanceId> iterPerformances = entity.getPerformances()
            .iterator();
      while (iterPerformances.hasNext())
      {
         boolean found = false;
         PerformanceId performance = iterPerformances.next();
         Iterator<NestedPerformanceIdDTO> iterDtoPerformances = this
               .getPerformances().iterator();
         while (iterDtoPerformances.hasNext())
         {
            NestedPerformanceIdDTO dtoPerformance = iterDtoPerformances
                  .next();
            if (dtoPerformance.getId().equals(performance.getId()))
            {
               found = true;
               break;
            }
         }
         if (found == false)
         {
            iterPerformances.remove();
            List<SectionAllocation> sectionAllocations = findSectionAllocationsByPerformance(performance, em);
            for(SectionAllocation sectionAllocation: sectionAllocations)
            {
               em.remove(sectionAllocation);
            }
            List<Booking> bookings = findBookingsByPerformance(performance, em);
            for(Booking booking: bookings)
            {
               em.remove(booking);
            }
            em.remove(performance);
         }
      }
      Iterator<NestedPerformanceIdDTO> iterDtoPerformances = this
            .getPerformances().iterator();
      while (iterDtoPerformances.hasNext())
      {
         boolean found = false;
         NestedPerformanceIdDTO dtoPerformance = iterDtoPerformances.next();
         iterPerformances = entity.getPerformances().iterator();
         while (iterPerformances.hasNext())
         {
            PerformanceId performance = iterPerformances.next();
            if (dtoPerformance.getId().equals(performance.getId()))
            {
               found = true;
               break;
            }
         }
         if (found == false)
         {
            Iterator<PerformanceId> resultIter = em
                  .createQuery("SELECT DISTINCT p FROM Performance p",
                        PerformanceId.class).getResultList().iterator();
            while (resultIter.hasNext())
            {
               PerformanceId result = resultIter.next();
               if (result.getId() == dtoPerformance.getId())
               {
                  entity.getPerformances().add(result);
                  break;
               }
            }
         }
      }
      if (this.venue != null)
      {
         entity.setVenueId(this.venue.venueId());
      }
      Iterator<TicketPrice> iterTicketPrices = entity.getTicketPrices()
            .iterator();
      while (iterTicketPrices.hasNext())
      {
         boolean found = false;
         TicketPrice ticketPrice = iterTicketPrices.next();
         Iterator<NestedTicketPriceDTO> iterDtoTicketPrices = this
               .getTicketPrices().iterator();
         while (iterDtoTicketPrices.hasNext())
         {
            NestedTicketPriceDTO dtoTicketPrice = iterDtoTicketPrices
                  .next();
            if (dtoTicketPrice.getId().equals(ticketPrice.getId()))
            {
               found = true;
               break;
            }
         }
         if (found == false)
         {
            iterTicketPrices.remove();
         }
      }
      Iterator<NestedTicketPriceDTO> iterDtoTicketPrices = this
            .getTicketPrices().iterator();
      while (iterDtoTicketPrices.hasNext())
      {
         boolean found = false;
         NestedTicketPriceDTO dtoTicketPrice = iterDtoTicketPrices.next();
         iterTicketPrices = entity.getTicketPrices().iterator();
         while (iterTicketPrices.hasNext())
         {
            TicketPrice ticketPrice = iterTicketPrices.next();
            if (dtoTicketPrice.getId().equals(ticketPrice.getId()))
            {
               found = true;
               break;
            }
         }
         if (found == false)
         {
            Iterator<TicketPrice> resultIter = em
                  .createQuery("SELECT DISTINCT t FROM TicketPrice t",
                        TicketPrice.class).getResultList().iterator();
            while (resultIter.hasNext())
            {
               TicketPrice result = resultIter.next();
               if (result.getId().equals(dtoTicketPrice.getId()))
               {
                  entity.getTicketPrices().add(result);
                  break;
               }
            }
         }
      }
      entity = em.merge(entity);
      return entity;
   }

   public List<SectionAllocation> findSectionAllocationsByPerformance(PerformanceId performance, EntityManager em)
   {
      CriteriaQuery<SectionAllocation> criteria = em
             .getCriteriaBuilder().createQuery(SectionAllocation.class);
      Root<SectionAllocation> from = criteria.from(SectionAllocation.class);
      CriteriaBuilder builder = em.getCriteriaBuilder();
      Predicate performanceIsSame = builder.equal(from.get("performance"), performance);
      return em.createQuery(
             criteria.select(from).where(performanceIsSame)).getResultList();
   }
   
   public List<Booking> findBookingsByPerformance(PerformanceId performance, EntityManager em)
   {
      CriteriaQuery<Booking> criteria = em.getCriteriaBuilder().createQuery(Booking.class);
      Root<Booking> from = criteria.from(Booking.class);
      CriteriaBuilder builder = em.getCriteriaBuilder();
      Predicate performanceIsSame = builder.equal(from.get("performance"), performance);
      return em.createQuery(
             criteria.select(from).where(performanceIsSame)).getResultList();
   }

   public Long getId()
   {
      return this.id;
   }

   public void setId(final Long id)
   {
      this.id = id;
   }

   public NestedEventIdDTO getEvent()
   {
      return this.event;
   }

   public void setEvent(final NestedEventIdDTO event)
   {
      this.event = event;
   }

   public Set<NestedPerformanceIdDTO> getPerformances()
   {
      return this.performances;
   }

   public void setPerformances(final Set<NestedPerformanceIdDTO> performances)
   {
      this.performances = performances;
   }

   public NestedVenueIdDTO getVenue()
   {
      return this.venue;
   }

   public void setVenue(final NestedVenueIdDTO venue)
   {
      this.venue = venue;
   }

   public Set<NestedTicketPriceDTO> getTicketPrices()
   {
      return this.ticketPrices;
   }

   public void setTicketPrices(final Set<NestedTicketPriceDTO> ticketPrices)
   {
      this.ticketPrices = ticketPrices;
   }

   public String getDisplayTitle()
   {
      return this.displayTitle;
   }
}
