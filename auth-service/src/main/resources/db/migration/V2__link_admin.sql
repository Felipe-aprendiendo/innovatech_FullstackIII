UPDATE auth_users
SET users_service_id = 1
WHERE email = 'admin@innovatech.cl'
  AND users_service_id IS NULL;
