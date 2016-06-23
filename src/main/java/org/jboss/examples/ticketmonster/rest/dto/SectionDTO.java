package org.jboss.examples.ticketmonster.rest.dto;

import java.io.Serializable;
import org.jboss.examples.ticketmonster.model.Section;
import javax.persistence.EntityManager;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class SectionDTO implements Serializable
{

   private Long id;
   private String name;
   private String description;
   private int numberOfRows;
   private int rowCapacity;
   private int capacity;
   private NestedVenueIdDTO venue;

   public SectionDTO()
   {
   }

   public SectionDTO(final Section entity)
   {
      if (entity != null)
      {
         this.id = entity.getId();
         this.name = entity.getName();
         this.description = entity.getDescription();
         this.numberOfRows = entity.getNumberOfRows();
         this.rowCapacity = entity.getRowCapacity();
         this.capacity = entity.getCapacity();
         this.venue = new NestedVenueIdDTO(entity.getVenueId());
      }
   }

   public Section fromDTO(Section entity, EntityManager em)
   {
      if (entity == null)
      {
         entity = new Section();
      }
      entity.setName(this.name);
      entity.setDescription(this.description);
      entity.setNumberOfRows(this.numberOfRows);
      entity.setRowCapacity(this.rowCapacity);
      if (this.venue != null)
      {
         entity.setVenueId(this.venue.venueId());
      }
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

   public String getName()
   {
      return this.name;
   }

   public void setName(final String name)
   {
      this.name = name;
   }

   public String getDescription()
   {
      return this.description;
   }

   public void setDescription(final String description)
   {
      this.description = description;
   }

   public int getNumberOfRows()
   {
      return this.numberOfRows;
   }

   public void setNumberOfRows(final int numberOfRows)
   {
      this.numberOfRows = numberOfRows;
   }

   public int getRowCapacity()
   {
      return this.rowCapacity;
   }

   public void setRowCapacity(final int rowCapacity)
   {
      this.rowCapacity = rowCapacity;
   }

   public int getCapacity()
   {
      return this.capacity;
   }

   public void setCapacity(final int capacity)
   {
      this.capacity = capacity;
   }

   public NestedVenueIdDTO getVenue()
   {
      return this.venue;
   }

   public void setVenue(final NestedVenueIdDTO venue)
   {
      this.venue = venue;
   }
}