-- USERS
CREATE TABLE users (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    name          VARCHAR(100) NOT NULL,
    email         VARCHAR(150) NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    role          VARCHAR(20)  NOT NULL DEFAULT 'USER',
    created_at    DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    UNIQUE KEY uk_users_email (email),
    CONSTRAINT chk_users_role CHECK (role IN ('USER', 'ADMIN'))
) ENGINE=InnoDB;

-- MOVIES
CREATE TABLE movies (
    id               BIGINT       NOT NULL AUTO_INCREMENT,
    title            VARCHAR(200) NOT NULL,
    description      TEXT,
    genre            VARCHAR(50),
    language         VARCHAR(50),
    duration_minutes INT          NOT NULL,
    release_date     DATE,
    poster_url       VARCHAR(500),
    created_at       DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at       DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    KEY idx_movies_title (title),
    KEY idx_movies_genre (genre),
    KEY idx_movies_language (language)
) ENGINE=InnoDB;

-- THEATRES
CREATE TABLE theatres (
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    name       VARCHAR(150) NOT NULL,
    city       VARCHAR(80)  NOT NULL,
    address    VARCHAR(300),
    image_url  VARCHAR(500),
    created_at DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    KEY idx_theatres_city (city)
) ENGINE=InnoDB;

-- SCREENS
CREATE TABLE screens (
    id         BIGINT      NOT NULL AUTO_INCREMENT,
    theatre_id BIGINT      NOT NULL,
    name       VARCHAR(50) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_screens_theatre_name (theatre_id, name),
    CONSTRAINT fk_screens_theatre FOREIGN KEY (theatre_id) REFERENCES theatres (id)
) ENGINE=InnoDB;

-- SEATS (physical seats of a screen)
CREATE TABLE seats (
    id          BIGINT      NOT NULL AUTO_INCREMENT,
    screen_id   BIGINT      NOT NULL,
    row_label   VARCHAR(5)  NOT NULL,
    seat_number INT         NOT NULL,
    seat_type   VARCHAR(20) NOT NULL DEFAULT 'REGULAR',
    PRIMARY KEY (id),
    UNIQUE KEY uk_seats_position (screen_id, row_label, seat_number),
    CONSTRAINT fk_seats_screen FOREIGN KEY (screen_id) REFERENCES screens (id)
) ENGINE=InnoDB;

-- SHOWS
CREATE TABLE shows (
    id         BIGINT         NOT NULL AUTO_INCREMENT,
    movie_id   BIGINT         NOT NULL,
    screen_id  BIGINT         NOT NULL,
    start_time DATETIME       NOT NULL,
    end_time   DATETIME       NOT NULL,
    price      DECIMAL(10, 2) NOT NULL,
    status     VARCHAR(20)    NOT NULL DEFAULT 'ACTIVE',
    created_at DATETIME(6)    NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    UNIQUE KEY uk_shows_screen_start (screen_id, start_time),
    KEY idx_shows_movie (movie_id),
    KEY idx_shows_start_time (start_time),
    CONSTRAINT fk_shows_movie  FOREIGN KEY (movie_id)  REFERENCES movies (id),
    CONSTRAINT fk_shows_screen FOREIGN KEY (screen_id) REFERENCES screens (id),
    CONSTRAINT chk_shows_time  CHECK (end_time > start_time),
    CONSTRAINT chk_shows_price CHECK (price >= 0)
) ENGINE=InnoDB;

-- BOOKINGS
CREATE TABLE bookings (
    id              BIGINT         NOT NULL AUTO_INCREMENT,
    user_id         BIGINT         NOT NULL,
    show_id         BIGINT         NOT NULL,
    status          VARCHAR(20)    NOT NULL,
    total_amount    DECIMAL(10, 2) NOT NULL,
    idempotency_key VARCHAR(100),
    expires_at      DATETIME(6),
    version         BIGINT         NOT NULL DEFAULT 0,
    created_at      DATETIME(6)    NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at      DATETIME(6)    NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    UNIQUE KEY uk_bookings_user_idem (user_id, idempotency_key),
    KEY idx_bookings_user (user_id),
    KEY idx_bookings_show (show_id),
    KEY idx_bookings_status_expiry (status, expires_at),
    CONSTRAINT fk_bookings_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_bookings_show FOREIGN KEY (show_id) REFERENCES shows (id),
    CONSTRAINT chk_bookings_status CHECK (status IN ('PENDING', 'CONFIRMED', 'FAILED', 'CANCELLED', 'EXPIRED'))
) ENGINE=InnoDB;

-- BOOKING_SEATS: the double-booking safety net
-- active = 1 while the seat is held (PENDING/CONFIRMED), NULL once released.
CREATE TABLE booking_seats (
    id         BIGINT         NOT NULL AUTO_INCREMENT,
    booking_id BIGINT         NOT NULL,
    show_id    BIGINT         NOT NULL,
    seat_id    BIGINT         NOT NULL,
    price      DECIMAL(10, 2) NOT NULL,
    active     TINYINT        NULL DEFAULT 1,
    PRIMARY KEY (id),
    UNIQUE KEY uk_booking_seats_active (show_id, seat_id, active),
    KEY idx_booking_seats_booking (booking_id),
    KEY idx_booking_seats_seat (seat_id),
    CONSTRAINT fk_bs_booking FOREIGN KEY (booking_id) REFERENCES bookings (id),
    CONSTRAINT fk_bs_show    FOREIGN KEY (show_id)    REFERENCES shows (id),
    CONSTRAINT fk_bs_seat    FOREIGN KEY (seat_id)    REFERENCES seats (id),
    CONSTRAINT chk_bs_active CHECK (active IS NULL OR active = 1)
) ENGINE=InnoDB;

-- PAYMENTS
CREATE TABLE payments (
    id              BIGINT         NOT NULL AUTO_INCREMENT,
    booking_id      BIGINT         NOT NULL,
    amount          DECIMAL(10, 2) NOT NULL,
    status          VARCHAR(20)    NOT NULL,
    idempotency_key VARCHAR(100)   NOT NULL,
    failure_reason  VARCHAR(255),
    created_at      DATETIME(6)    NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at      DATETIME(6)    NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    UNIQUE KEY uk_payments_idem (idempotency_key),
    KEY idx_payments_booking (booking_id),
    CONSTRAINT fk_payments_booking FOREIGN KEY (booking_id) REFERENCES bookings (id),
    CONSTRAINT chk_payments_status CHECK (status IN ('PENDING', 'SUCCESS', 'FAILED', 'REFUNDED'))
) ENGINE=InnoDB;
