package org.jboss.examples.ticketmonster.rest.dto;

import org.jboss.examples.ticketmonster.model.VenueId;

import java.io.Serializable;

public class NestedVenueIdDTO implements Serializable
{

   private Long id;
   private String name;

   public NestedVenueIdDTO()
   {
   }

   public NestedVenueIdDTO(final VenueId venueId)
   {
      if (venueId != null)
      {
         this.id = venueId.getId();
         this.name = venueId.getName();
      }
   }

   public VenueId venueId() {
      return new VenueId(id, name);
   }

   public Long getId() {
      return id;
   }

   public void setId(Long id) {
      this.id = id;
   }

   public String getName() {
      return name;
   }

   public void setName(String name) {
      this.name = name;
   }
}