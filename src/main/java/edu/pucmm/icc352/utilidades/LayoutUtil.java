package edu.pucmm.icc352.utilidades;

public class LayoutUtil {

    public static String layoutAdmin(String titulo, String subtitulo, String contenido) {
        return """
                <!DOCTYPE html>
                <html lang="es">
                <head>
                    <meta charset="UTF-8">
                    <title>%s</title>
                    <style>
                        * {
                            box-sizing: border-box;
                        }

                        body {
                            margin: 0;
                            font-family: Arial, sans-serif;
                            background:
                                radial-gradient(circle at 10%% 10%%, rgba(255,214,77,0.20), transparent 18%%),
                                radial-gradient(circle at 90%% 85%%, rgba(255,214,77,0.16), transparent 20%%),
                                linear-gradient(135deg,#0a3d91,#0d47a1 55%%,#1565c0);
                            min-height: 100vh;
                            padding: 30px 20px 40px;
                        }

                        .contenedor {
                            max-width: 1400px;
                            margin: auto;
                            background: white;
                            border-radius: 24px;
                            box-shadow: 0 20px 40px rgba(0,0,0,0.25);
                            overflow: hidden;
                        }

                        .navbar {
                            display: flex;
                            align-items: center;
                            justify-content: space-between;
                            gap: 20px;
                            background: #0a3d91;
                            color: white;
                            padding: 16px 26px;
                            flex-wrap: wrap;
                        }

                        .nav-izq {
                            display: flex;
                            align-items: center;
                            gap: 16px;
                        }

                        .nav-izq img {
                            height: 52px;
                            width: 52px;
                            object-fit: contain;
                            background: white;
                            border-radius: 50%%;
                            padding: 4px;
                        }

                        .nav-izq h2 {
                            margin: 0;
                            font-size: 20px;
                        }

                        .nav-links {
                            display: flex;
                            gap: 10px;
                            align-items: center;
                            flex-wrap: wrap;
                        }

                        .nav-links a {
                            text-decoration: none;
                            color: white;
                            font-size: 15px;
                            padding: 9px 13px;
                            border-radius: 8px;
                            transition: 0.2s;
                        }

                        .nav-links a:hover {
                            background: rgba(255,255,255,0.18);
                        }

                        .logout {
                            background: #e53935;
                        }

                        .logout:hover {
                            background: #c62828;
                        }

                        .header {
                            padding: 30px 36px 10px 36px;
                        }

                        .header h1 {
                            margin: 0;
                            color: #0a3d91;
                            font-size: 36px;
                        }

                        .header p {
                            margin-top: 10px;
                            font-size: 17px;
                            color: #555;
                            line-height: 1.6;
                            max-width: 900px;
                        }

                        .linea {
                            width: 130px;
                            height: 6px;
                            background: linear-gradient(90deg,#f4c542,#ffd95a);
                            border-radius: 20px;
                            margin-top: 16px;
                        }

                        .contenido {
                            padding: 20px 36px 36px 36px;
                        }

                        .btn {
                            display: inline-block;
                            text-decoration: none;
                            padding: 13px 18px;
                            border-radius: 10px;
                            font-weight: bold;
                            font-size: 15px;
                        }

                        .btn-principal {
                            background: linear-gradient(90deg,#0a3d91,#174ea6);
                            color: white;
                        }

                        .btn-secundario {
                            background: #6c757d;
                            color: white;
                        }

                        .btn-alerta {
                            background: #ef4444;
                            color: white;
                        }

                        .panel {
                            background: linear-gradient(180deg,#ffffff,#fffdf7);
                            border: 1px solid #f3e6b2;
                            border-radius: 20px;
                            overflow: hidden;
                            box-shadow: inset 0 1px 0 rgba(255,255,255,0.75);
                        }

                        .panel-top {
                            height: 6px;
                            background: linear-gradient(90deg,#f4c542,#ffd95a,#f4c542);
                        }

                        .tabla-wrap {
                            overflow-x: auto;
                        }

                        table {
                            width: 100%%;
                            border-collapse: collapse;
                        }

                        thead {
                            background: #f8fafc;
                        }

                        th {
                            text-align: left;
                            padding: 18px 16px;
                            font-size: 16px;
                            color: #374151;
                            border-bottom: 1px solid #e5e7eb;
                        }

                        td {
                            padding: 16px;
                            border-bottom: 1px solid #eef2f7;
                            vertical-align: middle;
                            font-size: 16px;
                        }

                        tbody tr:hover {
                            background: #fafcff;
                        }

                        .badge {
                            display: inline-block;
                            padding: 8px 14px;
                            border-radius: 999px;
                            font-size: 14px;
                            font-weight: bold;
                        }

                        .badge-admin {
                            background: #fee2e2;
                            color: #991b1b;
                        }

                        .badge-organizador {
                            background: #dbeafe;
                            color: #1d4ed8;
                        }

                        .badge-participante {
                            background: #ede9fe;
                            color: #6d28d9;
                        }

                        .badge-activo {
                            background: #dcfce7;
                            color: #166534;
                        }

                        .badge-bloqueado {
                            background: #fff7ed;
                            color: #c2410c;
                        }

                        .badge-publicado {
                            background: #dbeafe;
                            color: #1d4ed8;
                        }

                        .badge-no-publicado {
                            background: #f3f4f6;
                            color: #374151;
                        }

                        .badge-cancelado {
                            background: #fee2e2;
                            color: #991b1b;
                        }
                
                        .badge-programado {
                            background: #dbeafe;
                            color: #1d4ed8;
                        }
                
                        .badge-en-curso {
                            background: #fff7ed;
                            color: #c2410c;
                        }
                
                        .badge-finalizado {
                            background: #e5e7eb;
                            color: #374151;
                        }

                        .btn-eliminar {
                            display: inline-block;
                            text-decoration: none;
                            padding: 10px 14px;
                            border-radius: 8px;
                            background: #ef4444;
                            color: white;
                            font-size: 14px;
                            font-weight: bold;
                        }

                        @media (max-width: 900px) {
                            .header {
                                padding: 24px 20px 10px 20px;
                            }

                            .contenido {
                                padding: 20px;
                            }

                            .header h1 {
                                font-size: 30px;
                            }

                            .header p {
                                font-size: 16px;
                            }
                        }
                    </style>
                </head>
                <body>
                    <div class="contenedor">
                        <div class="navbar">
                            <div class="nav-izq">
                                <img src="/img/logo-pucmm.png" alt="PUCMM">
                                <h2>Eventos Académicos PUCMM</h2>
                            </div>

                            <div class="nav-links">
                                <a href="/admin/dashboard">Dashboard</a>
                                <a href="/admin/usuarios">Usuarios</a>
                                <a href="/admin/eventos">Eventos</a>
                                <a href="/asistencia/registrar">Asistencia</a>
                                <a href="/me">Mi sesión</a>
                                <a class="logout" href="/logout">Cerrar sesión</a>
                            </div>
                        </div>

                        <div class="header">
                            <h1>%s</h1>
                            <p>%s</p>
                            <div class="linea"></div>
                        </div>

                        <div class="contenido">
                            %s
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(titulo, titulo, subtitulo, contenido);
    }

    public static String layoutOrganizador(String titulo, String subtitulo, String contenido) {
        return """
            <!DOCTYPE html>
            <html lang="es">
            <head>
                <meta charset="UTF-8">
                <title>%s</title>
                <style>
                    * {
                        box-sizing: border-box;
                    }

                    body {
                        margin: 0;
                        font-family: Arial, sans-serif;
                        background:
                            radial-gradient(circle at 10%% 10%%, rgba(255,214,77,0.20), transparent 18%%),
                            radial-gradient(circle at 90%% 85%%, rgba(255,214,77,0.16), transparent 20%%),
                            linear-gradient(135deg,#0a3d91,#0d47a1 55%%,#1565c0);
                        min-height: 100vh;
                        padding: 30px 20px 40px;
                    }

                    .contenedor {
                        max-width: 1400px;
                        margin: auto;
                        background: white;
                        border-radius: 24px;
                        box-shadow: 0 20px 40px rgba(0,0,0,0.25);
                        overflow: hidden;
                    }

                    .navbar {
                        display: flex;
                        align-items: center;
                        justify-content: space-between;
                        gap: 20px;
                        background: #0a3d91;
                        color: white;
                        padding: 16px 26px;
                        flex-wrap: wrap;
                    }

                    .nav-izq {
                        display: flex;
                        align-items: center;
                        gap: 16px;
                    }

                    .nav-izq img {
                        height: 52px;
                        width: 52px;
                        object-fit: contain;
                        background: white;
                        border-radius: 50%%;
                        padding: 4px;
                    }

                    .nav-izq h2 {
                        margin: 0;
                        font-size: 20px;
                    }

                    .nav-links {
                        display: flex;
                        gap: 10px;
                        align-items: center;
                        flex-wrap: wrap;
                    }

                    .nav-links a {
                        text-decoration: none;
                        color: white;
                        font-size: 15px;
                        padding: 9px 13px;
                        border-radius: 8px;
                        transition: 0.2s;
                    }

                    .nav-links a:hover {
                        background: rgba(255,255,255,0.18);
                    }

                    .logout {
                        background: #e53935;
                    }

                    .logout:hover {
                        background: #c62828;
                    }

                    .header {
                        padding: 30px 36px 10px 36px;
                    }

                    .header h1 {
                        margin: 0;
                        color: #0a3d91;
                        font-size: 36px;
                    }

                    .header p {
                        margin-top: 10px;
                        font-size: 17px;
                        color: #555;
                        line-height: 1.6;
                        max-width: 900px;
                    }

                    .linea {
                        width: 130px;
                        height: 6px;
                        background: linear-gradient(90deg,#f4c542,#ffd95a);
                        border-radius: 20px;
                        margin-top: 16px;
                    }

                    .contenido {
                        padding: 20px 36px 36px 36px;
                    }

                    .btn {
                        display: inline-block;
                        text-decoration: none;
                        padding: 13px 18px;
                        border-radius: 10px;
                        font-weight: bold;
                        font-size: 15px;
                    }

                    .btn-principal {
                        background: linear-gradient(90deg,#0a3d91,#174ea6);
                        color: white;
                    }

                    .btn-secundario {
                        background: #6c757d;
                        color: white;
                    }

                    .btn-alerta {
                        background: #ef4444;
                        color: white;
                    }

                    .panel {
                        background: linear-gradient(180deg,#ffffff,#fffdf7);
                        border: 1px solid #f3e6b2;
                        border-radius: 20px;
                        overflow: hidden;
                        box-shadow: inset 0 1px 0 rgba(255,255,255,0.75);
                    }

                    .panel-top {
                        height: 6px;
                        background: linear-gradient(90deg,#f4c542,#ffd95a,#f4c542);
                    }

                    .tabla-wrap {
                        overflow-x: auto;
                    }

                    table {
                        width: 100%%;
                        border-collapse: collapse;
                    }

                    thead {
                        background: #f8fafc;
                    }

                    th {
                        text-align: left;
                        padding: 18px 16px;
                        font-size: 16px;
                        color: #374151;
                        border-bottom: 1px solid #e5e7eb;
                    }

                    td {
                        padding: 16px;
                        border-bottom: 1px solid #eef2f7;
                        vertical-align: middle;
                        font-size: 16px;
                    }

                    tbody tr:hover {
                        background: #fafcff;
                    }

                    .badge {
                        display: inline-block;
                        padding: 8px 14px;
                        border-radius: 999px;
                        font-size: 14px;
                        font-weight: bold;
                    }

                    .badge-publicado {
                        background: #dbeafe;
                        color: #1d4ed8;
                    }

                    .badge-borrador {
                        background: #f3f4f6;
                        color: #374151;
                    }

                    .badge-cancelado {
                        background: #fee2e2;
                        color: #991b1b;
                    }
            
                    .badge-programado {
                        background: #dbeafe;
                        color: #1d4ed8;
                    }
            
                    .badge-en-curso {
                        background: #fff7ed;
                        color: #c2410c;
                    }
            
                    .badge-finalizado {
                        background: #e5e7eb;
                        color: #374151;
                    }

                    .badge-activo {
                        background: #dcfce7;
                        color: #166534;
                    }

                    @media (max-width: 900px) {
                        .header {
                            padding: 24px 20px 10px 20px;
                        }

                        .contenido {
                            padding: 20px;
                        }

                        .header h1 {
                            font-size: 30px;
                        }

                        .header p {
                            font-size: 16px;
                        }
                    }
                </style>
            </head>
            <body>
                <div class="contenedor">
                    <div class="navbar">
                        <div class="nav-izq">
                            <img src="/img/logo-pucmm.png" alt="PUCMM">
                            <h2>Eventos Académicos PUCMM</h2>
                        </div>

                        <div class="nav-links">
                            <a href="/organizador/dashboard">Dashboard</a>
                            <a href="/organizador/eventos">Mis eventos</a>
                            <a href="/organizador/eventos/nuevo">Crear evento</a>
                            <a href="/asistencia/registrar">Asistencia</a>
                            <a href="/me">Mi sesión</a>
                            <a class="logout" href="/logout">Cerrar sesión</a>
                        </div>
                    </div>

                    <div class="header">
                        <h1>%s</h1>
                        <p>%s</p>
                        <div class="linea"></div>
                    </div>

                    <div class="contenido">
                        %s
                    </div>
                </div>
            </body>
            </html>
            """.formatted(titulo, titulo, subtitulo, contenido);
    }

    public static String layoutParticipante(String titulo, String subtitulo, String contenido) {
        return """
            <!DOCTYPE html>
            <html lang="es">
            <head>
                <meta charset="UTF-8">
                <title>%s</title>
                <style>
                    * {
                        box-sizing: border-box;
                    }

                    body {
                        margin: 0;
                        font-family: Arial, sans-serif;
                        background:
                            radial-gradient(circle at 10%% 10%%, rgba(255,214,77,0.20), transparent 18%%),
                            radial-gradient(circle at 90%% 85%%, rgba(255,214,77,0.16), transparent 20%%),
                            linear-gradient(135deg,#0a3d91,#0d47a1 55%%,#1565c0);
                        min-height: 100vh;
                        padding: 30px 20px 40px;
                    }

                    .contenedor {
                        max-width: 1400px;
                        margin: auto;
                        background: white;
                        border-radius: 24px;
                        box-shadow: 0 20px 40px rgba(0,0,0,0.25);
                        overflow: hidden;
                    }

                    .navbar {
                        display: flex;
                        align-items: center;
                        justify-content: space-between;
                        gap: 20px;
                        background: #0a3d91;
                        color: white;
                        padding: 16px 26px;
                        flex-wrap: wrap;
                    }

                    .nav-izq {
                        display: flex;
                        align-items: center;
                        gap: 16px;
                    }

                    .nav-izq img {
                        height: 52px;
                        width: 52px;
                        object-fit: contain;
                        background: white;
                        border-radius: 50%%;
                        padding: 4px;
                    }

                    .nav-izq h2 {
                        margin: 0;
                        font-size: 20px;
                    }

                    .nav-links {
                        display: flex;
                        gap: 10px;
                        align-items: center;
                        flex-wrap: wrap;
                    }

                    .nav-links a {
                        text-decoration: none;
                        color: white;
                        font-size: 15px;
                        padding: 9px 13px;
                        border-radius: 8px;
                        transition: 0.2s;
                    }

                    .nav-links a:hover {
                        background: rgba(255,255,255,0.18);
                    }

                    .logout {
                        background: #e53935;
                    }

                    .logout:hover {
                        background: #c62828;
                    }

                    .header {
                        padding: 30px 36px 10px 36px;
                    }

                    .header h1 {
                        margin: 0;
                        color: #0a3d91;
                        font-size: 36px;
                    }

                    .header p {
                        margin-top: 10px;
                        font-size: 17px;
                        color: #555;
                        line-height: 1.6;
                        max-width: 900px;
                    }

                    .linea {
                        width: 130px;
                        height: 6px;
                        background: linear-gradient(90deg,#f4c542,#ffd95a);
                        border-radius: 20px;
                        margin-top: 16px;
                    }

                    .contenido {
                        padding: 20px 36px 36px 36px;
                    }

                    .btn {
                        display: inline-block;
                        text-decoration: none;
                        padding: 13px 18px;
                        border-radius: 10px;
                        font-weight: bold;
                        font-size: 15px;
                    }

                    .btn-principal {
                        background: linear-gradient(90deg,#0a3d91,#174ea6);
                        color: white;
                    }

                    .btn-secundario {
                        background: #6c757d;
                        color: white;
                    }

                    .btn-alerta {
                        background: #ef4444;
                        color: white;
                    }

                    .panel {
                        background: linear-gradient(180deg,#ffffff,#fffdf7);
                        border: 1px solid #f3e6b2;
                        border-radius: 20px;
                        overflow: hidden;
                        box-shadow: inset 0 1px 0 rgba(255,255,255,0.75);
                    }

                    .panel-top {
                        height: 6px;
                        background: linear-gradient(90deg,#f4c542,#ffd95a,#f4c542);
                    }

                    .tabla-wrap {
                        overflow-x: auto;
                    }

                    table {
                        width: 100%%;
                        border-collapse: collapse;
                    }

                    thead {
                        background: #f8fafc;
                    }

                    th {
                        text-align: left;
                        padding: 18px 16px;
                        font-size: 16px;
                        color: #374151;
                        border-bottom: 1px solid #e5e7eb;
                    }

                    td {
                        padding: 16px;
                        border-bottom: 1px solid #eef2f7;
                        vertical-align: middle;
                        font-size: 16px;
                    }

                    tbody tr:hover {
                        background: #fafcff;
                    }

                    .badge {
                        display: inline-block;
                        padding: 8px 14px;
                        border-radius: 999px;
                        font-size: 14px;
                        font-weight: bold;
                    }

                    .badge-activo {
                        background: #dcfce7;
                        color: #166534;
                    }

                    .badge-cancelado {
                        background: #fee2e2;
                        color: #991b1b;
                    }

                    .badge-finalizado {
                        background: #e5e7eb;
                        color: #374151;
                    }

                    .badge-programado {
                        background: #dbeafe;
                        color: #1d4ed8;
                    }

                    .badge-en-curso {
                        background: #fff7ed;
                        color: #c2410c;
                    }

                    @media (max-width: 900px) {
                        .header {
                            padding: 24px 20px 10px 20px;
                        }

                        .contenido {
                            padding: 20px;
                        }

                        .header h1 {
                            font-size: 30px;
                        }

                        .header p {
                            font-size: 16px;
                        }
                    }
                </style>
            </head>
            <body>
                <div class="contenedor">
                    <div class="navbar">
                        <div class="nav-izq">
                            <img src="/img/logo-pucmm.png" alt="PUCMM">
                            <h2>Eventos Académicos PUCMM</h2>
                        </div>

                        <div class="nav-links">
                            <a href="/participante/dashboard">Dashboard</a>
                            <a href="/participante/eventos">Eventos</a>
                            <a href="/participante/mis-inscripciones">Mis inscripciones</a>
                            <a href="/me">Mi sesión</a>
                            <a class="logout" href="/logout">Cerrar sesión</a>
                        </div>
                    </div>

                    <div class="header">
                        <h1>%s</h1>
                        <p>%s</p>
                        <div class="linea"></div>
                    </div>

                    <div class="contenido">
                        %s
                    </div>
                </div>
            </body>
            </html>
            """.formatted(titulo, titulo, subtitulo, contenido);
    }

}