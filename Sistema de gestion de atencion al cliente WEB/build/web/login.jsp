<%-- 
    Document   : login
    Created on : 11 nov 2025, 22:06:55
    Author     : Erick :)
--%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>KIA - Iniciar Sesión</title>

    <style>
   body {
    margin: 0;
    font-family: "Segoe UI", Arial, sans-serif;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    height: 100vh;
    display: flex;
    justify-content: center;
    align-items: center;
   }

   .container {
       width: 900px;
       height: 520px;
       background: white;
       border-radius: 20px;
       display: flex;
       overflow: hidden;
       box-shadow: 0px 10px 35px rgba(0, 0, 0, 0.25);
   }

   /* Panel izquierdo */
   .left-section {
       width: 45%;
       background: #3b00a5;
       padding: 40px;
       color: white;
       display: flex;
       flex-direction: column;
       justify-content: center;
       align-items: center;
       text-align: center;
       gap: 20px;
   }

   .logo img {
       width: 100%;
       max-width: 380px;
       margin-top: -50px;
   }

   .welcome {
       margin-top: -100px;
   }

   .left-section h1 {
       font-size: 32px;
       margin-bottom: 10px;
   }

   .left-section p {
       opacity: 0.85;
       font-size: 15px;
       line-height: 1.4;
   }

   /* Panel derecho (Formulario) */
   .right-section {
       width: 55%;
       padding: 50px;
       display: flex;
       flex-direction: column;
       justify-content: center;
   }

   .right-section h2 {
       font-size: 28px;
       color: #2a2a2a;
       margin-bottom: 25px;
       text-align: center;
   }

   .input-group {
       margin-bottom: 18px;
   }

   .input-group label {
       font-size: 15px;
       font-weight: bold;
   }

   .input-group input {
       width: 100%;
       padding: 12px;
       margin-top: 5px;
       border-radius: 8px;
       border: 1px solid #ccc;
       font-size: 15px;
       outline: none;
       transition: 0.3s;
       box-sizing: border-box;  /* Incluye padding en el ancho */
   }

   .input-group input:focus {
       border-color: #6a5af9;
       box-shadow: 0px 0px 6px rgba(106, 90, 249, 0.5);
   }

   .btn-login {
       width: 100%;
       padding: 13px;
       background: linear-gradient(135deg, #6a5af9, #8758ff);
       border: none;
       border-radius: 10px;
       color: white;
       font-size: 17px;
       font-weight: 600;
       cursor: pointer;
       margin-top: 10px;
       transition: 0.3s;
       box-sizing: border-box;  /* Incluye padding en el ancho */
   }

   .btn-login:hover {
       opacity: 0.85;
   }

   /* Texto Crear Cuenta */
   .create {
       margin-top: 18px;
       font-size: 14px;
       color: #1a1a1a;
       text-align: center;
   }

   .create a {
       color: #6a5af9;
       font-weight: 600;
       text-decoration: none;
   }

   .create a:hover {
       text-decoration: underline;
   }

   /* Mensajes del servidor */
   .error-message {
       margin-top: 10px;
       font-size: 14px;
       font-weight: bold;
       color: red;
       text-align: center;
   }
</style>
</head>
<body>

<div class="container">

    <!-- Sección Izquierda -->
    <div class="left-section">

    <div class="logo">
        <img src="logo/logoSinFondo.png" alt="Logo KIA" class="logo-img">
    </div>

    <div class="welcome">
        <h1>¡Bienvenido a KIA!</h1>
        <p>Inicia sesión para acceder al sistema y gestionar solicitudes de atención al cliente.</p>
    </div>
</div>

    <!-- Sección Derecha / Login -->
    <div class="right-section">
        <h2>Iniciar Sesión</h2>

        <form method="POST" action="Login">

            <div class="input-group">
                <label>Usuario (Identificador):</label>
                <input type="text" name="identificador" required>
            </div>

            <div class="input-group">
                <label>Contraseña:</label>
                <input type="password" name="clave" required>
            </div>

            <% 
                String errorMsg = (String) request.getAttribute("error");
                String mensajeCreado = (String) request.getAttribute("mensaje");

                if (errorMsg != null) {
            %>
                <p class="error-message">[!] <%= errorMsg %></p>
            <%
                } else if (mensajeCreado != null) {
                    String color = mensajeCreado.contains("Error") ? "red" : "green";
            %>
                <p class="error-message" style="color:<%= color %>;">[!] <%= mensajeCreado %></p>
            <% } %>

            <button type="submit" class="btn-login">Iniciar Sesión</button>

            <p class="create">¿No tienes una cuenta? 
                <a href="CrearCuenta.jsp">Crear Cuenta</a>
            </p>

        </form>
    </div>

</div>

</body>
</html>

