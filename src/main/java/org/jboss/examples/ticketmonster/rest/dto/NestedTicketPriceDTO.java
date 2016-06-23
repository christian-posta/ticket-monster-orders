package org.jboss.examples.ticketmonster.rest.dto;

import java.io.Serializable;
import org.jboss.examples.ticketmonster.model.TicketPriceGuide;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

public class NestedTicketPriceDTO implements Serializable
{

   private Long id;
   private float price;
   private String displayTitle;

   public NestedTicketPriceDTO()
   {
   }

   public NestedTicketPriceDTO(final TicketPriceGuide entity)
   {
      if (entity != null)
      {
         this.id = entity.getId();
         this.price = entity.getPrice();
         this.displayTitle = entity.toString();
      }
   }

   public TicketPriceGuide fromDTO(TicketPriceGuide entity, EntityManager em)
   {
      if (entity == null)
      {
         entity = new TicketPriceGuide();
      }
      if (this.id != null)
      {
         TypedQuery<TicketPriceGuide> findByIdQuery = em
               .createQuery(
                     "SELECT DISTINCT t FROM TicketPrice t WHERE t.id = :entityId",
                     TicketPriceGuide.class);
         findByIdQuery.setParameter("entityId", this.id);
         try
         {
            entity = findByIdQuery.getSingleResult();
         }
         catch (javax.persistence.NoResultException nre)
         {
            entity = null;
         }
         return entity;
      }
      entity.setPrice(this.price);
      entity = em.merge(entity);
      return entity;
   }

   public Long getId()
   {
      return this.id;
   }

   public void setId(final Long id)
   {
      this.id = id;
   }

   public float getPrice()
   {
      return this.price;
   }

   public void setPrice(final float price)
   {
      this.price = price;
   }

   public String getDisplayTitle()
   {
      return this.displayTitle;
   }
}
