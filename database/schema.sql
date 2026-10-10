-- =====================================================================
-- WILDTRAIL Safari Booking System — schema.sql
-- Logical database: wildtrail_safari
-- Written in portable SQL (PostgreSQL dialect; MySQL notes inline).
-- Executed automatically & idempotently by src/server/config/DatabaseBootstrap.ts
-- =====================================================================

CREATE TABLE IF NOT EXISTS users (
  id            SERIAL PRIMARY KEY,                -- MySQL: BIGINT AUTO_INCREMENT
  email         VARCHAR(160) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  full_name     VARCHAR(120) NOT NULL,
  role          VARCHAR(20)  NOT NULL DEFAULT 'ROLE_CUSTOMER'
                CHECK (role IN ('ROLE_CUSTOMER','ROLE_ADMIN')),
  created_at    TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS customers (
  id         SERIAL PRIMARY KEY,
  user_id    INTEGER REFERENCES users(id) ON DELETE SET NULL,
  full_name  VARCHAR(120) NOT NULL,
  email      VARCHAR(160) NOT NULL UNIQUE,
  phone      VARCHAR(40),
  country    VARCHAR(80),
  created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS admins (
  id          SERIAL PRIMARY KEY,
  user_id     INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  staff_title VARCHAR(80) NOT NULL DEFAULT 'Administrator',
  created_at  TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS destinations (
  id             SERIAL PRIMARY KEY,
  slug           VARCHAR(60)  NOT NULL UNIQUE,
  name           VARCHAR(80)  NOT NULL,
  location       VARCHAR(120) NOT NULL,
  description    TEXT NOT NULL,
  best_wildlife  VARCHAR(200) NOT NULL,
  safari_types   VARCHAR(200) NOT NULL,
  starting_price NUMERIC(10,2) NOT NULL CHECK (starting_price >= 0),
  image_url      TEXT NOT NULL,
  created_at     TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS safari_packages (
  id             SERIAL PRIMARY KEY,
  name           VARCHAR(120) NOT NULL,
  package_type   VARCHAR(20)  NOT NULL
                 CHECK (package_type IN ('MORNING','EVENING','HALF_DAY','FULL_DAY','PRIVATE','FAMILY','PHOTOGRAPHY')),
  destination_id INTEGER NOT NULL REFERENCES destinations(id) ON DELETE RESTRICT,
  duration_hours INTEGER NOT NULL CHECK (duration_hours > 0),
  start_time     VARCHAR(10) NOT NULL,
  max_guests     INTEGER NOT NULL CHECK (max_guests > 0),
  base_price     NUMERIC(10,2) NOT NULL CHECK (base_price >= 0),
  description    TEXT NOT NULL,
  image_url      TEXT NOT NULL,
  active         BOOLEAN NOT NULL DEFAULT TRUE,
  created_at     TIMESTAMP NOT NULL DEFAULT NOW(),
  updated_at     TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS vehicles (
  id           SERIAL PRIMARY KEY,
  name         VARCHAR(120) NOT NULL,
  category     VARCHAR(20)  NOT NULL CHECK (category IN ('STANDARD','HILUX','LUXURY','FAMILY')),
  capacity     INTEGER NOT NULL CHECK (capacity > 0),
  four_by_four BOOLEAN NOT NULL DEFAULT TRUE,
  features     TEXT NOT NULL,
  available    BOOLEAN NOT NULL DEFAULT TRUE,
  price        NUMERIC(10,2) NOT NULL CHECK (price >= 0),
  image_url    TEXT NOT NULL,
  created_at   TIMESTAMP NOT NULL DEFAULT NOW(),
  updated_at   TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS bookings (
  id               SERIAL PRIMARY KEY,
  reference        VARCHAR(20) NOT NULL UNIQUE,
  customer_id      INTEGER NOT NULL REFERENCES customers(id) ON DELETE CASCADE,
  package_id       INTEGER NOT NULL REFERENCES safari_packages(id) ON DELETE RESTRICT,
  vehicle_id       INTEGER NOT NULL REFERENCES vehicles(id) ON DELETE RESTRICT,
  safari_date      DATE NOT NULL,
  time_slot        VARCHAR(10) NOT NULL,
  guests           INTEGER NOT NULL CHECK (guests > 0),
  special_requests TEXT,
  total_amount     NUMERIC(10,2) NOT NULL CHECK (total_amount >= 0),
  status           VARCHAR(20) NOT NULL DEFAULT 'PENDING'
                   CHECK (status IN ('PENDING','CONFIRMED','COMPLETED','CANCELLED')),
  created_at       TIMESTAMP NOT NULL DEFAULT NOW(),
  updated_at       TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS payments (
  id              SERIAL PRIMARY KEY,
  booking_id      INTEGER NOT NULL UNIQUE REFERENCES bookings(id) ON DELETE CASCADE,
  method          VARCHAR(10) NOT NULL CHECK (method IN ('CARD','CASH')),
  amount          NUMERIC(10,2) NOT NULL CHECK (amount >= 0),
  status          VARCHAR(20) NOT NULL DEFAULT 'PENDING'
                  CHECK (status IN ('PENDING','PAID','REFUNDED','FAILED')),
  transaction_ref VARCHAR(40) NOT NULL,
  card_last4      VARCHAR(4),
  paid_at         TIMESTAMP,
  created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS reviews (
  id          SERIAL PRIMARY KEY,
  customer_id INTEGER REFERENCES customers(id) ON DELETE SET NULL,
  author_name VARCHAR(120) NOT NULL,
  country     VARCHAR(80),
  rating      INTEGER NOT NULL CHECK (rating BETWEEN 1 AND 5),
  comment     TEXT NOT NULL,
  approved    BOOLEAN NOT NULL DEFAULT FALSE,
  created_at  TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS contact_messages (
  id         SERIAL PRIMARY KEY,
  name       VARCHAR(120) NOT NULL,
  email      VARCHAR(160) NOT NULL,
  phone      VARCHAR(40),
  message    TEXT NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_bookings_customer ON bookings(customer_id);
CREATE INDEX IF NOT EXISTS idx_bookings_date     ON bookings(safari_date);
CREATE INDEX IF NOT EXISTS idx_packages_dest     ON safari_packages(destination_id);

ALTER TABLE reviews ADD COLUMN IF NOT EXISTS review_type VARCHAR(30) NOT NULL DEFAULT 'CUSTOMER';

CREATE TABLE IF NOT EXISTS booking_guide_assignments (
  booking_id INTEGER PRIMARY KEY REFERENCES bookings(id) ON DELETE CASCADE,
  guide_id INTEGER NOT NULL
);

-- Serialize guide assignment/date changes until their SQL transaction commits.
CREATE TABLE IF NOT EXISTS guide_schedule_lock(id INTEGER PRIMARY KEY);
INSERT INTO guide_schedule_lock(id) SELECT 1 WHERE NOT EXISTS (SELECT 1 FROM guide_schedule_lock WHERE id=1);
