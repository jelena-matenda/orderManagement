# Order Management API

## 📌 Opis
Ovaj projekt implementira REST API za upravljanje narudžbama u sustavu za maloprodaju. API omogućuje CRUD operacije za entitete **Customer** i **Order**, s validacijom ulaznih podataka i poslovnom logikom. Također sadrži autentifikaciju i autorizaciju pomoću **JWT tokena**.

---

## 🚀 Postavljanje Projekta

### 📦 Preduvjeti
- **Java 17**
- **Gradle**
- **PostgreSQL** baza podataka (hostana na Renderu)
- **Postman** ili drugi API klijent za testiranje

### 📥 Instalacija
1. **Klonirajte repozitorij**
   ```sh
   git clone https://github.com/jelena-matenda/orderManagement.git
   cd order-management-api
   ```

2. **Postavite bazu podataka na Render**
   - Kreirajte PostgreSQL bazu na **Render**.
   - Ažurirajte `application.yml` s povezivanjem na Render bazu:
   
   ```yaml
   spring:
     datasource:
       url: jdbc:postgresql://dpg-cud0amogph6c738jjne0-a.frankfurt-postgres.render.com:5432/ordermanagementpostgresql
       username: jelena
       password: ${DB_PASSWORD}
     jpa:
       hibernate:
         ddl-auto: update
       show-sql: true
   ```

3. **Pokrenite aplikaciju**
   ```sh
   ./gradlew bootRun
   ```
   Aplikacija će biti dostupna na: `http://localhost:8080`

---

## 📜 API Dokumentacija
API je definiran pomoću **OpenAPI specifikacije**. Možete pregledati dokumentaciju pomoću Swagger UI:

📌 **Swagger UI:** `http://localhost:8080/swagger-ui.html`

📌 **OpenAPI JSON:** `http://localhost:8080/v3/api-docs`

---

## 🛠️ Tehnologije
- **Spring Boot** – Za razvoj REST API-ja
- **Spring Security** – Autentifikacija i autorizacija s JWT tokenima
- **PostgreSQL (Render)** – Baza podataka
- **JDBC Template** – Pristup bazi podataka
- **OpenAPI Generator** – Automatsko generiranje modela
- **SLF4J** – Logiranje
- **Bean Validation** – Validacija ulaznih podataka

---

## 📌 Entiteti

### **Customer**
| Polje      | Tip   | Ograničenja |
|------------|-------|--------------|
| `id` | UUID | Primarni ključ |
| `name` | String | Obavezno, min. 3 znaka |
| `email` | String | Obavezno, valjana email adresa |
| `createdAt` | Timestamp | Automatski generiran |

### **Order**
| Polje | Tip | Ograničenja |
|-----------|------------|--------------|
| `id` | UUID | Primarni ključ |
| `customerId` | UUID | Strani ključ na Customer |
| `orderDate` | Date | Obavezno |
| `totalAmount` | Decimal | Obavezno, > 0 |
| `status` | Enum | `NEW`, `IN_PROGRESS`, `COMPLETED` |
| `createdAt` | Timestamp | Automatski generiran |

---

## 🔒 Autentifikacija i Autorizacija

1. **Registracija korisnika** (`/auth/register`)
2. **Prijava korisnika** (`/auth/login`)
3. **Dobivanje JWT tokena** nakon prijave
4. **Korištenje tokena u zahtjevima** (Postaviti `Authorization: Bearer <token>` u headeru)

### 🏷️ Uloge korisnika
- **ADMIN** – Može upravljati svim korisnicima i narudžbama
- **USER** – Može upravljati samo vlastitim narudžbama

🔹 **Admin može:** Kreirati, ažurirati, brisati sve narudžbe i korisnike.
🔹 **User može:** Kreirati, ažurirati i brisati samo svoje narudžbe.

---

## 🏗️ CRUD Operacije

### **Customer Endpoints**
- `GET /customers` – Dohvati sve kupce (Admin only)
- `GET /customers/{id}` – Dohvati kupca po ID-u (Admin only)
- `POST /customers` – Dodaj kupca (Admin only)
- `PUT /customers/{id}` – Ažuriraj kupca (Admin only)
- `DELETE /customers/{id}` – Obriši kupca (Admin only)

### **Order Endpoints**
- `GET /orders` – Dohvati sve narudžbe (Admin) ili vlastite (User)
- `GET /orders/{id}` – Dohvati narudžbu po ID-u (Admin može sve, User samo svoje)
- `POST /orders` – Kreiraj narudžbu (User samo za sebe)
- `PUT /orders/{id}` – Ažuriraj narudžbu (Admin može sve, User samo svoje)
- `DELETE /orders/{id}` – Obriši narudžbu (Admin može sve, User samo svoje)

---

## 📏 Validacija Podataka
- **Customer:**
  - `name` mora imati min. 3 znaka
  - `email` mora biti valjan format
- **Order:**
  - `totalAmount` mora biti veći od 0
  - `status` se može promijeniti samo iz `NEW` u `IN_PROGRESS` ili `COMPLETED`

---

## 📜 OpenAPI Integracija
Aplikacija koristi **OpenAPI Gradle Generator** za generiranje modela:

📌 **Generiranje modela:**
```sh
./gradlew openApiGenerate
```
📌 **Izgradnja projekta:**
```sh
./gradlew build
```

---

## 📊 Logiranje
Svi API pozivi su logirani pomoću **SLF4J**. Lozinke i osjetljivi podaci nisu uključeni u logove.

Primjer logiranja zahtjeva:
```java
logger.info("🔹 Received Order Request: {}", order);
```
