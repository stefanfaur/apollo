-- Insert mock notifications for device d951e527-d709-4f93-8b20-7c0699129c7f
INSERT INTO notification (uuid, title, message, type, image_url, created_at, emitter_uuid)
VALUES
    (gen_random_uuid(), 'Handle Tried', 'Unauthorized person tried handle.', 'DOORLOCK_HANDLE_TRIED_UNAUTHORIZED', 'https://placehold.co/300x300.jpeg', NOW() - interval '10 minutes', 'd951e527-d709-4f93-8b20-7c0699129c7f'),

    (gen_random_uuid(), 'Door Opened', 'Unauthorized person forced door open.', 'DOORLOCK_OPENED_UNAUTHORIZED', 'https://placehold.co/300x300.jpeg', NOW() - interval '20 minutes', 'd951e527-d709-4f93-8b20-7c0699129c7f'),

    (gen_random_uuid(), 'Owner Entry', 'Successful three-way authorization.', 'DOORLOCK_OPENED_AUTHORIZED', 'https://placehold.co/300x300.jpeg', NOW() - interval '30 minutes', 'd951e527-d709-4f93-8b20-7c0699129c7f'),

    (gen_random_uuid(), 'Suspicious Activity', 'Unknown person lingering around door.', 'DOORLOCK_SUSPICIOUS_ACTIVITY', 'https://placehold.co/300x300.jpeg', NOW() - interval '40 minutes', 'd951e527-d709-4f93-8b20-7c0699129c7f');
