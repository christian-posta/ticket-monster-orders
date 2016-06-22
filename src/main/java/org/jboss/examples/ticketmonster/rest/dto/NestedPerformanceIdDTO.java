package org.jboss.examples.ticketmonster.rest.dto;

import org.jboss.examples.ticketmonster.model.PerformanceId;

import java.io.Serializable;

public class NestedPerformanceIdDTO implements Serializable
{

   private Long id;
   private String name;

   public NestedPerformanceIdDTO()
   {
   }

   public NestedPerformanceIdDTO(final PerformanceId performanceId)
   {
      if (performanceId != null)
      {
         this.id = performanceId.getId();
         this.name = performanceId.getName();
      }
   }

   public PerformanceId performanceId(){
      return new PerformanceId(id,name);
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
