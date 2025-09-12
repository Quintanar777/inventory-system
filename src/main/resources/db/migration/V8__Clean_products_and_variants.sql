-- Clean products and variants tables
-- This migration removes all test data from products and variants
-- Tables structure remains intact for future use

-- Then delete Sales
DELETE FROM sale_items;
DELETE FROM sales;

-- First delete variants (due to foreign key constraint)
DELETE FROM product_variants;

-- Then delete products
DELETE FROM products;



-- Reset auto-increment sequences to start from 1
-- PostgreSQL syntax for resetting sequences
ALTER SEQUENCE IF EXISTS products_id_seq RESTART WITH 1;
ALTER SEQUENCE IF EXISTS product_variants_id_seq RESTART WITH 1;
ALTER SEQUENCE IF EXISTS sales_id_seq RESTART WITH 1;
ALTER SEQUENCE IF EXISTS sale_items_id_seq RESTART WITH 1;

-- Optional: Add a comment to track when this cleanup was done
-- You can uncomment the lines below if you want to restore sample data later

/*
-- To restore sample data later, you can run these migrations in order:
-- V2__Initial_products.sql
-- V4__Add_perra_madre_products.sql
-- 
-- Or create new sample data as needed
*/