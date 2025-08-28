-- Add brand field to products table
-- This allows products to have a brand like "Perro Amor" or "Perra Madre"

-- Add the brand column
ALTER TABLE products ADD COLUMN brand VARCHAR(255) NOT NULL DEFAULT 'Perro Amor';

-- Update existing products with the default brand
UPDATE products SET brand = 'Perro Amor' WHERE brand = 'Perro Amor';

-- Remove the default constraint after updating existing records
-- (In H2, we need to recreate the column to remove default)
-- This is safe because all existing records now have a brand value