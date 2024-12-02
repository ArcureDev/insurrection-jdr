CREATE EXTENSION IF NOT EXISTS unaccent;

DO $$ DECLARE
    removable_table RECORD;

BEGIN

    FOR removable_table IN (SELECT tablename FROM pg_tables WHERE schemaname = current_schema()) LOOP
            EXECUTE 'DROP TABLE IF EXISTS ' || quote_ident(removable_table.tablename) || ' CASCADE';
        END LOOP;

END $$;