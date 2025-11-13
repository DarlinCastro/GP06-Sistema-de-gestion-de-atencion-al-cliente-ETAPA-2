<%-- 
    Document   : CrearCuenta.j´p
    Created on : 12 nov 2025, 12:11:12
    Author     : RYZEN
--%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>KIA - Crear Cuenta</title>
        <style>
            body {
                font-family: Arial, sans-serif;
                margin: 0;
                display: flex;
                flex-direction: column;
                min-height: 100vh;
                background-color: #f0f2f5; /* Color de fondo general */
            }

            /* Encabezado - Parte superior morada */
            .header {
                background-color: #6a0dad; /* Morado oscuro */
                color: white;
                padding: 15px 20px;
                display: flex;
                justify-content: space-between;
                align-items: center;
                box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
            }
            .header h1 {
                margin: 0;
                font-size: 24px;
            }
            .header .back-button {
                background-color: #8a2be2; /* Morado más claro */
                color: white;
                padding: 10px 20px;
                border: none;
                border-radius: 5px;
                cursor: pointer;
                font-size: 16px;
                text-decoration: none; /* Para el enlace */
            }
            .header .back-button:hover {
                background-color: #9370db; /* Morado al pasar el ratón */
            }

            /* Contenido principal - Caja blanca */
            .main-content {
                flex-grow: 1; /* Hace que el contenido ocupe el espacio restante */
                display: flex;
                justify-content: center;
                align-items: center;
                padding: 20px;
            }
            .create-account-box {
                background-color: #fff;
                padding: 40px 50px;
                border-radius: 8px;
                box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
                width: 400px;
                text-align: center;
            }
            .create-account-box h2 {
                color: #333;
                margin-top: 0;
                margin-bottom: 30px;
                font-size: 28px;
            }
            .form-group {
                display: flex;
                justify-content: space-between; /* Alinea label y input */
                align-items: center;
                margin-bottom: 20px;
            }
            .form-group label {
                width: 150px; /* Ancho fijo para las etiquetas */
                text-align: right;
                margin-right: 15px;
                color: #555;
                font-size: 16px;
            }
            .form-group input[type="text"],
            .form-group input[type="email"],
            .form-group input[type="password"] {
                flex-grow: 1; /* Ocupa el espacio restante */
                padding: 12px;
                border: 1px solid #ccc;
                border-radius: 5px;
                font-size: 16px;
            }
            .create-account-box button[type="submit"] {
                background-color: #8a2be2; /* Botón Registrar */
                color: white;
                padding: 12px 30px;
                border: none;
                border-radius: 5px;
                cursor: pointer;
                font-size: 18px;
                margin-top: 20px;
                transition: background-color 0.3s ease;
            }
            .create-account-box button[type="submit"]:hover {
                background-color: #9370db;
            }

            /* Pie de página */
            .footer {
                background-color: #1a237e; /* Azul oscuro para el footer */
                color: white;
                text-align: center;
                padding: 15px 0;
                font-size: 14px;
            }
        </style>
    </head>
    <body>

        <div class="header">
            <h1>KIA</h1> <a href="login.jsp" class="back-button">Atrás</a>
        </div>

        <div class="main-content">
            <div class="create-account-box">
                <h2>CREAR CUENTA</h2>

                <form action="CrearCuentaServlet" method="POST"> 

                    <div class="form-group">
                        <label for="nombres">Nombres:</label>
                        <input type="text" id="nombres" name="nombres" required>
                    </div>

                    <div class="form-group">
                        <label for="apellidos">Apellidos:</label>
                        <input type="text" id="apellidos" name="apellidos" required>
                    </div>

                    <div class="form-group">
                        <label for="correo">Correo electronico:</label>
                        <input type="email" id="correo" name="correo" required>
                    </div>

                    <div class="form-group">
                        <label for="identificador">Identificador:</label>
                        <input type="text" id="identificador" name="identificador" required>
                    </div>

                    <div class="form-group">
                        <label for="clave">Contraseña:</label>
                        <input type="password" id="clave" name="clave" required>
                    </div>

                    <button type="submit">Registrar</button>
                </form>
            </div>
        </div>

        <div class="footer">
            &copy; 2025 KIA. Todos los derechos reservados
        </div>

    </body>
</html>
