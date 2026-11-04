ALTER TABLE categories
DROP CONSTRAINT uk_categories_user_name_type;

CREATE UNIQUE INDEX uk_categories_user_name_type_ci
    ON categories (user_id, UPPER(name), type);