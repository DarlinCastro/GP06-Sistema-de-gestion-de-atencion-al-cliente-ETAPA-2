<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="es">

<head>
    <meta charset="UTF-8">
    <title>KIA - Crear Cuenta</title>

    <style>
    body {
        margin: 0;
        font-family: "Segoe UI", Arial, sans-serif;
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        display: flex;
        flex-direction: column;
        min-height: 100vh;
    }

    /* BARRA SUPERIOR (IGUAL A MENU) */
    .top-header {
        background: #001f3f;
        padding: 15px 30px;
        display: flex;
        justify-content: flex-end;
        align-items: center;
    }

    .back-btn {
        background: #4169E1;
        color: white;
        padding: 10px 25px;
        border: none;
        border-radius: 8px;
        text-decoration: none;
        font-weight: 600;
        font-size: 16px;
        cursor: pointer;
        transition: 0.3s;
    }

    .back-btn:hover {
        background: #315bb5;
        transform: translateY(-2px);
        box-shadow: 0px 4px 12px rgba(0,0,0,0.3);
    }

    /* CONTENEDOR CENTRAL (MISMO ESTILO QUE LOGIN) */
    .container {
        width: 900px;
        background: white;
        border-radius: 20px;
        display: flex;
        overflow: hidden;
        box-shadow: 0px 10px 35px rgba(0,0,0,0.25);
        margin: 40px auto;
    }

    /* Panel Izquierdo */
    .left-section {
        width: 45%;
        background: #3b00a5;
        padding: 40px;
        color: white;

        display: flex;
        flex-direction: column;
        justify-content: center;     /* centra verticalmente */
        align-items: center;         /* centra horizontalmente */
        gap: -40px;                   /* separa la imagen, el título y el texto */
        text-align: center;
    }

    /* Imagen centrada y ajustada */
    .left-section img {
        width: 100%;
        max-width: 380px;
    }
    .logo-img {
        margin-top: -60px;   /* sube la imagen */
    }

    .welcome {
    margin-top: -80px;   /* sube el texto */
    text-align: center;
}

    /* Descripción debajo del título */
    .left-section p {
        opacity: 0.85;
        font-size: 15px;
        line-height: 1.4;
        margin: 0;
    }

    /* Panel derecho */
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

    .form-group {
        margin-bottom: 18px;
    }

    .form-group label {
        font-size: 15px;
        font-weight: bold;
    }

    .form-group input {
        width: 100%;
        padding: 12px;
        margin-top: 5px;
        border-radius: 8px;
        border: 1px solid #ccc;
        font-size: 15px;
        outline: none;
        transition: 0.3s;
    }

    .form-group input:focus {
        border-color: #6a5af9;
        box-shadow: 0 0 6px rgba(106, 90, 249, 0.5);
    }

    button {
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
    }

    button:hover {
        opacity: 0.85;
    }

    /* Footer */
    .footer {
        background: #001f3f;
        color: white;
        text-align: center;
        padding: 15px;
        margin-top: auto;
    }
</style>

</head>

<body>

    <!-- BARRA SUPERIOR -->
    <div class="top-header">
        <a href="login.jsp" class="back-btn">Atrás</a>
    </div>

    <!-- CONTENEDOR PRINCIPAL -->
    <div class="container">

        <!-- IZQUIERDA -->
    <div class="left-section">
        <img src="logo/logoSinFondo.png" alt="Logo KIA" class="logo-img">

    <div class="welcome">
        <h1>Crear Cuenta</h1>
        <p>Regístrate para comenzar a gestionar solicitudes de atención al cliente.</p>
    </div>
    </div>
        

        <!-- DERECHA -->
        <div class="right-section">
            <h2>Formulario de Registro</h2>

            <form action="CrearCuentaServlet" method="POST">

                <div class="form-group">
                    <label>Nombres:</label>
                    <input type="text" name="nombres" required>
                </div>

                <div class="form-group">
                    <label>Apellidos:</label>
                    <input type="text" name="apellidos" required>
                </div>

                <div class="form-group">
                    <label>Correo electrónico:</label>
                    <input type="email" name="correo" required>
                </div>

                <div class="form-group">
                    <label>Identificador:</label>
                    <input type="text" name="identificador" required>
                </div>

                <div class="form-group">
                    <label>Contraseña:</label>
                    <input type="password" name="clave" required>
                </div>

                <button type="submit">Registrar</button>

            </form>

        </div>

    </div>

    <!-- FOOTER -->
    <div class="footer">
        © 2025 KIA. Todos los derechos reservados
    </div>

</body>

</html>
