CREATE TABLE notifications (
    id          UUID PRIMARY KEY,
    customer_id UUID        NOT NULL,
    order_id    UUID        NOT NULL,
    type        VARCHAR(50) NOT NULL,
    channel     VARCHAR(20) NOT NULL,
    message     TEXT        NOT NULL,
    status      VARCHAR(20) NOT NULL,
    sent_at     TIMESTAMP WITH TIME ZONE,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_notifications_customer_id ON notifications (customer_id);
CREATE INDEX idx_notifications_order_id ON notifications (order_id);
