<%-- 
    Document   : CrearSolicitud
    Created on : 12 nov 2025
    Author     : Erick :)
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@page import="java.time.LocalDate"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Crear Solicitud</title>

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

            /* Header */
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
                font-weight: 600;
                text-decoration: none;
                transition: 0.3s ease;
            }

            .btn-atras:hover {
                background: #315bb5;
                transform: translateY(-2px);
                box-shadow: 0 4px 10px rgba(0,0,0,0.3);
            }

            /* Contenedor principal */
            .container {
                flex: 1;
                display: flex;
                justify-content: center;
                align-items: center;
                padding: 40px 20px;
            }

            .form-wrapper {
                background: white;
                width: 100%;
                max-width: 700px;
                padding: 40px 50px;
                border-radius: 20px;
                box-shadow: 0 20px 60px rgba(0,0,0,0.3);
            }

            h1 {
                text-align: center;
                font-size: 30px;
                color: #001f3f;
                margin-bottom: 30px;
            }

            /* Inputs */
            .form-group {
                margin-bottom: 20px;
            }

            label {
                font-weight: 600;
                margin-bottom: 8px;
                display: block;
                color: #001f3f;
            }

            input[type="text"],
            input[type="date"],
            select,
            textarea {
                width: 100%;
                padding: 12px;
                border: 1px solid #ccc;
                border-radius: 10px;
                font-size: 15px;
                background: #f5f5f5;
            }

            select, textarea {
                background: white;
            }

            textarea {
                resize: vertical;
                min-height: 140px;
            }

            /* Botón principal */
            .btn-submit {
                background: #4169E1;
                color: white;
                padding: 15px 25px;
                font-size: 18px;
                font-weight: 600;
                border: none;
                border-radius: 12px;
                cursor: pointer;
                width: 100%;
                transition: 0.3s ease;
                box-shadow: 0 4px 15px rgba(65,105,225,0.4);
            }

            .btn-submit:hover {
                background: #315bb5;
                transform: translateY(-3px);
                box-shadow: 0 6px 20px rgba(65,105,225,0.6);
            }

            /* Mensaje error */
            .mensaje-error {
                color: #721c24;
                background: #f8d7da;
                border: 1px solid #f5c6cb;
                padding: 12px;
                border-radius: 8px;
                margin-bottom: 20px;
                font-weight: 600;
            }

            /* Footer */
            .footer {
                background: #001f3f;
                padding: 15px;
                text-align: center;
                color: white;
                font-size: 14px;
            }

            /* Responsive */
            @media(max-width: 600px) {
                .form-wrapper {
                    padding: 30px 20px;
                }

                h1 {
                    font-size: 24px;
                }
            }
        </style>
    </head>

    <body>

        <!-- HEADER -->
        <div class="top-header">
            <a href="MenuCliente.jsp" class="btn-atras">Atrás</a>
        </div>

        <!-- CONTENEDOR -->
        <div class="container">
            <div class="form-wrapper">

                <h1>Crear Solicitud</h1>

                <!-- Mensajes -->
                <c:if test="${not empty mensaje or not empty param.mensaje}">
                    <div class="mensaje-error">
                        <c:out value="${mensaje}"/>
                        <c:out value="${param.mensaje}"/>
                    </div>
                </c:if>

                <form action="${pageContext.request.contextPath}/CrearSolicitudServlet" method="POST">

                    <!-- Nº Ticket -->
                    <div class="form-group">
                        <label>Nº Ticket</label>
                        <input type="text" value="Pendiente (Asignación Automática)" readonly>
                    </div>

                    <!-- Fecha -->
                    <div class="form-group">
                        <label>Fecha Creación</label>
                        <input type="text" value="<%= LocalDate.now().toString() %>" readonly>
                    </div>

                    <!-- Tipo Servicio -->
                    <div class="form-group">
                        <label>Tipo de Servicio</label>
                        <select name="tipoServicio" required>
                            <option value="" disabled selected>-- Seleccione Tipo de Servicio --</option>

                            <c:forEach var="entry" items="${listaServiciosMap}">
                                <option value="${entry.key}">
                                    ${entry.value}
                                </option>
                            </c:forEach>

                            <c:if test="${empty listaServiciosMap}">
                                <option disabled>No hay servicios disponibles</option>
                            </c:if>
                        </select>
                    </div>

                    <!-- Descripción -->
                    <div class="form-group">
                        <label>Descripción</label>
                        <textarea name="descripcion" required placeholder="Describa su problema con el mayor detalle posible"></textarea>
                    </div>

                    <!-- Botón -->
                    <button type="submit" class="btn-submit">Crear Solicitud</button>

                </form>
            </div>
        </div>

        <!-- FOOTER -->
        <div class="footer">
            © 2025 KIA. Todos los derechos reservados
        </div>

    </body>
</html>
