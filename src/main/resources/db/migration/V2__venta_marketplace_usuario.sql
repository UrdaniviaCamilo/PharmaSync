DO $patch$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.tables
        WHERE table_schema = current_schema() AND table_name = 'ventas'
    ) AND NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = current_schema() AND table_name = 'ventas' AND column_name = 'marketplace_usuario_id'
    ) THEN
        ALTER TABLE ventas ADD COLUMN marketplace_usuario_id BIGINT;
    END IF;
END
$patch$;
