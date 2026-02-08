-- Fix PostgreSQL timezone from Asia/Calcutta to Asia/Kolkata
-- This needs to be run as a superuser

-- Option 1: Set timezone for current session
SET TIME ZONE 'Asia/Kolkata';

-- Option 2: Set timezone for the database
ALTER DATABASE vectordb SET timezone TO 'Asia/Kolkata';

-- Verify the timezone
SHOW timezone;
