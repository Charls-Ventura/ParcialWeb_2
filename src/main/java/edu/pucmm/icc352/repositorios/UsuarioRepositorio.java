package edu.pucmm.icc352.repositorios;

import edu.pucmm.icc352.config.HibernateUtil;
import edu.pucmm.icc352.modelos.Usuario;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;
import java.util.Optional;

public class UsuarioRepositorio {

    public Usuario guardar(Usuario usuario) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.persist(usuario);
            transaction.commit();
            return usuario;
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            throw new RuntimeException("Error guardando usuario", e);
        }
    }

    public Usuario actualizar(Usuario usuario) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            Usuario usuarioActualizado = (Usuario) session.merge(usuario);
            transaction.commit();
            return usuarioActualizado;
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            throw new RuntimeException("Error actualizando usuario", e);
        }
    }

    public Optional<Usuario> buscarPorId(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return Optional.ofNullable(session.get(Usuario.class, id));
        } catch (Exception e) {
            throw new RuntimeException("Error buscando usuario por id", e);
        }
    }

    public Optional<Usuario> buscarPorCorreo(String correo) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "from Usuario u where lower(u.correo) = :correo";
            Usuario usuario = session.createQuery(hql, Usuario.class)
                    .setParameter("correo", correo.toLowerCase())
                    .uniqueResult();
            return Optional.ofNullable(usuario);
        } catch (Exception e) {
            throw new RuntimeException("Error buscando usuario por correo", e);
        }
    }

    public List<Usuario> listarTodos() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("from Usuario u order by u.id", Usuario.class).list();
        } catch (Exception e) {
            throw new RuntimeException("Error listando usuarios", e);
        }
    }
}