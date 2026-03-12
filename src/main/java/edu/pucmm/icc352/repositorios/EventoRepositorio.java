package edu.pucmm.icc352.repositorios;

import edu.pucmm.icc352.config.HibernateUtil;
import edu.pucmm.icc352.modelos.Evento;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;
import java.util.Optional;

public class EventoRepositorio {

    public Evento guardar(Evento evento) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.persist(evento);
            transaction.commit();
            return evento;
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            throw new RuntimeException("Error guardando evento", e);
        }
    }

    public Evento actualizar(Evento evento) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            Evento eventoActualizado = (Evento) session.merge(evento);
            transaction.commit();
            return eventoActualizado;
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            throw new RuntimeException("Error actualizando evento", e);
        }
    }

    public Optional<Evento> buscarPorId(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return Optional.ofNullable(session.get(Evento.class, id));
        } catch (Exception e) {
            throw new RuntimeException("Error buscando evento por id", e);
        }
    }

    public List<Evento> listarTodos() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("from Evento e order by e.fecha, e.hora", Evento.class).list();
        } catch (Exception e) {
            throw new RuntimeException("Error listando eventos", e);
        }
    }

    public void eliminar(Evento evento) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            Evento eventoAdjunto = session.merge(evento);
            session.remove(eventoAdjunto);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            throw new RuntimeException("Error eliminando evento", e);
        }
    }
}