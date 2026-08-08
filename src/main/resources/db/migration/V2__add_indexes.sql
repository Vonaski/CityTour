CREATE INDEX idx_guide_languages_language
    ON guide_languages (language, guide_id);

CREATE INDEX idx_attractions_category
    ON attractions (category);

CREATE INDEX idx_attractions_name_lower
    ON attractions (LOWER(name));

CREATE INDEX idx_tours_guide_start_time
    ON tours (guide_id, start_time);

CREATE INDEX idx_tours_status_start_time
    ON tours (status, start_time);

CREATE INDEX idx_tour_stops_attraction_tour
    ON tour_stops (attraction_id, tour_id);

CREATE INDEX idx_bookings_tour_status
    ON bookings (tour_id, status);