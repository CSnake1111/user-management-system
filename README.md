# Gestor de Usuarios - Java + Supabase

## Requisitos
- Java 17+
- Maven 3.8+
- Cuenta en Supabase

## Configuracion

### 1. Base de datos
Ejecuta el SQL del esquema en Supabase Dashboard -> SQL Editor.
Luego actualiza el hash del admin por defecto:

```sql
UPDATE usuarios
SET password_hash = '$2a$12$HASH_GENERADO'
WHERE email = 'admin@sistema.com';
```

Para generar el hash desde Java (ejecutar una vez):
```java
System.out.println(PasswordUtil.hash("Admin1234!"));
```

### 2. Variables de entorno
Edita el archivo `.env`:

```
DB_URL=jdbc:postgresql://aws-0-us-east-1.pooler.supabase.com:6543/postgres
DB_USER=postgres.tu_project_ref
DB_PASSWORD=tu_password_supabase

SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USER=tucorreo@gmail.com
SMTP_PASSWORD=xxxx_xxxx_xxxx_xxxx   <- App Password de Gmail (16 chars)
SMTP_FROM=tucorreo@gmail.com
```

Para Gmail App Password:
1. Activa verificacion en 2 pasos en tu cuenta Google
2. Ve a: Cuenta -> Seguridad -> Contrasenas de aplicaciones
3. Genera una contrasena para "Correo"

### 3. Compilar y ejecutar

```bash
mvn clean package -q
java -jar target/gestor-usuarios-1.0.0-jar-with-dependencies.jar
```

## Credenciales por defecto
- Email:     admin@sistema.com
- Password:  Admin1234!  (cambiar despues del primer login)

## Funcionalidades

### Admin
- Crear usuarios (password manual o autogenerado)
- Listar todos los usuarios
- Buscar por ID
- Editar nombre, apellido, email, rol, estado
- Desactivar usuario (sin eliminar)
- Eliminar usuario permanentemente
- Cambiar su propia contrasena

### Usuario
- Ver su perfil
- Cambiar su contrasena
- Solicitar reset de contrasena por email

## Correos automaticos
- Bienvenida con credenciales al crear cuenta
- Notificacion al cambiar password
- Token de reset por email
- Notificacion al desactivar cuenta

## Seguridad
- BCrypt con costo 12 para hashing
- Tokens reset de 64 chars hex con expiracion 1 hora
- Auditoria completa en tabla `auditoria`
- Registro de sesiones en tabla `sesiones`
- Validacion de fortaleza de password
- 3 intentos maximos de login
