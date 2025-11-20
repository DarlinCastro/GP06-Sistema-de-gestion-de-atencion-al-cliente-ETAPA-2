<%-- 
    Document   : MenuAdmin
    Created on : 12 nov 2025
    Author     : Erick :)
--%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Menú Administrador</title>

        <style>
            * {
                margin: 0;
                padding: 0;
                box-sizing: border-box;
            }

            body {
                font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                min-height: 100vh;
                display: flex;
                flex-direction: column;
            }

            /* Header superior */
            .top-header {
                background: #001f3f;
                padding: 15px 30px;
                display: flex;
                justify-content: flex-end;
                align-items: center;
            }

            .btn-atras {
                background: #4169E1;
                color: white;
                padding: 10px 25px;
                border: none;
                border-radius: 8px;
                text-decoration: none;
                font-weight: 600;
                font-size: 16px;
                cursor: pointer;
                transition: all 0.3s ease;
            }

            .btn-atras:hover {
                background: #315bb5;
                transform: translateY(-2px);
                box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
            }

            /* Contenedor principal */
            .container {
                flex: 1;
                display: flex;
                align-items: center;
                justify-content: center;
                padding: 40px 20px;
            }

            .menu-wrapper {
                background: white;
                border-radius: 20px;
                padding: 50px 80px;
                box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
                max-width: 1000px;
                width: 100%;
            }

            .menu-title {
                font-size: 32px;
                color: #001f3f;
                font-weight: 700;
                margin-bottom: 30px;
                text-align: center;
            }

            /* Contenido en flex */
            .content-flex {
                display: flex;
                align-items: center;
                gap: 80px;
            }

            /* Logo */
            .logo-section {
                flex-shrink: 0;
            }

            .logo-img {
                width: 350px;
                height: auto;
                object-fit: contain;
            }

            /* Botones */
            .menu-buttons {
                flex: 1;
                display: flex;
                flex-direction: column;
                gap: 25px;
                max-width: 320px;
            }

            .menu-btn {
                background: #4169E1;
                color: white;
                padding: 18px 25px;
                border: none;
                border-radius: 12px;
                text-decoration: none;
                font-weight: 600;
                font-size: 18px;
                cursor: pointer;
                transition: all 0.3s ease;
                box-shadow: 0 4px 15px rgba(65, 105, 225, 0.4);
                display: block;
                text-align: center;
            }

            .menu-btn:hover {
                background: #315bb5;
                transform: translateY(-3px);
                box-shadow: 0 6px 20px rgba(65, 105, 225, 0.6);
            }

            /* Mensajes */
            .success {
                color: green;
                font-weight: bold;
                margin-bottom: 10px;
            }

            .error {
                color: red;
                font-weight: bold;
                margin-bottom: 10px;
            }

            .footer {
                background: #001f3f;
                color: white;
                text-align: center;
                padding: 15px;
                font-size: 14px;
            }

            /* Responsive */
            @media (max-width: 768px) {
                .menu-wrapper {
                    padding: 40px 30px;
                }

                .content-flex {
                    flex-direction: column;
                    gap: 30px;
                }

                .logo-img {
                    width: 180px;
                }

                .menu-title {
                    font-size: 24px;
                }

                .menu-btn {
                    font-size: 16px;
                    padding: 15px 30px;
                }
            }
        </style>
    </head>

    <body>

        <% 
            if (session.getAttribute("usuarioActual") == null) {
                response.sendRedirect("login.jsp");
                return;
            }
        %>

        <!-- Header superior -->
        <div class="top-header">
            <a href="Logout" class="btn-atras">Atrás</a>
        </div>

        <!-- Contenedor principal -->
        <div class="container">
            <div class="menu-wrapper">

                <div class="content-flex">

                    <!-- Logo -->
                    <div class="logo-section">
                        <img src="logo/logo_KIA.jpeg" alt="Logo KIA" class="logo-img">
                    </div>

                    <!-- Título, mensajes y botones -->
                    <div class="menu-buttons">

                        <h1 class="menu-title">Menú Administrador</h1>

                        <p class="success">${requestScope.mensajeExito}</p>
                        <p class="error">${requestScope.error}</p>

                        <a href="Asignacion" class="menu-btn">Asignar Solicitudes</a>

                        <a href="GestionarUsuarios" class="menu-btn">Gestionar Usuarios</a>

                        <a href="GenerarReporteController?origen=MenuAdmin.jsp" class="menu-btn">
                            Generar Reportes
                        </a>

                    </div>

                </div>
            </div>
        </div>

        <!-- Footer -->
        <div class="footer">
            © 2025 KIA. Todos los derechos reservados
        </div>

    </body>
</html>
