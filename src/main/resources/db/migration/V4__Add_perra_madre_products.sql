-- Add some example products from "Perra Madre" brand
-- This demonstrates the brand functionality with different products

INSERT INTO products (name, category, brand, description, price, stock, can_be_personalized, has_variants) VALUES
('Premio Natural Pollo', 'Premios', 'Perra Madre', 'Premios naturales de pollo deshidratado para entrenamientos', 85.00, 20, false, false),
('Snack Dental', 'Premios', 'Perra Madre', 'Snacks dentales para la higiene bucal de tu mascota', 65.00, 15, false, false),
('Huesos de Cuero Natural', 'Premios', 'Perra Madre', 'Huesos de cuero 100% natural para masticación', 45.00, 25, false, true);

-- Add variants for the "Huesos de Cuero Natural" product (assuming it will be product_id = 8)
INSERT INTO product_variants (product_id, variant_name, size, stock, sku) VALUES
(8, 'Pequeño', 'S', 10, 'HCN-S'),
(8, 'Mediano', 'M', 8, 'HCN-M'),
(8, 'Grande', 'L', 7, 'HCN-L');