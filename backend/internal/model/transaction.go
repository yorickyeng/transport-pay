package model

import "time"

type Transaction struct {
	ID         int64     `json:"id"`
	Amount     float64   `json:"amount"`
	CardID     int64     `json:"card_id"`
	TerminalID int64     `json:"terminal_id"`
	CreatedAt  time.Time `json:"created_at"`
}

type CreateTransactionRequest struct {
	Amount     float64 `json:"amount" binding:"required"`
	CardID     int64   `json:"card_id" binding:"required"`
	TerminalID int64   `json:"terminal_id" binding:"required"`
}

type AuthTransactionRequest struct {
	CardNumber string  `json:"card_number" binding:"required"`
	Amount     float64 `json:"amount" binding:"required"`
	TerminalID int64   `json:"terminal_id" binding:"required"`
}

type AuthTransactionResponse struct {
	Authorized bool   `json:"authorized"`
	Message    string `json:"message"`
}
