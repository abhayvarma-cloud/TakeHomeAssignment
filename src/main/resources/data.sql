

-- 1. Make sure the counter row exists
INSERT INTO item_seq (id, last_seq) VALUES (1, 0) ON CONFLICT (id) DO NOTHING;

-- 2. Delete all existing items and restart the id numbering at 1
TRUNCATE TABLE items RESTART IDENTITY;

-- 3. Insert 10,000 items with seq 1..10000
INSERT INTO items (seq, name, description, created_at)
SELECT n,
       'Item ' || LPAD(n::text, 5, '0'),
       'Sample description for item ' || n,
       now() - ((10000 - n) * INTERVAL '1 minute')
FROM generate_series(1, 10000) AS n;

-- 4. Set the counter so new items continue from 10001
UPDATE item_seq SET last_seq = 10000 WHERE id = 1;