package model

import "time"

type Card struct {
	ID          int64    `json:"id"`
	CardNumber  string   `json:"card_number" binding:"required"`
	Balance     float64  `json:"balance"`
	IsBlocked   bool     `json:"is_blocked"`
	OwnerName   string   `json:"owner_name"`
	KeyID       *int64   `json:"key_id"`
	CreatedAt   time.Time `json:"created_at"`
	UpdatedAt   time.Time `json:"updated_at"`
}

type CreateCardRequest struct {
	CardNumber string  `json:"card_number" binding:"required"`
	Balance    float64 `json:"balance"`
	IsBlocked  bool    `json:"is_blocked"`
	OwnerName  string  `json:"owner_name"`
	KeyID      *int64  `json:"key_id"`
}

type UpdateCardRequest struct {
	CardNumber string  `json:"card_number,omitempty"`
	Balance    float64 `json:"balance,omitempty"`
	IsBlocked  *bool   `json:"is_blocked,omitempty"`
	OwnerName  string  `json:"owner_name,omitempty"`
	KeyID      *int64  `json:"key_id,omitempty"`
}
