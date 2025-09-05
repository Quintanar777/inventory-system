-- Agregar campo wholesale_price a la tabla products
ALTER TABLE products ADD COLUMN wholesale_price DECIMAL(10,2);

-- Inicializar el precio de mayoreo con el precio regular (se puede ajustar después)
UPDATE products SET wholesale_price = price * 0.85 WHERE wholesale_price IS NULL;

-- Hacer el campo NOT NULL después de inicializarlo
ALTER TABLE products ALTER COLUMN wholesale_price SET NOT NULL;

-- Comentario para documentar el cambio
COMMENT ON COLUMN products.wholesale_price IS 'Precio especial para ventas de mayoreo, generalmente menor al precio regular';