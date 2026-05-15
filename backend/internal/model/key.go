package model

import "time"

type Key struct {
	ID          int64     `json:"id"`
	KeyValue    string    `json:"key_value" binding:"required"`
	Description string    `json:"description"`
	CreatedAt   time.Time `json:"created_at"`
	UpdatedAt   time.Time `json:"updated_at"`
}

type CreateKeyRequest struct {
	KeyValue    string `json:"key_value" binding:"required"`
	Description string `json:"description"`
}

type UpdateKeyRequest struct {
	KeyValue    string `json:"key_value,omitempty"`
	Description string `json:"description,omitempty"`
}
