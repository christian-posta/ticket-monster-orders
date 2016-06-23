package org.jboss.examples.ticketmonster.rest;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.OptimisticLockException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;

import org.jboss.examples.ticketmonster.model.TicketPriceGuide;
import org.jboss.examples.ticketmonster.rest.dto.TicketPriceDTO;

/**
 * 
 */
@Stateless
@Path("/ticketprices")
public class TicketPriceEndpoint
{
   @PersistenceContext(unitName = "primary")
   private EntityManager em;

   @POST
   @Consumes("application/json")
   public Response create(TicketPriceDTO dto)
   {
      TicketPriceGuide entity = dto.fromDTO(null, em);
      em.persist(entity);
      return Response.created(UriBuilder.fromResource(TicketPriceEndpoint.class).path(String.valueOf(entity.getId())).build()).build();
   }

   @DELETE
   @Path("/{id:[0-9][0-9]*}")
   public Response deleteById(@PathParam("id") Long id)
   {
      TicketPriceGuide entity = em.find(TicketPriceGuide.class, id);
      if (entity == null)
      {
         return Response.status(Status.NOT_FOUND).build();
      }
      em.remove(entity);
      return Response.noContent().build();
   }

   @GET
   @Path("/{id:[0-9][0-9]*}")
   @Produces("application/json")
   public Response findById(@PathParam("id") Long id)
   {
      TypedQuery<TicketPriceGuide> findByIdQuery = em.createQuery("SELECT DISTINCT t FROM TicketPrice t LEFT JOIN FETCH t.show LEFT JOIN FETCH t.section LEFT JOIN FETCH t.ticketCategory WHERE t.id = :entityId ORDER BY t.id", TicketPriceGuide.class);
      findByIdQuery.setParameter("entityId", id);
      TicketPriceGuide entity;
      try
      {
         entity = findByIdQuery.getSingleResult();
      }
      catch (NoResultException nre)
      {
         entity = null;
      }
      if (entity == null)
      {
         return Response.status(Status.NOT_FOUND).build();
      }
      TicketPriceDTO dto = new TicketPriceDTO(entity);
      return Response.ok(dto).build();
   }

   @GET
   @Produces("application/json")
   public List<TicketPriceDTO> listAll(@QueryParam("start") Integer startPosition, @QueryParam("max") Integer maxResult)
   {
      TypedQuery<TicketPriceGuide> findAllQuery = em.createQuery("SELECT DISTINCT t FROM TicketPrice t LEFT JOIN FETCH t.show LEFT JOIN FETCH t.section LEFT JOIN FETCH t.ticketCategory ORDER BY t.id", TicketPriceGuide.class);
      if (startPosition != null)
      {
         findAllQuery.setFirstResult(startPosition);
      }
      if (maxResult != null)
      {
         findAllQuery.setMaxResults(maxResult);
      }
      final List<TicketPriceGuide> searchResults = findAllQuery.getResultList();
      final List<TicketPriceDTO> results = new ArrayList<TicketPriceDTO>();
      for (TicketPriceGuide searchResult : searchResults)
      {
         TicketPriceDTO dto = new TicketPriceDTO(searchResult);
         results.add(dto);
      }
      return results;
   }

   @PUT
   @Path("/{id:[0-9][0-9]*}")
   @Consumes("application/json")
   public Response update(@PathParam("id") Long id, TicketPriceDTO dto)
   {
      TypedQuery<TicketPriceGuide> findByIdQuery = em.createQuery("SELECT DISTINCT t FROM TicketPrice t LEFT JOIN FETCH t.show LEFT JOIN FETCH t.section LEFT JOIN FETCH t.ticketCategory WHERE t.id = :entityId ORDER BY t.id", TicketPriceGuide.class);
      findByIdQuery.setParameter("entityId", id);
      TicketPriceGuide entity;
      try
      {
         entity = findByIdQuery.getSingleResult();
      }
      catch (NoResultException nre)
      {
         entity = null;
      }
      entity = dto.fromDTO(entity, em);
      try
      {
         entity = em.merge(entity);
      }
      catch (OptimisticLockException e)
      {
         return Response.status(Response.Status.CONFLICT).entity(e.getEntity()).build();
      }
      return Response.noContent().build();
   }
}
