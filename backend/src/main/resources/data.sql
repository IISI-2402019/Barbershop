UPDATE appointments SET status = 'CANCELED' WHERE status = 'CANCELLED';

INSERT INTO system_settings (setting_key, setting_value) 
SELECT 'business_hours_start', '10:00' 
WHERE NOT EXISTS (SELECT 1 FROM system_settings WHERE setting_key = 'business_hours_start');

INSERT INTO system_settings (setting_key, setting_value) 
SELECT 'business_hours_end', '20:00' 
WHERE NOT EXISTS (SELECT 1 FROM system_settings WHERE setting_key = 'business_hours_end');
