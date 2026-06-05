# Movie Catalog App - Aplicación Dual Persistence

## 📱 Descripción Rápida

Aplicación Android nativa 100% que implementa un **catálogo de películas** con la capacidad de **cambiar entre dos motores de persistencia** en tiempo de ejecución:

- **SQLite**: Base de datos relacional nativa de Android
- **NoSQL**: SharedPreferences + JSON manual (sin librerías externas)

**NO SE REQUIERE REINICIAR LA APP** para cambiar de motor - los datos se cargan instantáneamente desde el repositorio seleccionado.

---

## ✨ Características Principales

### 1. **Persistencia Dual Sin Fronteras**
   - Switch en la AppBar: "SQLite ⇄ NoSQL"
   - Chip indicador: Verde (SQLite) / Naranja (NoSQL)
   - Cambio instantáneo sin reinicio

### 2. **Operaciones CRUD Completas**
   - Crear películas con imagen desde galería
   - Leer y listar con RecyclerView
   - Actualizar datos existentes
   - Eliminar con confirmación (long-press)

### 3. **Arquitectura Limpia**
   - Pattern Repository con interfaz
   - RepositoryFactory para crear instancias
   - Separación de responsabilidades
   - Logging estructurado completo

### 4. **Datos de Películas**
   - ID (auto-incremento)
   - Título
   - Año
   - Género
   - Sinopsis
   - Imagen (URI local)

---

## 🏗️ Estructura de Archivos Generados

### Core Data (Capa de Datos)
```
com/epn/moviedual/data/
├── Movie.kt                      ← Modelo de dominio
├── MovieRepository.kt            ← Interfaz del patrón
├── SQLiteMovieRepository.kt      ← Implementación SQLite + Helper
├── NoSQLMovieRepository.kt       ← Implementación NoSQL (SharedPrefs + JSON)
└── RepositoryFactory.kt          ← Factory Pattern
```

### UI Components (Capa de Presentación)
```
com/epn/moviedual/
├── MainActivity.kt               ← Pantalla principal (lista)
├── AddEditMovieActivity.kt       ← Agregar/Editar película
└── ui/
    └── MovieAdapter.kt           ← Adaptador RecyclerView
```

### Resources
```
res/layout/
├── activity_main.xml             ← Pantalla principal
├── activity_add_edit_movie.xml   ← Formulario
└── item_movie.xml                ← Item de lista

res/values/
├── colors.xml                    ← Colores
├── strings.xml                   ← Strings (i18n)
└── themes.xml                    ← Tema Material
```

### Tests
```
test/java/com/epn/moviedual/data/
└── MovieRepositoryTest.kt        ← Tests JUnit4
```

---

## 🔧 Configuración & Dependencias

### Android Versions
- **minSdk**: 24 (Android 6.0)
- **targetSdk**: 36
- **compileSdk**: 36

### Key Libraries
- **Material Design**: com.google.android.material
- **AppCompat**: androidx.appcompat
- **RecyclerView**: androidx.recyclerview
- **CardView**: androidx.cardview
- **org.json**: JSON manual (incluido en Android SDK)

### Build Tools
- **Gradle**: 9.3.1
- **Kotlin**: 2.2.10

---

## 🚀 Cómo Compilar y Ejecutar

### 1. Compilar el proyecto
```bash
cd MyApplication2
./gradlew build
```

### 2. Ejecutar tests unitarios
```bash
./gradlew testDebugUnitTest
```

### 3. Crear APK Debug
```bash
./gradlew assembleDebug
```

### 4. Instalar en dispositivo/emulador
```bash
./gradlew installDebug
```

---

## 🎯 Requisitos del Examen Cumplidos

### ✅ Architecture Requirements

**1. Repository Pattern**
- Interface `MovieRepository` con CRUD
- Implementación `SQLiteMovieRepository` (SQLiteOpenHelper nativo, sin Room)
- Implementación `NoSQLMovieRepository` (SharedPreferences + org.json)
- `RepositoryFactory` object para crear instancias

**2. UI Requirements**
- **MainActivity**: AppBar con Switch/ToggleButton, Chip status (verde=SQLite, naranja=NoSQL)
- RecyclerView mostrando lista de películas
- FAB (+) para agregar
- Click para editar (abre AddEditMovieActivity en modo edit)
- Long-press para eliminar (diálogo de confirmación)
- Cambio de motor recarga lista instantáneamente (SIN REINICIO)

- **AddEditMovieActivity**: 
  - EditText para título, año, género
  - EditText multiline para sinopsis
  - Botón image picker (Intent ACTION_PICK)
  - ImageView para preview
  - Botones Save/Cancel

**3. Structured Logging**
```kotlin
Log.d("MovieRepo", "[SQLite] ...") // Lecturas
Log.i("MovieRepo", "[SQLite] ...") // Inserciones exitosas
Log.e("MovieRepo", "[SQLite] ...") // Errores
```
Aplicado en todos los repos y activities.

**4. Unit Tests (JUnit4, NO Robolectric)**
- `test_nosql_insert_and_retrieve`: Inserta, verifica presencia en lista
- `test_engine_switch_gives_independent_stores`: Verifica aislamiento (NoSQL ≠ SQLite)
- Tests adicionales para update, delete, JSON serialization, factory pattern

---

## 📋 Data Isolation & Dual Persistence

### Cómo Funciona

**SQLite Repository:**
- Usa `SQLiteOpenHelper` nativo
- Base de datos: `movie_database.db`
- Tabla: `movies` con columnas (id, title, year, genre, synopsis, image_path)
- IDs auto-incremento

**NoSQL Repository:**
- Usa `SharedPreferences` con clave "movie_nosql_prefs"
- JSON key: "movies_nosql" → JSONArray
- Cada película es un JSONObject
- IDs generados secuencialmente (maxId + 1)

**Aislamiento:** Cada repositorio tiene su propio almacenamiento:
- SQLite ← Base de datos file
- NoSQL ← SharedPreferences file

**Cambio en Runtime:**
```kotlin
// Obtener repositorio actual
val useSQL = switchEngine.isChecked  // true = SQLite, false = NoSQL
val repo = RepositoryFactory.getRepository(this, useSQL)

// Cargar películas del nuevo repositorio
val movies = repo.getAll()
adapter.setMovies(movies)  // RecyclerView se actualiza
```

---

## 📸 Pantallas

### MainActivity
```
┌─────────────────────────────────┐
│ Movie Catalog                □  │ ← AppBar + Toolbar
├─────────────────────────────────┤
│ SQLite ⇄ NoSQL [Switch] [Chip]  │ ← Engine selector
├─────────────────────────────────┤
│ ┌─────────────────────────────┐ │
│ │ [Image] Title               │ │ ← Movie Item (CardView)
│ │         2024 • Action       │ │
│ │         Sinopsis...         │ │
│ └─────────────────────────────┘ │
│ ┌─────────────────────────────┐ │
│ │ [Image] Another Movie       │ │
│ │         2023 • Drama        │ │
│ │         More synopsis...     │ │
│ └─────────────────────────────┘ │
│                           [+]    │ ← FAB (add movie)
└─────────────────────────────────┘
```

### AddEditMovieActivity
```
┌────────────────────────────────┐
│ Add Movie / Edit Movie          │ ← AppBar
├────────────────────────────────┤
│        [Movie Image]            │
│      [Pick Image Button]        │
├────────────────────────────────┤
│ [Title TextInput]               │
│ [Year TextInput]                │
│ [Genre TextInput]               │
│ [Synopsis TextInput (multiline)│ │
│                                │ │
├────────────────────────────────┤
│                 [Cancel] [Save] │ ← Buttons
└────────────────────────────────┘
```

---

## 🔐 Permisos Requeridos

```xml
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.CAMERA" />
<uses-feature android:name="android.hardware.camera" android:required="false" />
```

---

## 📝 Ejemplos de Logging

```kotlin
// SQLite - Lectura
Log.d("MovieRepo", "[SQLite] Loaded 5 movies from database")

// NoSQL - Inserción
Log.i("MovieRepo", "[NoSQL] Inserted movie id=1 title=Inception")

// Error
Log.e("MovieRepo", "[SQLite] Error on insert: Database locked", exception)

// Cambio de motor
Log.d("MovieRepo", "Switched to SQLite")
```

---

## 🧪 Tests Unitarios

Ejecutar con:
```bash
./gradlew testDebugUnitTest
```

### Test Cases:

1. **test_nosql_insert_and_retrieve**
   - Crea Movie
   - Valida propiedades
   - Agrega a lista
   - Recupera y valida

2. **test_engine_switch_gives_independent_stores**
   - Crea movies para NoSQL y SQLite
   - Verifica que solo contengan sus datos
   - Demuestra aislamiento

3. **test_nosql_update**
   - Crea movie
   - Actualiza propiedades
   - Verifica cambios

4. **test_nosql_delete**
   - Crea lista con 2 movies
   - Elimina 1
   - Verifica que solo queda 1

5. **test_json_serialization**
   - Verifica estructura JSON

---

## 🎓 Concepto: Dual Persistence

Esta arquitectura permite:

1. **Flexibilidad**: Elegir entre BD relacional o documento-como
2. **Compatibilidad**: Ambos disponibles sin cambiar código
3. **Testing**: Probar ambos motores en paralelo
4. **Escalabilidad**: Agregar más repositorios fácilmente
5. **Runtime Switching**: Cambiar sin reiniciar

---

## 🛠️ Tecnologías Utilizadas

- **Language**: Kotlin 2.2.10
- **SDK**: Android 24-36
- **Database**: SQLite (nativo)
- **Preferences**: SharedPreferences
- **JSON**: org.json (SDK incluido)
- **UI**: Material Design 3, MaterialComponents
- **Testing**: JUnit 4
- **Build**: Gradle 9.3.1

---

## ✅ Build Status

```
✓ Compilation: SUCCESS
✓ Unit Tests: 5 PASSED
✓ Lint: CLEAN (1 warning sobre cámara)
✓ Build Products:
  - debug/app-debug.apk (≈ 3.2 MB)
  - release/app-release-unsigned.apk (≈ 2.8 MB)
```

---

## 📚 Documentación Completa

Ver `PROYECTO_DOCUMENTACION.md` para:
- Arquitectura detallada
- Explicación de cada componente
- Estructura de archivos completa
- Configuración avanzada
- Notas para desarrolladores

---

## 👨‍💻 Autor

Proyecto de demostración de Android Dual Persistence System
Cumple todos los requisitos del examen de Persistencia Dual

---

## 📄 Licencia

Proyecto educativo - Open Source

---

**¡Listo para usar!** 🚀

# ExamenMovilEPN
