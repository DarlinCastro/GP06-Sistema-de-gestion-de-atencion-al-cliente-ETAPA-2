<%-- 
    Document   : login
    Created on : 11 nov 2025, 22:06:55
    Author     : DELL
--%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>KIA - Iniciar Sesión</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            background-color: #f4f4f9;
            display: flex;
            justify-content: center;
            align-items: center;
            flex-direction: column;
            height: 100vh;
            margin: 0;
        }
        .login-box {
            background-color: #fff;
            padding: 30px;
            border-radius: 8px;
            box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
            width: 300px;
            text-align: center;
        }
        input[type="text"], input[type="password"] {
            width: 100%;
            padding: 10px;
            margin: 8px 0;
            display: inline-block;
            border: 1px solid #ccc;
            border-radius: 4px;
            box-sizing: border-box;
        }
        button[type="submit"] {
            background-color: #007bff;
            color: white;
            padding: 10px 15px;
            margin: 10px 0;
            border: none;
            border-radius: 4px;
            cursor: pointer;
            width: 100%;
            font-size: 16px;
        }
        button[type="submit"]:hover {
            background-color: #0056b3;
        }
        a {
            color: #007bff;
            text-decoration: none;
            display: block;
            margin-top: 10px;
        }
        .error-message {
            color: red;
            font-weight: bold;
            margin-top: 15px;
        }
    </style>
</head>
<body>

    <div class="login-box">
        <h2>INICIAR SESIÓN (KIA)</h2>
        
        <form method="POST" action="Login">
            
            <p>Usuario (Identificador):</p>
            <input type="text" name="identificador" required>
            
            <p>Contraseña:</p>
            <input type="password" name="clave" required>
            
            <% 
                // El Servlet coloca el mensaje de error en el atributo "error" del objeto request
                String errorMsg = (String) request.getAttribute("error");
                String mensajeCreado = (String) request.getAttribute("mensaje");
                
                if (errorMsg != null) {
            %>
                <p class="error-message">[!] <%= errorMsg %></p>
            <%
                } else if (mensajeCreado != null) {
                    String color = mensajeCreado.contains("Error") ? "red" : "green";
            %>
                <p style="color: <%= color %>; font-weight: bold;">[!] <%= mensajeCreado %></p>
            <%
                }
            %>
            
            <button type="submit">Iniciar Sesion</button>
            <a href="crearCuenta.jsp">Crear cuenta</a>
        </form>
    </div>

</body>
</html>