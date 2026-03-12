package edu.pucmm.icc352.repositorios;

import edu.pucmm.icc352.config.HibernateUtil;
import edu.pucmm.icc352.modelos.Asistencia;
import edu.pucmm.icc352.modelos.Evento;
import edu.pucmm.icc352.modelos.Usuario;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;
import java.util.Optional;

public class AsistenciaRepositorio {

    public Asistencia guardar(Asistencia asistencia) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.persist(asistencia);
            transaction.commit();
            return asistencia;
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            throw new RuntimeException("Error guardando asistencia", e);
        }
    }

    public Optional<Asistencia> buscarPorUsuarioYEvento(Usuario usuario, Evento evento) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "from Asistencia a where a.usuario = :usuario and a.evento = :evento";
            Asistencia asistencia = session.createQuery(hql, Asistencia.class)
                    .setParameter("usuario", usuario)
                    .setParameter("evento", evento)
                    .uniqueResult();
            return Optional.ofNullable(asistencia);
        } catch (Exception e) {
            throw new RuntimeException("Error buscando asistencia por usuario y evento", e);
        }
    }

    public List<Asistencia> listarPorEvento(Evento evento) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "from Asistencia a where a.evento = :evento order by a.fechaRegistro";
            return session.createQuery(hql, Asistencia.class)
                    .setParameter("evento", evento)
                    .list();
        } catch (Exception e) {
            throw new RuntimeException("Error listando asistencias por evento", e);
        }
    }

    public long contarPorEvento(Evento evento) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "select count(a) from Asistencia a where a.evento = :evento";
            Long total = session.createQuery(hql, Long.class)
                    .setParameter("evento", evento)
                    .uniqueResult();
            return total != null ? total : 0;
        } catch (Exception e) {
            throw new RuntimeException("Error contando asistencias por evento", e);
        }
    }
}