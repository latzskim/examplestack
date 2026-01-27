-- Catalog
CREATE TABLE categories (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    parent_category_id UUID REFERENCES categories(id),
    created_at TIMESTAMP NOT NULL
);

CREATE TABLE product_definitions (
    id UUID PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    price_amount DECIMAL(19,4) NOT NULL,
    price_currency VARCHAR(3) NOT NULL,
    category_id UUID REFERENCES categories(id),
    image_url VARCHAR(500),
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- Inventory
CREATE TABLE warehouses (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    street VARCHAR(200),
    city VARCHAR(100),
    postal_code VARCHAR(20),
    country VARCHAR(100),
    created_at TIMESTAMP NOT NULL
);

CREATE TABLE stocks (
    id UUID PRIMARY KEY,
    product_definition_id UUID NOT NULL REFERENCES product_definitions(id),
    warehouse_id UUID NOT NULL REFERENCES warehouses(id),
    quantity INT NOT NULL DEFAULT 0,
    reserved_quantity INT NOT NULL DEFAULT 0,
    updated_at TIMESTAMP NOT NULL,
    UNIQUE(product_definition_id, warehouse_id)
);

-- Identity
CREATE TABLE users (
    id UUID PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    role VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    last_login_at TIMESTAMP
);

-- Cart
CREATE TABLE carts (
    id UUID PRIMARY KEY,
    session_id VARCHAR(100),
    user_id UUID REFERENCES users(id),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE cart_items (
    id UUID PRIMARY KEY,
    cart_id UUID NOT NULL REFERENCES carts(id) ON DELETE CASCADE,
    product_definition_id UUID NOT NULL REFERENCES product_definitions(id),
    quantity INT NOT NULL,
    price_at_addition_amount DECIMAL(19,4) NOT NULL,
    price_at_addition_currency VARCHAR(3) NOT NULL,
    added_at TIMESTAMP NOT NULL
);

-- Orders
CREATE TABLE orders (
    id UUID PRIMARY KEY,
    order_number VARCHAR(50) NOT NULL UNIQUE,
    user_id UUID NOT NULL REFERENCES users(id),
    shipping_street VARCHAR(200),
    shipping_city VARCHAR(100),
    shipping_postal_code VARCHAR(20),
    shipping_country VARCHAR(100),
    status VARCHAR(30) NOT NULL,
    total_amount DECIMAL(19,4) NOT NULL,
    total_currency VARCHAR(3) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    paid_at TIMESTAMP
);

CREATE TABLE order_items (
    id UUID PRIMARY KEY,
    order_id UUID NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    product_definition_id UUID NOT NULL,
    product_name VARCHAR(200) NOT NULL,
    quantity INT NOT NULL,
    unit_price_amount DECIMAL(19,4) NOT NULL,
    unit_price_currency VARCHAR(3) NOT NULL,
    warehouse_id UUID NOT NULL
);

-- Shipping
CREATE TABLE shipments (
    id UUID PRIMARY KEY,
    tracking_number VARCHAR(50) NOT NULL UNIQUE,
    order_id UUID NOT NULL REFERENCES orders(id),
    warehouse_id UUID NOT NULL REFERENCES warehouses(id),
    destination_street VARCHAR(200),
    destination_city VARCHAR(100),
    destination_postal_code VARCHAR(20),
    destination_country VARCHAR(100),
    status VARCHAR(30) NOT NULL,
    estimated_delivery DATE,
    created_at TIMESTAMP NOT NULL
);

CREATE TABLE shipment_status_history (
    id UUID PRIMARY KEY,
    shipment_id UUID NOT NULL REFERENCES shipments(id) ON DELETE CASCADE,
    status VARCHAR(30) NOT NULL,
    changed_at TIMESTAMP NOT NULL,
    location VARCHAR(200),
    notes TEXT
);

-- Notification
CREATE TABLE notification_logs (
    id UUID PRIMARY KEY,
    type VARCHAR(50) NOT NULL,
    recipient_email VARCHAR(255) NOT NULL,
    subject VARCHAR(500),
    status VARCHAR(20) NOT NULL,
    sent_at TIMESTAMP,
    error_message TEXT,
    created_at TIMESTAMP NOT NULL
);

-- Spring Modulith Event Publication
CREATE TABLE event_publication (
    id UUID PRIMARY KEY,
    listener_id VARCHAR(255) NOT NULL,
    event_type VARCHAR(512) NOT NULL,
    serialized_event TEXT NOT NULL,
    publication_date TIMESTAMP NOT NULL,
    completion_date TIMESTAMP
);

-- Indexes
CREATE INDEX idx_product_definitions_category ON product_definitions(category_id);
CREATE INDEX idx_product_definitions_status ON product_definitions(status);
CREATE INDEX idx_stocks_product ON stocks(product_definition_id);
CREATE INDEX idx_carts_session ON carts(session_id);
CREATE INDEX idx_carts_user ON carts(user_id);
CREATE INDEX idx_orders_user ON orders(user_id);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_shipments_order ON shipments(order_id);
CREATE INDEX idx_shipments_tracking ON shipments(tracking_number);
CREATE INDEX idx_event_publication_incomplete ON event_publication(completion_date) WHERE completion_date IS NULL;
