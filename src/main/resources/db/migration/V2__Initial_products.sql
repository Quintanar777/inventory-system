-- Initial product data for Perro Amor inventory system
-- Based on DataInitializer.kt products

INSERT INTO products (name, category, description, price, stock, can_be_personalized, has_variants) VALUES
('Collar Santo Remedio', 'Collares', 'Collar elegante con diseño único', 199.00, 0, true, true),
('Correa Binomio', 'Correas', 'Correa resistente con diseños únicos', 189.00, 0, false, true),
('Porta Alerta', 'Correas', 'Correa de seguridad con alta visibilidad', 139.00, 0, false, true),
('Collar Vida Mía', 'Collares', 'Collar con estilo único para tu mascota', 199.00, 10, true, false),
('Mochila Mimi', 'Mochilas', 'Mochila práctica para paseos largos', 289.00, 5, false, false),
('Grabado de Nombre', 'Personalización', 'Servicio de grabado de nombre en productos', 50.00, 100, false, false),
('Grabado de Teléfono', 'Personalización', 'Servicio de grabado de teléfono en productos', 50.00, 100, false, false);

-- Product variants for Collar Santo Remedio (assuming product_id = 1)
INSERT INTO product_variants (product_id, variant_name, color, size, material, stock, sku, shopify_variant_id, shopify_product_id) VALUES
(1, 'Listón Rojo - S', 'Rojo', 'S', 'Listón 1.9cm', 15, 'CSR-R-S', 'gid://shopify/ProductVariant/44123456789', 'gid://shopify/Product/8123456789'),
(1, 'Listón Azul - M', 'Azul', 'M', 'Listón 3cm', 12, 'CSR-A-M', 'gid://shopify/ProductVariant/44123456790', 'gid://shopify/Product/8123456789'),
(1, 'Listón Verde - L', 'Verde', 'L', 'Listón 3cm', 8, 'CSR-V-L', 'gid://shopify/ProductVariant/44123456791', 'gid://shopify/Product/8123456789');

-- Product variants for Correa Binomio (assuming product_id = 2)
INSERT INTO product_variants (product_id, variant_name, design, color, size, stock, sku, shopify_variant_id, shopify_product_id) VALUES
(2, 'Pilatos Dog - M', 'Pilatos Dog', 'Multicolor', 'M', 8, 'CB-PD-M', 'gid://shopify/ProductVariant/44987654321', 'gid://shopify/Product/8987654321'),
(2, 'Dolce Vida - L', 'Dolce Vida', 'Rosa/Dorado', 'L', 6, 'CB-DV-L', 'gid://shopify/ProductVariant/44987654322', 'gid://shopify/Product/8987654321'),
(2, 'Ohana - S', 'Ohana', 'Azul/Blanco', 'S', 4, 'CB-OH-S', null, null),
(2, 'Love is Love - Único', 'Love is Love', 'Arcoíris', 'Único', 3, 'CB-LIL-U', null, null);

-- Product variants for Porta Alerta (assuming product_id = 3)
INSERT INTO product_variants (product_id, variant_name, color, size, stock, sku) VALUES
(3, 'Rojo - S', 'Rojo', 'S', 10, 'PA-R-S'),
(3, 'Morado - M', 'Morado', 'M', 8, 'PA-M-M'),
(3, 'Rosa - L', 'Rosa', 'L', 12, 'PA-RS-L'),
(3, 'Naranja - Mediano', 'Naranja', 'Mediano', 6, 'PA-N-MD'),
(3, 'Amarillo - Grande', 'Amarillo', 'Grande', 9, 'PA-A-G'),
(3, 'Verde - Unitalla', 'Verde', 'Unitalla', 7, 'PA-V-U'),
(3, 'Azul - XL', 'Azul', 'XL', 11, 'PA-AZ-XL');