INSERT INTO guides (id, full_name, phone, experience_years, active)
VALUES
    (1, 'Updated Test Guide', '+998901111111', 8, TRUE),
    (2, 'Dilshod Aliyev', '+998902222222', 5, TRUE),
    (3, 'Rustam Tursunov', '+998903333333', 12, TRUE),
    (4, 'Bekzod Rahimov', '+998904444444', 3, FALSE),
    (5, 'Sardor Ismailov', '+998905555555', 7, TRUE);


INSERT INTO guide_languages (guide_id, language)
VALUES
    (1, 'UZ'),
    (1, 'EN'),

    (2, 'UZ'),
    (2, 'EN'),

    (3, 'UZ'),
    (3, 'RU'),
    (3, 'EN'),
    (3, 'TR'),

    (4, 'RU'),

    (5, 'UZ'),
    (5, 'EN'),
    (5, 'RU');

INSERT INTO attractions
(id, name, address, latitude, longitude, category, entry_fee)
VALUES
    (
        1,
        'Registan Square',
        'Samarkand, Registan Street',
        39.6542,
        66.9597,
        'MONUMENT',
        50000.00
    ),
    (
        2,
        'Gur-e-Amir Mausoleum',
        'Samarkand, Toshkent Road',
        39.6483,
        66.9586,
        'MONUMENT',
        30000.00
    ),
    (
        3,
        'Bibi-Khanym Mosque',
        'Samarkand, Tashkent Road',
        39.6611,
        66.9877,
        'MOSQUE',
        25000.00
    ),
    (
        4,
        'Amir Timur Museum',
        'Tashkent, Amir Temur Avenue',
        41.3111,
        69.2797,
        'MUSEUM',
        40000.00
    ),
    (
        5,
        'Chorsu Bazaar',
        'Tashkent, Eski Shahar',
        41.3265,
        69.2283,
        'BAZAAR',
        0.00
    ),
    (
        6,
        'Amir Temur Square',
        'Tashkent, Amir Temur Avenue',
        41.3110,
        69.2797,
        'PARK',
        0.00
    ),
    (
        7,
        'Itchan Kala',
        'Khiva, Ichankala',
        41.3781,
        60.3614,
        'MONUMENT',
        60000.00
    ),
    (
        8,
        'Kalon Mosque',
        'Bukhara, Poi Kalon Complex',
        39.7753,
        64.4151,
        'MOSQUE',
        30000.00
    );

INSERT INTO tours
(id, title, guide_id, start_time, end_time, max_seats, price_per_seat, status)
VALUES
    (
        1,
        'Samarkand Heritage Tour',
        1,
        '2026-08-15 10:00:00',
        '2026-08-15 13:00:00',
        10,
        150.00,
        'PUBLISHED'
    ),
    (
        2,
        'Tashkent Historical Tour',
        2,
        '2026-08-16 11:00:00',
        '2026-08-16 14:00:00',
        8,
        200.00,
        'PUBLISHED'
    ),
    (
        3,
        'Khiva Old City Tour',
        1,
        '2026-08-20 09:00:00',
        '2026-08-20 12:00:00',
        12,
        180.00,
        'DRAFT'
    ),
    (
        4,
        'Bukhara Monuments Tour',
        3,
        '2026-08-18 10:00:00',
        '2026-08-18 13:00:00',
        15,
        175.00,
        'PUBLISHED'
    ),
    (
        5,
        'Tashkent Evening Bazaar Tour',
        5,
        '2026-08-12 18:00:00',
        '2026-08-12 20:00:00',
        10,
        90.00,
        'CANCELLED'
    ),
    (
        6,
        'Samarkand Grand Tour',
        5,
        '2026-08-25 09:00:00',
        '2026-08-25 15:00:00',
        20,
        250.00,
        'DRAFT'
    );

INSERT INTO tour_stops
(id, tour_id, attraction_id, visit_order, stay_minutes)
VALUES
    -- Tour 1
    (1, 1, 1, 1, 60),
    (2, 1, 2, 2, 45),
    (3, 1, 3, 3, 60),

    -- Tour 2
    (4, 2, 4, 1, 60),
    (5, 2, 5, 2, 90),
    (6, 2, 6, 3, 45),

    -- Tour 3
    (7, 3, 7, 1, 120),

    -- Tour 4
    (8, 4, 8, 1, 90),
    (9, 4, 1, 2, 60),

    -- Tour 5
    (10, 5, 5, 1, 60),
    (11, 5, 6, 2, 45),

    -- Tour 6
    (12, 6, 1, 1, 90),
    (13, 6, 2, 2, 60),
    (14, 6, 3, 3, 60);

INSERT INTO bookings
(id, tour_id, customer_name, customer_phone, seats, total_price, status)
VALUES
    (
        1,
        1,
        'Ivan Petrov',
        '+998911111111',
        2,
        300.00,
        'CONFIRMED'
    ),
    (
        2,
        1,
        'Anna Ivanova',
        '+998922222222',
        3,
        450.00,
        'CONFIRMED'
    ),
    (
        3,
        1,
        'Sergey Smirnov',
        '+998933333333',
        1,
        150.00,
        'CANCELLED'
    ),

    (
        4,
        2,
        'Maria Petrova',
        '+998944444444',
        4,
        800.00,
        'CONFIRMED'
    ),

    (
        5,
        4,
        'Alex Brown',
        '+998955555555',
        2,
        350.00,
        'CONFIRMED'
    ),
    (
        6,
        4,
        'John Smith',
        '+998966666666',
        3,
        525.00,
        'CANCELLED'
    );

SELECT setval(
               pg_get_serial_sequence('guides', 'id'),
               (SELECT MAX(id) FROM guides)
       );

SELECT setval(
               pg_get_serial_sequence('attractions', 'id'),
               (SELECT MAX(id) FROM attractions)
       );

SELECT setval(
               pg_get_serial_sequence('tours', 'id'),
               (SELECT MAX(id) FROM tours)
       );

SELECT setval(
               pg_get_serial_sequence('tour_stops', 'id'),
               (SELECT MAX(id) FROM tour_stops)
       );

SELECT setval(
               pg_get_serial_sequence('bookings', 'id'),
               (SELECT MAX(id) FROM bookings)
       );