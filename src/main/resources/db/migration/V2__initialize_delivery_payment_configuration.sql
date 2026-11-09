INSERT INTO configuracion_domicilios (id, monto_maximo_contraentrega, activa)
SELECT '00000000-0000-0000-0000-000000000001', 80000.00, TRUE
WHERE NOT EXISTS (
    SELECT 1
    FROM configuracion_domicilios
    WHERE activa = TRUE
);
