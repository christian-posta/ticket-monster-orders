package org.jboss.examples.ticketmonster.rest.dto;

import org.jboss.examples.ticketmonster.model.EventId;

import java.io.Serializable;

public class NestedEventIdDTO implements Serializable
{

   private Long id;
   private String name;

   public NestedEventIdDTO()
   {
   }

   public NestedEventIdDTO(final EventId eventId)
   {
      if (eventId != null)
      {
         this.id = eventId.getId();
         this.name = eventId.getName();
      }
   }

   public EventId eventId() {
      return new EventId(id, name);
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

}