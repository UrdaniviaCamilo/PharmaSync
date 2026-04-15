-- Alinea BD existente con la entidad Producto.visibleMarketplace (hibernate.update no siempre añade columnas).
DO $patch$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.tables
        WHERE table_schema = current_schema()
          AND table_name = 'productos'
    ) THEN
        ALTER TABLE productos
            ADD COLUMN IF NOT EXISTS visible_marketplace boolean NOT NULL DEFAULT true;
    END IF;
END
$patch$;
