package edu.pucmm.icc352.repositorios;

import edu.pucmm.icc352.config.HibernateUtil;
import edu.pucmm.icc352.modelos.Evento;
import edu.pucmm.icc352.modelos.Inscripcion;
import edu.pucmm.icc352.modelos.Usuario;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;
import java.util.Optional;

public class InscripcionRepositorio {

    public Inscripcion guardar(Inscripcion inscripcion) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.persist(inscripcion);
            transaction.commit();
            return inscripcion;
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            throw new RuntimeException("Error guardando inscripcion", e);
        }
    }

    public Inscripcion actualizar(Inscripcion inscripcion) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            Inscripcion inscripcionActualizada = (Inscripcion) session.merge(inscripcion);
            transaction.commit();
            return inscripcionActualizada;
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            throw new RuntimeException("Error actualizando inscripcion", e);
        }
    }

    public Optional<Inscripcion> buscarPorUsuarioYEvento(Usuario usuario, Evento evento) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "from Inscripcion i where i.usuario = :usuario and i.evento = :evento";
            Inscripcion inscripcion = session.createQuery(hql, Inscripcion.class)
                    .setParameter("usuario", usuario)
                    .setParameter("evento", evento)
                    .uniqueResult();
            return Optional.ofNullable(inscripcion);
        } catch (Exception e) {
            throw new RuntimeException("Error buscando inscripcion por usuario y evento", e);
        }
    }

    public Optional<Inscripcion> buscarPorTokenQr(String tokenQr) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "from Inscripcion i where i.tokenQr = :tokenQr";
            Inscripcion inscripcion = session.createQuery(hql, Inscripcion.class)
                    .setParameter("tokenQr", tokenQr)
                    .uniqueResult();
            return Optional.ofNullable(inscripcion);
        } catch (Exception e) {
            throw new RuntimeException("Error buscando inscripcion por token QR", e);
        }
    }

    public List<Inscripcion> listarPorEvento(Evento evento) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "from Inscripcion i where i.evento = :evento order by i.id";
            return session.createQuery(hql, Inscripcion.class)
                    .setParameter("evento", evento)
                    .list();
        } catch (Exception e) {
            throw new RuntimeException("Error listando inscripciones por evento", e);
        }
    }

    public List<Inscripcion> listarPorUsuario(Usuario usuario) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "from Inscripcion i where i.usuario = :usuario order by i.id";
            return session.createQuery(hql, Inscripcion.class)
                    .setParameter("usuario", usuario)
                    .list();
        } catch (Exception e) {
            throw new RuntimeException("Error listando inscripciones por usuario", e);
        }
    }

    public long contarInscripcionesActivasPorEvento(Evento evento) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "select count(i) from Inscripcion i where i.evento = :evento and i.cancelada = false";
            Long total = session.createQuery(hql, Long.class)
                    .setParameter("evento", evento)
                    .uniqueResult();
            return total != null ? total : 0;
        } catch (Exception e) {
            throw new RuntimeException("Error contando inscripciones activas", e);
        }
    }

    public Optional<Inscripcion> buscarPorId(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Inscripcion inscripcion = session.get(Inscripcion.class, id);
            return Optional.ofNullable(inscripcion);
        } catch (Exception e) {
            throw new RuntimeException("Error buscando inscripcion por id", e);
        }
    }


}