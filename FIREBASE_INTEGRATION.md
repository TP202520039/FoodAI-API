# 🔥 Integración Firebase Authentication con Spring Boot

## ✅ Implementación Completada

Se ha implementado la **Opción 2: Sincronización Híbrida** con Firebase Authentication.

### 📁 Archivos Creados

```
src/main/java/com/tp/foodai/security/
├── config/
│   ├── FirebaseConfig.java          # Inicializa Firebase Admin SDK
│   └── SecurityConfig.java          # Configuración de Spring Security
├── controllers/
│   └── AuthController.java          # Endpoints de autenticación
├── dtos/
│   ├── AuthRequestDto.java          # DTO para recibir token
│   └── UserResponseDto.java         # DTO de respuesta de usuario
├── entities/
│   └── User.java                    # Entidad de usuario con firebaseUid
├── filters/
│   └── FirebaseAuthFilter.java      # Filtro que valida tokens en cada request
├── repositories/
│   └── UserRepository.java          # Repositorio JPA
└── services/
    └── UserService.java             # Lógica de sincronización
```

---

## 🚀 Pasos Finales para Usar

### 1️⃣ Descargar Credenciales de Firebase

1. Ve a [Firebase Console](https://console.firebase.google.com/)
2. Selecciona tu proyecto
3. Ve a **⚙️ Project Settings** > **Service accounts**
4. Click en **Generate new private key**
5. Descarga el archivo JSON

### 2️⃣ Configurar el Archivo de Credenciales

Coloca el archivo descargado en:
```
src/main/resources/firebase-service-account.json
```

**⚠️ IMPORTANTE:** Agrega este archivo al `.gitignore`:
```gitignore
# Firebase credentials
src/main/resources/firebase-service-account.json
```

### 3️⃣ Actualizar Dependencias de Maven

Ejecuta en la terminal:
```bash
mvn clean install -DskipTests
```

O desde VS Code:
- Presiona `Ctrl+Shift+P`
- Busca "Java: Clean Java Language Server Workspace"
- Luego "Java: Reload Projects"

### 4️⃣ Iniciar PostgreSQL

Asegúrate de que tu base de datos esté corriendo:
```bash
# El proyecto está configurado para:
# Host: localhost
# Puerto: 5433
# Base de datos: foodai
# Usuario: postgres
# Contraseña: postgres
```

### 5️⃣ Ejecutar la Aplicación

```bash
mvn spring-boot:run
```

---

## 📡 Endpoints Disponibles

### 🔓 Público (sin autenticación)

#### POST `/api/auth/sync`
**Sincroniza el usuario de Firebase con la BD local**

Request:
```json
{
  "idToken": "eyJhbGciOiJSUzI1NiIsImtpZCI6..."
}
```

Response:
```json
{
  "id": 1,
  "firebaseUid": "abc123xyz",
  "email": "usuario@example.com",
  "displayName": "Juan Pérez",
  "photoUrl": "https://lh3.googleusercontent.com/...",
  "provider": "google.com",
  "isActive": true
}
```

### 🔒 Protegido (requiere token)

#### GET `/api/auth/me`
**Obtiene la información del usuario autenticado**

Headers:
```
Authorization: Bearer eyJhbGciOiJSUzI1NiIsImtpZCI6...
```

Response:
```json
{
  "id": 1,
  "firebaseUid": "abc123xyz",
  "email": "usuario@example.com",
  "displayName": "Juan Pérez",
  "photoUrl": "https://lh3.googleusercontent.com/...",
  "provider": "google.com",
  "isActive": true
}
```

---

## 🔄 Flujo de Autenticación

### Desde Flutter

```dart
// 1. Login con Firebase
UserCredential userCredential = await FirebaseAuth.instance
    .signInWithEmailAndPassword(email: email, password: password);

// 2. Obtener ID Token
String? idToken = await userCredential.user?.getIdToken();

// 3. Sincronizar con backend
final response = await http.post(
  Uri.parse('http://localhost:8080/api/auth/sync'),
  headers: {'Content-Type': 'application/json'},
  body: jsonEncode({'idToken': idToken}),
);

// 4. Guardar token para requests futuros
final user = jsonDecode(response.body);
SharedPreferences prefs = await SharedPreferences.getInstance();
await prefs.setString('firebase_token', idToken!);

// 5. En requests posteriores
final token = prefs.getString('firebase_token');
final ordersResponse = await http.get(
  Uri.parse('http://localhost:8080/api/orders'),
  headers: {'Authorization': 'Bearer $token'},
);
```

---

## 🔗 Relacionar Usuario con Otras Entidades

### Ejemplo: Entidad Order

```java
@Entity
@Table(name = "orders")
public class Order {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // ✅ Relación con User
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    private String productName;
    private Double totalAmount;
    
    // ... getters y setters
}
```

### En tu Controller

```java
@RestController
@RequestMapping("/api/orders")
public class OrderController {
    
    @PostMapping
    public ResponseEntity<Order> createOrder(
        @AuthenticationPrincipal User user,  // ✅ Usuario inyectado automáticamente
        @RequestBody CreateOrderDto dto
    ) {
        Order order = new Order();
        order.setUser(user);  // ✅ Asignar usuario autenticado
        order.setProductName(dto.getProductName());
        // ...
        
        Order saved = orderRepository.save(order);
        return ResponseEntity.ok(saved);
    }
    
    @GetMapping
    public ResponseEntity<List<Order>> getMyOrders(
        @AuthenticationPrincipal User user
    ) {
        List<Order> orders = orderRepository.findByUser(user);
        return ResponseEntity.ok(orders);
    }
}
```

---

## 🛡️ Seguridad

### Rutas Públicas
- `/api/auth/sync` - Sincronización inicial
- `/swagger-ui/**` - Documentación Swagger
- `/v3/api-docs/**` - OpenAPI docs

### Rutas Protegidas
Todas las demás rutas requieren el header:
```
Authorization: Bearer <firebase-id-token>
```

---

## 🧪 Probar con Postman/Insomnia

### 1. Sincronizar Usuario
```
POST http://localhost:8080/api/auth/sync
Content-Type: application/json

{
  "idToken": "TU_TOKEN_DE_FIREBASE"
}
```

### 2. Obtener Usuario Actual
```
GET http://localhost:8080/api/auth/me
Authorization: Bearer TU_TOKEN_DE_FIREBASE
```

---

## ⚙️ Configuración Adicional

### Agregar Roles/Permisos (Opcional)

Si necesitas roles como ADMIN, USER, etc.:

1. Modifica `User.java`:
```java
@ElementCollection(fetch = FetchType.EAGER)
@CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
@Column(name = "role")
private Set<String> roles = new HashSet<>();
```

2. Modifica `FirebaseAuthFilter.java`:
```java
List<GrantedAuthority> authorities = user.getRoles().stream()
    .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
    .collect(Collectors.toList());

UsernamePasswordAuthenticationToken authentication = 
    new UsernamePasswordAuthenticationToken(user, null, authorities);
```

3. En `SecurityConfig.java`:
```java
.requestMatchers("/api/admin/**").hasRole("ADMIN")
.requestMatchers("/api/users/**").hasAnyRole("USER", "ADMIN")
```

---

## 📝 Notas Importantes

1. **Tokens Expiran:** Los ID Tokens de Firebase expiran en 1 hora. Flutter debe renovarlos automáticamente.

2. **Sincronización Automática:** El primer request después del login llamará a `/api/auth/sync`. Los siguientes requests solo validarán el token.

3. **Usuario No Sincronizado:** Si un usuario hace un request sin sincronizar, recibirá `401 Unauthorized`.

4. **Actualización de Datos:** Si el usuario cambia su email/nombre en Firebase, llama nuevamente a `/api/auth/sync`.

---

## 🐛 Troubleshooting

### Error: "Failed to initialize Firebase Admin SDK"
- Verifica que `firebase-service-account.json` esté en `src/main/resources/`
- Verifica que el formato JSON sea válido

### Error: "User not found. Please sync first"
- El usuario no ha llamado a `/api/auth/sync`
- Llama primero al endpoint de sincronización

### Error: "Invalid or expired token"
- El token expiró (1 hora)
- Obtén un nuevo token desde Flutter: `await user.getIdToken(true)`

---

## 📚 Documentación API

Una vez ejecutada la aplicación, visita:
- **Swagger UI:** http://localhost:8080/swagger-ui.html
- **OpenAPI JSON:** http://localhost:8080/v3/api-docs

---

¡Listo! 🎉 Tu API ahora está integrada con Firebase Authentication.
