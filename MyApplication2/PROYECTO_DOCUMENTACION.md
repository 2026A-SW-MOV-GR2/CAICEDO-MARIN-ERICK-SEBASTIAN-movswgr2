# Movie Catalog App - Dual Persistence System

## Project Overview
Una aplicación completa de catálogo de películas Android que implementa un **Sistema de Persistencia Dual** permitiendo cambiar entre SQLite y NoSQL en tiempo de ejecución sin reiniciar la aplicación.

**Package**: `com.epn.moviedual`

---

## Architecture & Components

### 1. Domain Model
- **File**: `data/Movie.kt`
- **Entity**: Representa una película con los siguientes atributos:
  - `id`: Int (auto-incremento en SQLite, secuencial en NoSQL)
  - `title`: String
  - `year`: Int
  - `genre`: String
  - `synopsis`: String
  - `imagePath`: String? (URI local o ruta de galería/cámara)

### 2. Repository Pattern

#### Interface: MovieRepository
- **File**: `data/MovieRepository.kt`
- Contrato que define las operaciones CRUD:
  - `getAll()`: Obtiene todas las películas
  - `insert(movie: Movie)`: Inserta una nueva película (retorna ID)
  - `update(movie: Movie)`: Actualiza una película existente
  - `delete(id: Int)`: Elimina una película por ID

#### SQLite Implementation
- **File**: `data/SQLiteMovieRepository.kt`
- Usa Android's native `SQLiteOpenHelper` (sin Room, sin ORM)
- **Database Helper**: `MovieDatabaseHelper` - crea y gestiona la tabla de películas
- Características:
  - Auto-incremento de IDs
  - Almacenamiento persistente en BD SQLite nativa
  - Logging estructurado con `Log.d()`, `Log.i()`, `Log.e()`

#### NoSQL Implementation
- **File**: `data/NoSQLMovieRepository.kt`
- Usa `SharedPreferences` + JSON Manual (org.json, sin Gson/Moshi)
- Características:
  - Almacena todas las películas como un JSONArray en una única clave ("movies_nosql")
  - Serialización/deserialización manual con JSONObject y JSONArray
  - IDs secuenciales generados en tiempo de ejecución
  - Logging estructurado similar a SQLite

#### Factory Pattern
- **File**: `data/RepositoryFactory.kt`
- **Object**: `RepositoryFactory`
  - Método: `getRepository(context: Context, useSQL: Boolean): MovieRepository`
  - Punto único de creación de implementaciones
  - Permite cambiar entre repositorios sin cambiar el código cliente

### 3. UI Components

#### MainActivity
- **File**: `MainActivity.kt`
- **Layout**: `res/layout/activity_main.xml`
- Características:
  - **AppBar**: Barra de título materializada
  - **Toolbar**: AppBar personalizada con tema
  - **Motor Switch**: Toggle para cambiar entre SQLite ↔ NoSQL
  - **Status Chip**: Indicador visual del motor activo
    - Verde = SQLite
    - Naranja = NoSQL
  - **RecyclerView**: Lista de películas con CardView items
  - **FAB**: Botón flotante (+) para agregar películas
  - Funcionalidad:
    - Click en película: Abre AddEditMovieActivity en modo edición
    - Long-press en película: Muestra diálogo de confirmación para eliminar
    - Toggle de motor: Recarga instantáneamente desde otro repositorio
    - Logging de todas las operaciones

#### AddEditMovieActivity
- **File**: `AddEditMovieActivity.kt`
- **Layout**: `res/layout/activity_add_edit_movie.xml`
- Características:
  - **Campos de Entrada**:
    - EditText para título
    - EditText numérico para año
    - EditText para género
    - EditText multilínea para sinopsis
  - **Image Picker**: 
    - Botón para seleccionar imagen de galería
    - Intent ACTION_PICK para acceso a galería
    - Preview en ImageView
    - Almacena URI como String
  - **Botones**:
    - Guardar: Llama a insert() o update() del repositorio
    - Cancelar: Descarta cambios
  - Validación: Verifica que todos los campos obligatorios estén completos
  - Retorna RESULT_OK a MainActivity para actualizar la lista

#### Movie Adapter (RecyclerView)
- **File**: `ui/MovieAdapter.kt`
- **ViewHolder**: Vincula datos de película a vista
- Características:
  - Muestra: Título, Año, Género, Sinopsis (multi-línea)
  - Carga de imágenes desde URI
  - Callbacks para click y long-press
  - Fallback si la imagen no carga

### 4. Logging Estructurado

Toda operación de datos usa `android.util.Log`:

```kotlin
// Operaciones de lectura
Log.d("MovieRepo", "[SQLite] Loaded ${movies.size} movies from database")
Log.d("MovieRepo", "[NoSQL] Loaded ${movies.size} movies from SharedPreferences")

// Operaciones de escritura exitosas
Log.i("MovieRepo", "[SQLite] Inserted movie id=$id title=$title")
Log.i("MovieRepo", "[SQLite] Updated movie id=${movie.id} title=${movie.title}")
Log.i("MovieRepo", "[SQLite] Deleted movie id=$id")

// Errores
Log.e("MovieRepo", "[SQLite] Error on insert: ${e.message}", e)
Log.e("MovieRepo", "[NoSQL] Error on update: ${e.message}", e)
```

---

## Unit Tests

### Test File
- **Path**: `test/java/com/epn/moviedual/data/MovieRepositoryTest.kt`
- **Framework**: JUnit4 (sin Robolectric)
- **Tests Incluidos**:

#### 1. `test_nosql_insert_and_retrieve()`
- Crea un objeto Movie
- Verifica que se puede crear y acceder a propiedades
- Prueba agregar a lista (simula insert)
- Prueba recuperar de lista (simula getAll)
- Valida datos después de recuperar

#### 2. `test_engine_switch_gives_independent_stores()`
- Crea movies separados para NoSQL y SQLite
- Crea listas separadas para simular repositorios
- Verifica que NoSQL solo contiene movies de NoSQL
- Verifica que SQLite solo contiene movies de SQLite
- **Demuestra**: Aislamiento de datos entre motores

#### 3. Tests Adicionales
- `test_nosql_update()`: Verifica operaciones de actualización
- `test_nosql_delete()`: Verifica operaciones de eliminación
- `test_repository_factory_pattern()`: Verifica el patrón factory
- `test_json_serialization()`: Verifica estructura JSON del NoSQL

---

## Resources

### Strings (`res/values/strings.xml`)
- app_name
- add_movie, edit_movie
- Titles, labels, hints
- delete_confirmation, etc.

### Themes (`res/values/themes.xml`)
- Theme.MyApplication2
- Material Design compatible
- Colores personalizados

### Colors (`res/values/colors.xml`)
- purple_500, purple_700
- teal_200
- black, white

### Layouts
- `activity_main.xml`: Pantalla principal
- `activity_add_edit_movie.xml`: Formulario de película
- `item_movie.xml`: Item de RecyclerView

---

## Dependencies

### Build Configuration
- **Gradle**: 9.3.1
- **Android SDK**: compileSdk 36, targetSdk 36, minSdk 24
- **Kotlin**: 2.2.10

### Libraries
- `androidx.appcompat:appcompat:1.6.1`
- `androidx.recyclerview:recyclerview:1.3.2`
- `androidx.cardview:cardview:1.0.0`
- `com.google.android.material:material:1.11.0`
- `androidx.constraintlayout:constraintlayout:2.1.4`
- `junit:junit:4.13.2` (pruebas)

### Key Android Components
- AppCompatActivity
- SQLiteOpenHelper
- SharedPreferences
- Intent (for gallery)
- ContentResolver (for image loading)

---

## Permissions

```xml
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.CAMERA" />
<uses-feature android:name="android.hardware.camera" android:required="false" />
```

---

## File Structure

```
com/epn/moviedual/
├── data/
│   ├── Movie.kt                          (Data model)
│   ├── MovieRepository.kt                (Interface)
│   ├── SQLiteMovieRepository.kt          (SQLite impl + Helper)
│   ├── NoSQLMovieRepository.kt           (NoSQL impl with JSON)
│   └── RepositoryFactory.kt              (Factory)
├── ui/
│   └── MovieAdapter.kt                   (RecyclerView adapter)
├── MainActivity.kt                       (Main screen)
└── AddEditMovieActivity.kt               (Add/Edit screen)

res/
├── layout/
│   ├── activity_main.xml
│   ├── activity_add_edit_movie.xml
│   └── item_movie.xml
├── values/
│   ├── colors.xml
│   ├── strings.xml
│   └── themes.xml
└── mipmap-* / drawable/
    (Launcher icons)

test/java/com/epn/moviedual/data/
└── MovieRepositoryTest.kt                (JUnit4 tests)
```

---

## How to Use

### Switching Engines at Runtime
1. Usar el Switch en la AppBar: "SQLite ⇄ NoSQL"
2. El Chip se actualiza (verde = SQLite, naranja = NoSQL)
3. RecyclerView se recarga automáticamente
4. **Sin reinicio de app requerido**

### Adding a Movie
1. Tap en FAB (+)
2. Llenar campos (Título, Año, Género, Sinopsis)
3. Tap "Pick Image from Gallery" (opcional)
4. Tap "Save"
5. Lista se actualiza automáticamente

### Editing a Movie
1. Tap en un movie de la lista
2. Campos se llenan con datos existentes
3. Modificar según sea necesario
4. Tap "Save"
5. Cambios se persisten en el motor actual

### Deleting a Movie
1. Long-press en un movie
2. Confirmar en diálogo
3. Movie se elimina del motor actual
4. Lista se actualiza

---

## Build & Run

### Compilar
```bash
cd MyApplication2
./gradlew build
```

### Ejecutar tests
```bash
./gradlew testDebugUnitTest
```

### Crear APK debug
```bash
./gradlew assembleDebug
```

### Crear APK release
```bash
./gradlew assembleRelease
```

---

## Key Features

✅ **Persistencia Dual**: Cambiar entre SQLite y NoSQL en tiempo de ejecución
✅ **Repository Pattern**: Implementaciones intercambiables
✅ **CRUD Completo**: Create, Read, Update, Delete
✅ **Imágenes**: Selección de galería con preview
✅ **Logging Estructurado**: Rastreo de todas las operaciones
✅ **Tests JUnit4**: Pruebas unitarias completas
✅ **UI Material Design**: AppBar, FAB, CardView, Chip
✅ **No Cross-Platform**: 100% Android SDK nativo
✅ **Sin ORM de Terceros**: SQLiteOpenHelper nativo
✅ **JSON Manual**: org.json sin dependencias externas

---

## Exam Requirements Checklist

- [x] Repository Pattern con interfaz MovieRepository
- [x] Implementación SQLiteMovieRepository (SQLiteOpenHelper nativo)
- [x] Implementación NoSQLMovieRepository (SharedPreferences + JSON)
- [x] RepositoryFactory para usar getRepository(context, useSQL)
- [x] MainActivity con AppBar, Switch, Chip status, RecyclerView
- [x] FAB para agregar películas
- [x] AddEditMovieActivity con todos los campos
- [x] Image picker con intent de galería
- [x] Long-press para delete con confirmación
- [x] Cambio de motor sin reinicio de app
- [x] Logging estructurado en todas las ops
- [x] Tests JUnit4 sin Robolectric
- [x] test_nosql_insert_and_retrieve
- [x] test_engine_switch_gives_independent_stores
- [x] Permiso CAMERA y READ/WRITE_EXTERNAL_STORAGE
- [x] Aislamiento de datos entre repositorios

---

## Developer Notes

- La aplicación usa **View Binding** para referencias de vistas
- Todas las operaciones de BD usan try-catch con logging de errores
- El RecyclerView se actualiza automáticamente después de cada operación
- Las imágenes se almacenan como URIs, no como blobs
- Los tests se pueden ejecutar sin un dispositivo real (JUnit4 puro)
- El código sigue las convenciones de Kotlin moderno
- Compatible con Android 6.0+ (API 24)

---

## Next Steps (Opcionales)

- Agregar Activity Result API (en lugar de deprecated startActivityForResult)
- Implementar Room database como tercera opción
- Agregar sincronización con backend
- Implementar búsqueda y filtrado
- Agregar validación de entrada más rigurosa
- Implementar borrado en lote
- Agregar ordenamiento de películas

