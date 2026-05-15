package service

import (
	"database/sql"
	"errors"

	"transport-pay-backend/internal/database"
	"transport-pay-backend/internal/model"
)

type TransactionService struct {
	cardService *CardService
}

func NewTransactionService() *TransactionService {
	return &TransactionService{
		cardService: NewCardService(),
	}
}

func (s *TransactionService) Create(req model.CreateTransactionRequest) (*model.Transaction, error) {
	result, err := database.DB.Exec(
		"INSERT INTO transactions (amount, card_id, terminal_id) VALUES (?, ?, ?)",
		req.Amount, req.CardID, req.TerminalID,
	)
	if err != nil {
		return nil, err
	}

	id, _ := result.LastInsertId()
	return s.GetByID(id)
}

func (s *TransactionService) GetByID(id int64) (*model.Transaction, error) {
	transaction := &model.Transaction{}
	err := database.DB.QueryRow(
		"SELECT id, amount, card_id, terminal_id, created_at FROM transactions WHERE id = ?",
		id,
	).Scan(&transaction.ID, &transaction.Amount, &transaction.CardID, &transaction.TerminalID, &transaction.CreatedAt)

	if err == sql.ErrNoRows {
		return nil, errors.New("transaction not found")
	}
	if err != nil {
		return nil, err
	}
	return transaction, nil
}

func (s *TransactionService) GetAll() ([]model.Transaction, error) {
	rows, err := database.DB.Query("SELECT id, amount, card_id, terminal_id, created_at FROM transactions")
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	var transactions []model.Transaction
	for rows.Next() {
		var transaction model.Transaction
		if err := rows.Scan(&transaction.ID, &transaction.Amount, &transaction.CardID, &transaction.TerminalID, &transaction.CreatedAt); err != nil {
			return nil, err
		}
		transactions = append(transactions, transaction)
	}
	return transactions, nil
}

func (s *TransactionService) Delete(id int64) error {
	_, err := database.DB.Exec("DELETE FROM transactions WHERE id = ?", id)
	return err
}

// AuthTransaction authorizes a transaction by checking card existence, balance, and block status
func (s *TransactionService) AuthTransaction(req model.AuthTransactionRequest) (*model.AuthTransactionResponse, error) {
	// Get card by card number
	card, err := s.cardService.GetByCardNumber(req.CardNumber)
	if err != nil {
		return &model.AuthTransactionResponse{
			Authorized: false,
			Message:    "Card not found",
		}, nil
	}

	// Check if card is blocked
	if card.IsBlocked {
		return &model.AuthTransactionResponse{
			Authorized: false,
			Message:    "Card is blocked",
		}, nil
	}

	// Check if sufficient balance
	if card.Balance < req.Amount {
		return &model.AuthTransactionResponse{
			Authorized: false,
			Message:    "Insufficient balance",
		}, nil
	}

	// Update card balance
	err = s.cardService.UpdateBalance(card.ID, req.Amount)
	if err != nil {
		return &model.AuthTransactionResponse{
			Authorized: false,
			Message:    "Failed to update balance",
		}, nil
	}

	// Create transaction record
	_, err = database.DB.Exec(
		"INSERT INTO transactions (amount, card_id, terminal_id) VALUES (?, ?, ?)",
		req.Amount, card.ID, req.TerminalID,
	)
	if err != nil {
		return &model.AuthTransactionResponse{
			Authorized: false,
			Message:    "Failed to create transaction",
		}, nil
	}

	return &model.AuthTransactionResponse{
		Authorized: true,
		Message:    "Transaction authorized",
	}, nil
}
